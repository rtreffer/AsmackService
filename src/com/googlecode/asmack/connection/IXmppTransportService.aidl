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
import com.googlecode.asmack.XmppIdentity;

/**
 * Service interface for a xmpp service.
 */
interface IXmppTransportService {

    /**
     * Try to login with a given jid/password pair. Returns true on success.
     * @param jid The user jid.
     * @param password The user password.
     */
    boolean tryLogin(String jid, String password);

    /**
     * Send a stanza via this service, return true on successfull delivery to
     * the network buffer (plus flush). Please note that some phones ignore
     * flush request, thus "true" doesn't mean "on the wire".
     * @param stanza The stanza to send.
     */
    boolean send(in Stanza stanza);

    /**
     * Send a stanza via this service, through all resource jids.
     * @param stanza The stanza to send.
     */
    void sendFromAllResources(in Stanza stanza);

    /**
     * Send a stanza via this service, through all account jids.
     * @param stanza The stanza to send.
     */
    void sendFromAllAccounts(in Stanza stanza);

    /**
     * Scan all connections for the current connection of the given jid and
     * return the full resource jid for the user.
     * @return The full user jid (including resource).
     */
    String getFullJidByBare(String bare);

    /**
     * Enable a new feature for a given jid only. The new feature will be
     * announced during the next tick.
     * @param jid The jid of the account that should announce the feature.
     * @param feature The feature to enable.
     */
    void enableFeatureForJid(String jid, String feature);

    /**
     * Enable a feature service wide. This means that all connection will take
     * advantage of the new feature. This may have unexpected side effects on
     * other applications, so use with care.
     * @param feature The feature to enable.
     */
    void enableFeature(String feature);

    /**
     * Add a new identity to the xmpp account specified by the jid.
     * @param jid The jid to enable.
     * @param identity The identity to add.
     */
    void addIdentityForJid(String jid, in XmppIdentity identity);

    /**
     * Add a new identity to all xmpp accounts. This affects all connections
     * and should thus be handled with care.
     * @param identity The xmpp identity to add.
     */
    void addIdentity(in XmppIdentity identity);

    /**
     * Retrieve all current account jids.
     * @param connected True if you only jids of connected acocunts should be
     *                  returned.
     * @return List of account jids.
     */
    String[] getAllAccountJids(boolean connected);

    /**
     * Retrieve all resource jids (where available).
     * @param connected True if you only jids of connected acocunts should be
     *                  returned.
     * @return List of account jids.
     */
    String[] getAllResourceJids(boolean connected);

}
