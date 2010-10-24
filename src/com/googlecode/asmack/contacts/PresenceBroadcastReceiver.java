package com.googlecode.asmack.contacts;

import org.w3c.dom.Node;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XMLUtils;
import com.googlecode.asmack.XMPPUtils;
import com.googlecode.asmack.XmppMalformedException;
import com.googlecode.asmack.contacts.StatusUpdate.Presence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Stanza Broadcast Receiver listening for xmpp &lt;presence/&gt; updates.
 */
public class PresenceBroadcastReceiver extends BroadcastReceiver {

    /**
     * The data mapper used to load/save contacts and metadata.
     */
    private final ContactDataMapper mapper;

    /**
     * Create a new presence broadcast receiver with a given data mapper
     * backend.
     * @param mapper The data mapper backend of this broadcast receiver.
     */
    public PresenceBroadcastReceiver(ContactDataMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Receive a single stanza intent, check for xmpp &lt;presence/&gt; and
     * store it to the database.
     * @param Context The current application context.
     * @param intent The event Intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Stanza stanza = intent.getParcelableExtra("stanza");
        if (!"presence".equals(stanza.getName())) {
            return;
        }
        String accountJid = XMPPUtils.getBareJid(stanza.getVia());
        String jid = XMPPUtils.getBareJid(stanza.getAttribute("from").getValue());
        StatusUpdate update = null;
        if (stanza.getAttribute("type") != null) {
            if ("unavailable".equals(stanza.getAttribute("type").getValue())) {
                update = mapper.getStatusUpdate(accountJid, jid);
                update.setPresence(Presence.OFFLINE);
                mapper.persist(update);
            }
        }
        try {
            update = mapper.getStatusUpdate(accountJid, jid);
            update.setPresence(Presence.AVAILABLE);
            Node node = stanza.getDocumentNode();
            Node show = XMLUtils.getFirstChild(node, null, "show");
            if (show != null) {
                String presence = show.getTextContent();
                if ("away".equals(presence)) {
                    update.setPresence(Presence.AWAY);
                }
                if ("dnd".equals(presence)) {
                    update.setPresence(Presence.DO_NOT_DISTURB);
                }
            }
            Node status = XMLUtils.getFirstChild(node, null, "status");
            if (status != null) {
                update.setStatus(status.getTextContent());
            }
            mapper.persist(update);
        } catch (XmppMalformedException e) {
            e.printStackTrace();
        }
    }

}
