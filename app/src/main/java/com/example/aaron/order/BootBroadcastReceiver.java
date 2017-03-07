package com.example.aaron.order;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.HashMap;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String BOOT = "android.intent.action.BOOT_COMPLETED";
    private static final String Unknown = "<unknown ssid>";
    private static boolean connected = false;
    private static long lastconn = 0;
    private static String lastconnssid;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BOOT)){
            SharedPreferences usrsetting = context.getSharedPreferences("setting",Context.MODE_PRIVATE);
            if (usrsetting.getBoolean("Appstatus",true)) {
                Intent i = new Intent(Intent.ACTION_RUN);
                i.setClass(context, MainService.class);
                context.startService(i);
            }
        }
        else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){//wifi连接上与否
            Log.i("WIFI","Status Changed");
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
                connected = false;
                if (context.getSharedPreferences("setting",Context.MODE_PRIVATE).getBoolean("helperstatus",true)) {
                    if (lastconnssid != null) {
                        WIFIDBManager db = new WIFIDBManager(context);
                        int dmode = db.endwork(lastconnssid);
                        db.close();
                        if (dmode != -1 && dmode != 4) {
                            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                            int RingerMode = audioManager.getRingerMode();
                            if (RingerMode != dmode) {
                                audioManager.setRingerMode(dmode);
                            }
                        }
                        Log.i("disconnect from",lastconnssid);
                        lastconnssid = null;
                    }
                }
                Log.i("WIFI","Disconnected");
            }
            else if (info.getState().equals(NetworkInfo.State.DISCONNECTING))
                connected = false;
            else if (info.getState().equals(NetworkInfo.State.CONNECTED)){
                if (!connected) {
                    connected = true;
                    long now = System.currentTimeMillis();
                    if (now - lastconn > 8000) {
                        lastconn = now;
                        if (context.getSharedPreferences("setting",Context.MODE_PRIVATE).getBoolean("helperstatus",true)) {
                            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                            String ssid = wifiInfo.getSSID();
                            if (ssid.compareTo(Unknown) != 0) {
                                WIFIDBManager db = new WIFIDBManager(context);
                                int cmode = db.work(ssid);
                                if (cmode != -1 && cmode != 4) {
                                    lastconnssid = ssid;
                                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                    int RingerMode = audioManager.getRingerMode();
                                    if (RingerMode != cmode) {
                                        audioManager.setRingerMode(cmode);
                                    }
                                }
                                db.exist_and_count(ssid);
                                db.close();
                                //获取当前wifi名称
                                Log.i("Connect to", wifiInfo.getSSID());
                            }
                        }
                    }
                }
            }

        }
    }
}
