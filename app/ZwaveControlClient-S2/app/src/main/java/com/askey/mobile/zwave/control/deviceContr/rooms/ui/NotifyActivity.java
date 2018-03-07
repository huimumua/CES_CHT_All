package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.askey.mobile.zwave.control.R;

public class NotifyActivity extends AppCompatActivity implements View.OnClickListener{
    private Button notifySetting;
    private Switch btnHighConsumption,btnAtypicalConsumption,btnDeviceSwitched/*btnSwitchSwitched*/;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        notifySetting = (Button) findViewById(R.id.btn_notify_setting);

        initView();
    }

    private void initView() {
        btnHighConsumption = (Switch) findViewById(R.id.togBtn_high_consumption);
        btnAtypicalConsumption = (Switch) findViewById(R.id.togBtn_atypical_consumption);
        btnDeviceSwitched = (Switch) findViewById(R.id.togBtn_device_switched);
//        btnSwitchSwitched = (ToggleButton) findViewById(R.id.togBtn_switch_switched);

        notifySetting.setOnClickListener(this);
        btnHighConsumption.setOnClickListener(this);
        btnAtypicalConsumption.setOnClickListener(this);
        btnDeviceSwitched.setOnClickListener(this);
//        btnSwitchSwitched.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_notify_setting:
                Intent intent = new Intent(this,NotificationSetting.class);
                startActivity(intent);
                break;
            case R.id.togBtn_high_consumption:
                if (btnHighConsumption.isChecked()) {

                }
                break;
            case R.id.togBtn_atypical_consumption:
                if (btnAtypicalConsumption.isChecked()) {

                }
                break;
            case R.id.togBtn_device_switched:
                if (btnDeviceSwitched.isChecked()) {

                }
                break;
//            case R.id.togBtn_switch_switched:
//                if (btnSwitchSwitched.isChecked()) {
//
//                }
//                break;
        }
    }
}
