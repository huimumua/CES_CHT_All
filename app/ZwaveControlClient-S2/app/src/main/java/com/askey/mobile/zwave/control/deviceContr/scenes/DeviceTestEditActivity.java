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

import com.askey.mobile.zwave.control.R;
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

public class DeviceTestEditActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener{
    private final String TAG = DeviceTestEditActivity.class.getSimpleName();
    private Button back,ok;
    private EditText dsk,networkInclusionState;
    private Spinner deviceBootMode,deviceInclusionState;
    private ProvisionInfo provisionInfo;

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
            mqttMessageResult(result);

        }
    };

    private void editProvision() {
        Log.d(TAG, "======editProvision=======");
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
    }

    private void mqttMessageResult(String mqttResult) {


    }

    private void init() {
        dsk.setText(provisionInfo.getDsk());
        String bootModeText = provisionInfo.getDeviceBootMode();
        if (bootModeText.equals("PL_BOOT_MODE_S2")) {
            deviceBootMode.setSelection(0);
        } else if (bootModeText.equals("PL_BOOT_MODE_SMART_STRT")) {
            deviceBootMode.setSelection(1);
        } else if (bootModeText.equals("PL_INCL_STS_PENDING")) {
            deviceBootMode.setSelection(2);
        }
        String inclusionStateText = provisionInfo.getDeviceInclusionState();
        if (inclusionStateText.equals("PL_INCL_STS_PENDING")) {
            deviceInclusionState.setSelection(0);
        } else if (inclusionStateText.equals("PL_INCL_STS_PASSIVE")) {
            deviceInclusionState.setSelection(1);
        } else if (inclusionStateText.equals("PL_INCL_STS_IGNORED")) {
            deviceInclusionState.setSelection(2);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_ok:
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.editProvisionListEntry(dsk.getText().toString(),
                        deviceInclusionState.getSelectedItem().toString(),
                        deviceBootMode.getSelectedItem().toString()));
                finish();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
