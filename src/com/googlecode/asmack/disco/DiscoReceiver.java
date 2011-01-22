package com.googlecode.asmack.disco;

import java.util.Arrays;

import org.w3c.dom.Node;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.googlecode.asmack.Attribute;
import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XMLUtils;
import com.googlecode.asmack.XMPPUtils;
import com.googlecode.asmack.XmppIdentity;
import com.googlecode.asmack.XmppMalformedException;
import com.googlecode.asmack.connection.XmppTransportService;

/**
 * A broadcast receiver that handles service discovery.
 */
public class DiscoReceiver extends BroadcastReceiver {

    /**
     * Called on incoming stanzas and replies on service discovery requests.
     * @param context The current context.
     * @param intent The stanza intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Stanza stanza = intent.getParcelableExtra("stanza");
        // only accept IQ stanzas
        if (!"iq".equals(stanza.getName())) {
            return;
        }
        // of type="get"
        if (!"get".equals(stanza.getAttributeValue("type"))) {
            return;
        }
        // from / to / id are mandatory
        Attribute from = stanza.getAttribute("from");
        Attribute to = stanza.getAttribute("to");
        Attribute id = stanza.getAttribute("id");
        if (id == null || from == null || to == null) {
            return;
        }

        try {
            Node node = stanza.getDocumentNode();
            Node query = XMLUtils.getFirstChild(
                node,
                "http://jabber.org/protocol/disco#info",
                "query");
            if (query == null || !query.hasAttributes()) {
                return;
            }
            Node discoAttributeNode =
                        query.getAttributes().getNamedItem("node");
            String discoNode = null;
            if (discoAttributeNode != null) {
                discoNode = discoAttributeNode.getTextContent();
            }

            // we got a disco, reply
            StringBuilder payload = new StringBuilder("<iq type='result'");
            payload.append(" from='");
            payload.append(XMLUtils.xmlEscape(to.getValue()));
            payload.append("' to='");
            payload.append(XMLUtils.xmlEscape(from.getValue()));
            payload.append("'>");
            payload.append("<query xmlns='");
            payload.append("http://jabber.org/protocol/disco#info");
            if (discoNode != null) {
                payload.append("' node='");
                payload.append(XMLUtils.xmlEscape(discoNode));
            }
            payload.append("'>");

            String myJid = XMPPUtils.getBareJid(to.getValue());
            for (XmppIdentity identity :
                Database.getIdentities(context, myJid, null)) {
                payload.append("<identity");
                if (identity.getCategory().length() > 0) {
                    payload.append(" category='");
                    payload.append(XMLUtils.xmlEscape(identity.getCategory()));
                    payload.append('\'');
                }
                if (identity.getType().length() > 0) {
                    payload.append(" type='");
                    payload.append(XMLUtils.xmlEscape(identity.getType()));
                    payload.append('\'');
                }
                if (identity.getLang().length() > 0) {
                    payload.append(" lang='");
                    payload.append(XMLUtils.xmlEscape(identity.getLang()));
                    payload.append('\'');
                }
                if (identity.getName().length() > 0) {
                    payload.append(" name='");
                    payload.append(XMLUtils.xmlEscape(identity.getName()));
                    payload.append('\'');
                }
                payload.append("/>");
            }
            for (String feature : Database.getFeatures(context, myJid, null)) {
                payload.append("<feature ver='");
                payload.append(XMLUtils.xmlEscape(feature));
                payload.append("'/>");
            }
            payload.append("</query></iq>");
            Stanza discoReply = new Stanza(
                "iq",
                "",
                XMPPUtils.getBareJid(to.getValue()),
                payload.toString(),
                Arrays.asList(new Attribute[]{id})
            );
            intent = new Intent();
            intent.setAction(XmppTransportService.XMPP_STANZA_SEND_INTENT);
            intent.putExtra("stanza", discoReply);
            intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
            context.sendBroadcast(
                intent,
                XmppTransportService.XMPP_STANZA_SEND_INTENT
            );
        } catch (XmppMalformedException e) {
            // Impossible
            Log.e("AsmackService", "Please report (impossible condition)", e);
        }
    }

}
