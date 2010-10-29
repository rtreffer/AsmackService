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

import android.provider.ContactsContract.CommonDataKinds.Im;

/**
 * ImMetadata represents an instant messaging metadata. This metadata is used
 * to show the online/offline status in the contacts app, as well as additional
 * information in the contact detail view.
 */
public class ImMetadata extends Metadata {

    /**
     * The IM Metadata string. Equivalent to {@link Im#CONTENT_ITEM_TYPE} 
     */
    public static final String MIMETYPE = Im.CONTENT_ITEM_TYPE;

    /**
     * The contact type class for instant messaging metadata blocks.
     */
    public static enum Type {

        /**
         * Custom instant messaging type.
         */
        CUSTOM(Im.TYPE_CUSTOM),

        /**
         * Home contact.
         */
        HOME(Im.TYPE_HOME),

        /**
         * Work contact.
         */
        WORK(Im.TYPE_WORK),

        /**
         * Other/Unknown/Unspecified contact type.
         */
        OTHER(Im.TYPE_OTHER);

        /**
         * The internal type value, as stored in the database.
         */
        private final int value;

        /**
         * Create a new type, wrapping a the given raw value.
         * @param value The database value for this type.
         */
        Type(int value) {
            this.value = value;
        }

        /**
         * Retrieve a type instalce for a given database value, or null.
         * @param id The database value.
         * @return A corresponding Type instance, or null.
         */
        private static Type byTypeId(int id) {
            switch(id) {
            case Im.TYPE_CUSTOM: return CUSTOM;
            case Im.TYPE_HOME: return HOME;
            case Im.TYPE_OTHER: return OTHER;
            case Im.TYPE_WORK: return WORK;
            }
            return null;
        }

        /**
         * Retrieve the database value for this type.
         * @return The database value of this type.
         */
        public int getValue() {
            return value;
        }
    };

    /**
     * Protocol type for metadata blocks.
     */
    public static enum Protocol {

        /**
         * Custom instant messaging protocol.
         */
        CUSTOM(Im.PROTOCOL_CUSTOM),

        /**
         * AOL instant messaging protocol.
         */
        AIM(Im.PROTOCOL_AIM),

        /**
         * MSN instant messaging protocol.
         */
        MSN(Im.PROTOCOL_MSN),

        /**
         * Yahoo! instant messaging protocol.
         */
        YAHOO(Im.PROTOCOL_YAHOO),

        /**
         * Skype instant messaging protocol.
         */
        SKYPE(Im.PROTOCOL_SKYPE),

        /**
         * QQ instant messaging protocol.
         */
        QQ(Im.PROTOCOL_QQ),

        /**
         * Google Talk instant messaging protocol.
         */
        GOOGLE_TALK(Im.PROTOCOL_GOOGLE_TALK),

        /**
         * ICQ instant messaging protocol.
         */
        ICQ(Im.PROTOCOL_ICQ),

        /**
         * Jabber instant messaging protocol.
         */
        JABBER(Im.PROTOCOL_JABBER),

        /**
         * Netmeeting instant messaging protocol.
         */
        NETMEETING(Im.PROTOCOL_NETMEETING);

        /**
         * The internal type value, as stored in the database.
         */
        private final int value;

        /**
         * Create a new type, wrapping a the given raw value.
         * @param value The database value for this type.
         */
        Protocol(int value) {
            this.value = value;
        }

        /**
         * Retrieve the Protocol instance for a given raw database value.
         * @param id The raw database value.
         * @return The corresponding Protocol instance, or null.
         */
        public static Protocol byProtocolId(int id) {
            switch(id) {
            case Im.PROTOCOL_CUSTOM: return CUSTOM;
            case Im.PROTOCOL_AIM: return AIM;
            case Im.PROTOCOL_MSN: return MSN;
            case Im.PROTOCOL_YAHOO: return YAHOO;
            case Im.PROTOCOL_SKYPE: return SKYPE;
            case Im.PROTOCOL_QQ: return QQ;
            case Im.PROTOCOL_GOOGLE_TALK: return GOOGLE_TALK;
            case Im.PROTOCOL_ICQ: return ICQ;
            case Im.PROTOCOL_JABBER: return JABBER;
            case Im.PROTOCOL_NETMEETING: return NETMEETING;
            }
            return null;
        }

        /**
         * Retrieve the raw database value for this protocol type.
         * @return The raw database value.
         */
        public int getValue() {
            return value;
        }
    };

    /**
     * Create a new instant messaging metadata type.
     */
    public ImMetadata() {
        mimetype = MIMETYPE;
    }

    /**
     * Change the type of this instant messaging metadata.
     * @param type The new instant messaging type.
     */
    public void setType(Type type) {
        setData(1, Integer.toString(type.getValue()));
    }

    /**
     * Retrieve the instant messaging contact type.
     * @return The current instant messaging contact type.
     */
    public Type getType() {
        return Type.byTypeId(Integer.parseInt(getData(1)));
    }

    /**
     * Change the label of the custom instant messaging type.
     * @param label The label of the custom instant messaging type.
     */
    public void setCustomTypeLabel(String label) {
        setData(2, label);
    }

    /**
     * Retrieve the custom instant messaging type label.
     * @return The custom instant messaging type label.
     */
    public String getCustomTypeLabel() {
        return getData(2);
    }

    /**
     * Change the protocol type.
     * @param protocol The new protocol type.
     */
    public void setProtocol(Protocol protocol) {
        setData(4, Integer.toString(protocol.getValue()));
    }

    /**
     * Retrieve the current protocol type.
     * @return The current protocol type.
     */
    public Protocol getProtocol() {
        return Protocol.byProtocolId(Integer.parseInt(getData(4)));
    }

    /**
     * Change the label of the custom protocol type.
     * @param label The new custom protocol label.
     */
    public void setCustomProtocolLabel(String label) {
        setData(5, label);
    }

    /**
     * Retrieve the current custom protocol label.
     * @return The custom protocol label.
     */
    public String getCustomProtocolLabel() {
        return getData(5);
    }

    /**
     * Retrieve the account jid, your local jid. The account jid is stored on
     * SYNC2.
     * @return The account jid, your local jid.
     */
    public String getAccounttJid() {
        return getSync(1);
    }

    /**
     * Change the account jid, your local jid. The acocunt jid is stored on
     * SYNC2. This metod updates the combined remote/local string on SYNC1, too.
     * @param jid The new account jid, your local jid.
     */
    public void setAccountJid(String jid) {
        setSync(1, jid);
        setData(0, getJid() + "/" + jid);
    }

    /**
     * Retrieve the remote jid of this contact.
     * @return The remote jid.
     */
    public String getJid() {
        return getSync(2);
    }

    /**
     * Change the remote jid of the instant messaging contact.
     * @param jid The new remote jid.
     */
    public void setJid(String jid) {
        setSync(2, jid);
        setData(0, jid + "/" + getAccounttJid());
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     * @param mimetype Ignored.
     */
    @Override
    public void setMimetype(String mimetype) {
        throw new UnsupportedOperationException("Mimetype of Im is " + MIMETYPE);
    }

}
