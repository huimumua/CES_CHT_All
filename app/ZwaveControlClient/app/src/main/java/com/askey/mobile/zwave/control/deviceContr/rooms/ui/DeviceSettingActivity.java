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

import com.askey.mobile.zwave.control.R;

public class DeviceSettingActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText mDeviceName,mRoomName;
    private CheckBox mFavorite;
    private Button mDetail,mDelete;
    private String nodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setting);

        mDeviceName = (EditText) findViewById(R.id.et_device_name);
        mRoomName = (EditText) findViewById(R.id.et_room_name);
        mFavorite = (CheckBox) findViewById(R.id.cb_favorite);
        mDetail = (Button) findViewById(R.id.btn_detail);
        mDelete = (Button) findViewById(R.id.btn_delete_device);

        mFavorite.setOnClickListener(this);
        mDetail.setOnClickListener(this);
        mDelete.setOnClickListener(this);

        nodeId = getIntent().getStringExtra("nodeId");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cb_favorite:
                if (mFavorite.isChecked()) {
                } else {

                }
                break;
            case R.id.btn_detail:
                Intent intent = new Intent(this,DetailActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_delete_device:
                showDialog(this,"test","test");
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
