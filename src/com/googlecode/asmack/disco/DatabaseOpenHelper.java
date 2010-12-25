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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper to tearup and upgrade message databases. Versions are split
 * into minor and major version parts. Major version upgrades will be handled
 * with a drop, whereas minor version upgrades will preserver the content.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    /**
     * Current major database version.
     */
    private final static short MAJOR = 5;

    /**
     * Current minor database version.
     */
    private final static short MINOR = 0;

    /**
     * Combined version ((MAJOR &lt;&lt; 16) + MINOR).
     */
    private final static int VERSION = MINOR + (MAJOR << 16);

    /**
     * Open the database, performing a data upgrade if needed.
     * @param context The cntext used to open the database.
     * @param name The database name.
     * @param factory A CursorFactory.
     */
    public DatabaseOpenHelper(Context context, String name, CursorFactory factory) {
        super(context, name, factory, VERSION);
    }

    /**
     * Called when a new database is created.
     * @param db The database to initialize.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE identity(" +
                "_id INTEGER," +
                "category TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "lang TEXT NOT NULL DEFAULT ''," +
                "name TEXT NOT NULL DEFAULT ''," +
                "jid TEXT," +
                "PRIMARY KEY(_id)" +
            ")"
        );
        db.execSQL("CREATE INDEX lookup_identity ON identity(" +
                   "category ASC, type ASC, lang ASC, name ASC, _id ASC" +
                   ")");
        db.execSQL(
            "CREATE TABLE feature(" +
                "_id INTEGER," +
                "ver TEXT NOT NULL," +
                "jid TEXT," +
                "PRIMARY KEY(_id)" +
            ")"
        );
        db.execSQL("CREATE INDEX lookup_feature ON feature(" +
                   "ver ASC, _id ASC)");

        // insert a default identity
        ContentValues values = new ContentValues();
        values.put("category", "client");
        values.put("type", "phone");
        values.put("name", "asmack service");
        db.insert("identity", "_id", values);

        // and support caps/disco#info.
        // we can't reach this block otherwise :-)
        values.clear();
        values.put("ver", "http://jabber.org/protocol/caps");
        db.insert("feature", "_id", values);
        values.put("ver", "http://jabber.org/protocol/disco#info");
        db.insert("feature", "_id", values);
    }

    /**
     * Called when there is a database update.
     * @param db The database in need of an update.
     * @param oldVersion The old on disk database version.
     * @param newVersion The new and current target verstion.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int oldMajor = oldVersion >> 16;
        int newMajor = newVersion >> 16;

        if (oldMajor != newMajor) {
            db.execSQL("DROP TABLE feature");
            db.execSQL("DROP TABLE identity");
            onCreate(db);
            return;
        }
    }

}
