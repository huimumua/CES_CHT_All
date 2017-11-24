package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.ConsumptionActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.DeviceSettingActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.NotifyActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.ScheduleActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.SetupKeyActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.TriggerActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.VacationModeActivity;

public class BaseScenceActivity extends AppCompatActivity implements View.OnClickListener{
    public TextView deviceName;
    public ImageView schedule,timmer,trigger,setting,info;
    public ImageView scheduleSetted,timmerSetted,triggerSetted;
    private Intent intent;
    public boolean isDetailStatus = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initView() {
        deviceName = (TextView) findViewById(R.id.tv_scene_name);

        schedule = (ImageView) findViewById(R.id.iv_schedule);
        timmer = (ImageView) findViewById(R.id.iv_timmer);
        trigger = (ImageView) findViewById(R.id.iv_trigger);

        scheduleSetted = (ImageView) findViewById(R.id.iv_schedule_setted);
        timmerSetted = (ImageView) findViewById(R.id.iv_timmer_setted);
        triggerSetted = (ImageView) findViewById(R.id.iv_trigger_setted);

        setting = (ImageView) findViewById(R.id.iv_setting);
        info = (ImageView) findViewById(R.id.iv_info);

        schedule.setOnClickListener(this);
        timmer.setOnClickListener(this);
        trigger.setOnClickListener(this);

        setting.setOnClickListener(this);
        info.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_schedule:
                schedule();
                break;
            case R.id.iv_timmer:
                timmer();
                break;
            case R.id.iv_trigger:
                trigger();
                break;
            case R.id.iv_setting:
                setting();
                break;
            case R.id.iv_info:
                info();
                break;
        }
    }

    public void info() {
        if (isDetailStatus) {
            isDetailStatus = false;
        } else {
            isDetailStatus = true;
        }
    }

    public void setting() {
        intent = new Intent(this,DeviceSettingActivity.class);
        startActivity(intent);
    }


    public void trigger() {
        intent = new Intent(this,TriggerActivity.class);
        startActivity(intent);
    }

    public void timmer() {
        intent = new Intent(this,SetTimerActivity.class);
        startActivity(intent);
    }

    public void schedule() {
        intent = new Intent(this,ScheduleRunTimeActivity.class);
        startActivity(intent);
    }

    public void initSetted() {
    }
}
