package com.googlecode.asmack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.googlecode.asmack.connection.IXmppTransportService;

/**
 * BootCompletedReceiver is registered for the ACTION_BOOT_COMPLETED to start
 * the XmppTransportService.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    /**
     * Start the XmppTransportService on boot.
     */
    public void onReceive(Context context, Intent intent) {
        Intent transportService = new Intent();
        transportService.setAction(IXmppTransportService.class.getCanonicalName());
        context.startService(transportService);
    }

}
