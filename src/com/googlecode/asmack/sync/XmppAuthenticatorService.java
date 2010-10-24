package com.googlecode.asmack.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Authenticator service for the android account authentification system.
 */
public class XmppAuthenticatorService extends Service {

    /**
     * The internal authenticator.
     */
    private Authenticator authenticator;

    /**
     * Create the service and initialize the authenticator..
     */
    @Override
    public void onCreate() {
        super.onCreate();
        authenticator = new Authenticator(this);
    }

    /**
     * Destroy the authenticator service.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Bind to the authenticator.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }

}
