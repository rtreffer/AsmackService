package com.googlecode.asmack.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {

    private static final String TAG =
                                ConnectivityReceiver.class.getSimpleName();

    private final XmppTransportService xmppTransportService;

    private boolean disconnected = false;

    public ConnectivityReceiver(XmppTransportService xmppTransportService) {
        this.xmppTransportService = xmppTransportService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(
                ConnectivityManager.EXTRA_NO_CONNECTIVITY,
                false
        )) {
            if (!disconnected) {
                Log.d(TAG, "Disconnected");
                disconnected = true;
            }
            return;
        }
        if (!disconnected) {
            return;
        }
        disconnected = false;
        xmppTransportService.onConnectivityAvailable();
    }

}
