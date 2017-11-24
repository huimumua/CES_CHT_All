package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.askey.mobile.zwave.control.R;

public class VacationModeActivity extends AppCompatActivity implements View.OnClickListener{
    private ToggleButton btnVacationMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_mode);

        btnVacationMode = (ToggleButton) findViewById(R.id.togBtn_vacation_mode);
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
