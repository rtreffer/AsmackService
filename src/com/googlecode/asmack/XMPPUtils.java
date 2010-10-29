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
 * XMPPUtils provide helper for xmpp specific functionality like jid splitting.
 */
public class XMPPUtils {

    /**
     * Get the bare jid (username@domain.tld) out of a resource jid
     * (username@domain.tld/resource).
     * @param resourceJid The resource jid.
     * @return A bare jid representation of the jid.
     */
    public final static String getBareJid(String resourceJid) {
        int index = resourceJid.indexOf('/');
        if (index == -1) {
            return resourceJid;
        }
        return resourceJid.substring(0, index);
    }

    /**
     * Retrieve the domain out of a full resource jid or bare jid.
     * @param jid The full or bare jid.
     * @return The contained domain.
     */
    public static String getDomain(String jid) {
        String bareJid = getBareJid(jid);
        return bareJid.substring(bareJid.indexOf('@') + 1);
    }

    /**
     * Retrieve the username of a full or bare jid.
     * @param jid The full or bare jud.
     * @return The username part of the jid.
     */
    public static String getUser(String jid) {
        String bareJid = getBareJid(jid);
        int index = bareJid.indexOf('@');
        if (index <= 0) {
            return null;
        }
        return jid.substring(0, index);
    }

}
