package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;

public class BaseDeviceActivity extends AppCompatActivity implements View.OnClickListener{
    public TextView deviceName;
    public ImageView consumption,notify,schedule,vacation,trigger,setting,info;
    public ImageView consumptionSetted,notifySetted,scheduleSetted,vacationSetted,triggerSetted;
    public Intent intent;
    public boolean isDetailStatus = true;
    public String nodeId;
    public String type;
    public String name;
    public String room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initView() {
        deviceName = (TextView) findViewById(R.id.tv_device_name);
        nodeId = getIntent().getStringExtra("nodeId");//需要穿nodeId

//        consumption = (ImageView) findViewById(R.id.iv_consumption);
        notify = (ImageView) findViewById(R.id.iv_notify);
        schedule = (ImageView) findViewById(R.id.iv_schedule);
        vacation = (ImageView) findViewById(R.id.iv_vacation);
//        trigger = (ImageView) findViewById(R.id.iv_trigger);

//        consumptionSetted = (ImageView) findViewById(R.id.iv_consumption_setted);
        notifySetted = (ImageView) findViewById(R.id.iv_notify_setted);
        scheduleSetted = (ImageView) findViewById(R.id.iv_schedule_setted);
        vacationSetted = (ImageView) findViewById(R.id.iv_vacation_setted);
//        triggerSetted = (ImageView) findViewById(R.id.iv_trigger_setted);

        setting = (ImageView) findViewById(R.id.iv_setting);
        info = (ImageView) findViewById(R.id.iv_info);

//        consumption.setOnClickListener(this);
        notify.setOnClickListener(this);
        schedule.setOnClickListener(this);
        vacation.setOnClickListener(this);
//        trigger.setOnClickListener(this);

        setting.setOnClickListener(this);
        info.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.iv_consumption:
//                consume();
//                break;
            case R.id.iv_notify:
                notifyMessage();
                break;
            case R.id.iv_schedule:
                schedule();
                break;
            case R.id.iv_vacation:
                vacation();
                break;
//            case R.id.iv_trigger:
//                trigger();
//                break;
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
        intent.putExtra("nodeId",nodeId);
        intent.putExtra("type",type);
        intent.putExtra("name",name);
        intent.putExtra("room",room);
        startActivity(intent);

    }


    public void trigger() {
        intent = new Intent(this,TriggerActivity.class);
        intent.putExtra("nodeId",nodeId);
        startActivity(intent);
    }

    public void vacation() {
        intent = new Intent(this,VacationModeActivity.class);
        intent.putExtra("nodeId",nodeId);
        startActivity(intent);
    }

    public void schedule() {
        intent = new Intent(this,ScheduleActivity.class);
        intent.putExtra("nodeId",nodeId);
        startActivity(intent);
    }

    public void notifyMessage() {
        intent = new Intent(this,NotifyActivity.class);
        intent.putExtra("nodeId",nodeId);
        startActivity(intent);
    }

    public void consume() {
        intent = new Intent(this,ConsumptionActivity.class);
        intent.putExtra("nodeId",nodeId);
        startActivity(intent);
    }

    public void initSetted() {
    }


}
