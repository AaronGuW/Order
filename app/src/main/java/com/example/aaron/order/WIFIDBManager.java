package com.example.aaron.order;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Aaron on 2015/12/13.
 */
public class WIFIDBManager {
    private SQLiteDatabase db;
    private WIFIDBUtil helper;

    public WIFIDBManager(Context context) {
        helper = new WIFIDBUtil(context);
        db = helper.getReadableDatabase();
    }

    public void initialize(ArrayList<wifi> wifis, int all) {
        Cursor cursor;
        if (all == 1)
            cursor = db.rawQuery("SELECT * FROM wifis",null);
        else if (all == 0)
            cursor = db.rawQuery("SELECT * FROM wifis WHERE cmode <> -1",null);
        else
            cursor = db.rawQuery("SELECT * FROM wifis WHERE cmode = -1",null);
        String ssid;
        int cnt,cmode,dmode,status;
        long lastconnect;
        while (cursor.moveToNext()) {
            ssid = cursor.getString(cursor.getColumnIndex("ssid"));
            cnt = cursor.getInt(cursor.getColumnIndex("connecttimecnt"));
            lastconnect = cursor.getLong(cursor.getColumnIndex("lastconnect"));
            cmode = cursor.getInt(cursor.getColumnIndex("cmode"));
            dmode = cursor.getInt(cursor.getColumnIndex("dmode"));
            status = cursor.getInt(cursor.getColumnIndex("status"));
            wifi tmp = new wifi(ssid,cnt,lastconnect,cmode,dmode,status == 1);
            wifis.add(tmp);
        }
    }

    public boolean is_wifi_on(String ssid) {
        Cursor cursor = db.rawQuery("SELECT * FROM wifis WHERE ssid=? AND status=1", new String[]{ssid});
        if (cursor.moveToNext())
            return true;
        return false;
    }

    public void setWifi(String ssid, int cmode, int dmode) {
        db.execSQL("UPDATE wifis SET cmode=?,dmode=?,status=1 WHERE ssid=?",new Object[]{cmode,dmode,ssid});
    }

    public int work(String ssid) {
        Cursor cursor = db.rawQuery("SELECT cmode FROM wifis WHERE ssid=? AND status=1",new String[]{ssid});
        if (cursor.moveToNext())
            return cursor.getInt(cursor.getColumnIndex("cmode"));
        return -1;
    }

    public int endwork(String ssid) {
        Cursor cursor = db.rawQuery("SELECT dmode FROM wifis WHERE ssid=? AND status=1",new String[]{ssid});
        if (cursor.moveToNext())
            return cursor.getInt(cursor.getColumnIndex("dmode"));
        return -1;
    }

    public void exist_and_count(String ssid) {
        Cursor cursor = db.rawQuery("SELECT * FROM wifis WHERE ssid=?",new String[]{ssid});
        if (cursor.moveToNext())
            db.execSQL("UPDATE wifis SET connecttimecnt = connecttimecnt + 1,lastconnect=? WHERE ssid=?",new Object[]{System.currentTimeMillis(),ssid});
        else
            db.execSQL("INSERT INTO wifis VALUES (?,?,?,?,?,?,?)",new Object[]{null,ssid,1,System.currentTimeMillis(),-1,-1,0});
    }

    public void switchwifi(String ssid, boolean status) {
        try {
            db.execSQL("UPDATE wifis SET status=? WHERE ssid=?", new Object[]{status ? 1 : 0, ssid});
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("switchwifi","dumped");
        }
    }

    public void close() {
        db.close();
    }

    private class WIFIDBUtil extends SQLiteOpenHelper {

        private static final String DBName = "WIFI.db";
        private static final int Version = 1;

        public WIFIDBUtil(Context context) {
            super(context,DBName,null,Version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS wifis" +
                    "(wid INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "ssid VARCHAR NOT NULL," +
                    "connecttimecnt INTEGER," +
                    "lastconnect INTEGER," +
                    "cmode INTEGER," +
                    "dmode INTEGER," +
                    "status INTEGER)" );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //db.execSQL("ALTER TABLE wifis ADD COLUMN other STRING");
        }
    }
}
