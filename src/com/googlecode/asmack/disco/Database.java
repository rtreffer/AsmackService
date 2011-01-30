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

package com.googlecode.asmack.disco;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.googlecode.asmack.XmppIdentity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Base64;
import android.util.Log;

/**
 * Database helper to ensure that there is just one sqlite database.
 */
public class Database {

    /**
     * The internal sqlite database instance.
     */
    private static SQLiteDatabase DATABASE = null;

    /**
     * Retrieve a sqlite database instance, shared between all clients.
     * @param context The context to use for opening the database.
     * @param factory A cursor factory.
     * @return A SQLiteDatabase instance of the messages database.
     */
    public static synchronized SQLiteDatabase getDatabase(
        Context context,
        CursorFactory factory
    ) {
        if (DATABASE == null) {
            DatabaseOpenHelper helper =
                    new DatabaseOpenHelper(context, "disco", factory);
            DATABASE = helper.getWritableDatabase();
        }
        return DATABASE;
    }

    /**
     * Compute the entity capabilities as described in
     * <a href="http://xmpp.org/extensions/xep-0115.html#ver">
     * XEP-0115 / 5. Verification String</a>. Please note that data forms
     * (XEP-0128) are not yet supported.
     * @param context The application/service context.
     * @param jid The user jid.
     * @param factory The cursor factory (can be null).
     * @return The base64 encoded sha-1 of the verification string.
     */
    public static synchronized String computeVerificationHash(
        Context context,
        String jid,
        CursorFactory factory
    ) {
        StringBuilder sb = new StringBuilder();
        for (XmppIdentity identity: getIdentities(context, jid, factory)) {
            sb.append(identity.getCategory());
            sb.append('/');
            sb.append(identity.getType());
            sb.append('/');
            sb.append(identity.getLang());
            sb.append('/');
            sb.append(identity.getName());
            sb.append('<');
        }
        for (String feature: getFeatures(context, jid, factory)) {
            sb.append(feature);
            sb.append('<');
        }
        Log.d("XMPP/DISCO", "Feature string for " + jid + ": " + sb.toString());
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte digest[] = sha1.digest(sb.toString().getBytes());
            String base64 =
                Base64.encodeToString(digest, Base64.NO_WRAP);
            return base64.trim();
        } catch (NoSuchAlgorithmException e) {
            // This should never happen.
            Log.e("AsmackService", "Please report (impossible condition)", e);
        }
        return sb.toString();
    }

    /**
     * Retrieve a list of all features enabled on a given connection.
     * @param context The current context.
     * @param jid The user jid.
     * @param factory A cursor factory (may be null).
     * @return An array of enabled features.
     */
    public static synchronized String[] getFeatures(
        Context context,
        String jid,
        CursorFactory factory
    ) {
        ArrayList<String> features = new ArrayList<String>();
        SQLiteDatabase database = getDatabase(context, factory);
        Cursor result = database.query(
            true,
            "feature",
            new String[]{"ver"},
            "(jid=? OR (jid IS NULL))",
            new String[]{jid},
            null, null, "ver ASC", null
        );
        features.ensureCapacity(result.getCount() + 1);
        if (result.getCount() > 0) {
            int verId = result.getColumnIndex("ver");
            result.moveToFirst();
            do {
                features.add(result.getString(verId));
                result.moveToNext();
            } while (!result.isAfterLast());
        }
        result.close();
        String output[] = new String[features.size()];
        return features.toArray(output);
    }

    /**
     * Retrieve a list of all identities on a given connection.
     * @param context The current context.
     * @param jid The user account jid.
     * @param factory A cursor factory (may be null).
     * @return
     */
    public static synchronized XmppIdentity[] getIdentities(
        Context context,
        String jid,
        CursorFactory factory
    ) {
        ArrayList<XmppIdentity> identities = new ArrayList<XmppIdentity>();
        SQLiteDatabase database = getDatabase(context, factory);
        Cursor result = database.query(
            true,
            "identity",
            new String[]{"category", "type", "lang", "name"},
            "jid=? OR (jid IS NULL)", new String[]{jid},
            null, null, 
            "category ASC, type ASC, lang ASC, name ASC",
            null
        );
        if (result.getCount() > 0) {
            result.moveToFirst();
            int categoryIndex = result.getColumnIndex("category");
            int typeIndex = result.getColumnIndex("type");
            int langIndex = result.getColumnIndex("lang");
            int nameIndex = result.getColumnIndex("name");
            do {
                identities.add(new XmppIdentity(
                    result.getString(categoryIndex),
                    result.getString(typeIndex),
                    result.getString(langIndex),
                    result.getString(nameIndex)
                ));
                result.moveToNext();
            } while (!result.isAfterLast());
        }
        result.close();
        XmppIdentity output[] = new XmppIdentity[identities.size()];
        return identities.toArray(output);
    }

    /**
     * Check if a feature is enabled for all accounts.
     * @param context The current context.
     * @param feature The feature to test.
     * @param factory A cursor factory (may be null).
     * @return True if the feature is globally enabled.
     */
    public static synchronized boolean hasFeature(
        Context context,
        String feature,
        CursorFactory factory
    ) {
        return hasFeature(context, feature, null, factory);
   }

    /**
     * Check if a given feature is enabled for a single account or globally.
     * @param context The current context.
     * @param feature The feature to test.
     * @param jid The account jid.
     * @param factory A cursor factory (may be null).
     * @return True if the feature is available.
     */
    public static synchronized boolean hasFeature(
        Context context,
        String feature,
        String jid,
        CursorFactory factory
    ) {
        SQLiteDatabase database = getDatabase(context, factory);
        boolean featureAvailable = false;
        if (jid == null) {
            Cursor result = database.query(
                "feature",
                new String[]{"_id"},
                "(jid IS NULL) AND ver=?",
                new String[]{feature},
                null, null, null
            );
            featureAvailable = result.getCount() > 0;
            result.close();
        } else {
            Cursor result = database.query(
                "feature",
                new String[]{"_id"},
                "(jid=? OR (jid IS NULL)) AND ver=?",
                new String[]{jid, feature},
                null, null, null
            );
            featureAvailable = result.getCount() > 0;
            result.close();
        }
        return featureAvailable;
    }

    /**
     * Enable a feature for all accounts.
     * @param context The current context.
     * @param feature The feature to enable.
     * @param factory A cursor factory (may be null).
     */
    public static synchronized void enableFeature(
        Context context,
        String feature,
        CursorFactory factory
    ) {
        enableFeature(context, feature, null, factory);
    }

    /***
     * Enable a feature for a given account.
     * @param context The current context.
     * @param feature The feature to enable.
     * @param jid The account jid.
     * @param factory A cursor factory (may be null).
     */
    public static synchronized void enableFeature(
        Context context,
        String feature,
        String jid,
        CursorFactory factory
    ) {
        if (hasFeature(context, feature, jid, factory)) {
            return;
        }
        SQLiteDatabase database = getDatabase(context, factory);
        ContentValues values = new ContentValues();
        if (jid != null) {
            values.put("jid", jid);
        }
        values.put("ver", feature);
        database.insert("feature", "_id", values);
    }

    /**
     * Check if a xmpp identity is available for all accounts.
     * @param context The current context.
     * @param identity The xmpp identity.
     * @param factory A cursor factory (may be null).
     * @return True if the identity is available.
     */
    public static synchronized boolean hasIdentity(
        Context context,
        XmppIdentity identity,
        CursorFactory factory
    ) {
        return hasIdentity(context, identity, null, factory);
    }

    /**
     * Check if a xmpp identity is available for a given account.
     * @param context The current context.
     * @param identity The xmpp identity.
     * @param jid The account jid.
     * @param factory A cursor factory (may be null).
     * @return True if the identity is available.
     */
    public static synchronized boolean hasIdentity(
        Context context,
        XmppIdentity identity,
        String jid,
        CursorFactory factory
    ) {
        SQLiteDatabase database = getDatabase(context, factory);
        Cursor result = database.query(
            "feature",
            new String[]{"_id"},
            "(jid=? OR (jid IS NULL)) AND " +
            "category=? AND type=? AND lang=? AND name=?",
            new String[]{
                jid,
                identity.getCategory(),
                identity.getType(),
                identity.getLang(),
                identity.getName()},
            null, null, null
        );
        boolean identityAvailable = result.getCount() > 0;
        result.close();
        return identityAvailable;
    }

    /**
     * Add a xmpp identity to a given account.
     * @param context The current context.
     * @param identity The xmpp identity.
     * @param jid The account jid.
     * @param factory A cursor factory (may be null).
     */
    public static synchronized void addIdentity(
        Context context,
        String jid,
        XmppIdentity identity,
        CursorFactory factory
    ) {
        if (hasIdentity(context, identity, jid, factory)) {
            return;
        }
        SQLiteDatabase database = getDatabase(context, factory);
        ContentValues values = new ContentValues();
        if (jid != null) {
            values.put("jid", jid);
        }
        values.put("category", identity.getCategory());
        values.put("type", identity.getType());
        values.put("lang", identity.getLang());
        values.put("name", identity.getName());
        database.insert("identity", "_id", values);
    }

    /**
     * Add a xmpp identity to all accounts.
     * @param context The current context.
     * @param identity The xmpp identity.
     * @param factory A cursor factory (may be null).
     */
    public static synchronized void addIdentity(
            Context context,
            XmppIdentity identity,
            CursorFactory factory
    ) {
        addIdentity(context, null, identity, factory);
    }

}
