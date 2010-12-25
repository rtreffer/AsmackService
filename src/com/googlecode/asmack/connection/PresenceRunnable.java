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

package com.googlecode.asmack.connection;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XmppException;

/**
 * A runnable to move the presence update into the background, reducing the
 * total roundtrip of a presence updates.
 */
public class PresenceRunnable implements Runnable {

    /**
     * The connection for the presence update.
     */
    private final Connection connection;
    private final String verification;

    /**
     * Create a new presence runnable for a given connection.
     * @param connection The output connection for the presence update.
     */
    public PresenceRunnable(Connection connection, String verification) {
        this.connection = connection;
        this.verification = verification;
    }

    /**
     * Execute the presence update.
     */
    @Override
    public void run() {
        String payload = "<presence />";
        if (verification != null) {
            payload = "<presence><c xmlns='http://jabber.org/protocol/caps' " +
                      "hash='sha-1' " +
                      "node='http://github.com/rtreffer/AsmackService' " +
                      "ver='" + verification +"'" +
                      "/></presence>";
        }
        Stanza stanza = new Stanza(
                "presence",
                "",
                "",
                payload,
                null
        );
        try {
            connection.send(stanza);
        } catch (XmppException e) {
            /* PING is non critical */
        }
    }

}
