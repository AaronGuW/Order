package com.example.aaron.order;

import android.media.AudioManager;

import java.util.Date;

/**
 * Created by Aaron on 2015/11/18.
 */
public class Event {
    static final private int MEETING = 0, CLASS = 1, SLEEP = 2, DATE = 3, work = 4, OTHER = 5;
    static final private int MUTE = AudioManager.RINGER_MODE_SILENT, NORMAL = AudioManager.RINGER_MODE_NORMAL, VIBRATE = AudioManager.RINGER_MODE_VIBRATE, OUTDOOR = 3;
    protected long eid;
    protected String title;
    protected int scene;
    protected Date start, end, expired_date;
    protected int dur_mode, end_mode;
    protected String note;
    protected boolean status;
    protected boolean expired;
    public boolean firstday;


    public Event(long eid, String title, int scene, Date start, Date end, int dur_mode, int end_mode, String note, boolean status) {
        this.eid = eid;
        this.title = title;
        this.scene = scene;
        this.start = start;
        this.end = end;
        this.dur_mode = dur_mode;
        this.end_mode = end_mode;
        this.note = note;
        this.status = status;
        this.expired = false;
    }

    public long getEid() { return eid; }
    public boolean getStatus() { return status; }
    public int getScene() { return scene; }
    public Date getStart() { return start; }
    public Date getEnd() { return end; }
    public int getDur_mode() { return dur_mode; }
    public int getEnd_mode() { return end_mode; }
    public String getNote() { return note; }
    public String getTitle() { return title; }
    public Date getExpired_date() { return expired_date; }
    public void setEid(long id) { eid = id; }
    public void setStatus(boolean ison) { status = ison; }
    public boolean isExpired() { return expired; }
    public void setExpired_date(Date expd) {
        expired = true;
        expired_date = expd;
    }
    public void expire() {
        expired = true;
        expired_date = new Date(System.currentTimeMillis());
        status = false;
    }

    public void refresh() {
        expired = false;
        expired_date = null;
    }
}
