package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import com.askey.mobile.zwave.control.R;

public class TimerActivity extends AppCompatActivity implements View.OnClickListener{
    private TimePicker timePicker;
    private RelativeLayout mTimer;
    private Intent fromIntent;
    private String fromActivity;
    private int hour = 0;
    private int min = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void initView() {
        fromIntent = getIntent();

        mTimer = (RelativeLayout) findViewById(R.id.rl_timer);
        timePicker = (TimePicker) findViewById(R.id.time_picker);
        mTimer.setOnClickListener(this);
        timePicker.setIs24HourView(true);

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
//            timePicker.setHour(22);获取出来设置
        } else {
//            timePicker.setCurrentHour(22);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_timer:
                int currentApiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
                    hour = timePicker.getHour();
                    min = timePicker.getMinute();
                } else {
                    hour = timePicker.getCurrentHour();
                    min = timePicker.getCurrentMinute();
                }
                Intent intent = null;

                fromActivity = fromIntent.getStringExtra("from");

                intent = new Intent(TimerActivity.this, ActionSummaryActivity.class);


                intent.putExtra("from", TimerActivity.class.getSimpleName());
                intent.putExtra("nodeId", fromIntent.getStringExtra("nodeId"));
                intent.putExtra("type", fromIntent.getStringExtra("type"));
                intent.putExtra("name",fromIntent.getStringExtra("name"));
                intent.putExtra("action", fromIntent.getStringExtra("action"));
                intent.putExtra("timer", hour + ":" + min);//timmer 要改
                startActivity(intent);

                break;
        }
    }

}
