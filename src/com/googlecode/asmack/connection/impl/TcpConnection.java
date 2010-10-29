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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.StanzaSink;
import com.googlecode.asmack.XmppAccount;
import com.googlecode.asmack.XmppException;
import com.googlecode.asmack.XmppMalformedException;
import com.googlecode.asmack.connection.Connection;
import com.googlecode.asmack.connection.XmppTransportException;

/**
 * <p>TCP xmpp stream implementation. This object is responsible for connection
 * string based on hostname:port or ip:port.
 * <ul>
 *   <li>tcp:hostname</li>
 *   <li>tcp:hostname:1234</li>
 *   <li>tcp:1.2.3.4</li>
 *   <li>tcp:1.2.3.4:1234</li>
 *   <li>tcp:[2a01:198:500::1]:1234</li>
 * </ul>
 * </p>
 * <p><b>Note:</b> Android usually isn't ipv6 compatible. This means that the
 * use of ipv6 addresses is discouraged and has lower priority than ipv4</p> 
 */
public class TcpConnection implements Connection {

    /**
     * Internal logging tag (TcpConnection).
     */
    private static final String TAG = TcpConnection.class.getSimpleName();

    /**
     * The account name to use for login or realm domain.
     */
    protected XmppAccount account;

    /**
     * The low level tcp socket of this connection.
     */
    private Socket socket;

    /**
     * The fully bound resource jid (username@domain.tld/resource).
     */
    private String resourceJid;

    /**
     * The bare jid (username@domain.tld).
     */
    private String bareJid;

    /**
     * Field xmppInput.
     */
    private XmppInputStream xmppInput;

    /**
     * Field xmppOutput.
     */
    private XmppOutputStream xmppOutput;

    /**
     * Constructor for TcpConnection.
     * @param account XmppAccount
     */
    public TcpConnection(XmppAccount account) {
        this.account = account;
        bareJid = account.getJid();
    }

    /**
     * Method connect.
     * @param sink StanzaSink
     * @throws XmppException
     * @see com.googlecode.asmack.connection.Connection#connect(StanzaSink)
     */
    public void connect(StanzaSink sink) throws XmppException {
        String connection = account.getConnection();
        connection = connection.substring(4).trim(); // cut "tcp:"

        // Target
        int port = 5222;
        InetAddress addresse;

        // Get Port
        int split = connection.lastIndexOf(':');
        if (split != -1) {
            String portNumber = connection.substring(split + 1);
            port = Integer.parseInt(portNumber);
            connection = connection.substring(0, split);
        }

        // Get Host IPs
        InetAddress[] inetAddresses;
        if (connection.charAt(0) == '[' &&
            connection.charAt(connection.length()) == ']') {
            // IPv6
            if (split == -1) {
                throw new IllegalStateException(
                    "Not a valid tcp uri (" + account.getConnection() + ")"
                );
            }
            String ipv6 = connection.substring(1, connection.length() - 1);
            try {
                inetAddresses = InetAddress.getAllByName(ipv6);
            } catch (UnknownHostException e) {
                throw new XmppTransportException("can't resolve host", e);
            }
        } else {
            // IPv4 or domain
            // prefer IPv4, IPv6 if no IPv4 record found
            try {
                inetAddresses = Inet4Address.getAllByName(connection);
                if (inetAddresses.length == 0) {
                    inetAddresses = Inet6Address.getAllByName(connection);
                }
            } catch (UnknownHostException uhe) {
                try {
                    inetAddresses = Inet6Address.getAllByName(connection);
                } catch (UnknownHostException e) {
                    throw new XmppTransportException("can't resolve host", e);
                }
            }
        }

        if (inetAddresses == null || inetAddresses.length == 0) {
            throw new XmppTransportException("Couldn't resolve " + connection);
        }
        if (inetAddresses.length == 1) {
            addresse = inetAddresses[0];
        } else {
            int index = (int)(Math.random() * Integer.MAX_VALUE);
            index %= inetAddresses.length;
            addresse = inetAddresses[index];
        }

        connect(addresse, port);

        new ConncetionPullToSinkPushThread(this, xmppInput, sink).start();
    }

    /**
     * Start the tcp connection to a given ip/port pair.
     * @param addresse InetAddress The target internet address.
     * @param port int The target port.
     * @throws XmppException In case of a lower level exception.
     */
    protected void connect(InetAddress addresse, int port)
        throws XmppException
    {
        SocketFactory socketFactory = SocketFactory.getDefault();
        try {
            socket = socketFactory.createSocket(addresse, port);
        } catch (IOException e) {
            throw new XmppTransportException("Can't connect", e);
        }
        FeatureNegotiationEngine engine;
        try {
            engine = new FeatureNegotiationEngine(socket);
        } catch (XmlPullParserException e) {
            throw new XmppMalformedException("Can't connect", e);
        } catch (IOException e) {
            throw new XmppTransportException("Can't connect", e);
        }
        engine.open(account);
        resourceJid = engine.bind(account.getResource());
        if (resourceJid == null) {
            throw new XmppTransportException("Can't bind");
        }
        Log.d(TAG, "Bound as " + resourceJid);
        xmppInput = engine.getXmppInputStream();
        xmppOutput = engine.getXmppOutputStream();
    }

    /**
     * Return the full resource jid (username@domain.tld/resource).
     * @return String The full resource jid.
     * @see com.googlecode.asmack.connection.Connection#getResourceJid()
     */
    @Override
    public String getResourceJid() {
        return resourceJid;
    }

    /**
     * Send a stanza through this connection.
     * @param stanza Stanza The stanza.
     * @throws XmppException In case of stanza or connection errors.
     * @see com.googlecode.asmack.connection.Connection#send(Stanza)
     */
    @Override
    public void send(Stanza stanza) throws XmppException {
        xmppOutput.send(stanza);
    }

    /**
     * Close the TCP connection.
     * @throws XmppException In case of a lowlevel connection problem.
     * @see com.googlecode.asmack.connection.Connection#close()
     */
    @Override
    public void close() throws XmppException {
        if (xmppInput != null) {
            xmppInput.close();
        }
        if (xmppOutput != null) {
            xmppOutput.close();
        }
    }

    /**
     * Retrieve the bare jid (username@domain.tld).
     * @return String The bare user jid (username@domain.tld).
     */
    public String getBareJid() {
        return bareJid;
    }

    /**
     * Retrieve the timestamp of the last received element.
     * @return long The unix time of the last received package.
     * @see com.googlecode.asmack.connection.Connection#lastReceive()
     */
    @Override
    public long lastReceive() {
        return xmppInput.getLastReceiveTime();
    }

    /**
     * Retrieve the underlying xmpp account.
     * @return XmppAccount The xmpp account used for connection/authentication.
     * @see com.googlecode.asmack.connection.Connection#getAccount()
     */
    @Override
    public XmppAccount getAccount() {
        return account;
    }

    /**
     * Check if this connection has already been closed.
     * @return boolean True if there has been a call to {@link #close()}.
     * @see com.googlecode.asmack.connection.Connection#isClosed()
     */
    @Override
    public boolean isClosed() {
        return xmppInput.isClosed() || xmppOutput.isClosed();
    }

}
