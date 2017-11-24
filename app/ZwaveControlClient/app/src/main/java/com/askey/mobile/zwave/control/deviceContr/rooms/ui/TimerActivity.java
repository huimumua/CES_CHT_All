package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.RelativeLayout;

import com.askey.mobile.zwave.control.R;

public class TimerActivity extends AppCompatActivity implements View.OnClickListener{
    private Chronometer mChronometer;
    private RelativeLayout mTimer;
    private Intent fromIntent;
    private String fromActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        initView();
    }

    private void initView() {
        fromIntent = getIntent();

        mTimer = (RelativeLayout) findViewById(R.id.rl_timer);
        mTimer.setOnClickListener(this);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_timer:

                Intent intent = null;

                fromActivity = fromIntent.getStringExtra("from");

                intent = new Intent(TimerActivity.this, ActionSummaryActivity.class);

                intent.putExtra("from", TimerActivity.class.getSimpleName());
                intent.putExtra("nodeId", fromIntent.getStringExtra("nodeId"));
                intent.putExtra("endpointId", fromIntent.getStringExtra("endpointId"));
                intent.putExtra("groupId", fromIntent.getStringExtra("groupId"));
                intent.putExtra("arr",fromIntent.getStringExtra("arr"));
                intent.putExtra("type", fromIntent.getStringExtra("type"));
                intent.putExtra("action", fromIntent.getStringExtra("action"));
                intent.putExtra("timer", fromIntent.getStringExtra("timer"));//timmer 要改
                startActivity(intent);
                break;
        }
    }

}
