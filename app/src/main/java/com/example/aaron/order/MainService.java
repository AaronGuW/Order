package com.example.aaron.order;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Aaron on 2015/11/18.
 */
public class MainService extends Service {
    static final private int manually = 0, immediately = 1, nextday = 2, threedays = 3, sevendays = 4, never = 5;

    private IBinder binder = new MainService.LocalBinder();
    private DBManager dbManager;
    private WIFIDBManager wifidbManager;
    private boolean paused = false,active = true;
    private AudioManager audio;
    private int RingerMode;
    private Date now;
    private SimpleDateFormat sdf;
    private String nows;
    private ArrayList<TempEvent> telist;
    private ArrayList<RoutineEvent> relist;
    private ArrayList<Event> eventlist;
    private SimpleDateFormat daysdf;
    private String today,ntoday;
    private Context context;
    private SharedPreferences usrsetting;
    private int delete_period;
    private boolean mof,ef,work;

    private Thread maintask = new Thread(new Runnable(){
        @Override
        public void run(){
            Looper.prepare();

            try {
                while (active) {
                    work = true;
                    if (!ef) {
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                            String ssid = wifiManager.getConnectionInfo().getSSID();
                            if (wifidbManager.is_wifi_on(ssid))
                                work = false;
                        }
                    }
                    if (work) {
                        RingerMode = audio.getRingerMode();
                        now = new Date(System.currentTimeMillis());
                        nows = sdf.format(now);
                        ntoday = daysdf.format(now);
                        if (ntoday.compareTo(today) != 0) {
                            today = ntoday;
                            Order.ArrangeEvents(telist, relist, eventlist);
                        }
                        Log.i("now", nows);

                        for (int i = 0; i != eventlist.size(); ++i) {
                            Event e = eventlist.get(i);
                            String start = sdf.format(e.getStart()), end = sdf.format(e.getEnd());
                            if (e.getStart().compareTo(e.getEnd()) < 0  || e.firstday) {
                                if (nows.compareTo(start) >= 0 && nows.compareTo(end) < 0) {
                                    Log.i("mode change", "catch");
                                    if (RingerMode != e.getDur_mode()) {
                                        audio.setRingerMode(e.getDur_mode());
                                    }
                                } else if (nows.compareTo(end) >= 0) {
                                    Log.i("mode change end", "catch");
                                    audio.setRingerMode(e.getEnd_mode());
                                    eventlist.remove(i);
                                    i--;
                                }
                            } else {
                                if (nows.compareTo(end) < 0) {
                                    Log.i("mode change", "catch");
                                    if (RingerMode != e.getDur_mode()) {
                                        audio.setRingerMode(e.getDur_mode());
                                    }
                                } else if (nows.compareTo(end) >= 0) {
                                    Log.i("mode change end", "catch");
                                    audio.setRingerMode(e.getEnd_mode());
                                    eventlist.remove(i);
                                    i--;
                                }
                            }
                        }
                    }
                    Thread.sleep(1000);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        //返回本地服务
        MainService getService(){
            return MainService.this;
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i("service", "onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        Log.i("service","start!");
        super.onCreate();
        context = this;
        dbManager = new DBManager(context);
        wifidbManager = new WIFIDBManager(context);
        usrsetting = getSharedPreferences("setting",MODE_PRIVATE);

        delete_period = usrsetting.getInt("period",0);
        mof = usrsetting.getBoolean("MOF",false);
        ef = usrsetting.getBoolean("EF",true);

        telist = new ArrayList<>();
        relist = new ArrayList<>();
        eventlist = new ArrayList<>();

        dbManager.initialize(telist,relist);
        daysdf = new SimpleDateFormat("yyyy-MM-dd");
        daysdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        Order.ArrangeEvents(telist,relist,eventlist);
        Log.i("size",String.valueOf(eventlist.size()));
        Date dtoday = new Date(System.currentTimeMillis());
        today = daysdf.format(dtoday);
        sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        maintask.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        active = false;
    }

}
