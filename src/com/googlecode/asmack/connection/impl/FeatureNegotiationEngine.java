package com.googlecode.asmack.connection.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XMLUtils;
import com.googlecode.asmack.XMPPUtils;
import com.googlecode.asmack.XmppAccount;
import com.googlecode.asmack.XmppException;
import com.googlecode.asmack.XmppMalformedException;
import com.googlecode.asmack.connection.XmppTransportException;

/**
 * <p>Manages the setup of an xml input/output stream based on core input/output
 * streams. This setup is called feature negotiation, as the server announces
 * features and the client negotiates supported features.</p>
 *
 * <p>The usual flow to use this class will look close to
 * <code>
 * fnegEngine = new FeatureNegotiationEngine(rawSocket);
 * fnegEngine.open(account);
 * fnegEngine.bind("mobileApp");
 * xmppInputStream in = fnegEngine.getXmppInputStream();
 * xmppOutputStream in = fnegEngine.getXmppOutputStream();
 * </code>
 * The engine is usually a temporary helper for the synchronous phase of
 * feature negotiation.</p>
 */
public class FeatureNegotiationEngine {

    /**
     * Random static asmack_ prefix to use one session while avoiding
     * collission with other asmack instances.
     * Value: {@value session}
     */
    private final static String session = "asmack_" +
        Integer.toHexString((int)(Math.random() * Integer.MAX_VALUE));

    /**
     * Lowlevel {@link OutputStream} used by the {@link #xmppOutput}.
     */
    private OutputStream outputStream;

    /**
     * Lowlevel {@link InputStream} used by the {@link #xmppInput}.
     */
    private InputStream inputStream;

    /**
     * The {@link XmppInputStream} used for reading feature stanzas.
     */
    private final XmppInputStream xmppInput;

    /**
     * The {@link XmppOutputStream} used during negotiation.
     */
    private final XmppOutputStream xmppOutput;

    /**
     * Indicate the current tls state.
     */
    private boolean secure = false;

    /**
     * Indicate the current zlib status.
     */
    private boolean compressed = false;

    /**
     * Indicate the authentification status.
     */
    private boolean authenticated = false;

    /**
     * Indicate the availability of tls.
     */
    private boolean hasTLS = false;

    /**
     * Indicate the availability of zlib compression.
     */
    private boolean compressionSupported = false;

    /**
     * Indicate sasl support.
     */
    private boolean SASLSupported = false;

    /**
     * Indicate roster versioning support.
     */
    private boolean rosterVersioningSupported = false;

    /**
     * Indicate session support.
     */
    private boolean sessionsSupported = false;

    /**
     * The basic socket for this connection.
     */
    private Socket socket;

    /**
     * Create a new FeatureNegotiationEngine for a given tcp socket.
     * @param socket Socket The basic socket.
     * @throws XmlPullParserException If the pull parser can't be created.
     * @throws IOException When there is an IOException during intialization.
     * @throws XmppTransportException When this connection failes.
     */
    public FeatureNegotiationEngine(Socket socket)
        throws XmlPullParserException, IOException, XmppTransportException
    {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        xmppOutput = new XmppOutputStream(outputStream);
        xmppInput = new XmppInputStream(inputStream);
    }

