package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.rooms.schedule.CalendarScheduleView;
import com.askey.mobile.zwave.control.deviceContr.rooms.schedule.OnDateClick;

import java.util.ArrayList;
import java.util.List;


public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener{
    private ToggleButton btnSchedule;
    private TextView tvScheduleStatus;
    private CalendarScheduleView mSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        btnSchedule = (ToggleButton) findViewById(R.id.togBtn_schedule);
        tvScheduleStatus = (TextView) findViewById(R.id.tv_schedule_status);
        btnSchedule.setOnClickListener(this);
        initView();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.togBtn_schedule:
                if (btnSchedule.isChecked()) {
                } else {

                }
                break;
        }
    }


    private void initView() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(8);
        list.add(9);
        mSchedule = (CalendarScheduleView) findViewById(R.id.schedule);
//        mSchedule.setmSelectableDates(list);
        mSchedule.setOnDateClick(new OnDateClick() {
            @Override
            public void onClick(int year, int month, int data) {
//                Log.e("TestActivity", "日历onClick点击事件可用" + data);
            }
        });

        mSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TestActivity", "普通onClick点击事件可用");
            }
        });
    }

}
