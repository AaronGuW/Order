package com.example.aaron.order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.LogRecord;


/**
 * Created by Aaron on 2015/11/21.
 */
public class NewEventActivity extends Activity {
    private NonSlideViewPager setting;
    private ClickSwitchViewPager dur,end,scene,type;
    private Button back,cancel,save,add,minus;
    private WeekDayPicker weekDayPicker;
    private TextView interval;                  //间隔天数
    private TextView OnetimeDateView,IntervalStartDateView,StartTime,EndTime;
    private LinearLayout OnetimeDateBar,IntervalDateBar,warning;
    private int itvl = 1;
    private ArrayList<View> typelist,settinglist,modelist,modelist2,scenelist;
    private DBManager db;
    private static final String[] months = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    private Date OneTimedate,Intervalstartdate,start_time,end_time;
    private PopupWindow odp,idp,stime,etime;
    private DatePicker OnetimeDatePicker,IntervalDatePicker;
    private TimePicker sTimePicker,eTimePicker;
    private EditText Title,Note;
    private int position,typ;

    private Context context;
    private final SimpleDateFormat DateUtil = new SimpleDateFormat("yyyy MM.dd"), TimeUtil = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd"),simpleDateFormat1 = new SimpleDateFormat("HHmm"),simpleDateFormat2 = new SimpleDateFormat("yyyyMMddHHmm");
    private Event res;
    private Intent reIntent;
    private String action;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newevent);
        context = this;

        action = getIntent().getStringExtra("action");

        db = new DBManager(this);
        DateUtil.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        TimeUtil.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        warning = (LinearLayout)findViewById(R.id.warningbar);

        init_type_icon();
        init_setting();
        init_mode();
        init_pop();
        init_button();
        ViewsInitialValue();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("KeyDown","called");
        if (keyCode == 4) {         //返回键
            if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
                //隐藏软键盘
                Log.i("keyboard", "drawback");
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            } else {
                Log.i("activity", "finish");
                reIntent = new Intent();
                reIntent.putExtra("tmp", false);
                reIntent.putExtra("routine", false);
                reIntent.putExtra("delete", false);
                reIntent.putExtra("modify", false);
                NewEventActivity.this.setResult(0, reIntent);
                NewEventActivity.this.finish();
            }
        } else {
            super.onKeyDown(keyCode, event);
        }
        return false;
    }

    private void ViewsInitialValue() {
        Title = (EditText)findViewById(R.id.title);
        Note = (EditText)findViewById(R.id.notebody);
        action = getIntent().getStringExtra("action");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));

        if (action.compareTo("new") == 0) {
            //Time now = new Time("GMT+08:00");
            //now.setToNow();
            //String n = new String(String.valueOf(now.monthDay)+"th "+months[now.month]+", "+String.valueOf(now.year));
            Calendar now = Calendar.getInstance();
            String n = String.valueOf(now.get(Calendar.DAY_OF_MONTH))+"th "+months[now.get(Calendar.MONTH)]+", "+String.valueOf(now.get(Calendar.YEAR));
            String tmp = String.valueOf(now.get(Calendar.YEAR))+"."+String.valueOf(now.get(Calendar.MONTH)+1)+"."+String.valueOf(now.get(Calendar.DAY_OF_MONTH));
            SimpleDateFormat sss = new SimpleDateFormat("yyyy.MM.dd");
            try {
                OneTimedate = sss.parse(tmp);
                Intervalstartdate = sss.parse(tmp);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Parse","fail");
            }
            OnetimeDateView.setText(n);
            IntervalStartDateView.setText(n);
            try {
                start_time = sdf.parse("7:00");
                end_time = sdf.parse("8:00");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            typ = getIntent().getIntExtra("type",0);
            position = getIntent().getIntExtra("position",0);
            Event e = typ == 0? Order.telist.get(position):Order.relist.get(position);
            Title.setText(e.getTitle());
            start_time = e.getStart();
            end_time = e.getEnd();
            StartTime.setText(sdf.format(start_time));
            EndTime.setText(sdf.format(end_time));
            Note.setText(e.getNote());
            scene.setCurrentItem(e.getScene());
            switch (e.getDur_mode()) {
                case AudioManager.RINGER_MODE_SILENT:
                    dur.setCurrentItem(0);
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    dur.setCurrentItem(1);
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    dur.setCurrentItem(2);
                    break;
            }
            switch (e.getEnd_mode()) {
                case AudioManager.RINGER_MODE_NORMAL:
                    end.setCurrentItem(0);
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    end.setCurrentItem(1);
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    end.setCurrentItem(2);
                    break;
            }

            if (typ == 1) {
                RoutineEvent re = (RoutineEvent)e;
                if (re.getWeekday() != null) {
                    type.setCurrentItem(1);
                    setting.setCurrentItem(1);
                    weekDayPicker.setWeekday(re.getWeekday());
                } else {
                    type.setCurrentItem(2);
                    setting.setCurrentItem(2);
                    Date isd = re.getStart_day();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    String sd = simpleDateFormat.format(isd);
                    Time t = new Time("GMT+8");
                    t.parse(sd);
                    Intervalstartdate = isd;
                    itvl = re.getInterval();
                    IntervalStartDateView.setText(String.valueOf(t.monthDay)+"th "+months[t.month]+", "+String.valueOf(t.year));
                    interval.setText(String.valueOf(re.getInterval()));
                }
            } else {
                TempEvent te = (TempEvent)e;
                Date sd = te.getDate();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                String d = simpleDateFormat.format(sd);
                Time t = new Time("GMT+8");
                t.parse(d);
                OneTimedate = sd;
                OnetimeDateView.setText(String.valueOf(t.monthDay)+"th "+months[t.month]+", "+String.valueOf(t.year));
            }
        }

        /** Events expired warning **/
        warning.setVisibility(View.GONE);
        if (type.getCurrentItem() == 0) {
            if (isExpired(start_time, end_time, OneTimedate)) {
                warning.setVisibility(View.VISIBLE);
            }
        }


        OnetimeDateBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                odp.showAtLocation(findViewById(R.id.holder),Gravity.CENTER|Gravity.CENTER_HORIZONTAL,0,0);
            }
        });
        IntervalDateBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idp.showAtLocation(findViewById(R.id.holder),Gravity.CENTER|Gravity.CENTER_HORIZONTAL,0,0);
            }
        });
        StartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stime.showAtLocation(findViewById(R.id.holder),Gravity.CENTER|Gravity.CENTER_HORIZONTAL,0,0);
            }
        });

        EndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etime.showAtLocation(findViewById(R.id.holder),Gravity.CENTER|Gravity.CENTER_HORIZONTAL,0,0);
            }
        });
    }

    private void init_pop() {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.datepicker,null);
        odp = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        OnetimeDatePicker = (DatePicker)view.findViewById(R.id.datepicker);
        OneTimedate = new Date();
        view.findViewById(R.id.shadow).setOnClickListener(new MyOnClickListener(odp,0,OnetimeDateView,OnetimeDatePicker));

        View view2 = inflater.inflate(R.layout.datepicker,null);
        idp = new PopupWindow(view2, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        IntervalDatePicker = (DatePicker)view2.findViewById(R.id.datepicker);
        Intervalstartdate = new Date();
        view2.findViewById(R.id.shadow).setOnClickListener(new MyOnClickListener(idp,1,IntervalStartDateView,IntervalDatePicker));

        View view3 = inflater.inflate(R.layout.timepicker,null);
        stime = new PopupWindow(view3, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        sTimePicker = (TimePicker)view3.findViewById(R.id.timepicker);
        sTimePicker.setIs24HourView(true);
        StartTime = (TextView)findViewById(R.id.start);
        start_time = new Date();
        view3.findViewById(R.id.shadow).setOnClickListener(new MyOnClickListener(stime,0,StartTime,sTimePicker));

        View view4 = inflater.inflate(R.layout.timepicker,null);
        etime = new PopupWindow(view4, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        eTimePicker = (TimePicker)view4.findViewById(R.id.timepicker);
        eTimePicker.setIs24HourView(true);
        EndTime = (TextView)findViewById(R.id.end);
        end_time = new Date();
        view4.findViewById(R.id.shadow).setOnClickListener(new MyOnClickListener(etime,1,EndTime,eTimePicker));

    }

    private void init_button() {
        cancel = (Button)findViewById(R.id.cancel);
        save = (Button)findViewById(R.id.save);
        back = (Button)findViewById(R.id.back);

        if (action.compareTo("modify")==0) {
            cancel.setText("Delete");
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reIntent = new Intent();
                reIntent.putExtra("tmp", false);
                reIntent.putExtra("delete", false);
                reIntent.putExtra("modify", false);
                db.close();
                NewEventActivity.this.setResult(0, reIntent);
                NewEventActivity.this.finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reIntent = new Intent();
                reIntent.putExtra("tmp",false);
                reIntent.putExtra("routine",false);
                if (action.compareTo("new") == 0)
                    reIntent.putExtra("delete",false);
                else {
                    reIntent.putExtra("delete", true);
                    reIntent.putExtra("action", String.valueOf(typ) + String.valueOf(position));
                }
                reIntent.putExtra("modify",false);
                db.close();
                NewEventActivity.this.setResult(0,reIntent);
                NewEventActivity.this.finish();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reIntent = new Intent();
                reIntent.putExtra("delete",false);
                int mode = type.getCurrentItem();
                int Scene = scene.getCurrentItem();
                int dur_mode = dur.getCurrentItem();
                int end_mode = end.getCurrentItem();
                String title = String.valueOf(Title.getText());
                String note = String.valueOf(Note.getText());
                switch (dur_mode) {
                    case 0:
                        dur_mode = AudioManager.RINGER_MODE_SILENT;
                        break;
                    case 1:
                        dur_mode = AudioManager.RINGER_MODE_NORMAL;
                        break;
                    case 2:
                        dur_mode = AudioManager.RINGER_MODE_VIBRATE;
                        break;
                }
                switch (end_mode) {
                    case 0:
                        end_mode = AudioManager.RINGER_MODE_NORMAL;
                        break;
                    case 1:
                        end_mode = AudioManager.RINGER_MODE_VIBRATE;
                        break;
                    case 2:
                        end_mode = AudioManager.RINGER_MODE_SILENT;
                        break;
                }
                switch (mode) {
                    case 0:
                        res = new TempEvent(0,title,Scene,start_time,end_time,OneTimedate,dur_mode,end_mode,note,true);
                        if (isExpired(start_time,end_time,OneTimedate)) {
                            res.expire();
                            Log.i("event","expired");
                        } else {
                            res.refresh();
                            Log.i("event","fresh");
                        }
                        if (action.compareTo("new") == 0) {
                            db.saveEvent((TempEvent) res);
                            Order.telist.add(0,(TempEvent) res);
                            reIntent.putExtra("modify",false);
                            reIntent.putExtra("tmp", true);
                        } else {
                            res.setEid(Order.telist.get(position).getEid());
                            db.updateEvent((TempEvent) res);
                            Order.telist.set(position, (TempEvent)res);
                            reIntent.putExtra("modify",true);
                            reIntent.putExtra("action",0);
                        }
                        break;
                    case 1:
                        res = new RoutineEvent(0,title,Scene,start_time,end_time,dur_mode,end_mode,note,true,weekDayPicker.getWeekday());
                        if (action.compareTo("new") == 0) {
                            db.saveEvent((RoutineEvent) res);
                            Order.relist.add(0,(RoutineEvent) res);
                            reIntent.putExtra("modify",false);
                            reIntent.putExtra("routine", true);
                        } else {
                            res.setEid(Order.relist.get(position).getEid());
                            db.updateEvent((RoutineEvent) res);
                            Order.relist.set(position,(RoutineEvent)res);
                            reIntent.putExtra("modify",true);
                            reIntent.putExtra("action",1);
                        }
                        break;
                    case 2:
                        res = new RoutineEvent(0,title,Scene,start_time,end_time,dur_mode,end_mode,note,true,Intervalstartdate,itvl);
                        if (action.compareTo("new") == 0) {
                            db.saveEvent((RoutineEvent) res);
                            Order.relist.add(0,(RoutineEvent) res);
                            reIntent.putExtra("modify",false);
                            reIntent.putExtra("routine", true);
                        } else {
                            res.setEid(Order.relist.get(position).getEid());
                            db.updateEvent((RoutineEvent) res);
                            Order.relist.set(position,(RoutineEvent)res);
                            reIntent.putExtra("modify",true);
                            reIntent.putExtra("action",1);
                        }
                        break;
                }
                db.close();
                NewEventActivity.this.setResult(0,reIntent);
                NewEventActivity.this.finish();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interval.setText(String.valueOf(++itvl));
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itvl > 1)
                    interval.setText(String.valueOf(--itvl));
            }
        });
    }

    private void init_type_icon() {
        type = (ClickSwitchViewPager)findViewById(R.id.type_icon);
        type.viewcnt = 3;
        typelist = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(this);
        View onetime = inflater.inflate(R.layout.onetime,null);
        View weekly = inflater.inflate(R.layout.weekly,null);
        View interval = inflater.inflate(R.layout.interval,null);
        typelist.add(onetime);
        typelist.add(weekly);
        typelist.add(interval);
        type.setAdapter(new MyPagerAdapter(typelist));
        type.setOnPageChangeListener(new MyOnPageChangeListener());
        type.setCurrentItem(0);
    }

    private void init_mode() {
        dur = (ClickSwitchViewPager)findViewById(R.id.dur_mode);
        end = (ClickSwitchViewPager)findViewById(R.id.end_mode);
        ImageView mute= new ImageView(this);
        mute.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.mute));
        ImageView sound = new ImageView(this);
        sound.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
        ImageView vibrate = new ImageView(this);
        vibrate.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.vibrate));
        modelist = new ArrayList<>();
        modelist.add(mute);
        modelist.add(sound);
        modelist.add(vibrate);
        ImageView mute2= new ImageView(this);
        mute2.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.mute));
        ImageView sound2 = new ImageView(this);
        sound2.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
        ImageView vibrate2 = new ImageView(this);
        vibrate2.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.vibrate));
        modelist2 = new ArrayList<>();
        modelist2.add(sound2);
        modelist2.add(vibrate2);
        modelist2.add(mute2);
        dur.setAdapter(new MyPagerAdapter(modelist));
        dur.viewcnt = modelist.size();
        dur.setCurrentItem(0);
        end.setAdapter(new MyPagerAdapter(modelist2));
        end.viewcnt = modelist2.size();
        end.setCurrentItem(0);
    }

    private void init_setting() {
        scene = (ClickSwitchViewPager)findViewById(R.id.scene);
        scenelist = new ArrayList<>();
        ImageView meeting = new ImageView(this);
        meeting.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.meeting));
        ImageView classroom = new ImageView(this);
        classroom.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.classroom));
        ImageView sleep = new ImageView(this);
        sleep.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sleep));
        ImageView date = new ImageView(this);
        date.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.date));
        ImageView work = new ImageView(this);
        work.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.work));
        ImageView other = new ImageView(this);
        other.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.other));
        scenelist.add(meeting);
        scenelist.add(classroom);
        scenelist.add(sleep);
        scenelist.add(date);
        scenelist.add(work);
        scenelist.add(other);
        scene.viewcnt = scenelist.size();
        scene.setAdapter(new MyPagerAdapter(scenelist));
        scene.setCurrentItem(0);

        setting = (NonSlideViewPager)findViewById(R.id.timesetting);
        settinglist = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(this);
        View onetimesetting = inflater.inflate(R.layout.onetimesetting,null);
        View weeklysetting = inflater.inflate(R.layout.weeklysetting,null);
        View intervalsetting = inflater.inflate(R.layout.intervalsetting,null);
        weekDayPicker = (WeekDayPicker)weeklysetting.findViewById(R.id.weekdaypicker);
        add = (Button)intervalsetting.findViewById(R.id.add);
        minus = (Button)intervalsetting.findViewById(R.id.minus);
        interval = (TextView)intervalsetting.findViewById(R.id.interval);
        OnetimeDateView = (TextView)onetimesetting.findViewById(R.id.date);
        OnetimeDateBar = (LinearLayout)onetimesetting.findViewById(R.id.datebar);
        IntervalStartDateView = (TextView)intervalsetting.findViewById(R.id.start_date);
        IntervalDateBar = (LinearLayout)intervalsetting.findViewById(R.id.start_date_bar);
        settinglist.add(onetimesetting);
        settinglist.add(weeklysetting);
        settinglist.add(intervalsetting);
        setting.setAdapter(new MyPagerAdapter(settinglist));
        setting.setCurrentItem(0);
    }

    private static boolean isExpired(Date st, Date et, Date day) {
        Date now = new Date(System.currentTimeMillis());
        String endtime, snow = simpleDateFormat2.format(now);
        if (et.compareTo(st) < 0) {
            endtime = simpleDateFormat.format(new Date(day.getTime() + 24 * 60 * 60 * 1000)) + simpleDateFormat1.format(et);
        } else {
            endtime = simpleDateFormat.format(day) + simpleDateFormat1.format(et);
        }
        if (endtime.compareTo(snow) < 0)
            return true;
        else
            return false;
    }

    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }

    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            setting.setCurrentItem(arg0);
            setting.invalidate();
            if (arg0 != 0) {
                warning.setVisibility(View.GONE);
            } else {
                if (isExpired(start_time,end_time,OneTimedate))
                    warning.setVisibility(View.GONE);
                else
                    warning.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    private class MyOnClickListener implements View.OnClickListener {
        private PopupWindow pop;
        private TextView tv;
        private TimePicker tp;
        private DatePicker dp;
        private int typ;

        public MyOnClickListener(PopupWindow p, int ty, TextView t, DatePicker d) {
            super();
            pop = p;
            tv = t;
            dp = d;
            typ = ty;
        }

        public MyOnClickListener(PopupWindow p, int ty, TextView t, TimePicker _t) {
            super();
            pop = p;
            tv = t;
            tp = _t;
            typ = ty;
        }

        @Override
        public void onClick(View v) {
            if (pop.isShowing()) {
                pop.dismiss();
                if (dp != null) {
                    String d = new String(String.valueOf(dp.getYear())+" "+String.valueOf(dp.getMonth()+1)+"."+String.valueOf(dp.getDayOfMonth()));
                    try {
                        if (typ == 0)
                            OneTimedate = DateUtil.parse(d);
                        else
                            Intervalstartdate = DateUtil.parse(d);
                    } catch (Exception e) {
                        Log.e("Parse","Collapse");
                    }
                    tv.setText(String.valueOf(dp.getDayOfMonth())+"th "+months[dp.getMonth()]+", "+String.valueOf(dp.getYear()));
                } else {
                    String minute = tp.getCurrentMinute()<10 ? "0"+String.valueOf(tp.getCurrentMinute()):String.valueOf(tp.getCurrentMinute());
                    String t = new String(String.valueOf(tp.getCurrentHour())+":"+minute);
                    try {
                        if (typ == 0)
                            start_time = TimeUtil.parse(t);
                        else
                            end_time = TimeUtil.parse(t);
                    } catch (Exception e) {
                        Log.e("Parse","Collapse");
                    }
                    tv.setText(t);
                }
            }
            if (type.getCurrentItem() == 0) {
                if (isExpired(start_time,end_time,OneTimedate))
                    warning.setVisibility(View.VISIBLE);
                else
                    warning.setVisibility(View.GONE);
            }
        }
    }

}
