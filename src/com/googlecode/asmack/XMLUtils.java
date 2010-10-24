package com.googlecode.asmack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;

/**
 * Helper for copy/read XML fragments from or to a stream as well as general
 * XML query helpers.
 */
public class XMLUtils {

    /**
     * Class log tag.
     */
    private static final String TAG = XMLUtils.class.getSimpleName();

    /**
     * XML document factory configured for XMPP needs. This includes but is not
     * limited to namespace awareness and disabled validation / fetch.
     */
    private final static DocumentBuilderFactory documentBuilderFactory;

    /**
     * XML parser factory, configured for XMPP use.
     */
    private static final XmlPullParserFactory xmlPullParserFactory;

    /**
     * Initialize the factories.
     */
    static {
        XmlPullParserFactory xmlPullParserFactoryInstance = null;
        try {
            xmlPullParserFactoryInstance = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Couldn't create sax factory!", e);
        }
        xmlPullParserFactory = xmlPullParserFactoryInstance;
        xmlPullParserFactory.setNamespaceAware(true);
        xmlPullParserFactory.setValidating(false);

        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // XMPP requires namespace awareness
        documentBuilderFactory.setNamespaceAware(true);
        // slightly normalize
        documentBuilderFactory.setCoalescing(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(false);
        // No includes / outbound references
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setValidating(false);
    }

    /**
     * Read a stanza from a xml stream. This does not include the connection
     * related stanza settings like the via tag.
     * @param xmlPullParser The XML PullParser of the current stream.
     * @return The read stanza.
     * @throws XmlPullParserException If the reading failed.
     * @throws IllegalArgumentException When one of the components received invalid arguments.
     * @throws IOException In case of a closed connection.
     */
    public final static Stanza readStanza(XmlPullParser xmlPullParser
    ) throws XmlPullParserException, IllegalArgumentException, IOException
    {
        try {
            ArrayList<Attribute> attributes = new ArrayList<Attribute>(4);
            String stanzaName = null;
            String stanzaNamespace = null;
            String namespace = "";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XmlSerializer xmlSerializer = xmlPullParserFactory.newSerializer();
            xmlSerializer.setOutput(baos, "UTF-8");
            do {
                int type = xmlPullParser.next();
                switch (type) {
                case XmlPullParser.END_TAG:
                    xmlSerializer.endTag(
                        xmlPullParser.getNamespace(),
                        xmlPullParser.getName()
                    );
                    break;
                case XmlPullParser.START_TAG:
                    String ns = xmlPullParser.getNamespace();
                    if (!ns.equals(namespace)) {
                        /* XMPP Server like ejabberd fail to read stanzas
                         * unless the stanza has the "" prefix.
                         */
                        namespace = ns;
                        xmlSerializer.setPrefix("", ns);
                    }
                    xmlSerializer.startTag(
                        namespace,
                        xmlPullParser.getName()
                    );
                    if (xmlSerializer.getDepth() == 1) {
                        stanzaName = xmlPullParser.getName();
                        stanzaNamespace = namespace;
                    }
                    int attributeCount = xmlPullParser.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        xmlSerializer.attribute(
                            xmlPullParser.getAttributeNamespace(i),
                            xmlPullParser.getAttributeName(i),
                            xmlPullParser.getAttributeValue(i)
                        );
                        if (xmlSerializer.getDepth() == 1) {
                            attributes.add(new Attribute(
                                xmlPullParser.getAttributeName(i),
                                xmlPullParser.getAttributeNamespace(i),
                                xmlPullParser.getAttributeValue(i)
                            ));
                        }
                    }
                    break;
                case XmlPullParser.TEXT:
                    xmlSerializer.text(xmlPullParser.getText());
                    break;
                case XmlPullParser.END_DOCUMENT:
                    // We shouldn't see that!
                    throw new XmlPullParserException("Unexpected end of stream.");
                default:
                    throw new IllegalStateException(
                        "Unexpected pull parser type " + type
                     );
                }
            } while (xmlSerializer.getDepth() > 0);

            xmlSerializer.endDocument();
            baos.close();
            return new Stanza(
                stanzaName,
                stanzaNamespace,
                null,
                new String(baos.toByteArray(), "UTF-8"),
                attributes
            );
        } catch (ArrayIndexOutOfBoundsException e) {
            /* kXML misshandles some read errors and throws
             * ArrayIndexOutOfBoundsException.
             */
            throw new XmlPullParserException("Parser failed", xmlPullParser, e);
        } catch (IllegalStateException e) {
            throw new XmlPullParserException("Parser failed", xmlPullParser, e);
        }
    }

