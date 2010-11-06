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

import android.provider.BaseColumns;

/**
 * <p>Base class for Metadata contant. This data is usually stored in the
 * data table associated with a raw contact.</p>
 * <p>All columns are fixed, thus subclasses will provide a more natural API
 * instead of containing new fields.</p>
 */
public class Metadata {

    /**
     * The data entry id ({@link BaseColumns#_ID}.
     */
    protected long ID = -1l;

    /**
     * The referenced raw contact id.
     */
    protected long rawContactID = -1l;

    /**
     * The joined mimetype text.
     */
    protected String mimetype;

    /**
     * <p>The 14 generic data fields. It is up to the subclasses to provide
     * meaningfull get/set wrapper. DATA15 is stored as a blob, and may thus
     * be received via getBlob().</p>
     * <p>Please note that this array has a 0
     * offset instead of the 1 offset of the data table.</p>
     */
    protected final String data[] = new String[14];

    /**
     * <p>The 4 generic sync fields.</p>
     * <p>Please note that this array uses a 0 offset, wheras the data table
     * columns use a 1 offset.</p>
     */
    protected final String sync[] = new String[4];

    /**
     * <p>The DATA15 blob as a byte array.</p>
     */
    protected byte[] blob = null;

    /**
     * Retrieve the metadata id.
     * @return The metadata id, or -1.
     */
    public long getID() {
        return ID;
    }

    /**
     * Change the metadata id. This ID is used for updates.
     * @param ID The new ID.
     */
    public void setID(long ID) {
        this.ID = ID;
    }

    /**
     * Retrieve the crossreference ID 
     * @return The raw contact ID.
     */
    public long getRawContactID() {
        return rawContactID;
    }
    /**
     * Change the refenrenced contact id.
     * @param rawContactID The new id of the referenced contact.
     */
    public void setRawContactID(long rawContactID) {
        this.rawContactID = rawContactID;
    }

    /**
     * Retrieve the textual representation for this metadata mimetype.
     * @return The mimetype of this metadata.
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * Change the mimetype of this metadata. This method is usually blocked
     * in subclasses, as changing the mimetype would break their semantics.
     * @param mimetype The new mimetype.
     */
    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    /**
     * <p>Retrieve the value of a given data field.</p>
     * <p><i>Note:</i> index refers to the logical array index, starting with 0.
     * The data tables, on the other hand, start with 1. Thus reading the value
     * of DATA1 requires calling getData(0).</p>
     * @param index The data index.
     * @return The string representation of the data field.
     */
    public String getData(int index) {
        return data[index];
    }

    /**
     * <p>Change the value of a given data field.</p>
     * <p><i>Note:</i> index refers to the logical array index, starting with 0.
     * The data tables, on the other hand, start with 1. Thus changing the value
     * of DATA1 requires calling getData(0, "new value").</p>
     * @param index The data index.
     * @param value The new value.
     */
    public void setData(int index, String value) {
        data[index] = value;
    }

    /**
     * <p>Retrieve the value of a given sync column.</p>
     * <p><i>Note:</i> index refers to the logical array index, starting with 0.
     * The data cell columns start with 1. Thus reading SYNC1 requires a call to
     * getSync(0).</p>
     * @param index The sync column index, 0..3.
     * @return The current value of the sync field.
     */
    public String getSync(int index) {
        return sync[index];
    }

    /**
     * <p>Change the value of a given sync column.</p>
     * <p><i>Note:</i> index refers to the logical array index, starting with 0.
     * The data cell columns start with 1. Thus changing SYNC1 requires a call
     * to setSync(0, "new value").</p>
     * @param index The sync column index, 0..3.
     * @param value The new value.
     */
    public void setSync(int index, String value) {
        sync[index] = value;
    }

    /**
     * Retrieve the current blob field data (DATA15).
     * @return The current blob content.
     */
    public byte[] getBlob() {
        return blob;
    }

    /**
     * Set the new blob (DATA15) content for this metadata.
     * @param blob The new blob.
     */
    public void setBlob(byte[] blob) {
        this.blob = blob;
    }

}
