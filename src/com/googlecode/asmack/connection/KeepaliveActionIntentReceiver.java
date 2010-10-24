package com.googlecode.asmack.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * A broadcast event for the action intent, used to trigger ping/presence
 * notifications from the transport service.
 */
public class KeepaliveActionIntentReceiver extends BroadcastReceiver {

    /**
     * The used transport service.
     */
    private XmppTransportService service;

    /**
     * Create a new reveicer that triggers ping on the given service.
     * @param service The transport service to ping.
     */
    public KeepaliveActionIntentReceiver(XmppTransportService service) {
        super();
        this.service = service;
    }

    /**
     * Call ping on every time tick.
     * @param context Ignored, the context of the Intent.
     * @param intent Ignored, the initial intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        service.ping();
    }

}
