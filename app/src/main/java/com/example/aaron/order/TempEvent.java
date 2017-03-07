package com.example.aaron.order;

import java.util.Date;

/**
 * Created by Aaron on 2015/11/18.
 */
public class TempEvent extends Event {
    private Date date;

    public TempEvent(long eid, String title, int scene, Date start, Date end, Date date, int dur_mode, int end_mode, String note, boolean status) {
        super(eid, title, scene, start, end, dur_mode, end_mode, note, status);
        this.date = date;
    }

    public Date getDate() { return date; }

}