    /**
     * Copy an XML fragment from a source (pull parser) to a destination
     * (serializer), pulling all element namespaces into the empty prefix.
     * 
     * Errata: Some common XMPP servers (read: ejabberd) don't handle stanzas
     *         like <n0:iq to="romeo@example.com" xmlns:n0="jabber:client">
     *         this means we'll have to rename "n0" to "", whis isn't the
     *         default behaviour.
     *
     * @param xmlPullParser The pull parser for xml reading.
     * @param xmlSerializer The serializer for xml writing.
     * @throws XmlPullParserException In case of invalid XML.
     * @throws IOException In case of an closes connection.
     */
    public final static void copyXML(
        XmlPullParser xmlPullParser, XmlSerializer xmlSerializer
    ) throws XmlPullParserException, IOException
    {
        int startDepth = xmlPullParser.getDepth();
        String namespace = null;
        do {
            int type = xmlPullParser.next();
            switch (type) {
            case XmlPullParser.END_TAG:
                xmlSerializer.endTag(
                    xmlPullParser.getNamespace(),
                    xmlPullParser.getName()
                );
                break;
            case XmlPullParser.START_TAG:
                if (!xmlPullParser.getNamespace().equals(namespace)) {
                    namespace = xmlPullParser.getNamespace();
                    xmlSerializer.setPrefix("", namespace);
                }
                xmlSerializer.startTag(
                    xmlPullParser.getNamespace(),
                    xmlPullParser.getName()
                );
                int attributeCount = xmlPullParser.getAttributeCount();
                for (int i = 0; i < attributeCount; i++) {
                    xmlSerializer.attribute(
                        xmlPullParser.getAttributeNamespace(i),
                        xmlPullParser.getAttributeName(i),
                        xmlPullParser.getAttributeValue(i)
                    );
                }
                break;
            case XmlPullParser.TEXT:
                xmlSerializer.text(xmlPullParser.getText());
                break;
            case XmlPullParser.END_DOCUMENT:
                // We shouldnt see that!
                throw new XmlPullParserException("Unexpected end of stream.");
            default:
                throw new IllegalStateException(
                    "Unexpected pull parser type " + type
                );
            }
        } while (xmlPullParser.getDepth() > startDepth);
    }

    /**
     * Turn an XML String into a DOM.
     * @param xml The xml String.
     * @return A XML Document.
     * @throws SAXException In case of invalid XML.
     */
    public static Document getDocument(String xml)
        throws SAXException
    {
        try {
            DocumentBuilder documentBuilder =
                        documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Parser not configured", e);
        } catch (IOException e) {
            throw new IllegalStateException("IOException on read-from-memory", e);
        }
    }

    /**
     * Retrieve the document node of a XML string.
     * @param xml The XML string.
     * @return The root XML Node of a document.
     * @throws SAXException In case of invalid xml.
     */
    public static Node getDocumentNode(String xml)
        throws SAXException
    {
        return getDocument(xml).getDocumentElement();
    }

    /**
     * Return the first child of a node, based on name/namespace.
     * @param node The node to scan.
     * @param namespace The requested namespace, or null for no preference.
     * @param name The element name, or null for no preference.
     * @return The first matching node or null.
     */
    public static Node getFirstChild(
        Node node,
        String namespace,
        String name
    ) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0, l = childNodes.getLength(); i < l; i++) {
            Node child = childNodes.item(i);
            if (isInstance(child, namespace, name)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Test if a node matches a namespace/name criteria, handling a null as
     * match any.
     * @param node The node to scan.
     * @param namespace The requested namespace, or null for no preference.
     * @param name The element name, or null for no preference.
     * @return True if the element matched.
     */
    public static boolean isInstance(
            Node node,
            String namespace,
            String name
    ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }
        if (namespace != null &&
           !namespace.equals(node.getNamespaceURI())
        ) {
            return false;
        }
        if (name != null &&
           !name.equals(node.getLocalName()) &&
           !name.equals(node.getNodeName())
        ) {
            return false;
        }
        return true;
    }

    /**
     * Search for availability of a certain child.
     * @param node The node to scan.
     * @param namespace The requested namespace, or null for no preference.
     * @param name The element name, or null for no preference.
     * @return True if a matching child exists.
     */
    public static boolean hasChild(
            Node node,
            String namespace,
            String name
    ) {
        return getFirstChild(node, namespace, name) != null;
    }

    /**
     * Generate a xmpp capable (namespaces, no external validation) pull parser.
     * @return A new XML PullParser instance.
     * @throws XmlPullParserException In case of parser missconfiguration.
     */
    public static XmlPullParser getXMLPullParser()
        throws XmlPullParserException
    {
        return xmlPullParserFactory.newPullParser();
    }

    /**
     * Generate a xmpp capable (namespaces, no external validation) serializer.
     * @return A new XML PullParser instance.
     * @throws XmlPullParserException In case of parser missconfiguration.
     */
    public static XmlSerializer getXMLSerializer()
        throws XmlPullParserException
    {
        return xmlPullParserFactory.newSerializer();
    }

}
