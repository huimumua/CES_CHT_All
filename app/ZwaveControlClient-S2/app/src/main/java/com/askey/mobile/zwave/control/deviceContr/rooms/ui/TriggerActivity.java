package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.askey.mobile.zwave.control.R;

public class TriggerActivity extends AppCompatActivity implements View.OnClickListener{
    private ToggleButton btnLeave,btnEnter;
    private Button notifySetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);

        btnLeave = (ToggleButton) findViewById(R.id.togBtn_leaving_off);
        btnEnter = (ToggleButton) findViewById(R.id.togBtn_entering_on);
        notifySetting = (Button) findViewById(R.id.btn_notify_setting);

        btnLeave.setOnClickListener(this);
        btnEnter.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.togBtn_leaving_off:
                break;
            case R.id.togBtn_entering_on:
                break;
            case R.id.btn_notify_setting:
                Intent intent = new Intent(this, NotificationSetting.class);
                startActivity(intent);
                break;
        }
    }
}
