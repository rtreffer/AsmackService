/*
 * Licensed under Apache License, Version 2.0 or LGPL 2.1, at your option.
 * --
 *
 * Copyright 2010 Rene Treffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 *
 * Copyright (C) 2010 Rene Treffer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.googlecode.asmack.view;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.googlecode.asmack.R;
import com.googlecode.asmack.sync.LoginTestThread;

/**
 * AuthentificatorActivity is the login screen activity.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    /**
     * The logging tag, AuthenticatorActivity.
     */
    private static final String TAG = AuthenticatorActivity.class.getSimpleName();

    /**
     * The intent key for the username (jid).
     */
    public static final String PARAM_USERNAME = "username";

    /**
     * A handler for login result posting.
     */
    private final Handler handler = new Handler();

    /**
     * The underlying account manager.
     */
    private AccountManager accountManager;

    /**
     * A Thread to try out login credentials.
     */
    private LoginTestThread loginTestThread;

    /**
     * Create an AuthenticatorActivity based on a bundle.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        accountManager = AccountManager.get(this);
        final Intent intent = getIntent();
        String username = intent.getStringExtra(PARAM_USERNAME);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(com.googlecode.asmack.R.layout.authenticator_activity);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
            android.R.drawable.ic_dialog_alert);

        EditText usernameEdit = (EditText) findViewById(R.id.username_edit);

        usernameEdit.setText(username);
    }

    /**
     * Create a wait dialog while the login thread is running.
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(R.string.authenticator_activity_try_login));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.i(TAG, "dialog cancel has been invoked");
                try {
                    loginTestThread.interrupt();
                    try {
                        loginTestThread.join();
                    } catch (InterruptedException e) {
                        /* don't care */
                    }
                } finally {
                finish();
                }
            }
        });
        return dialog;
    }

    /**
     * Handle a login attempt by starting the login thread and showing a wait
     * dialog.
     * @param view The base view, ignored.
     */
    public void handleLogin(View view) {
        showDialog(0);

        EditText usernameEdit = (EditText) findViewById(R.id.username_edit);
        EditText passwordEdit = (EditText) findViewById(R.id.password_edit);

        String username = usernameEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        loginTestThread = new LoginTestThread(username, password, handler, this);
        loginTestThread.start();
    }

    /**
     * Result of the login attempt, create an account on success, dismiss
     * the wait dialog.
     * @param result The actual result.
     * @param username The tried out username, the local jid.
     * @param password The tried out password.
     */
    public void onAuthenticationResult(
        boolean result,
        String username,
        String password
    ) {
        dismissDialog(0);
        if (result) {
            Account account = new Account(username, "com.googlecode.asmack");
            accountManager.addAccountExplicitly(account, password, null);
            ContentResolver.setSyncAutomatically(account, 
                    ContactsContract.AUTHORITY, true);
            finish();
        }
    }

}
