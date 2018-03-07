package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.askey.mobile.zwave.control.R;

public class SetTimerActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView timerStatus;
    private TextView mTimer;
    private ToggleButton btnTimer;
    private Chronometer mChronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_timer);

        timerStatus = (TextView) findViewById(R.id.tv_timer_status);
        mTimer = (TextView) findViewById(R.id.tv_time);
        btnTimer = (ToggleButton) findViewById(R.id.togBtn_timer);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

            }
        });
        btnTimer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.togBtn_timer:
                if (btnTimer.isChecked()) {
                } else {

                }
                break;
        }
    }
}
