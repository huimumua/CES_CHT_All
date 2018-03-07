package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.home.activity.addDevice.ChooseRoomActivity;

public class ExtenderDeviceActivity extends AppCompatActivity implements View.OnClickListener{
    private RelativeLayout rlSelectRoom;
    private String name;
    private String type;
    private String nodeId;
    private String room;
    private EditText mName;
    private TextView mRoom;
    private Button deleteDevice;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zwave_device);

        rlSelectRoom = (RelativeLayout) findViewById(R.id.rl_select_room);
        layout = (LinearLayout) findViewById(R.id.activity_zwave_device);
        mName = (EditText) findViewById(R.id.et_device_name);
        mRoom = (TextView) findViewById(R.id.tv_room);
        deleteDevice = (Button) findViewById(R.id.btn_delete_device);

        rlSelectRoom.setOnClickListener(this);
        deleteDevice.setOnClickListener(this);
        layout.setOnClickListener(this);

        nodeId = getIntent().getStringExtra("nodeId");
        type = getIntent().getStringExtra("type");
        name = getIntent().getStringExtra("displayName");
        room = getIntent().getStringExtra("room");
        hideSoftInputFromWindow();
        mName.setText(name);
        mRoom.setText(room);
        mName.setCursorVisible(false);

        mName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                    mName.setCursorVisible(true);
                }
                return false;
            }
        });
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    mName.setCursorVisible(true);
                } else {
                    mName.setCursorVisible(false);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_select_room:
                Intent intent = new Intent(this, ChooseRoomActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_delete_device:
                showDialog(this,name,nodeId);
                break;
            case R.id.activity_zwave_device:
                hideSoftInputFromWindow();
                break;
        }
    }

    void showDialog(Context context, String deviceName, String deviceId) {
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

    private void hideSoftInputFromWindow() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm.isActive()) {
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
