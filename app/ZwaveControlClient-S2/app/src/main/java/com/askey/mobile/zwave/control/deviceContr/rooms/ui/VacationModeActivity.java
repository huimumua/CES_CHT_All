package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;

import com.askey.mobile.zwave.control.R;

public class VacationModeActivity extends AppCompatActivity implements View.OnClickListener{
    private Switch btnVacationMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_mode);

        btnVacationMode = (Switch) findViewById(R.id.togBtn_vacation_mode);
        btnVacationMode.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.togBtn_vacation_mode:
                if (btnVacationMode.isChecked()) {
                } else {

                }
                break;
        }
    }
}
