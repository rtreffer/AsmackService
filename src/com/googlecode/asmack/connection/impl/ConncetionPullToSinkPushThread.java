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

import android.util.Log;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.StanzaSink;
import com.googlecode.asmack.XmppException;
import com.googlecode.asmack.connection.Connection;

/**
 * Transform {@link XmppInputStream#nextStanza()} pull events into
 * {@link StanzaSink#receive(Stanza)} events.
 */
public class ConncetionPullToSinkPushThread extends Thread {

    /**
     * Class debugging tag (ConnectionPullToSinkPushThread).
     * Value: {@value TAG}
     */
    private static final String TAG = ConncetionPullToSinkPushThread.class
                                        .getSimpleName();

    /**
     * Sink to receive events.
     */
    private final StanzaSink sink;

    /**
     * The xmpp input stream used for reading. 
     */
    private final XmppInputStream xmppInput;

    /**
     * The lowleve connection uswed by the xmpp input stream.
     */
    private final Connection connection;

    /**
     * <p>Create a new Thread to pull stanzas from a {@link XmppInputStream}
     * and push it to a {@link StanzaSink}.</p>
     *
     * <p> This Thread must be explicitly explicitly started (as required for
     * polymorphism).</p>
     *
     * @param connection The symbolic {@link Connection}.
     * @param xmppInput The {@link XmppInputStream} for the connection.
     * @param sink The receiving {@link StanzaSink}.
     */
    public ConncetionPullToSinkPushThread(
        Connection connection,
        XmppInputStream xmppInput,
        StanzaSink sink
    ) {
        this.connection = connection;
        this.xmppInput = xmppInput;
        this.sink = sink;
    }

    /**
     * <p>Run the main pull/push loop.</p>
     * <p>The {@link XmppInputStream#nextStanza()} to
     * {@link StanzaSink#receive(Stanza)} will run until a
     * {@link XmppException} is received.</p>
     */
    @Override
    public void run() {
        String resourceJid = connection.getResourceJid();
        try {
            while (true) {
                Stanza stanza = xmppInput.nextStanza();
                stanza.setVia(resourceJid);
                sink.receive(stanza);
            }
        } catch (XmppException e) {
            try {
                connection.close();
            } catch (Exception ex) {
                // we just try to clean up, ignore problems
            }
            Log.e(TAG, "Connection aborted", e);
            sink.connectionFailed(connection, e);
        }
    }

}
