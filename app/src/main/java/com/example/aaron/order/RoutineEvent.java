package com.example.aaron.order;

import java.util.Date;

/**
 * Created by Aaron on 2015/11/18.
 */
public class RoutineEvent extends Event {
    static private int WEEK = 0, INTERVAL = 1;
    private int date_mode;
    private int interval;
    private String weekday;
    private Date start_day;

    public RoutineEvent(long eid, String title, int scene, Date start, Date end, int dur_mode, int end_mode, String note, boolean status, String weekday) {
        super(eid, title, scene, start, end, dur_mode, end_mode, note, status);
        date_mode = WEEK;
        this.weekday = weekday;
    }

    public RoutineEvent(long eid, String title, int scene, Date start, Date end, int dur_mode, int end_mode, String note, boolean status, Date start_day, int interval) {
        super(eid, title, scene, start, end, dur_mode, end_mode, note, status);
        date_mode = INTERVAL;
        this.start_day = start_day;
        this.interval = interval;
    }

    public int getDate_mode() { return date_mode; }
    public Date getStart_day() { return start_day; }
    public String getWeekday() { return weekday; }
    public int getInterval() { return interval; }

}
