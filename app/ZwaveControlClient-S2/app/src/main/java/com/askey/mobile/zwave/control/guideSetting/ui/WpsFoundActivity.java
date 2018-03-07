package com.askey.mobile.zwave.control.guideSetting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;

public class WpsFoundActivity extends BaseActivity implements View.OnClickListener{
    private ImageView right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wps_found);

        right = (ImageView) findViewById(R.id.iv_right);
        right.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_right:
                Intent intent = new Intent(this, DeviceGuideActivity.class);
                startActivity(intent);
                break;
        }
    }
}
