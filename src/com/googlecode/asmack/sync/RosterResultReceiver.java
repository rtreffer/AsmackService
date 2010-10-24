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