    /*
     * From RFC 3920-bis-13#page-26
     * 4.2.7. Flow Chart
     * 
     *                         +------------+
     *                         |  open TCP  |
     *                         | connection |
     *                         +------------+
     *                               |
     *                               | <------------ open() starts here
     *                               |
     *                               v
     *                        +---------------+
     *                        | send initial  |<-------------------------+
     *                        | stream header |                          ^
     *                        +---------------+                          |
     *                               |                                   |
     *                               v                                   |
     *                       +------------------+                        |
     *                       | receive response |                        |
     *                       | stream header    |                        |
     *                       +------------------+                        |
     *                               |                                   |
     *                               v                                   |
     *                        +----------------+                         |
     *                        | receive stream |                         |
     *    +------------------>| features       |                         |
     *    ^                   +----------------+                         |
     *    |                          |                                   |
     *    |                          v                                   |
     *    |       +<-----------------+                                   |
     *    |       |                                                      |
     *    |    {empty?} ----> {all voluntary?} ----> {some mandatory?}   |
     *    |       |      no          |          no         |             |
     *    |       | yes              | yes                 | yes         |
     *    |       |                  v                     v             |
     *    |       |           +---------------+    +----------------+    |
     *    |       |           | MAY negotiate |    | MUST negotiate |    |
     *    |       |           | any or none   |    | one feature    |    |
     *    |       |           +---------------+    +----------------+    |
     *    |       |                  |                     |             |
     *    |       v                  v                     |             |
     *    |   +----------+      +-----------+              |             |
     *    |   | process  |<-----| negotiate |              |             |
     *    |   | complete |  no  | a feature |              |             |
     *    |   +----------+      +-----------+              |             |
     *    |                          |                     |             |
     *    |                     yes  |                     |             |
     *    |                          v                     v             |
     *    |                          +--------->+<---------+             |
     *    |                                     |                        |
     *    |                                     v                        |
     *    +<-------------------------- {restart mandatory?} ------------>+
     *                   no                                     yes
     * 
     * The "open" method starts directly after opening the TCP streams,
     * negotiates the connection and returns true if the xmpp stream is ready
     * for a bind.
     * 
     * The usual way to bind is
     * if (streamEngine.open(account)) {
     *     String resource = streamEngine.bind(account.getResource);
     * }
     * 
     * Interresting and available features that require restarts:
     * - SASL
     * - TLS
     * - Compression
     */

