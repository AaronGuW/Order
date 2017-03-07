package com.example.aaron.order;

import android.app.Application;
import android.text.format.Time;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Aaron on 2015/11/17.
 */
public class Order extends Application {
    static private int WEEK = 0, INTERVAL = 1;
    static private float scale;
    static public void setScale(float s) { scale = s; }
    static public float dp2pix(float dp) { return dp*scale;}
    static public ArrayList<TempEvent> telist;
    static public ArrayList<RoutineEvent> relist;

    public static void ArrangeEvents(ArrayList<TempEvent> telist, ArrayList<RoutineEvent> relist, ArrayList<Event> eventlist) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        Date curDate = new Date(System.currentTimeMillis());
        String today = sdf.format(curDate);
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat csdf = new SimpleDateFormat("HHmm");
        csdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String Now = csdf.format(now);

        Time time = new Time("GMT+8");
        time.setToNow();
        int weekday = time.weekDay-1==-1?6:time.weekDay-1;

        /** copy the One-Time events today to the job list **/
        TempEvent tmp;
        for (int i = 0 ; i != telist.size() ; ++i) {
            if (telist.get(i).getStatus()) {
                tmp = telist.get(i);
                if (tmp.getStart().compareTo(tmp.getEnd()) < 0) {
                    if (today.compareTo(sdf.format(tmp.getDate())) == 0) {
                        if (Now.compareTo(csdf.format(tmp.getEnd())) < 0) {
                            tmp.firstday = true;
                            eventlist.add(tmp);
                        }
                    }
                } else {
                    if (today.compareTo(sdf.format(tmp.getDate())) == 0) {
                        tmp.firstday = true;
                        eventlist.add(tmp);
                    } else  if (today.compareTo(sdf.format( new Date(tmp.getDate().getTime()+86400000) )) == 0){
                        if (Now.compareTo(csdf.format(tmp.getEnd())) < 0) {
                            tmp.firstday = false;
                            eventlist.add(tmp);
                        }
                    }
                }
            }
        }

        /** copy the routine events today to the job list **/
        for (int i = 0 ; i != relist.size() ; ++i) {
            RoutineEvent re = relist.get(i);
            if (re.getStatus()) {
                if (re.getDate_mode() == WEEK) {
                    if (re.getStart().compareTo(re.getEnd()) < 0) {
                        if (re.getWeekday().substring(weekday, weekday + 1).compareTo("1") == 0)
                            if (Now.compareTo(csdf.format(re.getEnd())) < 0) {
                                re.firstday = true;
                                eventlist.add(re);
                            }
                    } else {
                        if (re.getWeekday().substring(weekday, weekday + 1).compareTo("1") == 0) {
                            re.firstday = true;
                            eventlist.add(re);
                        }
                        else if (re.getWeekday().substring( (weekday+7-1)%7,(weekday+7-1)%7 + 1 ).compareTo("1") == 0)
                            if (Now.compareTo(csdf.format(re.getEnd())) < 0) {
                                re.firstday = false;
                                eventlist.add(re);
                            }
                    }
                } else {
                    Date tdate = new Date();
                    try {
                        tdate = sdf.parse(today);
                    } catch (Exception e) {
                        Log.e("parse", "collapse");
                    }
                    if (tdate.compareTo(re.getStart_day()) >= 0) {
                        long diff = tdate.getTime() - re.getStart_day().getTime();
                        long days = diff / (1000 * 60 * 60 * 24);
                        Log.i("days",String.valueOf(days));
                        if (re.getStart().compareTo(re.getEnd()) < 0) {
                            if (days % re.getInterval() == 0)
                                if (Now.compareTo(csdf.format(re.getEnd())) < 0) {
                                    re.firstday = true;
                                    eventlist.add(re);
                                }
                        } else {
                            if (days % re.getInterval() == 0) {
                                re.firstday = true;
                                eventlist.add(re);
                            }
                            else if ((days-1) % re.getInterval() == 0)
                                if (Now.compareTo(csdf.format(re.getEnd())) < 0) {
                                    re.firstday = false;
                                    eventlist.add(re);
                                }
                        }
                    }
                }
            }
        }
        Log.i("eventget",String.valueOf(eventlist.size()));
        Collections.sort(eventlist,new EventComparator());
    }

    public static void initialize_lists() {
        telist = new ArrayList<>();
        relist = new ArrayList<>();
    }

    private static class EventComparator implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            Event r1 = (Event)lhs;
            Event r2 = (Event)rhs;
            return r1.getStart().compareTo(r2.getStart());
        }
    }
}
