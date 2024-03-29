package com.askey.mobile.zwave.control.guideSetting.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;

public class WifSetupThirdActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView autoWps,manuWifi, left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wif_setup_third);

        autoWps = (ImageView) findViewById(R.id.iv_auto_wps);
        manuWifi = (ImageView) findViewById(R.id.iv_manu_wifi);
        left = (ImageView) findViewById(R.id.iv_left);
        autoWps.setOnClickListener(this);
        manuWifi.setOnClickListener(this);
        left.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_auto_wps:
                Intent intent = new Intent(this, WpsOneActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_manu_wifi:
                break;
            case R.id.iv_left:
                finish();
                break;

        }
    }
}
