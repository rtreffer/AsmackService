/*
 * Licensed under Apache License, Version 2.0 or LGPL 2.1, at your option.
 * --
 *
 * Copyright 2010 Rene Treffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 *
 * Copyright (C) 2010 Rene Treffer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.googlecode.asmack.connection.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;

import com.googlecode.asmack.Attribute;
import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XMLUtils;
import com.googlecode.asmack.XmppException;
import com.googlecode.asmack.connection.XmppTransportException;

/**
 * Wrap an {@link OutputStream} into an XMPP compliant stream.
 */
public class XmppOutputStream {

    /**
     * The debugging tag of this class ("XmppOutputStream").
     */
    private static final String TAG = XmppOutputStream.class.getSimpleName();

    /**
     * The lowlevel output stream.
     */
    private OutputStream outputStream;

    /**
     * The global xml serializer, used for stanza copy.
     */
    private XmlSerializer xmlSerializer;

    /**
     * Create a new XmppOutputStram. dirctly attached to the given OutputStream.
     * @param out OutputStream The low level io OutputStream.
     * @throws XmlPullParserException In case of an xml error.
     * @throws IOException In case of a transport error.
     */
    public XmppOutputStream(OutputStream out)
        throws XmppTransportException, IOException
    {
        attach(out, true, false);
    }

    /**
     * Attach this stream to a new OutputStream. This method is usually needed
     * during feature negotiation, as some features like compression or tls
     * require a stream reset.
     * @param out OutputStream The new underlying output stream.
     * @param sendDeclaration boolean True if a <code><?xml></code> header
     *                                should be send.
     * @param sendBOM boolean False to suppress the utf-8 byte order marker.
     *                        This is usually what you want as BOM is poorly
     *                        tested in the xmpp world and discourged by the
     *                        unicode spec (at least for utf-8).
     * @throws IOException In case of a transport error.
     * @throws XmlPullParserException In case of a xml error.
     */
    public void attach(
        OutputStream out,
        boolean sendDeclaration,
        boolean sendBOM
    ) throws IOException, XmppTransportException
    {
        if (sendBOM) {
            Log.d(TAG, "BOM");
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);
        }
        if (sendDeclaration) {
            Log.d(TAG, "open stream");
            out.write(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>".getBytes()
            );
        }
        if (sendBOM || sendDeclaration) {
            Log.d(TAG, "flush()");
            out.flush();
        }
        outputStream = out;
        try {
            xmlSerializer = XMLUtils.getXMLSerializer();
        } catch (XmlPullParserException e) {
            throw new XmppTransportException("Can't initialize the pull parser", e);
        }
        Log.d(TAG, "set output");
        xmlSerializer.setOutput(outputStream, "UTF-8");
    }

    /**
     * Open a stream to a given target domain. This method writes the XMPP
     * stream opening.
     * <code>
     * <stream:stream .....>
     * </code>
     * @param to String The target domain.
     * @param lang String The target language.
     * @throws IOException In case of a transport problem.
     */
    public void open(String to, String lang) throws IOException {
        xmlSerializer.setPrefix("stream", "http://etherx.jabber.org/streams");
        xmlSerializer.setPrefix("", "jabber:client");
        xmlSerializer.startTag("http://etherx.jabber.org/streams", "stream");
        xmlSerializer.attribute(null, "version", "1.0");
        if (lang != null) {
            xmlSerializer.attribute("xml", "lang", lang);
        }
        if (to != null) {
            xmlSerializer.attribute(null, "to", to);
        }
        xmlSerializer.flush();
        outputStream.flush();
    }

    /**
     * Detach from the current output stream.
     */
    public void detach() {
        xmlSerializer = null;
        outputStream = null;
    }

    /**
     * Send a string fragment to the server, flushing the stream afterwards.
     * @param stanza String The stanza string.
     * @throws XmppTransportException In case of a transport exception.
     */
    public void sendUnchecked(String stanza) throws XmppTransportException {
        Log.d(TAG, stanza);
        synchronized (outputStream) {
            try {
                outputStream.write(stanza.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                throw new XmppTransportException("Stanza sending failed", e);
            }
        }
    }

    /**
     * Send a stanza through this stream. The stanza will be merged and
     * validated.
     * @param stanza Stanza The stanza to send.
     * @throws XmppException In case of an error.
     */
    public void send(Stanza stanza)
        throws XmppException
    {
        XmlPullParser xmlPullParser;
        try {
            xmlPullParser = XMLUtils.getXMLPullParser();
        } catch (XmlPullParserException e) {
            throw new XmppException("Can't create xml parser", e);
        }
        try {
            xmlPullParser.setInput(
                new ByteArrayInputStream(stanza.getXml().getBytes()), "UTF-8");
        } catch (XmlPullParserException e) {
            throw new XmppException("Can't parse input", e);
        }

        StringWriter stringWriter = new StringWriter();
        XmlSerializer xmlSerializer;
        try {
            xmlSerializer = XMLUtils.getXMLSerializer();
        } catch (XmlPullParserException e) {
            throw new XmppException("Can't create xml serializer", e);
        }
        try {
            xmlSerializer.setOutput(stringWriter);
        } catch (IllegalArgumentException e) {
            throw new XmppException("Please report", e);
        } catch (IllegalStateException e) {
            throw new XmppException("Please report", e);
        } catch (IOException e) {
            throw new XmppException("Please report", e);
        }

        try {
            xmlSerializer.startTag(stanza.getNamespace(), stanza.getName());

            HashSet<String> addedAttributes = new HashSet<String>();

            if (stanza.getAttributes() != null) {
                for (Attribute attr: stanza.getAttributes()) {
                    addedAttributes.add(
                        attr.getNamespace() + "\0" + attr.getName()
                    );
                    xmlSerializer.attribute(
                        attr.getNamespace(),
                        attr.getName(),
                        attr.getValue()
                    );
                }
            }

            xmlPullParser.nextTag();
            int attributeCount = xmlPullParser.getAttributeCount();
            for (int i = 0; i < attributeCount; i++) {
                String key = xmlPullParser.getAttributeNamespace(i) + "\0" +
                             xmlPullParser.getAttributeName(i);
                if (addedAttributes.contains(key)) {
                    continue;
                }
                xmlSerializer.attribute(
                    xmlPullParser.getAttributeNamespace(i),
                    xmlPullParser.getAttributeName(i),
                    xmlPullParser.getAttributeValue(i)
                );
            }

            if (xmlPullParser.isEmptyElementTag()) {
                xmlSerializer.endTag(stanza.getNamespace(), stanza.getName());
            } else {
                XMLUtils.copyXML(xmlPullParser, xmlSerializer);
            }
            xmlSerializer.endDocument();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            new XmppException("Please report", e);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        sendUnchecked(stringWriter.toString());
    }

    /**
     * Close this connection.
     */
    public synchronized void close() {
        if (outputStream == null) {
            return;
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            /* not important */
        }
        outputStream = null;
    }

    /**
     * Retrieve the closed state.
     * @return boolean True if this stream has been closed.
     */
    public boolean isClosed() {
        return outputStream == null;
    }

}
