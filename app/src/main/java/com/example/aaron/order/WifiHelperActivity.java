package com.example.aaron.order;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Image;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class WifiHelperActivity extends Activity {

    static final private int MOST = 0, LAST = 1;
    static final private int MUTE = AudioManager.RINGER_MODE_SILENT, NORMAL = AudioManager.RINGER_MODE_NORMAL, VIBRATE = AudioManager.RINGER_MODE_VIBRATE, NOACTION = 4, OUTDOOR = 3;
    /** mute = 0, normal = 2, vibrate = 1 **/
    static final private int CONN[] = new int[]{MUTE,NORMAL,VIBRATE,NOACTION}, DISCONN[] = new int[]{NORMAL,VIBRATE,MUTE,NOACTION};
    private MyListView wifilist;
    private BaseAdapter baseAdapter;
    private ArrayList<wifi> wifiArrayList, unrecordwifilist;
    private LayoutInflater inflater;
    private LinearLayout newWifi;
    private boolean wifihelperstatus;
    private SwitchButton helperstatus;
    private Handler handler;
    private WIFIDBManager wifidbManager;
    private PopupWindow wifipicker;
    private CntFirstComparator cntFirstComparator;
    private LastFirstComparator lastFirstComparator;
    private ClickSwitchViewPager conn,disconn;
    private wifi selectedwifi;
    private SharedPreferences usrsetting;
    private int order;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifihelper);
        wifidbManager = new WIFIDBManager(this);
        usrsetting = this.getSharedPreferences("setting", MODE_PRIVATE);
        wifihelperstatus = usrsetting.getBoolean("helperstatus",true);

        cntFirstComparator = new CntFirstComparator();
        lastFirstComparator = new LastFirstComparator();

        wifiArrayList = new ArrayList<>();
        wifidbManager.initialize(wifiArrayList,0);      // recorded

        unrecordwifilist = new ArrayList<>();
        wifidbManager.initialize(unrecordwifilist, -1);     // connected but not recorded
        Collections.sort(unrecordwifilist,cntFirstComparator);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if ((int)msg.obj != 4)
                    wifidbManager.switchwifi(msg.getData().getString("ssid"),msg.getData().getBoolean("status"));
                else {
                    SharedPreferences.Editor editor = usrsetting.edit();
                    editor.putBoolean("helperstatus",msg.arg1 == 1);
                    editor.commit();
                }

            }
        };


        helperstatus = (SwitchButton)findViewById(R.id.helperstatus);
        helperstatus.setting_mode = 4;
        helperstatus.handler = handler;

        ((Button)findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiHelperActivity.this.finish();
            }
        });

        inflater = LayoutInflater.from(this);
        //wifiArrayList.add(new wifi("SJTU",10,System.currentTimeMillis(),MUTE,NORMAL,false));
        init_list();
        init_picker();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (wifipicker.isShowing()) {
                wifipicker.dismiss();
            } else {
                WifiHelperActivity.this.finish();
            }
        }
        return false;
    }

    private void init_list() {
        wifilist = (MyListView)findViewById(R.id.wifi);
        baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return wifiArrayList.size();
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
                View view = convertView!=null?convertView:inflater.inflate(R.layout.wifi_item, null);
                wifi w = wifiArrayList.get(position);
                ((TextView)view.findViewById(R.id.SSID)).setText(wifiArrayList.get(position).getSSID());
                switch (w.getConnectedMode()) {
                    case MUTE:
                        ((ImageView) view.findViewById(R.id.mode0)).setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mute));
                        break;
                    case NORMAL:
                        ((ImageView) view.findViewById(R.id.mode0)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                    case VIBRATE:
                        ((ImageView) view.findViewById(R.id.mode0)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.vibrate));
                        break;
                    case NOACTION:
                        ((ImageView) view.findViewById(R.id.mode0)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.noaction));
                        break;
                    default:
                        ((ImageView) view.findViewById(R.id.mode0)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                }
                switch (w.getDisconnectedMode()) {
                    case MUTE:
                        ((ImageView) view.findViewById(R.id.mode1)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.mute));
                        break;
                    case NORMAL:
                        ((ImageView) view.findViewById(R.id.mode1)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                    case VIBRATE:
                        ((ImageView) view.findViewById(R.id.mode1)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.vibrate));
                        break;
                    case NOACTION:
                        ((ImageView) view.findViewById(R.id.mode1)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.noaction));
                        break;
                    default:
                        ((ImageView) view.findViewById(R.id.mode1)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
                        break;
                }
                SwitchButton s = (SwitchButton) view.findViewById(R.id.status);
                s.setStatus(w.getStatus());
                s.setting_mode = 3;
                s.handler = handler;
                s.BindWifi = wifiArrayList.get(position);

                return view;
            }
        };
        wifilist.setAdapter(baseAdapter);
    }

    private void init_picker() {
        View v = inflater.inflate(R.layout.wifipicker,null);
        wifipicker = new PopupWindow(v,ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        final TextView ssid = (TextView)v.findViewById(R.id.ssid);
        final TextView most = (TextView)v.findViewById(R.id.cntfirst);
        final TextView last = (TextView)v.findViewById(R.id.lastfirst);
        v.findViewById(R.id.body).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do nothing
            }
        });
        v.findViewById(R.id.shadow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifipicker.isShowing())
                    wifipicker.dismiss();
            }
        });
        v.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifipicker.dismiss();
            }
        });
        v.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String SSID = (String)ssid.getText();
                if (SSID.compareTo("Pick One") == 0)
                    Toast.makeText(WifiHelperActivity.this,"You haven't select a WIFI yet!",Toast.LENGTH_SHORT).show();
                else {
                    int cmode = CONN[conn.getCurrentItem()];
                    int dmode = DISCONN[disconn.getCurrentItem()];
                    wifidbManager.setWifi(SSID,cmode,dmode);
                    selectedwifi.setConnectedMode(cmode);
                    selectedwifi.setDisconnectedMode(dmode);
                    selectedwifi.setStatus(true);
                    wifiArrayList.add(0, selectedwifi);
                    baseAdapter.notifyDataSetChanged();
                    unrecordwifilist.clear();
                    wifidbManager.initialize(unrecordwifilist,-1);
                    wifipicker.dismiss();
                }
            }
        });
        order = MOST;

        ListView lv = (ListView)v.findViewById(R.id.list);
        final BaseAdapter ba = new BaseAdapter() {
            @Override
            public int getCount() {
                return unrecordwifilist.size();
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
                View v = convertView != null?convertView:inflater.inflate(R.layout.raw_wifi_item,null);
                wifi w = unrecordwifilist.get(position);
                ((TextView)v.findViewById(R.id.ssid)).setText(w.getSSID());
                ((TextView)v.findViewById(R.id.cnt)).setText(String.valueOf(w.getConnectedTime()));
                long now = System.currentTimeMillis();
                long last = w.getLastconnectDate();
                long diff = (now - last)/1000;
                String time;
                if (diff < 60) {
                    time = String.valueOf(diff) + " secs ago";
                } else if (diff < 3600) {
                    time = String.valueOf(diff/60) + " mins ago";
                } else if (diff < 86400) {
                    time = String.valueOf(diff/3600) + " hours ago";
                } else {
                    time = String.valueOf(diff/86400) + " days ago";
                }
                ((TextView)v.findViewById(R.id.last)).setText(time);
                return v;
            }
        };
        lv.setAdapter(ba);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ssid.setText(unrecordwifilist.get(position).getSSID());
                selectedwifi = unrecordwifilist.get(position);
                ssid.setTextColor(getResources().getColor(R.color.bestgrey));
            }
        });

        most.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order != MOST) {
                    Collections.sort(unrecordwifilist,cntFirstComparator);
                    most.setBackgroundColor(Color.WHITE);
                    most.setTextColor(getResources().getColor(R.color.bestgrey));
                    last.setTextColor(Color.WHITE);
                    last.setBackgroundColor(getResources().getColor(R.color.lightblue));
                    ba.notifyDataSetChanged();
                    order = MOST;
                }
            }
        });
        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order != LAST) {
                    Collections.sort(unrecordwifilist,lastFirstComparator);
                    last.setBackgroundColor(Color.WHITE);
                    last.setTextColor(getResources().getColor(R.color.bestgrey));
                    most.setTextColor(Color.WHITE);
                    most.setBackgroundColor(getResources().getColor(R.color.lightblue));
                    ba.notifyDataSetChanged();
                    order = LAST;
                }
            }
        });

        conn = (ClickSwitchViewPager)v.findViewById(R.id.cmode);
        disconn = (ClickSwitchViewPager)v.findViewById(R.id.dmode);
        ImageView mute= new ImageView(this);
        mute.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.mute));
        ImageView sound = new ImageView(this);
        sound.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
        ImageView vibrate = new ImageView(this);
        vibrate.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.vibrate));
        ImageView noaction = new ImageView(this);
        noaction.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.noaction));
        ArrayList<View> modelist = new ArrayList<>();
        modelist.add(mute);
        modelist.add(sound);
        modelist.add(vibrate);
        modelist.add(noaction);
        ImageView mute2= new ImageView(this);
        mute2.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.mute));
        ImageView sound2 = new ImageView(this);
        sound2.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sound));
        ImageView vibrate2 = new ImageView(this);
        vibrate2.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.vibrate));
        ImageView noaction2 = new ImageView(this);
        noaction2.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.noaction));
        ArrayList<View> modelist2 = new ArrayList<>();
        modelist2.add(sound2);
        modelist2.add(vibrate2);
        modelist2.add(mute2);
        modelist2.add(noaction2);
        conn.setAdapter(new MyPagerAdapter(modelist));
        conn.viewcnt = modelist.size();
        conn.setCurrentItem(0);
        disconn.setAdapter(new MyPagerAdapter(modelist2));
        disconn.viewcnt = modelist2.size();
        disconn.setCurrentItem(0);

        newWifi = (LinearLayout)findViewById(R.id.newwifi);
        newWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifipicker.showAtLocation(findViewById(R.id.holder), Gravity.CENTER|Gravity.CENTER_HORIZONTAL,0,0);
            }
        });
    }

    private static class CntFirstComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            wifi lw = (wifi)lhs;
            wifi rw = (wifi)rhs;
            return rw.getConnectedTime() - lw.getConnectedTime();
        }
    }

    private static class LastFirstComparator implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            wifi lw = (wifi)lhs;
            wifi rw = (wifi)rhs;
            return (int)(rw.getLastconnectDate() - lw.getLastconnectDate());
        }
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
}
