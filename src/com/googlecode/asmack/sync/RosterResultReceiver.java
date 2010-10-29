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

package com.googlecode.asmack.sync;

import java.util.concurrent.ArrayBlockingQueue;

import org.w3c.dom.Node;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XMLUtils;
import com.googlecode.asmack.XmppMalformedException;

/**
 * Listen for roster broadcast and return them via a queue.
 */
final class RosterResultReceiver extends BroadcastReceiver {

    /**
     * The logging tag, <code>Receiver.class.getSimpleName()</code>.
     */
    private static final String TAG = RosterResultReceiver.class.getSimpleName();

    /**
     * The result queue, used to return a roster result.
     */
    private ArrayBlockingQueue<Node> rosterQueue;

    /**
     * The account to listen for, drop roster results on other accounts.
     */
    private Account account;

    /**
     * Create a new RosterResultReceiver, listening for roster results on
     * account and writing the result to the roster queue.
     * @param account The user account.
     * @param rosterQueue The result queue.
     */
    RosterResultReceiver(Account account, ArrayBlockingQueue<Node> rosterQueue) {
        this.rosterQueue = rosterQueue;
        this.account = account;
    }

    /**
     * Receive a stanza intent, check for roster entries and write results to
     * the result queue.
     */
    public void onReceive(Context context, Intent intent) {
        Stanza stanza = intent.getParcelableExtra("stanza");
        if (stanza.getName() == null || stanza.getVia() == null ||
            stanza.getAttribute("type") == null) {
            return;
        }
        if (!"iq".equals(stanza.getName())) {
            return;
        }
        if (!stanza.getVia().startsWith(account.name + "/")) {
            return;
        }
        if (!"result".equals(stanza.getAttribute("type").getValue())) {
            return;
        }
        try {
            Node node = stanza.getDocumentNode();
            Node roster = XMLUtils.getFirstChild(node, "jabber:iq:roster", "query");
            if (roster == null) {
                return;
            }
            rosterQueue.put(roster);
        } catch (XmppMalformedException e) {
            Log.w(TAG, "PLEASE REPORT", e);
        } catch (InterruptedException e) {
            Log.w(TAG, "PLEASE REPORT", e);
        }
    }

}
