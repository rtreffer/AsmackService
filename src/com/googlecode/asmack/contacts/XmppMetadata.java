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

public class XmppMetadata extends Metadata {

    public final static String MIMETYPE =
        "vnd.android.cursor.item/vnd.xmpp.profile";

    public XmppMetadata() {
        mimetype = MIMETYPE;
        setSummary("Xmpp-Profil");
        setDetail("Show Profile");
    }

    public void setJid(String jid) {
        setDetail("Show " + jid);
        setData(0, jid);
    }

    public String getJid() {
        return getData(0);
    }

    public void setSummary(String summary) {
        setData(1, summary);
    }

    public String getSummary() {
        return getData(1);
    }

    public void setDetail(String detail) {
        setData(2, detail);
    }

    public String getDetail() {
        return getData(2);
    }
}
