package com.example.aaron.order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Aaron on 2015/11/16.
 */
public class EventsView extends Activity {
    static final private int MEETING = 0, CLASS = 1, SLEEP = 2, DATE = 3, WORK = 4, OTHER = 5;
    static final private int MUTE = AudioManager.RINGER_MODE_SILENT, NORMAL = AudioManager.RINGER_MODE_NORMAL, VIBRATE = AudioManager.RINGER_MODE_VIBRATE, OUTDOOR = 3;
    static final private String weekday[] = new String[]{"Mo","Tu","We","Th","Fr","Sa","Su"};
    static final private String Weekday[] = new String[]{"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
    static final private String Month[] = new String[]{"January","February","March","April","May","June","July","August","September","October","November","December"};
    private MyViewPager mPager;
    private ArrayList<View> listViews;
    private TextView t1,t2,Time,_Date;
    private ImageView cursor;
    private Button new_event,setting, wifi;
    private int currIndex = 0;
    private int bmpW;
    private int offset;
    private MyListView tmp,routine;
    private BaseAdapter tba, rba;
    private DBManager dbManager;
    private Intent sintent;
    private ClockAndCalendar cc;
    public Handler mainHandler;
    private Handler clockHandler;
    private SharedPreferences usrsetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Order.setScale(getResources().getDisplayMetrics().density);
        setContentView(R.layout.eventview);
        String testdate = "2015.9.1";
        SimpleDateFormat sss = new SimpleDateFormat("yyyy.MM.dd");
        sss.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        Date res = new Date();
        try {
            res = sss.parse(testdate);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("parse","fail");
        }
        Log.i("res",String.valueOf(res));

        usrsetting = getSharedPreferences("setting",MODE_PRIVATE);

        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                dbManager.changestatus(msg.arg1,msg.arg2==1);
                if (usrsetting.getBoolean("Appstatus",true)) {
                    restartService();
                }
            }
        };
        clockHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String time = (String)msg.obj;
                Time.setText(time.substring(0,time.indexOf("*")));
                _Date.setText(time.substring(time.indexOf("*") + 1, time.length()));
            }
        };

        sintent = new Intent(Intent.ACTION_RUN);
        sintent.setClass(this,MainService.class);
        new_event = (Button)findViewById(R.id.add_event);
        setting = (Button)findViewById(R.id.setting);
        wifi = (Button)findViewById(R.id.wifi);
        mPager = (MyViewPager)findViewById(R.id.event_list);
        t1 = (TextView)findViewById(R.id.temp);
        t2 = (TextView)findViewById(R.id.routine);
        cursor = (ImageView)findViewById(R.id.cursor);
        Time = (TextView)findViewById(R.id.time);
        _Date = (TextView)findViewById(R.id.date);
        cc = new ClockAndCalendar();
        cc.start();

        Order.initialize_lists();
        dbManager = new DBManager(this);
        dbManager.initialize(Order.telist,Order.relist);

        new_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsView.this,NewEventActivity.class);
                intent.putExtra("action","new");
                startActivityForResult(intent,0);
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EventsView.this,Setting.class));
            }
        });

        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EventsView.this, WifiHelperActivity.class));
            }
        });

        init_templist();
        init_routinelist();
        InitImageView();
        InitTextView();
        init_pager();
        if (usrsetting.getBoolean("Appstatus",true))
            startMainService();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cc.isrun = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Boolean delete = data.getBooleanExtra("delete",false);
        if (delete) {
            String action = data.getStringExtra("action");

            if (action.substring(0,1).compareTo("0") == 0) {
                dbManager.deleteEvent(Order.telist.get(Integer.valueOf(action.substring(1,action.length()))).getEid());
                int index = Integer.valueOf(action.substring(1));
                Order.telist.remove(index);
                tba.notifyDataSetChanged();
            } else {
                dbManager.deleteEvent(Order.relist.get(Integer.valueOf(action.substring(1,action.length()))).getEid());
                int index = Integer.valueOf(action.substring(1));
                Order.relist.remove(index);
                rba.notifyDataSetChanged();
            }
        } else {
            Boolean modify = data.getBooleanExtra("modify",false);
            if (modify) {
                if (data.getIntExtra("action",0) == 0) {
                    tba.notifyDataSetChanged();
                } else {
                    rba.notifyDataSetChanged();
                }
            } else {
                Boolean isTmpEventGened = data.getBooleanExtra("tmp",false);
                if (isTmpEventGened) {
                    tba.notifyDataSetChanged();
                } else {
                    rba.notifyDataSetChanged();
                }
            }
        }
        if (usrsetting.getBoolean("Appstatus",true))
            restartService();
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

    private void startMainService() {
        startService(sintent);
    }

    private void restartService() {
        stopService(sintent);
        startService(sintent);
    }

    private void init_pager() {
        mPager = (MyViewPager)findViewById(R.id.event_list);
        listViews = new ArrayList<>();
        listViews.add(tmp);
        listViews.add(routine);
        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitTextView() {
        t1.setOnClickListener(new title_onClickListener(0));
        t2.setOnClickListener(new title_onClickListener(1));
    }

    private void InitImageView() {
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.cursor)
                .getWidth();
        int screenW = (int)Order.dp2pix(390);
        offset = (screenW / 2 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        cursor.setImageMatrix(matrix);// 设置动画初始位置
    }

    private void init_templist() {
        tmp = new MyListView(EventsView.this);
        tmp.setPadding((int)Order.dp2pix(10),0,(int)Order.dp2pix(10),0);
        tmp.setDivider(getResources().getDrawable(R.drawable.alpha));
        //tmp.setDivider(getResources().getDrawable(R.drawable.divider));
        tmp.setDividerHeight((int)Order.dp2pix(10));
        final LayoutInflater inflater = LayoutInflater.from(this);
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        final SimpleDateFormat dsdf = new SimpleDateFormat("MM.dd");
        dsdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        tba = new BaseAdapter() {
            @Override
            public int getCount() {
                return Order.telist.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                if (convertView != null) {
                    view = convertView;
                } else {
                    view = inflater.inflate(R.layout.event_item,null);
                }
                TempEvent e = Order.telist.get(position);
                switch (e.getScene()) {
                    case MEETING:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.meeting));
                        break;
                    case CLASS:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.classroom));
                        break;
                    case SLEEP:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sleep));
                        break;
                    case DATE:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.date));
                        break;
                    case WORK:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.work));
                        break;
                    case OTHER:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.other));
                        break;
                    default:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.other));
                        break;
                }
                if (e.getTitle().length() != 0)
                    ((TextView)view.findViewById(R.id.title)).setText(e.getTitle());
                else
                    ((TextView)view.findViewById(R.id.title)).setText("Untitled Event");
                ((TextView)view.findViewById(R.id.duration)).setText(sdf.format(e.getStart())+" - "+sdf.format(e.getEnd())+" , "+dsdf.format(e.getDate()));
                ((ImageView)view.findViewById(R.id.eventstatus)).setImageBitmap(BitmapFactory.decodeResource(getResources(),Order.telist.get(position).isExpired()?R.drawable.expired:R.drawable.running));
                switch (e.getDur_mode()) {
                    case MUTE:
                        ((ImageView) view.findViewById(R.id.duration_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.mute));
                        break;
                    case NORMAL:
                        ((ImageView) view.findViewById(R.id.duration_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                    case VIBRATE:
                        ((ImageView) view.findViewById(R.id.duration_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.vibrate));
                        break;
                    default:
                        ((ImageView) view.findViewById(R.id.duration_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                }
                switch (e.getEnd_mode()) {
                    case MUTE:
                        ((ImageView) view.findViewById(R.id.end_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.mute));
                        break;
                    case NORMAL:
                        ((ImageView) view.findViewById(R.id.end_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                    case VIBRATE:
                        ((ImageView) view.findViewById(R.id.duration_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.vibrate));
                        break;
                    default:
                        ((ImageView) view.findViewById(R.id.end_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                }
                if (e.getNote().length() != 0) {
                    ((TextView) view.findViewById(R.id.note)).setText(e.getNote());
                } else {
                    ((TextView) view.findViewById(R.id.note)).setText("No comment...");
                }
                SwitchButton sb = (SwitchButton)view.findViewById(R.id.status);
                sb.setStatus(e.getStatus());
                sb.BindEvent = e;
                sb.context = EventsView.this;
                sb.handler = mainHandler;
                return view;
            }
        };
        tmp.setAdapter(tba);
        tmp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(EventsView.this, NewEventActivity.class);
                intent.putExtra("action","modify");
                intent.putExtra("type",0);
                intent.putExtra("position",position);
                startActivityForResult(intent, 0);
            }
        });
    }

    private void init_routinelist() {
        routine = new MyListView(EventsView.this);
        routine.setPadding((int)Order.dp2pix(10),0,(int)Order.dp2pix(10),0);
        routine.setDivider(getResources().getDrawable(R.drawable.alpha));
        //routine.setDivider(getResources().getDrawable(R.drawable.divider));
        routine.setDividerHeight((int)Order.dp2pix(10));
        final LayoutInflater inflater = LayoutInflater.from(this);
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        final SimpleDateFormat dsdf = new SimpleDateFormat("MM.dd");
        dsdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        rba = new BaseAdapter() {
            @Override
            public int getCount() {
                return Order.relist.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                if (convertView != null) {
                    view = convertView;
                } else {
                    view = inflater.inflate(R.layout.routineitem,null);
                }
                RoutineEvent e = Order.relist.get(position);
                switch (e.getScene()) {
                    case MEETING:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.meeting));
                        break;
                    case CLASS:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.classroom));
                        break;
                    case SLEEP:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sleep));
                        break;
                    case DATE:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.date));
                        break;
                    case WORK:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.work));
                        break;
                    case OTHER:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.other));
                        break;
                    default:
                        ((ImageView) view.findViewById(R.id.scene)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.other));
                        break;
                }
                if (e.getTitle().length() != 0)
                    ((TextView)view.findViewById(R.id.title)).setText(e.getTitle());
                else
                    ((TextView)view.findViewById(R.id.title)).setText("Untitled Event");
                ((TextView)view.findViewById(R.id.duration)).setText(sdf.format(e.getStart())+" - "+sdf.format(e.getEnd()));
                String days = new String();
                if (e.getWeekday() != null) {
                    String usrsetting = e.getWeekday();
                    boolean head = true;
                    for (int i = 0 ; i != 7 ; ++i) {
                        if (usrsetting.substring(i,i+1).compareTo("1") == 0) {
                            if (!head)
                                days += ", ";
                            days += weekday[i];
                            head = false;
                        }
                    }
                } else {
                    String sd = dsdf.format(e.getStart_day());
                    String itvl = String.valueOf(e.getInterval());
                    days = "Every " + itvl + " days from " + sd;
                }
                ((TextView)view.findViewById(R.id.days)).setText(days);
                switch (e.getDur_mode()) {
                    case MUTE:
                        ((ImageView) view.findViewById(R.id.duration_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.mute));
                        break;
                    case NORMAL:
                        ((ImageView) view.findViewById(R.id.duration_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                    default:
                        ((ImageView) view.findViewById(R.id.duration_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                }
                switch (e.getEnd_mode()) {
                    case MUTE:
                        ((ImageView) view.findViewById(R.id.end_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.mute));
                        break;
                    case NORMAL:
                        ((ImageView) view.findViewById(R.id.end_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                    default:
                        ((ImageView) view.findViewById(R.id.end_mode)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                }
                if (e.getNote().length() != 0) {
                    ((TextView) view.findViewById(R.id.note)).setText(e.getNote());
                } else {
                    ((TextView) view.findViewById(R.id.note)).setText("No comment...");
                }
                SwitchButton sb = (SwitchButton)view.findViewById(R.id.status);
                sb.setStatus(e.getStatus());
                sb.BindEvent = e;
                sb.context = EventsView.this;
                sb.handler = mainHandler;
                return view;
            }
        };
        routine.setAdapter(rba);
        routine.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(EventsView.this,NewEventActivity.class);
                intent.putExtra("action","modify");
                intent.putExtra("type",1);
                intent.putExtra("position",position);
                startActivityForResult(intent,0);
            }
        });
    }

    private class ClockAndCalendar extends Thread {
        public boolean isrun;
        private Time time;
        private int minute;


        public ClockAndCalendar() {
            super();
            isrun = true;
            time = new Time();
        }

        @Override
        public void run() {
            while (isrun) {
                try {
                    time.setToNow();
                    if (time.minute != minute) {
                        minute = time.minute;
                    }
                    Message msg = new Message();
                    msg.obj = String.valueOf(time.hour) + ":" + (time.minute >= 10?String.valueOf(time.minute):"0"+String.valueOf(time.minute)) + "*"
                            + Weekday[time.weekDay-1==-1?6:time.weekDay-1] + ", " + String.valueOf(time.monthDay) + "th, " + Month[time.month] + ", " + String.valueOf(time.year);
                    clockHandler.sendMessage(msg);
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public class title_onClickListener implements View.OnClickListener {
        private int index = 0;

        public title_onClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int one = offset * 2 + bmpW;

        @Override
        public void onPageSelected(int arg0) {
            Animation animation;
            animation = new TranslateAnimation(one*currIndex, one*arg0, 0, 0);
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            cursor.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
