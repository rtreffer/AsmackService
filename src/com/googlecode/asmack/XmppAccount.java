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

package com.googlecode.asmack;

/**
 * Xmpp Account metadata. This class holds passwords, jids and connection
 * strings.
 */
public class XmppAccount {

    /**
     * The user jid as username@domain.tld.
     */
    private String jid;

    /**
     * The user password.
     */
    private String password;

    /**
     * The connection string in any form accepted by the ConnectionFactory.
     */
    private String connection;

    /**
     * The preferred account resource. The server may alter this. The actual
     * resource can be queried via the connection interface.
     */
    private String resource;

    /**
     * Boolean representing the roster sync and contacts integration status.
     */
    private boolean rosterSyncEnabled;

    /**
     * The last roster version, if available.
     */
    private String rosterVersion;

    /**
     * Retrieve the account user jid.
     * @return A jid matching username@domain.tld.
     */
    public String getJid() {
        return jid;
    }

    /**
     * Change the account user jid-
     * @param jid The new jid.
     */
    public void setJid(String jid) {
        this.jid = jid;
    }

    /**
     * Retrieve the account password.
     * @return The account password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the account password. This does not affect any server passwords.
     * @param password The account password to be used.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retrieve the connection string.
     * @return The connection string.
     */
    public String getConnection() {
        return connection;
    }

    /**
     * Change the connection string.
     * @param connection The new connection string.
     */
    public void setConnection(String connection) {
        this.connection = connection;
    }

    /**
     * Retrieve the preferred resource name.
     * @return The preferred resource name.
     */
    public String getResource() {
        return resource;
    }

    /**
     * Change the preferred resource name.
     * @param resource The new preferred resource name.
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Retrieve the sync roster and contacts integration state.
     * @return True if roster sync is enabled.
     */
    public boolean isRosterSyncEnabled() {
        return rosterSyncEnabled;
    }

    /**
     * Set the roster sync state (enabled/disabled).
     * @param rosterSyncEnabled The new roster sync state.
     */
    public void setRosterSyncEnabled(boolean rosterSyncEnabled) {
        this.rosterSyncEnabled = rosterSyncEnabled;
    }

    /**
     * Retrieve the latest roster version.
     * @return The last received roster version string.
     */
    public String getRosterVersion() {
        return rosterVersion;
    }

    /**
     * Set the last received roster version string.
     * @param rosterVersion The new roster version string.
     */
    public void setRosterVersion(String rosterVersion) {
        this.rosterVersion = rosterVersion;
    }

}
