package com.example.aaron.order;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by Aaron on 2015/12/8.
 */
public class Setting extends Activity {
    private static final String period_option[] = new String[]{"Manually","Immediately","Next Day","3 Days Later","7 Days Later"};

    private Button back;
    private SwitchButton mof_status,ef_status,appstatus;
    private TextView period, mof, ef;
    private Handler handler;
    private PopupWindow PeriodPicker;
    private SharedPreferences usrsetting;
    private RadioGroup PeriodList;
    private RadioButton radioButtons[];
    private int RadioButtonId[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        usrsetting = this.getSharedPreferences("setting", MODE_PRIVATE);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Intent intent = new Intent(Setting.this,MainService.class);
                if ((int)msg.obj == 1) {
                    SharedPreferences.Editor editor = usrsetting.edit();
                    if (mof_status.getStatus()) {
                        mof.setText("On");
                        editor.putBoolean("MOF",true);
                    } else {
                        mof.setText("Off");
                        editor.putBoolean("MOF",false);
                    }
                    editor.commit();
                    if (appstatus.getStatus()) {
                        stopService(intent);
                        startService(intent);
                    }
                } else if ((int)msg.obj == 2) {
                    SharedPreferences.Editor editor = usrsetting.edit();
                    if (appstatus.getStatus()) {
                        startService(intent);
                        editor.putBoolean("Appstatus",true);
                    } else {
                        stopService(intent);
                        editor.putBoolean("Appstatus",false);
                    }
                    editor.commit();
                } else if ((int)msg.obj == 5) {
                    SharedPreferences.Editor editor = usrsetting.edit();
                    if (ef_status.getStatus()) {
                        editor.putBoolean("EF",true);
                        ef.setText("Event First");
                    } else {
                        editor.putBoolean("EF",false);
                        ef.setText("WIFI First");
                    }
                    editor.commit();
                    if (appstatus.getStatus()) {
                        stopService(intent);
                        startService(intent);
                    }
                }
            }
        };

        back = (Button)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Setting.this.finish();
            }
        });
        mof_status = (SwitchButton)findViewById(R.id.mof_status);
        mof_status.handler = handler;
        mof_status.setting_mode = 1;
        ef_status = (SwitchButton)findViewById(R.id.ef_status);
        ef_status.handler = handler;
        ef_status.setting_mode = 5;
        appstatus = (SwitchButton)findViewById(R.id.Appstatus);
        appstatus.handler = handler;
        appstatus.setting_mode = 2;
        period = (TextView)findViewById(R.id.delete_period);
        mof = (TextView)findViewById(R.id.mof);
        ef = (TextView)findViewById(R.id.ef);

        period.setText(period_option[usrsetting.getInt("period",0)]);
        mof.setText(usrsetting.getBoolean("MOF",false)?"On":"Off");
        mof_status.setStatus(usrsetting.getBoolean("MOF", false));
        ef.setText(usrsetting.getBoolean("EF", true) ? "Event First" : "WIFI First");
        ef_status.setStatus(usrsetting.getBoolean("EF", true));
        appstatus.setStatus(usrsetting.getBoolean("Appstatus",true));

        init_pop();
        init_settingitem();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (PeriodPicker.isShowing()) {
                PeriodPicker.dismiss();
            } else {
                Setting.this.finish();
            }
        }
        return false;
    }

    private void init_settingitem() {
        LinearLayout eed = (LinearLayout)findViewById(R.id.eed);
        eed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PeriodPicker.showAtLocation(findViewById(R.id.holder), Gravity.CENTER|Gravity.CENTER_HORIZONTAL,0,0);
            }
        });
    }

    private void init_pop() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.deletion_period_picker,null);
        v.findViewById(R.id.body).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do Nothing
            }
        });
        PeriodList = (RadioGroup)v.findViewById(R.id.period_list);
        RadioButtonId  = new int[]{R.id.manually,R.id.immediately,R.id.nextday,R.id.threedays,R.id.sevendays,R.id.never};
        radioButtons = new RadioButton[6];
        for (int i = 0 ; i != 6 ; ++i) {
            radioButtons[i] = (RadioButton)v.findViewById(RadioButtonId[i]);
        }
        radioButtons[usrsetting.getInt("period",0)].setChecked(true);

        PeriodPicker = new PopupWindow(v,ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        (v.findViewById(R.id.shadow)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PeriodPicker.isShowing()) {
                    int selectedid = PeriodList.getCheckedRadioButtonId();
                    SharedPreferences.Editor editor = usrsetting.edit();
                    for (int i = 0 ; i != 5 ; ++i) {
                        if (selectedid == RadioButtonId[i]) {
                            radioButtons[i].setChecked(true);
                            period.setText(period_option[i]);
                            editor.putInt("period",i);
                            editor.commit();
                            break;
                        }
                    }
                    PeriodPicker.dismiss();
                }
            }
        });
    }
}
