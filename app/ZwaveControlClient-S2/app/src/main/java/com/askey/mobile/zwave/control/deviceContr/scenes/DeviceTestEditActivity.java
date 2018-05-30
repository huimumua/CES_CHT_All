package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.ProvisionInfo;
import com.askey.mobile.zwave.control.home.fragment.ScenesFragment;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceTestEditActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private final String TAG = DeviceTestEditActivity.class.getSimpleName();
    private Button updata,revove;
    private Spinner deviceBootModeSpinner, deviceInclusionStateSpinner;
    private ProvisionInfo provisionInfo;
    private String DSKStr;
    private TextView networkInclusionStateTestView, dskTestView, nodeIdTextView, deviceBootModeTextView;

    private final String PL_INCL_STS_PENDING  = "0";
    private final String PL_INCL_STS_PASSIVE  = "2";
    private final String PL_INCL_STS_IGNORED  = "3";

    private static final String SMART_START = "01";
    private static final String SECURITY_2 = "00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_test_edit);


        updata = (Button) findViewById(R.id.btn_updata);
        revove = (Button) findViewById(R.id.btn_remove_dsk);

        nodeIdTextView = (TextView) findViewById(R.id.node_id);
        networkInclusionStateTestView = (TextView) findViewById(R.id.network_inclusionn_state);
        dskTestView = (TextView) findViewById(R.id.dsk_info);
        deviceBootModeTextView = (TextView) findViewById(R.id.device_boot_mode_textview);

        deviceBootModeSpinner = (Spinner) findViewById(R.id.device_boot_mode);
        deviceInclusionStateSpinner = (Spinner) findViewById(R.id.device_inclusion_state);


        updata.setOnClickListener(this);
        revove.setOnClickListener(this);
        deviceBootModeSpinner.setOnItemSelectedListener(this);
        deviceInclusionStateSpinner.setOnItemSelectedListener(this);

        provisionInfo = getIntent().getParcelableExtra("provisionInfo");
        DSKStr = provisionInfo.getDsk();

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        showWaitingDialog();
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getProvisionListEntry("getProvisionListEntry",DSKStr));
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(TAG, "============mqttMessage====" + result);

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

                    String mInterface= reportedObject.optString("Interface");
                    if(mInterface.equals("addProvisionListEntry")){
                        String result= reportedObject.optString("Result");
                        if(result.equals("true")){
                            finish();
                        }else {
                            Toast.makeText(getApplication(), "updata failed !", Toast.LENGTH_LONG).show();
                        }
                    }

                    if(mInterface.equals("rmProvisionListEntry")){
                        String result= reportedObject.optString("Result");
                        if(result.equals("true")){
                            finish();
                        }else {
                            Toast.makeText(getApplication(), "Remove  Provision List Entry Failed !", Toast.LENGTH_LONG).show();
                        }
                    }

                    String messageType = reportedObject.optString("MessageType");
                    if(messageType.equals("Provision List Report")){
                        String networkInclusion = reportedObject.optString("Network inclusion state");
                        JSONObject inclusion_state_Object = new JSONObject(networkInclusion);
                        int nodeId = inclusion_state_Object.optInt("Node Id");
                        String networkInclusionState = inclusion_state_Object.optString("Status");
                        String bootModeText = reportedObject.optString("Device Boot Mode");
                        String deviceInclusionstate = reportedObject.optString("Device Inclusion state");

                        nodeIdTextView.setText(String.valueOf(nodeId));
                        networkInclusionStateTestView.setText(networkInclusionState);
                        dskTestView.setText(DSKStr);
                        deviceBootModeTextView.setText(String.format(getResources().getString(R.string.device_boot_mode),bootModeText));

                        if(deviceInclusionstate.equals("State Pending")){
                            deviceInclusionStateSpinner.setSelection(0);
                        } else if(deviceInclusionstate.equals("State Passive")){
                            deviceInclusionStateSpinner.setSelection(1);
                        } else {
                            deviceInclusionStateSpinner.setSelection(2);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_updata:
                showWaitingDialog();
                String deviceInclusionStateStr = deviceInclusionStateSpinner.getSelectedItem().toString();
                //String deviceBootModeStr = deviceBootModeSpinner.getSelectedItem().toString();

                String comm = LocalMqttData.editProvisionListEntry(DSKStr, deviceInclusionStateStr, "Smart Start");
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, comm);
                ScenesFragment.newInstance().addDskResult();//??ProvisionList
                //finish();
                break;
            case R.id.btn_remove_dsk:
                showWaitingDialog();
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.rmProvisionListEntry("rmProvisionListEntry", DSKStr));
                ScenesFragment.newInstance().addDskResult();//??ProvisionList
                //finish();
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
