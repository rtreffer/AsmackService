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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.harmony.javax.security.auth.callback.NameCallback;
import org.apache.harmony.javax.security.sasl.RealmCallback;

import android.util.Log;

import com.googlecode.asmack.XMPPUtils;
import com.googlecode.asmack.XmppAccount;

/**
 * Sasl callback handler to answer realm/password/username queries based
 * on the xmpp user account.
 */
public class AccountCallbackHander implements CallbackHandler {

    /**
     * The xmpp user account used for replies.
     */
    private final XmppAccount account;

    /**
     * Create a new handler that answers callbacks with the data from a given
     * user account.
     * @param account The user account used for replies.
     */
    public AccountCallbackHander(XmppAccount account) {
        this.account = account;
    }

    /**
     * Answer callbacks of the type
     * <ul>
     *   <li>{@link NameCallback} with the user account jid</li>
     *   <li>{@link PasswordCallback} with the user password</li>
     *   <li>{@link RealmCallback} with the user account domain</li>
     * </ul>
     * @param callback A set of callbacks to be answered.
     * @throws IOException Never.
     * @throws UnsupportedCallbackException Whenever an unhandled callback is
     *                                      received. Note that this may be
     *                                      thrown after some callbacks were
     *                                      handled.
     */
    @Override
    public void handle(Callback[] callback) throws IOException,
            UnsupportedCallbackException {

        for (Callback cb: callback) {
            if (cb instanceof NameCallback) {
                NameCallback nameCallback = (NameCallback) cb;
                nameCallback.setName(XMPPUtils.getUser(account.getJid()));
                continue;
            }
            if (cb instanceof PasswordCallback) {
                PasswordCallback passwordCallback = (PasswordCallback) cb;
                passwordCallback.setPassword(
                    account.getPassword().toCharArray()
                );
                continue;
            }
            if (cb instanceof RealmCallback) {
                RealmCallback realmCallback = (RealmCallback) cb;
                realmCallback.setText(XMPPUtils.getDomain(account.getJid()));
                continue;
            }
            Log.d("CALLBACK", "" + cb);
            throw new UnsupportedCallbackException(cb);
        }
    }

}
