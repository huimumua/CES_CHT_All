package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;

/**
 * Created by skysoft on 2018/3/30.
 */

public class CsaActivity extends BaseActivity {
    private static final String TAG = "CsaActivity";
    private CheckBox csa;
    private Button confirmBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csa);
        init();
    }

    private void init() {
        csa = (CheckBox) findViewById(R.id.csa_select);
        confirmBtn = (Button)findViewById(R.id.csa_confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(csa.isChecked()){
                    TcpClient.getInstance().getTransceiver().send("CSA:1");
                }else {
                    TcpClient.getInstance().getTransceiver().send("CSA:0");
                }
                Log.i(TAG, "~~~~~onClick:");
                finish();
            }
        });
    }
}
