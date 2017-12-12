package com.askey.mobile.zwave.control.guideSetting.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;

public class SetupHomeActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView mCableConnect,mWifiConnect, mRouter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_home);

        mCableConnect = (ImageView) findViewById(R.id.iv_cable_connect);
//        mWifiConnect = (ImageView) findViewById(R.id.iv_wifi_connect);
        mRouter = (ImageView) findViewById(R.id.iv_router);

        mCableConnect.setOnClickListener(this);
//        mWifiConnect.setOnClickListener(this);
        mRouter.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.iv_cable_connect:
                intent = new Intent(this, CableConnectOneActivity.class);
                startActivity(intent);
                break;
//            case R.id.iv_wifi_connect:
//                intent = new Intent(this, WifiSetupOneActivity.class);
//                startActivity(intent);
//                break;
            case R.id.iv_router:
                intent = new Intent(this, RouterConnectOneActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
