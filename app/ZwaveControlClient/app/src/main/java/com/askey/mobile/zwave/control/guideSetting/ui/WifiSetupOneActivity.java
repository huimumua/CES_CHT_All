package com.askey.mobile.zwave.control.guideSetting.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;

public class WifiSetupOneActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView right,left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setup_one);
        right = (ImageView) findViewById(R.id.iv_right);
        left = (ImageView) findViewById(R.id.iv_left);
        right.setOnClickListener(this);
        left.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left:
                finish();
                break;
            case R.id.iv_right:
                Intent intent = new Intent(this,WifiSetupSecondActivity.class);
                startActivity(intent);
                break;
        }
    }
}
