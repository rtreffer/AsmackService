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

import java.util.Set;
import java.util.TreeMap;

import org.apache.harmony.javax.security.sasl.SaslClient;
import org.apache.harmony.javax.security.sasl.SaslException;
import org.apache.qpid.management.common.sasl.PlainSaslClient;
import org.w3c.dom.Node;

import android.util.Base64;

import com.googlecode.asmack.XMLUtils;
import com.googlecode.asmack.XMPPUtils;
import com.googlecode.asmack.XmppAccount;
import com.googlecode.asmack.XmppException;
import com.googlecode.asmack.XmppSaslException;
import com.novell.sasl.client.DigestMD5SaslClient;

/**
 * Simple connection-neutral SASL engine to handle XMPP SASL login. This class
 * is stateless and will barely perform the SASL roundtrips and framework
 * handling.
 */
public class SASLEngine {

    /**
     * The common namespace for sasl in xmpp.
     */
    private final static String NAMESPACE = "urn:ietf:params:xml:ns:xmpp-sasl";

    /**
     * Perform the sasl roundtrip on a given connection.
     * @param xmppInputStream XmppInputStream The underlying xmpp input stream.
     * @param xmppOutputStream XmppOutputStream The underlying xmpp output
     *                                          stream.
     * @param methods Set<String> The set of allowed authentification methods.
     * @param account XmppAccount The internal xmpp account.
     * @return boolean True on success.
     * @throws XmppException In case of a hard xml/xmpp error.
     */
    public static boolean login(
        XmppInputStream xmppInputStream,
        XmppOutputStream xmppOutputStream,
        Set<String> methods,
        XmppAccount account
    ) throws XmppException
    {
        SaslClient saslClient = null;
        if (methods.contains("DIGEST-MD5")) {
            saslClient = DigestMD5SaslClient.getClient(
                XMPPUtils.getUser(account.getJid()),
                "xmpp",
                XMPPUtils.getDomain(account.getJid()),
                new TreeMap<Object, Object>(),
                new AccountCallbackHander(account)
            );
        } else
        if (methods.contains("PLAIN")) {
            try {
                saslClient = new PlainSaslClient(
                    null, new AccountCallbackHander(account)
                );
            } catch (SaslException e) {
                throw new XmppSaslException("Could not instanciate plain auth", e);
            }
        }
        if (saslClient.hasInitialResponse()) {
            try {
                xmppOutputStream.sendUnchecked(
                        "<auth " +
                        "xmlns='" + NAMESPACE + "' " +
                        "mechanism='" + 
                        saslClient.getMechanismName() +
                        "'>" +
                        encodeBase64(saslClient.evaluateChallenge(null)) +
                        "</auth>"
                );
            } catch (SaslException e) {
                throw new XmppSaslException("Could not instanciate plain auth", e);
            }
        } else {
            xmppOutputStream.sendUnchecked(
                    "<auth " +
                    "xmlns='" + NAMESPACE + "' " +
                    "mechanism='" + 
                    saslClient.getMechanismName() +
                    "'/>"
            );
        }
        Node stanza = xmppInputStream.nextStanza().getDocumentNode();
        while (!XMLUtils.isInstance(stanza, NAMESPACE, "success")) {
            if (!XMLUtils.isInstance(stanza, NAMESPACE, "challenge")) {
                throw new XmppSaslException("Authentification failed: "
                        + stanza.getNodeValue());
            }
            String content = stanza.getFirstChild().getNodeValue().trim();
            byte[] response;
            try {
                response = saslClient.evaluateChallenge(decodeBase64(content));
            } catch (SaslException e) {
                throw new XmppSaslException("Could not evaluate challenge", e);
            }
            if (saslClient.isComplete()) {
                xmppOutputStream.sendUnchecked(
                    "<response xmlns='" + NAMESPACE + "'/>"
                );
            } else {
                xmppOutputStream.sendUnchecked(
                    "<response xmlns='" + NAMESPACE + "'>" +
                    encodeBase64(response) +
                    "</response>"
                );
            }
            stanza = xmppInputStream.nextStanza().getDocumentNode();
        }
        return true;
    }

    /**
     * XMPP/SASL compatible base64 encoder, equal to
     * <code>Base64.encodeToString(data, Base64.NO_WRAP);</code>
     * @param data byte[] The binary representation.
     * @return String The base64 String.
     */
    private static String encodeBase64(byte data[]) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    /**
     * XMPP/SASL compatible base64 decoder, equal to
     * <code>Base64.decode(input, Base64.DEFAULT);</code>
     * @param input String The base64 String.
     * @return byte[] The binary representation.
     */
    private static byte[] decodeBase64(String input) {
        return Base64.decode(input, Base64.DEFAULT);
    }

}
