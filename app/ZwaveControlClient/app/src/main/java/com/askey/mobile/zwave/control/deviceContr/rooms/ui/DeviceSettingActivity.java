package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.home.activity.addDevice.ChooseRoomActivity;

public class DeviceSettingActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText mDeviceName;
    private TextView mRoomName;
    private CheckBox mFavorite;
    private Button mDelete;
    private String nodeId;
    private String type;
    private String deviceName;
    private String room;
    private ImageView ivIcon;
    private RelativeLayout mSelectRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setting);

        mDeviceName = (EditText) findViewById(R.id.et_device_name);
        mRoomName = (TextView) findViewById(R.id.tv_room);
        mFavorite = (CheckBox) findViewById(R.id.cb_favorite);
        mDelete = (Button) findViewById(R.id.btn_delete_device);
        ivIcon = (ImageView) findViewById(R.id.iv_icon);
        mSelectRoom = (RelativeLayout) findViewById(R.id.rl_select_room);

        mFavorite.setOnClickListener(this);
        mSelectRoom.setOnClickListener(this);
        mDelete.setOnClickListener(this);

        nodeId = getIntent().getStringExtra("nodeId");
        type = getIntent().getStringExtra("type");
        deviceName = getIntent().getStringExtra("name");
        room = getIntent().getStringExtra("room");

        if ("PLUG".equals(type)) {
            ivIcon.setImageResource(R.drawable.vector_drawable_ic_80_bigger);
        } else if ("BULB".equals(type)) {
            ivIcon.setImageResource(R.drawable.vector_drawable_ic_79_bigger);
        } else if("WALLMOTE".equals(type)) {
            ivIcon.setImageResource(R.drawable.vector_drawable_ic_96_bigger);
        }

        mDeviceName.setText(deviceName);
        mRoomName.setText(room);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cb_favorite:
                if (mFavorite.isChecked()) {
                } else {
                }
                break;

            case R.id.btn_delete_device:
                showDialog(this,deviceName,nodeId);
                break;
            case R.id.rl_select_room:
                Intent intent = new Intent(this,ChooseRoomActivity.class);
                startActivity(intent);
                break;
        }
    }

    void showDialog(Context context,String deviceName,String deviceId) {
        final AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(this);
        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.setCancelable(false);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_device, null);
        ImageView icon = (ImageView) view.findViewById(R.id.iv_icon);
        icon.setImageResource(R.drawable.vector_drawable_ic_92);
        alertDialog.setContentView(view);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button proceed = (Button) view.findViewById(R.id.btn_proceed);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }
}
