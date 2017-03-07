package com.example.aaron.order;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Aaron on 2015/11/19.
 */
public class DBUtil extends SQLiteOpenHelper {
    private static final String DBName = "Order.db";
    private static final int Version = 1;

    public DBUtil(Context context) {
        super(context, DBName, null, Version);
    }

    /** onCreate will be called when the getreadabledatabase() or getwritabledatabase() is called while the db does not exist, otherwise not **/
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS events" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title VARCHAR, " +
                "scene INTEGER, " +
                "start INTEGER, " +
                "end INTEGER, " +
                "date INTEGER, " +
                "weekday VARCHAR, " +
                "startday INTEGER," +
                "interval INTEGER, " +
                "dur_mode INTEGER, " +
                "end_mode INTEGER, " +
                "note VARCHAR, " +
                "status INTEGER," +
                "expiredate INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("ALTER TABLE events ADD COLUMN other STRING");
    }
}
