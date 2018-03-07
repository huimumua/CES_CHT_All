package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;

import com.askey.mobile.zwave.control.R;

public class NotificationSetting extends AppCompatActivity implements View.OnClickListener{
    private Switch btnPushNotification,btnEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);

        btnPushNotification = (Switch) findViewById(R.id.togBtn_push_consumption);
        btnEmail = (Switch) findViewById(R.id.togBtn_email);

        btnPushNotification.setOnClickListener(this);
        btnEmail.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.togBtn_push_consumption:
                if (btnPushNotification.isChecked()) {

                } else {

                }
                break;
            case R.id.togBtn_email:
                if (btnEmail.isChecked()) {

                } else {

                }
                break;
        }
    }
}