    /**
     * <p>Open a connection for a given account. This will run the full
     * negotiation with the following precedence:
     * <ol>
     *     <li>TLS (if available)</li>
     *     <li>Compression (if available)</li>
     *     <li>SASL</li>
     * <ol></p>
     *
     * <p><b>Note:</b> Servers should not offer compression befor SASL is
     * completed. This is not violated by the rule, mobile devices love xml
     * compression, thus a higher preference. Everything will work as expected
     * when compression is offered after SASL.</p>
     *
     * <p>This method requires a call to bind (if you wish to bind) afterwards.
     * </p>
     * 
     * @param account XmppAccount The account used for negotiation.
     * @throws XmppException In case of an error.
     */
    public void open(XmppAccount account) throws XmppException {
        boolean rerun = true;
        boolean canBind = false;
        while (rerun) {
            try {
                rerun = false;
                xmppOutput.open(XMPPUtils.getDomain(account.getJid()), null);
                xmppInput.readOpening();

                Node features = null;
                do {
                    Node stanza = xmppInput.nextStanza().getDocumentNode();
                    if (XMLUtils.isInstance(
                            stanza,
                            "http://etherx.jabber.org/streams",
                            "features"
                    )) {
                        features = stanza;
                    }
                } while (features == null);

                // check basic stream features

                rosterVersioningSupported |= XMLUtils.hasChild(
                        features,
                        "urn:xmpp:features:rosterver",
                        "ver"
                );
                sessionsSupported |= XMLUtils.hasChild(
                        features,
                        "urn:ietf:params:xml:ns:xmpp-session",
                        "session"
                );
                canBind |= XMLUtils.hasChild(
                        features,
                        "urn:ietf:params:xml:ns:xmpp-bind",
                        "bind"
                );

                hasTLS = XMLUtils.hasChild(
                        features,
                        "urn:ietf:params:xml:ns:xmpp-tls",
                        "starttls"
                );

                Node compression = XMLUtils.getFirstChild(
                    features,
                    "http://jabber.org/features/compress",
                    "compression"
                );
                if (compression != null) {
                    NodeList methods = compression.getChildNodes();
                    for (int i = 0, l = methods.getLength(); i < l; i++) {
                        Node method = methods.item(i);
                        if (method.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        if (!"method".equals(method.getNodeName())) {
                            continue;
                        }
                        String methodName = method.getFirstChild().getNodeValue();
                        methodName = methodName.trim();
                        compressionSupported |= "zlib".equals(methodName);
                    }
               }

                Node saslMechanisms = XMLUtils.getFirstChild(
                        features,
                        "urn:ietf:params:xml:ns:xmpp-sasl",
                        "mechanisms"
                     );
                SASLSupported |= saslMechanisms != null;

                if (hasTLS && !secure) {
                    // enable tls
                    xmppOutput.sendUnchecked(
                        "<starttls xmlns='urn:ietf:params:xml:ns:xmpp-tls'/>"
                    );
                    boolean startTLS = XMLUtils.isInstance(
                        xmppInput.nextStanza().getDocumentNode(),
                        "urn:ietf:params:xml:ns:xmpp-tls",
                        "proceed"
                    );
                    if (startTLS) {
                        startTLS();
                        secure = true;
                        rerun = true;
                        continue;
                    }
                }

                if (compressionSupported && !compressed && ZLibOutputStream.SUPPORTED) {
                    startCompress();
                    rerun = true;
                    continue;
                }

                if (SASLSupported && !authenticated) {
                    if (saslLogin(saslMechanisms, account)) {
                        authenticated = true;
                        rerun = true;
                        continue;
                    }
                }

            } catch (IllegalArgumentException e) {
                throw new XmppMalformedException("Can't negotiate features", e);
            } catch (IllegalStateException e) {
                throw new XmppMalformedException("Can't negotiate features", e);
            } catch (IOException e) {
                throw new XmppTransportException("Can't negotiate features", e);
            } catch (XmlPullParserException e) {
                throw new XmppMalformedException("Can't negotiate features", e);
            } catch (NoSuchAlgorithmException e) {
                // Should never happen - TLS not available?
                throw new XmppTransportException("Can't enable tls", e);
            } catch (KeyManagementException e) {
                throw new XmppTransportException("Can't trust server", e);
            }
        }
        if (!canBind) {
            throw new XmppTransportException("Couldn't reach bind state.");
        }
    }

    /**
     * Start session binding. The session token is fix for the full service
     * runtime, thus allowing the server to detect reconnects.
     * @throws XmppTransportException In case of an error.
     */
    private void startSession() throws XmppTransportException {
        try {
            xmppOutput.sendUnchecked(
                    "<iq type=\"set\" id=\"" +
                    session +
                    "\">" +
                    "<session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/>" +
                    "</iq>"
            );
            return;
        } catch (IllegalArgumentException e) {
            throw new XmppTransportException("session bind failed", e);
        } catch (IllegalStateException e) {
            throw new XmppTransportException("session bind failed", e);
        }
    }

    /**
     * Bind a given resource, probably resuming an old session.
     * @param resource String The preferred resource string.
     * @return String The actual resource string.
     * @throws XmppException On Error.
     */
    public String bind(String resource) throws XmppException {
        try {
            if (!TextUtils.isEmpty(resource)) {
                xmppOutput.sendUnchecked(
                        "<iq type=\"set\" id=\"bind_1\">" +
                        "<bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">" +
                        "<resource>" +
                        resource +
                        "</resource>" +
                        "</bind>" +
                        "</iq>"
                );
            } else {
                xmppOutput.sendUnchecked(
                        "<iq type=\"set\" id=\"bind_1\">" +
                        "<bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">" +
                        "</bind>" +
                        "</iq>"
                );
            }
            Stanza stanza = xmppInput.nextStanza();
            Node node = XMLUtils.getDocumentNode(stanza.getXml());
            Node bind = XMLUtils.getFirstChild(node, "urn:ietf:params:xml:ns:xmpp-bind", "bind");
            Node jid = XMLUtils.getFirstChild(bind, null, "jid");
            if (sessionsSupported) {
                startSession();
            }
            return jid.getTextContent();
        } catch (IllegalArgumentException e) {
            throw new XmppMalformedException("bind malformed", e);
        } catch (IllegalStateException e) {
            throw new XmppMalformedException("bind malformed", e);
        } catch (SAXException e) {
            throw new XmppMalformedException("bind malformed", e);
        }
    }

    /**
     * Run a sasl based login. Most sals parts are handled by
     * {@link SASLEngine#login(XmppInputStream, XmppOutputStream, java.util.Set, XmppAccount)}.
     * @param saslMechanisms Node The DOM node of the sasl mechanisms.
     * @param account XmppAccount The xmpp account to use.
     * @return boolean True on success. False on failore.
     * @throws XmppException On critical connection errors-
     */
    protected boolean saslLogin(Node saslMechanisms, XmppAccount account)
        throws XmppException
    {
        NodeList nodes = saslMechanisms.getChildNodes();
        HashSet<String> methods = new HashSet<String>(13);
        for (int i = 0, l = nodes.getLength(); i < l; i++) {
            Node node = nodes.item(i);
            if (!XMLUtils.isInstance(node, null, "mechanism")) {
                continue;
            }
            methods.add(
                node.getFirstChild().getNodeValue()
                    .toUpperCase().trim()
            );
        }
        if (SASLEngine.login(
            xmppInput, xmppOutput, methods, account
        )) {
            xmppInput.detach();
            try {
                xmppOutput.detach();
                xmppInput.attach(inputStream);
                xmppOutput.attach(outputStream, true, false);
            } catch (IllegalArgumentException e) {
                throw new XmppMalformedException("Please report", e);
            } catch (IllegalStateException e) {
                throw new XmppMalformedException("Please report", e);
            } catch (IOException e) {
                throw new XmppTransportException("Couldn't restart connection", e);
            }
            return true;
        }
        return false;
    }

    /**
     * <p>Start TLS on the given connection.</p>
     * <p><b>TODO:</b> This method uses a non-validating key manager.</p>
     * @throws NoSuchAlgorithmException If the requested encryption algorithm
     *                                  is not supported.
     * @throws KeyManagementException In case of a key managment error.
     * @throws IOException If the underlying stream dies.
     * @throws XmppTransportException In case of XML/XMPP related errors.
     */
    protected void startTLS()
        throws NoSuchAlgorithmException, KeyManagementException, IOException,
        XmppTransportException
    {
        xmppOutput.detach();
        xmppInput.detach();
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(new KeyManager[]{},
            new javax.net.ssl.TrustManager[]{
                new UnTrustManager()
            },
            new java.security.SecureRandom()
        );
        socket = context.getSocketFactory().createSocket(
            socket,
            socket.getInetAddress().getHostName(),
            socket.getPort(),
            true
        );
        socket.setKeepAlive(false);
        socket.setSoTimeout(0);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        xmppOutput.attach(outputStream, true, false);
        xmppInput.attach(inputStream);
    }

    /**
     * Start compression on top of the current stream.
     * @throws XmppException In case of a XMPP/XML related error.
     * @throws IOException In case of a IOException on the underlying stream.
     */
    private void startCompress() throws XmppException, IOException {
        xmppOutput.sendUnchecked(
            "<compress xmlns='http://jabber.org/protocol/compress'>" +
            "<method>zlib</method>" +
            "</compress>"
        );
        boolean startCompression = XMLUtils.isInstance(
                xmppInput.nextStanza().getDocumentNode(),
                "http://jabber.org/protocol/compress",
                "compressed"
        );
        if (startCompression) {
            xmppOutput.detach();
            xmppInput.detach();

            try {
                outputStream = new ZLibOutputStream(outputStream);
            } catch (NoSuchAlgorithmException e) {
                // FAIL!
                throw new XmppTransportException("Can't create compressed stream", e);
            }
            xmppOutput.attach(outputStream, true, false);
            inputStream = new ZLibInputStream(inputStream);
            xmppInput.attach(inputStream);
            compressed = true;
        }
    }

    /**
     * Retrieve the underlying {@link XmppInputStream}.
     * @return XmppInputStream The raw xmpp input stream.
     */
    public XmppInputStream getXmppInputStream() {
        return xmppInput;
    }

    /**
     * Retrieve the underlying {@link XmppInputStream}.
     * @return XmppOutputStream The raw xmpp output stream.
     */
    public XmppOutputStream getXmppOutputStream() {
        return xmppOutput;
    }

    /**
     * <p>Check the current TLS status.</p>
     * <p>Note: this does not imply that the certificate has be thoroughly
     * checked</p>
     * @return boolean True if the connection is guarded by TLS.
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Check the current compression status.
     * @return boolean True if this connection is zlib compressen.
     */
    public boolean isCompressed() {
        return compressed;
    }

    /**
     * Check the sasl outcome.
     * @return boolean True on successful login.
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Check if compression was offered as part of the feature negotiation
     * process.
     * @return boolean True if compression was offered during feature
     *                 negotiation.
     */
    public boolean isCompressionSupported() {
        return compressionSupported;
    }

    /**
     * Check for roster versioning support. This can be a huge saving for
     * roster retrieval.
     * @return boolean True if roster versioning is supported.
     */
    public boolean isRosterVersioningSupported() {
        return rosterVersioningSupported;
    }

    /**
     * Check for session support. Sessions will be used whenever available, and
     * will be autonegotated after bind.
     * @return boolean  True if session support is available.
     */
    public boolean hasSessionSupport() {
        return sessionsSupported;
    }

}
