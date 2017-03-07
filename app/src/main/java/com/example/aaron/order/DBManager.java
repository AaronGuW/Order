package com.example.aaron.order;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;
import android.util.Log;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Aaron on 2015/11/19.
 */
public class DBManager {
    private DBUtil helper;
    private SQLiteDatabase db;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd"),simpleDateFormat1 = new SimpleDateFormat("HHmm"),simpleDateFormat2 = new SimpleDateFormat("yyyyMMddHHmm");
    private Context context;

    public DBManager(Context context) {
        helper = new DBUtil(context);
        this.context = context;
        db = helper.getReadableDatabase();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        simpleDateFormat2.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
    }

    public void saveEvent(TempEvent e) {
        String title = e.getTitle();
        long start = e.getStart().getTime(), end = e.getEnd().getTime();
        long date = e.getDate().getTime(), expired_date = e.isExpired()?e.expired_date.getTime():0;
        int dur_mode = e.getDur_mode(), end_mode = e.getEnd_mode(),scene = e.getScene();
        String note = e.getNote();
        int status = e.getStatus()?1:0;
        db.execSQL("INSERT INTO events VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{null,title,scene,start,end,date,0,0,0,dur_mode,end_mode,note,status,expired_date});
        Cursor cursor = db.rawQuery("SELECT last_insert_rowid() from events",null);
        if (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            e.setEid(id);
        } else {
            Log.e("id","not found!!!");
        }
    }

    public void changestatus(long eid, boolean status) {
        db.execSQL("UPDATE events SET status = ? where _id = ?",new Object[]{status?1:0,eid});
    }

    public void updateEvent(TempEvent e) {
        db.execSQL("UPDATE events SET title=?,scene=?,start=?,end=?,date=?,weekday=?,startday=?,interval=?,dur_mode=?,end_mode=?,note=?,status=? WHERE _id = ?",
                new Object[]{e.getTitle(),e.getScene(),e.getStart().getTime(),e.getEnd().getTime(),e.getDate().getTime(),"",0,0,e.getDur_mode(),e.getEnd_mode(),e.getNote(),e.getStatus()?1:0,e.getEid()});
    }

    public void updateEvent(RoutineEvent e) {
        if (e.getWeekday() != null)
            db.execSQL("UPDATE events SET title=?,scene=?,start=?,end=?,date=?,weekday=?,startday=?,interval=?,dur_mode=?,end_mode=?,note=?,status=? WHERE _id = ?",
                     new Object[]{e.getTitle(),e.getScene(),e.getStart().getTime(),e.getEnd().getTime(),0,e.getWeekday(),0,0,e.getDur_mode(),e.getEnd_mode(),e.getNote(),e.getStatus()?1:0,e.getEid()});
        else
            db.execSQL("UPDATE events SET title=?,scene=?,start=?,end=?,date=?,weekday=?,startday=?,interval=?,dur_mode=?,end_mode=?,note=?,status=? WHERE _id = ?",
                    new Object[]{e.getTitle(),e.getScene(),e.getStart().getTime(),e.getEnd().getTime(),0,"",e.getStart_day().getTime(),e.getInterval(),e.getDur_mode(),e.getEnd_mode(),e.getNote(),e.getStatus()?1:0,e.getEid()});

    }

    public void saveEvent(RoutineEvent e) {
        String title = e.getTitle();
        int interval = 0;
        String weekday = new String();
        long startday = 0;
        long start = e.getStart().getTime(), end = e.getEnd().getTime();
        int mode = e.getDate_mode();
        if (mode == 0) {
            weekday = e.getWeekday();
        } else {
            startday = e.getStart_day().getTime();
            interval = e.getInterval();
        }
        int dur_mode = e.getDur_mode(), end_mode = e.getEnd_mode(), scene = e.getScene();
        String note = e.getNote();
        int status = e.getStatus()?1:0;
        long expired = e.isExpired()?e.getExpired_date().getTime():0;
        db.execSQL("INSERT INTO events VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{null,title,scene,start,end,0,weekday,startday,interval,dur_mode,end_mode,note,status,expired});
        Cursor cursor = db.rawQuery("SELECT last_insert_rowid() from events",null);
        if (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            e.setEid(id);
        } else {
            Log.e("id","not found!!!");
        }
    }

    public void deleteEvent(long eid) {
        db.execSQL("DELETE FROM events WHERE _id = ?",new Object[]{eid});
    }

    public void initialize(ArrayList<TempEvent> telist, ArrayList<RoutineEvent> relist) {
        Cursor cursor = db.rawQuery("select * from events",null);
        String title,note;
        long start, end, date, startday, id, expiredate;
        int scene, interval, dur_mode, end_mode, status;
        String weekday;
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex("_id"));
            title = cursor.getString(cursor.getColumnIndex("title"));
            scene = cursor.getInt(cursor.getColumnIndex("scene"));
            start = cursor.getLong(cursor.getColumnIndex("start"));
            end = cursor.getLong(cursor.getColumnIndex("end"));
            date = cursor.getLong(cursor.getColumnIndex("date"));
            weekday = cursor.getString(cursor.getColumnIndex("weekday"));
            startday = cursor.getLong(cursor.getColumnIndex("startday"));
            interval = cursor.getInt(cursor.getColumnIndex("interval"));
            dur_mode = cursor.getInt(cursor.getColumnIndex("dur_mode"));
            end_mode = cursor.getInt(cursor.getColumnIndex("end_mode"));
            note = cursor.getString(cursor.getColumnIndex("note"));
            status = cursor.getInt(cursor.getColumnIndex("status"));
            expiredate = cursor.getInt(cursor.getColumnIndex("expiredate"));
            if (date != 0) {
                telist.add(new TempEvent(id,title,scene,new Date(start),new Date(end),new Date(date),dur_mode,end_mode,note,status==1?true:false));
                if (expiredate != 0)
                    telist.get(telist.size()-1).setExpired_date(new Date(expiredate));
            } else {
                if (weekday.length() != 0)
                    relist.add(new RoutineEvent(id,title,scene,new Date(start),new Date(end),dur_mode,end_mode,note,status==1?true:false,weekday));
                else
                    relist.add(new RoutineEvent(id,title,scene,new Date(start),new Date(end),dur_mode,end_mode,note,status==1?true:false,new Date(startday),interval));
            }
        }
        Handle_Expired_Event(telist);
        EventComparator ecmp = new EventComparator();
        Collections.sort(telist,ecmp);
        Collections.sort(relist,ecmp);
    }

