package com.askey.mobile.zwave.control.guideSetting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;

public class RouterConnectSecondActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView left, right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_connect_second);

        left = (ImageView) findViewById(R.id.iv_left);
        right = (ImageView) findViewById(R.id.iv_right);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left:
                finish();
                break;
            case R.id.iv_right:
                Intent intent = new Intent(this,RouterConnectThirdActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
