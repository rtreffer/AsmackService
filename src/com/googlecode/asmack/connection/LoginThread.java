package com.googlecode.asmack.connection;

import android.util.Log;

import com.googlecode.asmack.XmppException;

/**
 * A Thread to handle a login attempt.
 */
public class LoginThread extends Thread {

    /**
     * The account connection state tracking instance.
     */
    private final AccountConnection accountConnection;

    /**
     * Create a new login thread for the given account and connection pair.
     * @param accountConnection The account connection pair to use for login.
     */
    public LoginThread(AccountConnection accountConnection) {
        this.accountConnection = accountConnection;
    }

    /**
     * Run a login attempt, reporting results to AccountConnection:
     */
    @Override
    public void run() {
        Connection connection = ConnectionFactory.createConnection(
                accountConnection.getAccount()
        );
        try {
            connection.connect(accountConnection.getStanzaSink());
            accountConnection.connectionSuccess(this, connection);
        } catch (XmppException e) {
            Log.d("LoginThreadr", "Login failed", e);
            /* error */
            try {
                connection.close();
            } catch (XmppException e1) {
                /* ignore */
            }
            accountConnection.connectionFail(this);
        }
    }

}
