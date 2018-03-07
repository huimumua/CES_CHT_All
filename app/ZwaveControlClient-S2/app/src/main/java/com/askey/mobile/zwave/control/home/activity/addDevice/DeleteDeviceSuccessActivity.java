package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.askey.mobile.zwave.control.R;

public class DeleteDeviceSuccessActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_device_success);

        btn = (Button) findViewById(R.id.suc_btn);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.suc_btn:
                finish();
                break;
        }
    }


}
