package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.ProvisionInfo;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceTestEditActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener{
    private final String TAG = DeviceTestEditActivity.class.getSimpleName();
    private Button back,ok;
    private EditText dsk,networkInclusionState;
    private Spinner deviceBootMode,deviceInclusionState;
    private ProvisionInfo provisionInfo;
    private String DSKStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_test_edit);

        provisionInfo = getIntent().getParcelableExtra("provisionInfo");

        back = (Button) findViewById(R.id.btn_back);
        ok = (Button) findViewById(R.id.btn_ok);
        dsk = (EditText) findViewById(R.id.et_dsk);
        networkInclusionState = (EditText) findViewById(R.id.network_inclusionn_state);
        deviceBootMode = (Spinner) findViewById(R.id.device_boot_mode);
        deviceInclusionState = (Spinner) findViewById(R.id.device_inclusion_state);

        back.setOnClickListener(this);
        ok.setOnClickListener(this);
        deviceBootMode.setOnItemSelectedListener(this);
        deviceInclusionState.setOnItemSelectedListener(this);

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        init();
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(TAG, "=mqttMessageArrived=>=message=" + result);

            if (result.contains("desired")) {
                return;
            }
            stopWaitDialog();
            mqttMessageResult(result);

        }
    };


    private void mqttMessageResult(final String mqttResult) {

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(mqttResult);
                    String reported = jsonObject.optString("reported");
                    JSONObject reportedObject = new JSONObject(reported);
                    String mInterface = reportedObject.optString("Interface");
                    String result = reportedObject.optString("Result");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void init() {
        dsk.setText(provisionInfo.getDsk());
        DSKStr = provisionInfo.getDsk();
        String bootModeText = provisionInfo.getDeviceBootMode();
//        if (bootModeText.equals("PL_BOOT_MODE_S2")) {
//            deviceBootMode.setSelection(0);
//        } else if (bootModeText.equals("PL_BOOT_MODE_SMART_STRT")) {
//            deviceBootMode.setSelection(1);
//        }
        String inclusionStateText = provisionInfo.getDeviceInclusionState();
//        if (inclusionStateText.equals("PL_INCL_STS_PENDING")) {
//            deviceInclusionState.setSelection(0);
//        } else if (inclusionStateText.equals("PL_INCL_STS_PASSIVE")) {
//            deviceInclusionState.setSelection(1);
//        } else if (inclusionStateText.equals("PL_INCL_STS_IGNORED")) {
//            deviceInclusionState.setSelection(2);
//        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_ok:
                showWaitingDialog();
                String dskStr = dsk.getText().toString();
                String deviceInclusionStateStr = deviceInclusionState.getSelectedItem().toString();
                if (deviceInclusionStateStr.equals("PL_INCL_STS_PENDING")) {
                    deviceInclusionStateStr=String.valueOf(0);
                } else if (deviceInclusionStateStr.equals("PL_INCL_STS_PASSIVE")) {
                    deviceInclusionStateStr=String.valueOf(1);
                } else if (deviceInclusionStateStr.equals("PL_INCL_STS_IGNORED")) {
                    deviceInclusionStateStr=String.valueOf(2);
                }
                String deviceBootModeStr = deviceBootMode.getSelectedItem().toString();
                if (deviceBootModeStr.equals("PL_BOOT_MODE_S2")) {
                    deviceBootModeStr=String.valueOf(0);
                } else if (deviceBootModeStr.equals("PL_BOOT_MODE_SMART_STRT")) {
                    deviceBootModeStr=String.valueOf(1);
                }
                String comm = LocalMqttData.editProvisionListEntry(DSKStr,dskStr, deviceInclusionStateStr, deviceBootModeStr);
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic,comm );
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unrigister();
    }

    private void unrigister() {
        if (mMqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }
}
