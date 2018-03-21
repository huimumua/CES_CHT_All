package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;

/**
 * Created by skysoft on 2017/10/31.
 */

public class SceneActivity extends AppCompatActivity implements View.OnClickListener{
    private Button mScenceAction;
    public TextView deviceName;
    public ImageView schedule,timmer,setting,info;
    public ImageView scheduleSetted,timmerSetted;
    private Intent intent;
    public boolean isDetailStatus = true;
    private String sceneIcon,sceneName,isFavorite,roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_evening);

        initView();
        initSetted();
        deviceName.setText(getResources().getString(R.string.dining_evening));


        Intent intent = getIntent();
        sceneIcon = intent.getStringExtra("sceneIcon");
        sceneName = intent.getStringExtra("sceneName");
        isFavorite = intent.getStringExtra("isFavorite");

        mScenceAction = (Button) findViewById(R.id.btn_scene_action);
        mScenceAction.setOnClickListener(this);


    }
    public void initView() {
        deviceName = (TextView) findViewById(R.id.tv_scene_name);

        schedule = (ImageView) findViewById(R.id.iv_schedule);
        timmer = (ImageView) findViewById(R.id.iv_timmer);

        scheduleSetted = (ImageView) findViewById(R.id.iv_schedule_setted);
        timmerSetted = (ImageView) findViewById(R.id.iv_timmer_setted);

        setting = (ImageView) findViewById(R.id.iv_setting);
        info = (ImageView) findViewById(R.id.iv_info);

        schedule.setOnClickListener(this);
        timmer.setOnClickListener(this);

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
            case R.id.iv_setting:
                setting();
                break;
            case R.id.iv_info:
                info();
                break;
            case R.id.btn_scene_action:
                Intent intent = new Intent(this,SceneActionsActivity.class);
                intent.putExtra("from", SceneActivity.class.getSimpleName());
                intent.putExtra("sceneIcon","ic_scene_item");
                intent.putExtra("sceneName",sceneName);
                intent.putExtra("isFavorite",isFavorite);
                startActivity(intent);
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
        intent = new Intent(this,DeviceTestActivity.class);
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
