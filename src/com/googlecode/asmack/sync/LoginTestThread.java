package com.googlecode.asmack.sync;

import com.googlecode.asmack.connection.IXmppTransportService;
import com.googlecode.asmack.view.AuthenticatorActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Login test thread performs a threaded login attempt to verify account
 * credentials. This is usually done before an account is added to the account
 * list.
 */
public class LoginTestThread extends Thread {

    /**
     * The logging tag, LoginTestThread.
     */
    private static final String TAG = LoginTestThread.class.getSimpleName();

    /**
     * The username to try out.
     */
    private final String username;

    /**
     * The password to try out.
     */
    private final String password;

    /**
     * the result handler.
     */
    private final Handler handler;

    /**
     * The authenticator activity for replies.
     */
    private final AuthenticatorActivity authenticatorActivity;

    /**
     * The XMPP transport service binding used for the tryout.
     */
    private IXmppTransportService service;

    /**
     * The service connection of the xmpp transport service bind.
     */
    private ServiceConnection serviceConnection;

    /**
     * Create a new login test thread for the given credentials.
     * @param username The user jid.
     * @param password The user password.
     * @param handler The result handler.
     * @param authenticatorActivity The authenticator activity.
     */
    public LoginTestThread(
        String username,
        String password,
        Handler handler,
        AuthenticatorActivity authenticatorActivity
    ) {
        this.username = username;
        this.password = password;
        this.handler = handler;
        this.authenticatorActivity = authenticatorActivity;
    }

    /**
     * Run the login thread, posting the result when done.
     */
    @Override
    public void run() {
        boolean success = false;
        try {
            try {
                bindService();
                for (int i = 0; i < 100 && service == null; i++) {
                    Thread.sleep(10);
                }
                Thread.sleep(10);
                if (service != null && service.tryLogin(username, password)) {
                    success = true;
                    postResult(true);
                }
            } catch (RemoteException e) {
                // we handle all cases, so just log
                Log.e(TAG, "Login test failed", e);
            } finally {
                if (!success) {
                    postResult(false);
                }
                unbindService();
            }
        } catch (InterruptedException e) {
            /* ignore */
            /* this can be caused by UI abort */
        }
    }

    /**
     * Post a negative or positive result to the authenticator activity.
     * @param result The result to post.
     */
    private void postResult(final boolean result) {
        handler.post(new Runnable() {
            public void run() {
                authenticatorActivity.onAuthenticationResult(
                    result,
                    username,
                    password
                );
            }
        });
    }

    /**
     * Unbind the xmpp service, free the resources.
     */
    private final synchronized void unbindService() {
        if (serviceConnection != null) {
            try {
                authenticatorActivity.unbindService(serviceConnection);
            } catch (Exception e) { /* ignore */ }
            service = null;
            serviceConnection = null;
        }
    }

    /**
     * Bind to the xmpp service.
     */
    private final synchronized void bindService() {
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {

                public void onServiceDisconnected(ComponentName name) {
                }

                public void onServiceConnected(ComponentName name, IBinder binder) {
                    service = IXmppTransportService.Stub.asInterface(binder);
                }
            };
        }
        authenticatorActivity.bindService(
                new Intent(IXmppTransportService.class.getName()),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

}
