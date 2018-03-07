package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.util.ToastShow;

public class NewScenceActivity extends BaseActivity implements View.OnClickListener{
    private ImageView mNewScenceIcon;
    private EditText mScenceName;
    private EditText mRoomName;
    private CheckBox mFavorite;
    private Button mContinue;
    private String sceneIcon,sceneName,isFavorite,roomName;

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
        mScenceName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s!=null){
                    if(s.length()>16){
                        ToastShow.showShort(mContext,getResources().getString(R.string.scene_name_length));
                        String str = mScenceName.getText().toString().trim();
                        str = str.substring(0,15);
                        mScenceName.setText(str);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cb_favorite:
                if (mFavorite.isChecked()) {
                    isFavorite = "true";
                } else {
                    isFavorite = "false";
                }
                break;
            case R.id.btn_continue:
                sceneName = mScenceName.getText().toString().trim();
                if(sceneName.equals("")){
                    ToastShow.showShort(mContext,getResources().getString(R.string.scene_name));
                    return;
                }
                Intent intent = new Intent(this,SceneActivity.class);////////
                intent.putExtra("sceneIcon","ic_scene_item");
                intent.putExtra("sceneName",sceneName);
                intent.putExtra("isFavorite",isFavorite);
                startActivity(intent);
                break;

        }
    }
}
