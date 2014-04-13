/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2013-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.bikey.backend.provider;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jraf.android.bikey.BuildConfig;
import org.jraf.android.bikey.backend.provider.log.LogColumns;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;

public class BikeySQLiteUpgradeHelper {
    private static final String TAG = BikeySQLiteUpgradeHelper.class.getSimpleName();

    // @formatter:off
    // 1 -> 2
    private static final String SQL_UPGRADE_TABLE_LOG_2 = "ALTER TABLE "
            + LogColumns.TABLE_NAME
            + " ADD COLUMN "
            + LogColumns.CADENCE + " REAL "
            + " ;";

    // 2 -> 3
    private static final String SQL_UPGRADE_TABLE_RIDE_3 = "ALTER TABLE "
            + RideColumns.TABLE_NAME
            + " ADD COLUMN "
            + RideColumns.FIRST_ACTIVATED_DATE + " REAL "
            + " ;";
    private static final String SQL_POPULATE_TABLE_RIDE_3 = "UPDATE "
            + RideColumns.TABLE_NAME
            + " SET " 
            + RideColumns.FIRST_ACTIVATED_DATE
            + " = ("
            + " SELECT MIN ( " + LogColumns.RECORDED_DATE + " ) "
            + " FROM "
            + LogColumns.TABLE_NAME
            + " WHERE "
            + LogColumns.TABLE_NAME + "." + LogColumns.RIDE_ID
            + " = "
            + RideColumns.TABLE_NAME + "." + RideColumns._ID
            + ")"
            + " ;";
    // @formatter:on

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        int curVersion = oldVersion;
        while (curVersion < newVersion) {
            switch (curVersion) {
                case 1:
                    // 1 -> 2
                    // Add new CADENCE column
                    db.execSQL(SQL_UPGRADE_TABLE_LOG_2);
                    curVersion = 2;
                    break;

                case 2:
                    // 2 -> 3
                    // Add new FIRST_ACTIVATED_DATE column
                    db.execSQL(SQL_UPGRADE_TABLE_RIDE_3);
                    // Populate it with the first RECORDED_DATE of the corresponding Logs
                    db.execSQL(SQL_POPULATE_TABLE_RIDE_3);
                    curVersion = 3;
                    break;
            }
        }
    }
}
