package com.googlecode.asmack.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.googlecode.asmack.Attribute;
import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.connection.IXmppTransportService;
import com.googlecode.asmack.connection.XmppTransportService;
import com.googlecode.asmack.contacts.ContactDataMapper;
import com.googlecode.asmack.contacts.ImMetadata;
import com.googlecode.asmack.contacts.NicknameMetadata;
import com.googlecode.asmack.contacts.RawContact;
import com.googlecode.asmack.contacts.XmppMetadata;

/**
 * A sync adapter to fetch and compare the roster and the phone contacts.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    /**
     * The logging tag, SyncAdapter.
     */
    private static final String TAG = SyncAdapter.class.getSimpleName();

    /**
     * The application context used during sync.
     */
    private final Context applicationContext;

    /**
     * The xmpp service binding.
     */
    private IXmppTransportService service;

    /**
     * The service connection of the xmpp bind.
     */
    private ServiceConnection serviceConnection;

    /**
     * The account manager.
     */
    private AccountManager accountManager;

    /**
     * Create a new sync adapter based on a given application context.
     * @param applicationContext The application context.
     */
    public SyncAdapter(Context applicationContext) {
        super(applicationContext, true);
        this.applicationContext = applicationContext;
        accountManager = AccountManager.get(applicationContext);
    }

    /**
     * Perform a roster sync on a given account and a given content provider.
     * @param account The xmpp account to be synced.
     * @param extras SyncAdapter-specific parameters
     * @param authority The authority of this sync request.
     * @param provider A authority based ContentProvider for this sync.
     * @param syncResult Sync error and result counters.
     */
    @Override
    public void onPerformSync(
        final Account account,
        Bundle extras,
        String authority,
        ContentProviderClient provider,
        SyncResult syncResult
    ) {
        Log.d(TAG, "Start Roster Sync");
        final ArrayBlockingQueue<Node> rosterQueue = new ArrayBlockingQueue<Node>(1);
        BroadcastReceiver receiver = new RosterResultReceiver(account, rosterQueue);
        applicationContext.registerReceiver(receiver, new IntentFilter(XmppTransportService.XMPP_STANZA_INTENT));
        try {
            bindService();
            if (!waitForService()) {
                return;
            }
            if (!waitForServiceBind(account.name)) {
                return;
            }
            Stanza stanza = getRosterRequest(account);
            if (!sendWithRetry(stanza)) {
                syncResult.stats.numIoExceptions++;
                return;
            }
            Node roster = rosterQueue.poll(300, TimeUnit.SECONDS);
            if (roster == null) {
                return;
            }
            handleRosterResult(account, roster, provider);
        } catch (InterruptedException e) {
            Log.e(TAG, "Sync interrupted", e);
        } finally {
            applicationContext.unregisterReceiver(receiver);
            unbindService();
        }
    }

    /**
     * Retrieve and handle a roster result.
     * @param account The xmpp account.
     * @param roster The user roster xml node.
     * @param provider The content provider used to store the results.
     */
    private void handleRosterResult(
        Account account,
        Node roster,
        ContentProviderClient provider
    ) {
        long syncCount = getAndIncrementSyncCount(account);
        NodeList rosterItems = roster.getChildNodes();

        ContactDataMapper mapper = new ContactDataMapper(provider);

        ArrayList<ContentProviderOperation> operations =
                        new ArrayList<ContentProviderOperation>(60);

        HashMap<String, RawContact> oldContacts = new HashMap<String, RawContact>();
        for (RawContact contact: mapper.getRawContacts(account.name, true)) {
            oldContacts.put(contact.getJid(), contact);
        }

        for (int i = 0; i < rosterItems.getLength(); i++) {
            Node item = rosterItems.item(i);
            if (!"item".equals(item.getLocalName())) {
                continue;
            }
            if (!"both".equals(item.getAttributes().getNamedItem("subscription").getTextContent())) {
                continue;
            }
            String jid = item.getAttributes().getNamedItem("jid").getTextContent();
            String name = null;
            Node nameAttribute = item.getAttributes().getNamedItem("name");
            if (nameAttribute != null) {
                name = nameAttribute.getTextContent();
            }
            if (TextUtils.isEmpty(name)) {
                name = jid;
            }

            RawContact contact = oldContacts.remove(jid);
            if (contact == null) {
                contact = new RawContact();
                contact.setAccountName(account.name);
                contact.setJid(jid);
                contact.setSyncIndex(Long.toString(syncCount));
            }
            XmppMetadata xmpp = new XmppMetadata();
            xmpp.setJid(jid);
            contact.setMetadata(xmpp);
            if (!TextUtils.isEmpty(name)) {
                NicknameMetadata nick = new NicknameMetadata();
                nick.setNickname(name);
                contact.setMetadata(nick);
            } else {
                contact.removeMetadata(NicknameMetadata.MIMETYPE);
            }
            ImMetadata im = new ImMetadata();
            im.setType(ImMetadata.Type.OTHER);
            im.setAccountJid(account.name);
            im.setJid(jid);
            im.setProtocol(ImMetadata.Protocol.JABBER);
            contact.setMetadata(im);
            mapper.persist(contact, operations);
            Log.d(TAG, "Persisted " + jid);
            if (operations.size() > 100) {
                mapper.perform(operations);
                operations.clear();
            }
        }
        if (operations.size() > 0) {
            mapper.perform(operations);
        }

    }

    /**
     * Create a stanza to retrieve the roster of a xmpp account.
     * @param account The xmpp account.
     * @return A roster iq stanza.
     */
    private Stanza getRosterRequest(final Account account) {
        long syncCount = getAndIncrementSyncCount(account);
        List<Attribute> attributes = new ArrayList<Attribute>(3);
        attributes.add(new Attribute("type", null, "get"));
        attributes.add(new Attribute("id", null, "rostersync-" + Long.toHexString(syncCount)));
        Stanza stanza = new Stanza("iq", "", account.name,
            "<iq><query xmlns='jabber:iq:roster'/></iq>", attributes);
        String fullJid = null;
        try {
            fullJid = service.getFullJidByBare(account.name);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
        stanza.addAttribute(new Attribute("from", null, fullJid));
        return stanza;
    }

    /**
     * <p>Try to send a stanza, up to 2 times.</p>
     * <p><i>Note:</i> The guarantees of "send" are quite weak. The best
     * description is this: success means that the stanza has been written
     * to the network buffer of the gsm modem.</p>
     * @param stanza The stanza to send.
     * @return True on success, false otherwise.
     */
    private boolean sendWithRetry(Stanza stanza) {
        try {
            if (!service.send(stanza)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    /* non-critical */
                }
                if (!service.send(stanza)) {
                    return false;
                }
            }
        } catch (RemoteException e) {
            return false;
        }
        return true;
    }

    /**
     * Wait for the service binding to appear.
     * @return True on success.
     */
    private boolean waitForService() {
        for (int i = 0; i < 100 && service == null; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                /* non-critical */
            }
        }
        return service != null;
    }

    /**
     * Wait for a bind of a bare jid.
     * @param bare The bare jid that should be bound.
     * @return True on success.
     */
    private boolean waitForServiceBind(String bare) {
        try {
            for (int i = 0; i < 100 && service.getFullJidByBare(bare) == null; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    /* non-critical */
                }
            }
            return service.getFullJidByBare(bare) != null;
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Get and increment the atomic global sync counter. Used to identify
     * different sync runs.
     * @param account The acccount to use as a basis.
     * @return The old sync count.
     */
    private synchronized long getAndIncrementSyncCount(final Account account) {
        long syncCount = 0;
        String syncCountText = accountManager.getUserData(account, "SYNC_COUNT");
        if (syncCountText != null) {
            syncCount = Long.parseLong(syncCountText);
        }
        accountManager.setUserData(account, "SYNC_COUNT", Long.toString(syncCount + 1l));
        return syncCount;
    }

    /**
     * Unbind the xmpp transport service, freeing all resources.
     */
    private final synchronized void unbindService() {
        if (serviceConnection != null) {
            try {
                applicationContext.unbindService(serviceConnection);
            } catch (Exception e) { /* ignore */ }
            service = null;
            serviceConnection = null;
        }
    }

    /**
     * Bind to the xmpp transport service.
     */
    private final synchronized void bindService() {
        if (serviceConnection == null) {
            XmppTransportService.start(applicationContext);
            serviceConnection = new ServiceConnection() {

                public void onServiceDisconnected(ComponentName name) {
                }

                public void onServiceConnected(ComponentName name, IBinder binder) {
                    service = IXmppTransportService.Stub.asInterface(binder);
                }

            };
        }
        applicationContext.bindService(
                new Intent(IXmppTransportService.class.getName()),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

}
