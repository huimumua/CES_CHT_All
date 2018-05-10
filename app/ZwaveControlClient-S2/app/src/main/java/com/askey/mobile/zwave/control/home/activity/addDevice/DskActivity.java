package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;

/**
 * Created by skysoft on 2018/3/30.
 */

public class DskActivity extends BaseActivity {
    private static final String TAG = "DskActivity";
    private EditText enterDskEditText;
    private Button enterDskBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsk);
        init();
    }

    private void init() {
        enterDskEditText = (EditText) findViewById(R.id.enter_dsk_edit);
        enterDskBtn = (Button)findViewById(R.id.button_send_dsk);
        enterDskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dsk = enterDskEditText.getText().toString();
                if(dsk.length() == 47 || dsk.length() == 5){
                    String dskTcpMessage = "dsk:" + dsk;
                    TcpClient.getInstance().getTransceiver().send(dskTcpMessage);
                    finish();
                } else {
                    Toast.makeText(mContext,"DSK input error!",Toast.LENGTH_LONG).show();
                }
                Log.i(TAG, "~~~~~onClick:");
            }
        });
    }
}
