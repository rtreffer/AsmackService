package com.googlecode.asmack.sync;

import com.googlecode.asmack.view.AuthenticatorActivity;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Authenticator service to manage the xmpp accounts.
 */
public class Authenticator extends AbstractAccountAuthenticator {

    /**
     * The context of this authenticator.
     */
    private final Context context;

    /**
     * Create a new Authenticator based on the given Context.
     * @param context The authenticator context.
     */
    public Authenticator(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Add a new xmpp account.
     * @param response Ro send the results.
     * @param accountType The account type as specified in AndroidManifest.xml.
     * @param authTokenType The auth token type, should be null.
     * @param requiredFeatures An array of required features.
     * @param options Authenticator-specific options
     * @return A result Bundle to open an AuthenticatorActivity.
     */
    @Override
    public Bundle addAccount(
        AccountAuthenticatorResponse response,
        String accountType,
        String authTokenType,
        String[] requiredFeatures,
        Bundle options
    ) throws NetworkErrorException {
        Log.e("Authenticator", "addAccount");
        final Intent intent = new Intent(context, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
            response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    /**
     * Confirm the credentials of an account, return always true.
     * @param response Ignored.
     * @param account Ignored.
     * @param options Ignored.
     * @return A Bundle with {@link AccountManager#KEY_BOOLEAN_RESULT} set to
     *         true.
     */
    @Override
    public Bundle confirmCredentials(
        AccountAuthenticatorResponse response,
        Account account,
        Bundle options
    ) throws NetworkErrorException {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
        return bundle;
    }

    /**
     * Return null to delay the property change.
     * @param response Ignored.
     * @param accountType Ignored.
     * @return null.
     */
    @Override
    public Bundle editProperties(
        AccountAuthenticatorResponse response,
        String accountType
    ) {
        return null;
    }

    /**
     * Return null to delay the auth token request
     * @param response Ignored.
     * @param account Ignored.
     * @param authTokenType Ignored.
     * @param options Ignored.
     * @return null.
     */
    @Override
    public Bundle getAuthToken(
        AccountAuthenticatorResponse response,
        Account account,
        String authTokenType,
        Bundle options
    ) throws NetworkErrorException {
        return null;
    }

    /**
     * Return null as the authTokenType isn't known.
     * @param authTokenType Ignored.
     * @return null.
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    /**
     * Return null to delay the feature request.
     * @param response Ignored.
     * @param account Ignored.
     * @param features Ignored.
     * @return null.
     */
    @Override
    public Bundle hasFeatures(
        AccountAuthenticatorResponse response,
        Account account,
        String[] features
    ) throws NetworkErrorException {
        return null;
    }

    /**
     * Return null to delay the update request.
     * @param response Ignored.
     * @param account Ignored.
     * @param authTokenType Ignored.
     * @param options Ignored.
     * @return null.
     */
    @Override
    public Bundle updateCredentials(
        AccountAuthenticatorResponse response,
        Account account,
        String authTokenType,
        Bundle options
    ) throws NetworkErrorException {
        return null;
    }

}
