package com.googlecode.asmack.connection.impl;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XMLUtils;
import com.googlecode.asmack.XmppException;
import com.googlecode.asmack.XmppMalformedException;
import com.googlecode.asmack.connection.XmppTransportException;

/**
 * A non-threaded input stream to xmpp stanza converter.
 */
public class XmppInputStream {

    /**
     * Debugging tag of this class ("XmppInputStream").
     */
    private static final String TAG =
                                XmppInputStream.class.getSimpleName();

    /**
     * The low leve input stream used for xmpp reading..
     */
    private InputStream inputStream;

    /**
     * Status of stanza debugging, mainly repetition of received stanzas in the
     * Android logcat.
     */
    private boolean debugEnabled = true;

    /**
     * The xmpp version.
     */
    private String version;

    /**
     * The xmpp stream from field.
     */
    private String from;

    /**
     * The xmpp stream to field.
     */
    private String to;

    /**
     * The xmpp stream language.
     */
    private String language;

    /**
     * The xmpp stream id.
     */
    private String id;

    /**
     * The last time a stanza was received.
     */
    private long lastReceiveTime = System.currentTimeMillis();

    /**
     * The internal pull parser.
     */
    private XmlPullParser parser;

    /**
     * Create a new XMPP input stream on top of a lowlevel io stream.
     * @param in InputStream The underlying input stream. 
     * @throws XmppTransportException In case of a transport exception.
     */
    public XmppInputStream(InputStream in)
        throws XmppTransportException
    {
        attach(in);
    }

    /**
     * Read the stream opening.
     * @throws XmlPullParserException In case of invalid xml.
     * @throws IOException In case of a transport error.
     */
    public void readOpening() throws XmlPullParserException, IOException {
        parser.nextTag();

        Log.d(TAG, "Receiving stream start...");

        parser.require(
            XmlPullParser.START_TAG,
            "http://etherx.jabber.org/streams",
            "stream"
        );

        // we've seen a valid <stream start, save some parameters
        for (int i = 0, l = parser.getAttributeCount(); i < l; i++) {
            String attributeName = parser.getAttributeName(i);
            String attributeNamespace = parser.getAttributeNamespace(i);
            if (attributeNamespace.length() == 0) {
                attributeNamespace = parser.getNamespace();
            }

            if (attributeNamespace
                .equals("http://www.w3.org/XML/1998/namespace")
                && attributeName.equals("lang")
            ) {
                language = parser.getAttributeValue(i).toString();
                continue;
            }

            if (
                !attributeNamespace.equals("http://etherx.jabber.org/streams")
            ) {
                Log.d(TAG, "Unknown stream attribute namespace "
                        + attributeNamespace + " containing "
                        + attributeName);
                continue;
            }

            if (attributeName.equals("version")) {
                version = parser.getAttributeValue(i).toString();
                continue;
            }

            if (attributeName.equals("from")) {
                from = parser.getAttributeValue(i).toString();
                continue;
            }

            if (attributeName.equals("to")) {
                to = parser.getAttributeValue(i).toString();
                continue;
            }

            if (attributeName.equals("id")) {
                id = parser.getAttributeValue(i).toString();
                continue;
            }

            Log.d(TAG, "Unknown stream attribute "
                    + attributeName + " from namespace"
                    + attributeNamespace);
        }

        Log.d(TAG, "Stream started!");
    }

    /**
     * Pull the next stanza from the stream, throwing a XmppException on error.
     * @return Stanza The next stream stanza.
     * @throws XmppException In case of a xmpp error.
     */
    public Stanza nextStanza()
        throws XmppException {

        Stanza stanza = null;
        try {
            stanza = XMLUtils.readStanza(parser);
        } catch (IllegalArgumentException e) {
            throw new XmppMalformedException("can't parse stanza", e);
        } catch (IllegalStateException e) {
            throw new XmppMalformedException("can't parse stanza", e);
        } catch (XmlPullParserException e) {
            throw new XmppMalformedException("can't parse stanza", e);
        } catch (IOException e) {
            throw new XmppTransportException("error during stanza read", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new XmppTransportException("XML reader b0rked", e);
        }

        if (debugEnabled) {
            Log.d(TAG, "Stanza: " + stanza.getXml());
        }

        lastReceiveTime = System.currentTimeMillis();
        return stanza;
    }

    /**
     * Detach from the underlying input stream.
     */
    public void detach() {
        parser = null;
        inputStream = null;
    }

    /**
     * Attach to an underlying input stream, usually done during feature
     * negotiation as some features require stream resets.
     * @param in InputStream The new underlying input stream.
     * @throws XmppTransportException In case of a transport error.
     */
    public void attach(InputStream in) throws XmppTransportException {
        this.inputStream = in;
        try {
            parser = XMLUtils.getXMLPullParser();
            parser.setInput(in, "UTF-8");
        } catch (XmlPullParserException e) {
            throw new XmppTransportException("Can't initialize pull parser", e);
        }
    }

    /**
     * Retrieve the xmpp version string (usually 1.0 or null).
     * @return String The xmpp version string, or null.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Retrieve the xmpp streams from attribute.
     * @return String The xmpp stream from attribute value.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Retrieve the xmpp streams to attribute.
     * @return String The xmpp stream to attribute value.
     */
    public String getTo() {
        return to;
    }

    /**
     * Retrieve the xmpp xml language attribute.
     * @return String The xmpp xml language attribute value.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Retrieve the xmpp streams id attribute.
     * @return String The xmpp stream id attribute value.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieve the debugging state.
     * @return boolean True if stanzas are replayed to the Android log.
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Change the debugging state.
     * @param debugEnabled boolean True if stanzas should be replayed to the
     *                             Android log, usefull for debugging.
     */
    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    /**
     * Retrieve the unix timestamp of the last receive event.
     * @return long The unix timestamp of the last received stanza.
     */
    public long getLastReceiveTime() {
        return lastReceiveTime;
    }

    /**
     * Close this stream, closing the underlying input stream.
     * This method should be used as a cleanup method to kill stalled
     * connections.
     */
    public synchronized void close() {
        if (inputStream == null) {
            return;
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            /* not important */
        }
        inputStream = null;
    }

    /**
     * Check if {@link #close()} or {@link #detach()} has been called.
     * @return boolean True if close or detach has been called.
     */
    public boolean isClosed() {
        return inputStream == null;
    }

}
