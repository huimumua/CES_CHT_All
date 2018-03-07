package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.askey.mobile.zwave.control.R;

public class ScheduleRunTimeActivity extends AppCompatActivity implements View.OnClickListener{
    private ToggleButton btnSchedule;
    private ImageView mDelete;
    private ImageView mAddSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_run_time);

        btnSchedule = (ToggleButton) findViewById(R.id.togBtn_schedule);
        mDelete = (ImageView) findViewById(R.id.iv_delete);
        mAddSchedule = (ImageView) findViewById(R.id.iv_add_schedule);

        btnSchedule.setOnClickListener(this);
        mDelete.setOnClickListener(this);
        mAddSchedule.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.togBtn_schedule:
                if (btnSchedule.isChecked()) {
                } else {

                }
                break;
            case R.id.iv_delete:

                break;
            case R.id.iv_add_schedule:
                break;
        }
    }
}
