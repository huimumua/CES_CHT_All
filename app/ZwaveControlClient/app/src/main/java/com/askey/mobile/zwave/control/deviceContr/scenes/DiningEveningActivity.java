package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;

/**
 * Created by skysoft on 2017/10/31.
 */

public class DiningEveningActivity extends BaseScenceActivity {
    private Button mScenceAction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_evening);

        initView();
        initSetted();
        deviceName.setText(getResources().getString(R.string.dining_evening));

        mScenceAction = (Button) findViewById(R.id.btn_scene_action);
        mScenceAction.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_scene_action:
                Intent intent = new Intent(this,NewScenceActionActivity.class);
                startActivity(intent);
                break;

        }
    }
}
