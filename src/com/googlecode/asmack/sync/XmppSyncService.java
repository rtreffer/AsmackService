package com.googlecode.asmack.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * XmppSyncService is a minimal service that conforms with the system wide
 * acocunt sync.
 */
public class XmppSyncService extends Service {

    /**
     * The internal sync adapter.
     */
    private static SyncAdapter syncAdapter = null;

    /**
     * Create a new XmppSyncService, creating a new sync adapter if needed.
     */
    @Override
    public synchronized void onCreate() {
        if (syncAdapter == null) {
            syncAdapter = new SyncAdapter(getApplicationContext());
        }
    }

    /**
     * Bind to the sync adapter.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }

}
