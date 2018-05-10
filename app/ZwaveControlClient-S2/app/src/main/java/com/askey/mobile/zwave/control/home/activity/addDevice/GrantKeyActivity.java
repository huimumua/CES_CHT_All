package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;

/**
 * Created by skysoft on 2018/4/2.
 */

public class GrantKeyActivity extends BaseActivity {
    private static final String TAG = "GrantKeyActivity";

    private String safeLevel;
    private CheckBox security0, security2Key0, security2Key1, security2Key2;
    private Button safeButton;

    private static final String GRANT_KEY_0X01 = "1";
    private static final String GRANT_KEY_0X02 = "2";
    private static final String GRANT_KEY_0X03 = "3";
    private static final String GRANT_KEY_0X04 = "4";
    private static final String GRANT_KEY_0X05 = "5";
    private static final String GRANT_KEY_0X06 = "6";
    private static final String GRANT_KEY_0X07 = "7";
    private static final String GRANT_KEY_0X80 = "128";
    private static final String GRANT_KEY_0X81 = "129";
    private static final String GRANT_KEY_0X82 = "130";
    private static final String GRANT_KEY_0X83 = "131";
    private static final String GRANT_KEY_0X84 = "132";
    private static final String GRANT_KEY_0X85 = "133";
    private static final String GRANT_KEY_0X86 = "134";
    private static final String GRANT_KEY_0X87 = "135";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant_key);
        init();
    }

    private void init() {
        safeLevel = getIntent().getStringExtra("SAFE_LEVEL").trim();//从AddSmartStartActivity传过来的安全等级
        Log.i(TAG, "=========grantKeys=" + safeLevel + "==");
        safeButton = (Button) findViewById(R.id.safe_confirm);

        security0 = (CheckBox) findViewById(R.id.safe_s0);// OX80
        security2Key0 = (CheckBox) findViewById(R.id.safe_s2_0);//0x01 S2 Class 0 Unauthenticated
        security2Key1 = (CheckBox) findViewById(R.id.safe_s2_1);//0x02 S2 Class 1 Authenticated
        security2Key2 = (CheckBox) findViewById(R.id.safe_s2_2);//0x04 S2 Class 2 Access Control

        if (safeLevel.equals(GRANT_KEY_0X80)) {
            security2Key0.setVisibility(View.GONE);
            security2Key1.setVisibility(View.GONE);
            security2Key2.setVisibility(View.GONE);
        } else if (safeLevel.equals(GRANT_KEY_0X81)) {
            security2Key1.setVisibility(View.GONE);
            security2Key2.setVisibility(View.GONE);
        } else if (safeLevel.equals(GRANT_KEY_0X82)) {
            security2Key0.setVisibility(View.GONE);
            security2Key2.setVisibility(View.GONE);
        } else if (safeLevel.equals(GRANT_KEY_0X83)) {
            security0.setVisibility(View.GONE);
            security2Key0.setVisibility(View.GONE);
            security2Key1.setVisibility(View.GONE);
            security2Key2.setVisibility(View.VISIBLE);
        } else if (safeLevel.equals(GRANT_KEY_0X84)) {
            Log.i(TAG, "-------GRANT_KEY_0X84");
            security0.setVisibility(View.VISIBLE);
            security2Key0.setVisibility(View.GONE);
            security2Key1.setVisibility(View.GONE);
            security2Key2.setVisibility(View.VISIBLE);
        } else if (safeLevel.equals(GRANT_KEY_0X85)) {
            security0.setVisibility(View.VISIBLE);
            security2Key0.setVisibility(View.VISIBLE);
            security2Key1.setVisibility(View.GONE);
            security2Key2.setVisibility(View.VISIBLE);
        } else if (safeLevel.equals(GRANT_KEY_0X86)) {
            security0.setVisibility(View.VISIBLE);
            security2Key0.setVisibility(View.GONE);
            security2Key1.setVisibility(View.VISIBLE);
            security2Key2.setVisibility(View.VISIBLE);
        } else if (safeLevel.equals(GRANT_KEY_0X87)) {

        } else if (safeLevel.equals(GRANT_KEY_0X01)) {
            security0.setVisibility(View.VISIBLE);
            security2Key0.setVisibility(View.GONE);
            security2Key1.setVisibility(View.GONE);
            security2Key2.setVisibility(View.GONE);
        } else if (safeLevel.equals(GRANT_KEY_0X02)) {
            security0.setVisibility(View.GONE);
            security2Key0.setVisibility(View.GONE);
            security2Key1.setVisibility(View.VISIBLE);
            security2Key2.setVisibility(View.GONE);
        } else if (safeLevel.equals(GRANT_KEY_0X03)) {
            security0.setVisibility(View.GONE);
            security2Key0.setVisibility(View.VISIBLE);
            security2Key1.setVisibility(View.VISIBLE);
            security2Key2.setVisibility(View.GONE);
        } else if (safeLevel.equals(GRANT_KEY_0X04)) {
            security0.setVisibility(View.GONE);
            security2Key0.setVisibility(View.GONE);
            security2Key1.setVisibility(View.GONE);
            security2Key2.setVisibility(View.VISIBLE);
        } else if (safeLevel.equals(GRANT_KEY_0X05)) {
            security0.setVisibility(View.GONE);
            security2Key0.setVisibility(View.VISIBLE);
            security2Key1.setVisibility(View.GONE);
            security2Key2.setVisibility(View.VISIBLE);
        } else if (safeLevel.equals(GRANT_KEY_0X06)) {
            security0.setVisibility(View.GONE);
            security2Key0.setVisibility(View.GONE);
            security2Key1.setVisibility(View.VISIBLE);
            security2Key2.setVisibility(View.VISIBLE);
        } else if (safeLevel.equals(GRANT_KEY_0X07)) {
            security0.setVisibility(View.GONE);
            security2Key0.setVisibility(View.VISIBLE);
            security2Key1.setVisibility(View.VISIBLE);
            security2Key2.setVisibility(View.VISIBLE);
        }

        if((int)security0.getVisibility() != View.VISIBLE){
            security0.setChecked(false);
        }

        if((int)security2Key0.getVisibility() != View.VISIBLE){
            security2Key0.setChecked(false);
        }

        if((int)security2Key1.getVisibility() != View.VISIBLE){
            security2Key1.setChecked(false);
        }

        if((int)security2Key2.getVisibility() != View.VISIBLE){
            security2Key2.setChecked(false);
        }

        safeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a = (security0.isChecked() ? 80 : 0)
                        + (security2Key0.isChecked() ? 1 : 0)
                        + (security2Key1.isChecked() ? 2 : 0)
                        + (security2Key2.isChecked() ? 4 : 0);

                String grantKeys = "GrantKeys:"+String.valueOf(a);
                Log.i(TAG, "===============key = " + a + " "+grantKeys);

                //int 转成 String,并且以 Grant Keys:value的形式发送给TCP
                TcpClient.getInstance().getTransceiver().send(grantKeys);
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
