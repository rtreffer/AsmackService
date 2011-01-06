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

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import com.googlecode.asmack.Attribute;
import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.StanzaSink;
import com.googlecode.asmack.XMPPUtils;
import com.googlecode.asmack.XmppAccount;
import com.googlecode.asmack.XmppException;
import com.googlecode.asmack.XmppIdentity;
import com.googlecode.asmack.connection.AccountConnection.State;
import com.googlecode.asmack.contacts.ContactDataMapper;
import com.googlecode.asmack.contacts.PresenceBroadcastReceiver;
import com.googlecode.asmack.disco.Database;
import com.googlecode.asmack.disco.DiscoReceiver;
import com.googlecode.asmack.util.LRUCache;

/**
 * The core xmpp service, responsible for connection tracking, keepalive and
 * general stanza send/receive dispatching.
 */
public class XmppTransportService
    extends Service
    implements OnAccountsUpdateListener,
               StanzaSink,
               ConnectionStateChangeListener
{

    /**
     * Reconnect timing.
     */
    private static final long RECONNECT_TIMES[] = new long[]{
         0*60*1000, //    0 fails
         0*60*1000, //    1 fail
         1*60*1000, //    2 fails
         3*60*1000, //    3 fails
         5*60*1000, //    4 fails
        10*60*1000, //    5 fails
        15*60*1000, //    6 fails
        20*60*1000, //    7 fails
        30*60*1000, //    8 fails
        40*60*1000, //    9 fails
        50*60*1000, //   10 fails
        60*60*1000  // > 10 fails
    };

    /**
     * Cache of verification strings for presence.
     */
    private static final LRUCache<String, String> JID_VERIFICATION_CACHE =
                     new LRUCache<String, String>(100);

    /**
     * The stanza receive intent/right.
     */
    public static final String XMPP_STANZA_INTENT =
                             "com.googlecode.asmack.intent.XMPP.STANZA.RECEIVE";

    /**
     * The stanza send intent/right.
     */
    public static final String XMPP_STANZA_SEND_INTENT =
                            "com.googlecode.asmack.intent.XMPP.STANZA.SEND";

    /**
     * Logging tag for this class (class.getSimpleName()).
     */
    private static final String TAG = XmppTransportService.class.getSimpleName();

    /**
     * Random service id for resource name generation.
     */
    private static final String ID = Integer.toHexString((int)(255.999 * Math.random())).toLowerCase();

    /**
     * Executor for background presence and keepalive.
     */
    private static final Executor pingExecutor;

    /**
     * Intent name for xmpp connection state changes.
     */
    public static final String XMPP_STATE_INTENT =
                                    "com.googlecode.asmack.intent.XMPP.STATE";

    /**
     * Pingcount for background ping ids.
     */
    private static int pingCount;

    static {
        // Note: I hate static blocks
        int threads = Runtime.getRuntime().availableProcessors() * 3;
        pingExecutor = Executors.newFixedThreadPool(threads);
    }

    /**
     * Map of {{bare jid} => {AccountConnection}} pairs.
     */
    private HashMap<String, AccountConnection> connections = new HashMap<String, AccountConnection>();

    /**
     * Binder for remote connection access.
     */
    private final IXmppTransportService.Stub binder =
        new IXmppTransportService.Stub() {

            /**
             * Run a login try, without interfering with the real core.
             * @param jid The user jid.
             * @param password The user password.
             * @return True on success.
             */
            @Override
            public boolean tryLogin(String jid, String password) throws RemoteException {
                XmppAccount account = new XmppAccount();
                account.setJid(jid);
                account.setPassword(password);
                account.setConnection("xmpp:" + XMPPUtils.getDomain(account.getJid()));
                account.setResource("asmack-testlogin-" + ID);
                Connection connection =
                    ConnectionFactory.createConnection(account);
                try {
                    connection.connect(new StanzaSink() {
                        @Override
                        public void receive(Stanza stanza) {}

                        @Override
                        public void connectionFailed(Connection connection,
                                XmppException exception) {
                        }

                    });
                    connection.close();
                    return true;
                } catch (XmppException e) {
                    return false;
                }
            }

            /**
             * Send a single stanza via an appropriate connection.
             * @param stanza The stanza to send.
             */
            @Override
            public boolean send(Stanza stanza) throws RemoteException {
                return XmppTransportService.this.send(stanza);
            }

            /**
             * Send a single stanza through all connections, altering from
             * to be the resource address.
             * @param stanza The stanza to send.
             */
            @Override
            public void sendFromAllResources(Stanza stanza)
                throws RemoteException
            {
                XmppTransportService.this.sendFromAllResources(stanza);
            }

            /**
             * Send a single stanza through all connections, altering from
             * to be the account address.
             * @param stanza The stanza to send.
             */
            @Override
            public void sendFromAllAccounts(Stanza stanza)
                throws RemoteException
            {
                XmppTransportService.this.sendFromAllAccounts(stanza);
            }

            /**
             * Retrieve the full resource jid by bare jid.
             * @param bare The bare user jid.
             * @return The full resource jid.
             */
            @Override
            public String getFullJidByBare(String bare) throws RemoteException {
                return XmppTransportService.this.getFullJidByBare(bare);
            }

            /**
             * Enable a new feature for a given jid. A new presence will be
             * send with the next tick (max. 60s).
             * @param jid The jid to enhance
             * @param feature The new feature.
             */
            @Override
            public void enableFeatureForJid(String jid, String feature)
                    throws RemoteException {
                Database.enableFeature(
                    getApplicationContext(),
                    jid,
                    feature,
                    null
                );
                JID_VERIFICATION_CACHE.remove(jid);
            }

            /**
             * Enable a feature for all xmpp connections. New features will
             * be announced with the next time tick.
             * @param feature The feature to be announced.
             */
            @Override
            public void enableFeature(String feature) throws RemoteException {
                Database.enableFeature(
                    getApplicationContext(),
                    feature,
                    null
                );
                JID_VERIFICATION_CACHE.clear();
            }

            /**
             * Add an identity to a given xmpp connection. The identity will
             * be announced with the next time tick.
             * @param jid The user jid.
             * @param identity The new xmpp identity.
             */
            @Override
            public void addIdentityForJid(String jid, XmppIdentity identity)
                    throws RemoteException {
                Database.addIdentity(
                    getApplicationContext(),
                    jid,
                    identity,
                    null
                );
                JID_VERIFICATION_CACHE.remove(jid);
            }

            /**
             * Add a new identity to all xmpp accounts. The identity will be
             * announced during the next time tick.
             * @param identity The new identity.
             */
            @Override
            public void addIdentity(XmppIdentity identity)
                    throws RemoteException {
                Database.addIdentity(
                    getApplicationContext(),
                    identity,
                    null
                );
                JID_VERIFICATION_CACHE.clear();
            }

        };

    /**
     * Retrieve an IBinder interface for interaction with the xmpp service.
     * @param The binding intent.
     * @return The IBinder instance.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * The android account manager.
     */
    private AccountManager accountManager;

    /**
     * Initialize the xmpp service, binding all required receivers.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        accountManager = AccountManager.get(this);

        ContentProviderClient provider = getContentResolver()
                .acquireContentProviderClient(ContactsContract.AUTHORITY_URI);
        ContactDataMapper mapper = new ContactDataMapper(provider);

        BroadcastReceiver receiver = new PresenceBroadcastReceiver(mapper);
        registerReceiver(receiver, new IntentFilter(XmppTransportService.XMPP_STANZA_INTENT));

        receiver = new DiscoReceiver();
        registerReceiver(receiver, new IntentFilter(XmppTransportService.XMPP_STANZA_INTENT));

        receiver = new KeepaliveActionIntentReceiver(this);;
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        receiver = new SendStanzaReceiver(this);
        registerReceiver(receiver, new IntentFilter(XMPP_STANZA_SEND_INTENT));

        accountManager.addOnAccountsUpdatedListener(this, null, true);
    }

    /**
     * Start the background sync of the contacts.
     * @param accounts The new accountlist.
     */
    @Override
    public synchronized void onAccountsUpdated(Account[] accounts) {
        HashMap<String, AccountConnection> connectionStates =
            new HashMap<String, AccountConnection>();
        for (Account account: accounts) {
            if (!"com.googlecode.asmack".equals(account.type)) {
                continue;
            }
            String username = account.name;
            AccountConnection state = connections.get(username);
            if (state == null) {
                state = new AccountConnection(this, this);
            }
            String password = accountManager.getPassword(account);
            XmppAccount xmppAccount = new XmppAccount();
            xmppAccount.setJid(username);
            xmppAccount.setPassword(password);
            xmppAccount.setConnection("xmpp:" + XMPPUtils.getDomain(xmppAccount.getJid()));
            xmppAccount.setResource("asmack" + ID);
            state.setAccount(xmppAccount);
            connectionStates.put(username, state);
        }
        connections = connectionStates;
    }

    /**
     * Send a stanza via the first matching connection.
     * @param stanza The stanza to send.
     * @return True on success.
     */
    public boolean send(Stanza stanza) {
        Log.d(TAG, "Sending stanza " + stanza.getName() + " via " + stanza.getVia());
        String via = stanza.getVia();
        if (via == null) {
            Log.w(TAG, "Sending stanza without via");
            return false;
        }
        if ("iq".equals(stanza.getName())) {
            Attribute id = stanza.getAttribute("id");
            if (id == null) {
                Log.w(TAG, "Sending iq without id");
                return false;
            }
        }
        Connection connection = getConnectionForJid(via);
        if (connection == null) {
            Log.w(TAG, "No connection for " + via);
            return false;
        }
        try {
            connection.send(stanza);
            return true;
        } catch (XmppException e) {
            Log.e(TAG, "Connection failed, dropping...", e);
            try {
                connection.close();
            } catch (XmppException e1) {
                Log.d(TAG, "Closing a broken connection failed.", e);
            }
        }
        Log.e(TAG, "No stream for " + via);
        return false;
    }

    /**
     * Send a stanza via this service, through all account jids.
     * @param stanza The stanza to send.
     */
    public void sendFromAllAccounts(Stanza stanza) {
        Log.d(TAG, "Sending stanza " + stanza.getName() + " via *");
        for (AccountConnection state: connections.values()) {
            if (state.getCurrentState() != State.Connected) {
                continue;
            }
            stanza.addAttribute(
                new Attribute("from", "", state.getAccount().getJid())
            );
            try {
                state.getConnection().send(stanza);
            } catch (XmppException e) {
                Log.w(TAG, "Problem sending staza " + stanza.getName(), e);
            }
        }
    }

    /**
     * Send a stanza via this service, through all resource jids.
     * @param stanza The stanza to send.
     */
    public void sendFromAllResources(Stanza stanza) {
        Log.d(TAG, "Sending stanza " + stanza.getName() + " via *");
        for (AccountConnection state: connections.values()) {
            if (state.getCurrentState() != State.Connected) {
                continue;
            }
            Connection connection = state.getConnection();
            stanza.addAttribute(
                new Attribute("from", "", connection.getResourceJid())
            );
            try {
                state.getConnection().send(stanza);
            } catch (XmppException e) {
                Log.w(TAG, "Problem sending staza " + stanza.getName(), e);
            }
        }
    }

    /**
     * Retrieve the full resource based on the bare jid.
     * @param bare The bare jid (username@domain.tld).
     * @return The full resource jid (username@domain.tld/resource).
     */
    public String getFullJidByBare(String bare) {
        Connection connection = getConnectionForJid(bare);
        if (connection == null) {
            return null;
        }
        return connection.getResourceJid();
    }

    /**
     * Retrieve the active (connected) connection matching the given jid.
     * @param jid The jid to find.
     * @return The connection object matching the jid-
     */
    private Connection getConnectionForJid(String jid) {
        for (AccountConnection state: connections.values()) {
            if (state.getCurrentState() != State.Connected) {
                continue;
            }
            Connection connection = state.getConnection();
            String resourceJid = connection.getResourceJid();
            String bareJid = XMPPUtils.getBareJid(resourceJid);
            if (jid.equals(resourceJid) ||
                jid.equals(bareJid)
            ) {
                return connection;
            }
        }
        return null;
    }

    /**
     * Callback on received stanzas.
     * @param stanza The received stanza.
     */
    @Override
    public void receive(Stanza stanza) {
        Intent intent = new Intent();
        intent.setAction(XMPP_STANZA_INTENT);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        intent.putExtra("stanza", stanza);
        sendBroadcast(intent, XMPP_STANZA_INTENT);
    }

    /**
     * Helper method to trigger a service start.
     * @param context The context used to fire the start service intent.
     */
    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setAction(IXmppTransportService.class.getCanonicalName());
        context.startService(intent);
    }

    /**
     * Called on service kills.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "XMPP Service destroy?!?");
        super.onDestroy();
    }

    /**
     * Run a background ping on all idle connections.
     */
    public void ping() {
        pingCount++;
        long now = System.currentTimeMillis();
        for (AccountConnection state: connections.values()) {
            Connection connection = state.getConnection();
            if (connection == null) {
                continue;
            }
            if (state.getCurrentState() == State.Failed) {
                long reconnectTime = RECONNECT_TIMES[
                         Math.min(state.getFailCount(), RECONNECT_TIMES.length)
                ];
                if (now - connection.lastReceive() > reconnectTime) {
                    Log.d(TAG, "Reconnect on " + connection.getResourceJid());
                    if (state.getFailCount() > 0) {
                        // try to change the resource
                        state.getAccount().setResource(
                            "asmack" +
                            Integer.toHexString((int)(255.999 * Math.random()))
                                   .toLowerCase()
                        );
                    }
                    state.transition(State.Connecting);
                }
                continue;
            }
            if (state.getCurrentState() != State.Connected) {
                continue;
            }
            String jid = connection.getAccount().getJid();
            String verificationHash = JID_VERIFICATION_CACHE.get(jid);
            if (verificationHash == null) {
                verificationHash = Database.computeVerificationHash(
                    getApplicationContext(),
                    jid,
                    null
                );
                JID_VERIFICATION_CACHE.put(jid, verificationHash);
                pingExecutor.execute(new PresenceRunnable(connection,
                                     verificationHash));
            } else
            if (pingCount % 60 == 0) {
                pingExecutor.execute(new PresenceRunnable(connection,
                                     verificationHash));
            }
            if (now - connection.lastReceive() > 180000) {
                Log.d(TAG, "Fail on " + connection.getResourceJid());
                state.transition(State.Failed);
                continue;
            }
            if (now - connection.lastReceive() > 60000) {
                Log.d(TAG, "Keepalive on " + connection.getResourceJid());
                pingExecutor.execute(new PingRunnable(connection));
            }
        }
    }

    /**
     * Callback for failed connections. Triggers a state change to failed.
     * @param connection The failed connection.
     * @param exception The exception causing the connection failure.
     */
    @Override
    public synchronized void connectionFailed(
            Connection connection,
            XmppException exception) {
        AccountConnection state = 
            connections.get(connection.getAccount().getJid());
        if (state == null) {
            return;
        }
        if (state.getCurrentState() != State.Connected) {
            return;
        }
        state.transition(State.Failed);
    }

    /**
     * Called whenever a connection state is reseted. Fires a broadcast intent.
     * @param accountConnection The connection state that was reseted.
     */
    @Override
    public void onConnectionStart(AccountConnection accountConnection) {
        Intent intent = new Intent();
        intent.setAction(XMPP_STATE_INTENT);
        intent.putExtra("account", accountConnection.getAccount().getJid());
        intent.putExtra("state", "start");
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        sendBroadcast(intent, XMPP_STANZA_INTENT);
    }

    /**
     * Called whenever a connection starts connecting. Fires a broadcast
     * intent.
     * @param accountConnection The connection state that switched to
     *                          connecting.
     */
    @Override
    public void onConnectionConnecting(AccountConnection accountConnection) {
        Intent intent = new Intent();
        intent.setAction(XMPP_STATE_INTENT);
        intent.putExtra("account", accountConnection.getAccount().getJid());
        intent.putExtra("state", "connecting");
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        sendBroadcast(intent, XMPP_STANZA_INTENT);
    }

    /**
     * Called whenever a connection is fully established, including feature
     * negotiation. Fires a broadcast intent.
     * @param accountConnection The connection status that switched to
     *                          connected.
     */
    @Override
    public void onConnectionConnected(AccountConnection accountConnection) {
        Intent intent = new Intent();
        intent.setAction(XMPP_STATE_INTENT);
        intent.putExtra("account", accountConnection.getAccount().getJid());
        intent.putExtra("state", "connected");
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        sendBroadcast(intent, XMPP_STANZA_INTENT);
        JID_VERIFICATION_CACHE.remove(accountConnection.getAccount().getJid());
    }

    /**
     * Called whenever a connection faile, either due to high latency or due
     * to i/o errors. Fires a broadcast intent.
     * @param accountConnection The connection status that switched to failed.
     */
    @Override
    public void onConnectionFailed(AccountConnection accountConnection) {
        Intent intent = new Intent();
        intent.setAction(XMPP_STATE_INTENT);
        intent.putExtra("account", accountConnection.getAccount().getJid());
        intent.putExtra("state", "failed");
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        sendBroadcast(intent, XMPP_STANZA_INTENT);
    }

}
