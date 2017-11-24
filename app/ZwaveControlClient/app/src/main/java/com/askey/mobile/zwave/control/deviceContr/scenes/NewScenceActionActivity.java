package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.ChooseDeviceActivity;

public class NewScenceActionActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView mAddSceneAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_scence_action);

        mAddSceneAction = (ImageView) findViewById(R.id.iv_add_action_scene);
        mAddSceneAction.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_add_action_scene:
                Intent intent = new Intent(this, ChooseDeviceActivity.class);
                startActivity(intent);
                break;
        }
    }
}
