package com.askey.mobile.zwave.control.guideSetting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;

public class RouterConnectThirdActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView right,left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_connect_third);

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
                Intent intent = new Intent(this,RouterConnectFourActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
                break;
        }
    }
}
