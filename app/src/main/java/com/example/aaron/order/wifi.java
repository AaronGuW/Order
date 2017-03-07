package com.example.aaron.order;

import android.media.AudioManager;

import java.util.Date;

/**
 * Created by Aaron on 2015/12/11.
 */
public class wifi {
    static final private int MUTE = AudioManager.RINGER_MODE_SILENT, NORMAL = AudioManager.RINGER_MODE_NORMAL, VIBRATE = AudioManager.RINGER_MODE_VIBRATE, OUTDOOR = 3;
    private String SSID;
    private int ConnectedTime;
    private long LastconnectDate;
    private int connectedMode, disconnectedMode;
    private boolean status;


    public wifi(String ssid, int t, long d, int cm, int dm, boolean s) {
        SSID = ssid;
        ConnectedTime = t;
        LastconnectDate = d;
        connectedMode = cm;
        disconnectedMode = dm;
        status = s;
    }

    public String getSSID() { return SSID; }
    public int getConnectedMode() { return connectedMode; }
    public int getDisconnectedMode() { return disconnectedMode; }
    public boolean getStatus() { return status; }
    public void setStatus(boolean s) { status = s; }
    public int getConnectedTime() { return ConnectedTime; }
    public long getLastconnectDate() { return LastconnectDate; }
    public void setConnectedMode(int m) { connectedMode = m; }
    public void setDisconnectedMode(int m) { disconnectedMode = m; }
}