    public void close() {
        db.close();
    }

    private void Handle_Expired_Event(ArrayList<TempEvent> telist) {
        int period = context.getSharedPreferences("setting",Context.MODE_PRIVATE).getInt("period",0);
        Long now = System.currentTimeMillis()/1000 - 16*60*60,endtime, expiretime;
        boolean delete;
        for (int i = 0 ; i != telist.size() ; ++i) {
            TempEvent te = telist.get(i);
            if (!te.isExpired()) {
                if (isExpired(te)) {
                    te.expire();
                }
            }
            if (te.isExpired()) {
                if (period == 1) {
                    deleteEvent(te.getEid());
                    telist.remove(i);
                    i--;
                    break;
                } else{
                    if (te.getEnd().compareTo(te.getStart()) < 0) {
                        endtime = te.getDate().getTime()/1000 + 86400;
                    } else {
                        endtime = te.getDate().getTime()/1000;
                    }
                    endtime -= 16*60*60;
                    endtime = endtime - endtime%86400;
                    expiretime = (now - endtime)/86400;
                    delete = false;
                    switch (period) {
                        case 2:
                            delete = expiretime >= 1;
                            break;
                        case 3:
                            delete = expiretime >= 3;
                            break;
                        case 4:
                            delete = expiretime >= 7;
                            break;
                    }
                    if (delete) {
                        deleteEvent(te.getEid());
                        telist.remove(i);
                        i--;
                        break;
                    }
                }
            }
        }
    }

    private static boolean isExpired(TempEvent te) {
        Date now = new Date(System.currentTimeMillis());
        String endtime, snow = simpleDateFormat2.format(now);
        if (te.getEnd().compareTo(te.getStart()) < 0) {
            endtime = simpleDateFormat.format(new Date(te.getDate().getTime() + 24 * 60 * 60 * 1000)) + simpleDateFormat1.format(te.getEnd());
        } else {
            endtime = simpleDateFormat.format((te).getDate()) + simpleDateFormat1.format(te.getEnd());
        }
        if (endtime.compareTo(snow) < 0) {
            Log.i("now",snow);
            Log.i("end",endtime);
            return true;
        }
        else
            return false;
    }

    private static class EventComparator implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            Event r1 = (Event)lhs;
            Event r2 = (Event)rhs;
            if (r1.isExpired() == r2.isExpired())
                return (int)(r2.getEid()-r1.getEid());
            else if (r1.isExpired())
                return 1;
            else
                return -1;
        }
    }
}
