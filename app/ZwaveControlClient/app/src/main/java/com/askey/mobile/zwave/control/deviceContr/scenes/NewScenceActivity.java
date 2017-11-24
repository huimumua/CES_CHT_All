package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;

public class NewScenceActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView mNewScenceIcon;
    private EditText mScenceName;
    private EditText mRoomName;
    private CheckBox mFavorite;
    private Button mContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_scence);

        initView();
    }

    private void initView() {
        mNewScenceIcon = (ImageView) findViewById(R.id.iv_new_scence_icon);
        mScenceName = (EditText) findViewById(R.id.et_scence_name);
        mRoomName = (EditText) findViewById(R.id.et_room_name);
        mFavorite = (CheckBox) findViewById(R.id.cb_favorite);
        mContinue = (Button) findViewById(R.id.btn_continue);

        mFavorite.setOnClickListener(this);
        mContinue.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cb_favorite:
                if (mFavorite.isChecked()) {
                } else {

                }
                break;
            case R.id.btn_continue:
                Intent intent = new Intent(this,DiningEveningActivity.class);////////
                startActivity(intent);
                break;

        }
    }
}
