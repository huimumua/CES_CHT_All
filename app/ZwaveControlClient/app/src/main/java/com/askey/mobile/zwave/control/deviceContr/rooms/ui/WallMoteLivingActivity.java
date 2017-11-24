package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;

public class WallMoteLivingActivity extends BaseDeviceActivity {
    private TextView tvMoteExplain,tvShowExplain,tvSetExplain,tvDetail;
    private LinearLayout llMoteLiving;
    private ImageView setting,info;
    private Button btnShowKey,btnSetKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall_mote_living);

        setting = (ImageView) findViewById(R.id.iv_setting);
        info = (ImageView) findViewById(R.id.iv_info);
        deviceName = (TextView) findViewById(R.id.tv_device_name);
        setting.setOnClickListener(this);
        info.setOnClickListener(this);
        deviceName.setText("Wall Mote Living");

        tvMoteExplain = (TextView) findViewById(R.id.tv_mote_explain);
        tvShowExplain = (TextView) findViewById(R.id.tv_show_key_explain);
        tvSetExplain = (TextView) findViewById(R.id.tv_set_explain);
        tvDetail = (TextView) findViewById(R.id.tv_detail);
        llMoteLiving = (LinearLayout) findViewById(R.id.activity_plug);

        btnShowKey = (Button) findViewById(R.id.btn_show_key);
        btnSetKey = (Button) findViewById(R.id.btn_set_key);
        btnShowKey.setOnClickListener(this);
        btnSetKey.setOnClickListener(this);
    }

    @Override
    public void info() {
        super.info();
        if (isDetailStatus) {

            llMoteLiving.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            tvMoteExplain.setVisibility(View.INVISIBLE);
            tvShowExplain.setVisibility(View.INVISIBLE);
            tvSetExplain.setVisibility(View.INVISIBLE);
            tvDetail.setVisibility(View.INVISIBLE);

        } else {

            llMoteLiving.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
            tvMoteExplain.setVisibility(View.VISIBLE);
            tvShowExplain.setVisibility(View.VISIBLE);
            tvSetExplain.setVisibility(View.VISIBLE);
            tvDetail.setVisibility(View.VISIBLE);

        }

    }

    @Override
    public void setting() {
        super.setting();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.btn_show_key) {
            Intent intent = new Intent(this,AvailableActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_set_key) {
            Intent intent = new Intent(this,SetupKeyActivity.class);
            intent.putExtra("nodeId",getIntent().getStringExtra("nodeId"));
            startActivity(intent);
        }
    }
}
