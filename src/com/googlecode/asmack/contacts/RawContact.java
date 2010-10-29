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

package com.googlecode.asmack.contacts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * RawContact is an abstraction of the raw contacts table and the linked
 * metadata.
 */
public class RawContact {

    /**
     * The autoincrement id, or -1.
     */
    private long ID = -1l;

    /**
     * The account name, your local jid.
     */
    private String accountName;

    /**
     * The account type.
     */
    private String accountType = "com.googlecode.asmack";

    /**
     * The account source identifier, the remote jid.
     */
    private String sourceID;

    /**
     * Synchnronization metadata fields.
     */
    private final String sync[] = new String[4];

    /**
     * Hashmap of metadata mappings.
     */
    private final Map<String, Metadata> metadata = new HashMap<String, Metadata>(7);

    /**
     * Set a metadata field.
     * @param data The new metadata.
     */
    public void setMetadata(Metadata data) {
        String mimetype = data.getMimetype();
        Metadata other = metadata.get(mimetype);
        if (other != null && other.getID() != -1l) {
            data.setID(other.getID());
        }
        if (ID != -1) {
            data.setRawContactID(ID);
        }
        metadata.put(mimetype, data);
    }

    /**
     * Remove the metadata object for a given type.
     * @param mimetype The mime type to remove.
     */
    public void removeMetadata(String mimetype) {
        Metadata data = metadata.get(mimetype);
        if (data == null) {
            return;
        }
        if (data.getID() == -1l) {
            metadata.remove(mimetype);
        } else {
            metadata.put(mimetype, new DeletedMetadata(data.getID()));
        }
    }

    /**
     * Retrieve a readonly version of the metadata map.
     * @return 
     */
    public Map<String, Metadata> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Retrieve the id of this contact, or -1.
     * @return The contact id from the database, or -1.
     */
    public long getID() {
        return ID;
    }

    /**
     * <p>Change the contact id and all references in all metadata objects.</p>
     * <p><b>Warning:</b> Calling this method may or may not be safe. Use with
     * care.</p>
     * @param ID The new contact id.
     */
    public void setID(long ID) {
        this.ID = ID;
        for (Metadata data: metadata.values()) {
            data.setRawContactID(ID);
        }
    }

    /**
     * The account name associated with this contact, the local user jid.
     * @return The local user jid (you@yourserver.tld).
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Change the local account name.
     * @param accountName The new account name.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Retrieve the type of this account.
     * @return The type of this account.
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * Change the account type.
     * @param accountType The new account type.
     */
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    /**
     * Retrieve the current account source identifier, the remote jid.
     * @return The account source id, the remote jid.
     */
    public String getSourceID() {
        return sourceID;
    }

    /**
     * Change the account source identifier, the remote jid.
     * @param sourceID The new account source identifier, the new remote jid.
     */
    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

    /**
     * Retrieve the remote jid by reading SYNC1 <code>getSync(0);</code>.
     * @return The remote jid.
     */
    public String getJid() {
        return getSync(0);
    }

    /**
     * Change the remote jid, altering {@link RawContact#sourceID} and
     * SYNC1.
     * @param jid The remote jid.
     */
    public void setJid(String jid) {
        setSync(0, jid);
        setSourceID(jid);
    }

    /**
     * Set the sync identifier.
     * @param sync The3 sync identifier.
     */
    public void setSyncIndex(String sync) {
        setSync(1, sync);
    }

    /**
     * <p>Retrieve a SYNC field.</p>
     * <p><i>Note:</i> index starts at 0 not 1. This means that retrieving
     * SYNC1 means calling <code>getSync(0)</code>.</p>
     * @param index The index to retreive.
     * @return The value of SYNC&lt;index+1&gt;
     */
    public String getSync(int index) {
        return sync[index];
    }

    /**
     * <p>Change a SYNC field.</p>
     * <p><i>Note:</i> index starts at 0 not 1. This means that changing
     * SYNC1 means calling <code>setSync(0, value)</code>.</p>
     * @param index The sync field index.
     * @param value The new sync field value.
     */
    public void setSync(int index, String value) {
        sync[index] = value;
    }

}
