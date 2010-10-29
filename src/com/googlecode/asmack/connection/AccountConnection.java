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

import android.util.Log;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.StanzaSink;
import com.googlecode.asmack.XmppAccount;
import com.googlecode.asmack.XmppException;

/**
 * A statemachine like link between an account and a connection.
 */
public class AccountConnection {

    // TODO RRDlike tracking of failures

    /*
     * We have a few states, and async tries to reach the connected state.
     *
     * This class helps to reduce the number of corner cases and problems.
     * Handle with care!
     */

    /**
     * The current connection state.
     */
    public enum State {
        /**
         * The connection is uninitialized.
         */
        Start,      // idle state, no preferences
        /**
         * The connection is trying to connect.
         */
        Connecting, // login / connect running
        /**
         * The connection was successfully established.
         */
        Connected,  // connect succeeded
        /**
         * The connection failed.
         */
        Failed      // connection failed
    }

    /**
     * Log tag for this class (class.getSimpleName()).
     */
    private static final String TAG = AccountConnection.class.getSimpleName();

    /**
     * The xmpp account details.
     */
    private XmppAccount account;

    /**
     * The current connection state.
     */
    private State currentState = State.Start;

    /**
     * Number of times this connection has reached the failed state without
     * connecting successfully.
     */
    private int failCount = 0;

    /**
     * The current login thread.
     */
    private LoginThread loginThread;

    /**
     * The current connection if available.
     */
    private Connection connection;

    /**
     * The last time this connection failed.
     */
    private long lastFailTime;

    /**
     * The target stanza sink.
     */
    private final StanzaSink stanzaSink;

    /**
     * Create a new Account/Connection pair with the given stanza sink.
     * @param stanzaSink The final stanza sink.
     */
    public AccountConnection(StanzaSink stanzaSink) {
        this.stanzaSink = stanzaSink;
    }

    /**
     * Retrieve the current xmpp account.
     * @return The underlying XMPP Account.
     */
    public XmppAccount getAccount() {
        return account;
    }

    /**
     * Set the underlying xmpp account.
     * @param account The new xmpp account.
     */
    public synchronized void setAccount(XmppAccount account) {
        transition(State.Start);
        this.account = account;
        failCount = 0;
        transition(State.Connecting);
    }

    /**
     * Retrieve the number of fails since the last successfull connect.
     * @return
     */
    public int getFailCount() {
        return failCount;
    }

    /**
     * Retrieve the current connection state.
     * @return The current connection state.
     */
    public synchronized State getCurrentState() {
        return currentState;
    }

    /**
     * Retrieve the current stanza sink.
     * @return The current stanza sink.
     */
    public StanzaSink getStanzaSink() {
        return stanzaSink;
    }

    /**
     * Trigger a state transition. Connected should be set via
     * connectionSuccess.
     * @param state The new state of the connection.
     */
    public synchronized void transition(State state) {
        if (state == currentState) {
            return;
        }
        Log.d(TAG, "State transition from " + currentState + " -> " + state + " on " + account.getJid());
        switch(state) {
        case Start:
            break;
        case Connecting:
            disconnect();
            loginThread = new LoginThread(this);
            loginThread.start();
            currentState = state;
            break;
        case Connected:
            if (getConnection() == null || getConnection().isClosed()) {
                throw new IllegalStateException("Can't set state 'Connected' without a connection");
            }
            currentState = state;
            break;
        case Failed:
            disconnect();
            lastFailTime = System.currentTimeMillis();
            failCount++;
            currentState = state;
            break;
        }
    }

    /**
     * Safely cancel the current connection attempt and close any connection.
     */
    public synchronized void disconnect() {
        if (loginThread != null) {
            loginThread.interrupt();
            loginThread = null;
        }
        Connection connection = getConnection();
        if (connection != null) {
            try {
                connection.close();
            } catch (XmppException e) {
                /* IGNORE */
            }
        }
    }

    /**
     * Terminate a connection attempt as failed, happily ignoring the attempt
     * if the connection isn't attempting to connect.
     * @param loginThread The original login thread.
     */
    public synchronized void connectionFail(LoginThread loginThread) {
        // Hint: If you think that you can't get a failed attempt while being
        //       out of the Connection state: please think again.
        //       Think about timing.
        //       And finally: think again :-)
        if (loginThread != this.loginThread ||
            currentState != State.Connecting) {
            return;
        }
        transition(State.Failed);
    }

    /**
     * Mark the connection as connected, unless the connection is connected.
     * @param loginThread The initial login thread.
     * @param connection The new connection.
     */
    public synchronized void connectionSuccess(
        LoginThread loginThread,
        Connection connection
    ) {
        Connection oldConnection = null;
        if (currentState == State.Connected) {
            // we will likely kill this connection anyway
            oldConnection = this.connection;;
        }

        // Try to send an initial stanza
        Stanza stanza = new Stanza("presence", "", null, "<presence />", null);

        try {
            connection.send(stanza);
        } catch (XmppException e) {

            // Initial stanza failed
            try {
                connection.close();
            } catch (XmppException e1) {
                /* IGNORE */
            }
            if (oldConnection == null || oldConnection.isClosed()) {
                // Only fail if the old connection is invalid
                transition(State.Failed);
            }
            return;

        }

        // Attempt succeeded, cleanup

        if (loginThread != null && loginThread != this.loginThread) {
            // concurrent login attempt
            loginThread.interrupt();
        }
        this.loginThread = null;

        if (oldConnection != null) {
            try {
                oldConnection.close();
            } catch (XmppException e) {
                /* IGNORE */
            }
        }

        this.connection = connection;
        transition(State.Connected);

        failCount = 0;
    }

    /**
     * Retrieve the timestamp of the last connection failure.
     * @return The timestamp of the last connection failure.
     */
    public long getLastFailTime() {
        return lastFailTime;
    }

    /**
     * Retrieve the current connection.
     * @return The current connection.
     */
    public synchronized Connection getConnection() {
        return connection;
    }

}
