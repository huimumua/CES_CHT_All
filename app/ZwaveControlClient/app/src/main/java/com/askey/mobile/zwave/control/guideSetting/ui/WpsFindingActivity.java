package com.askey.mobile.zwave.control.guideSetting.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.askey.mobile.zwave.control.R;

public class WpsFindingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wps_finding);

        //test
        Intent intent = new Intent(this,WpsFoundActivity.class);
        startActivity(intent);
        finish();
    }
}
