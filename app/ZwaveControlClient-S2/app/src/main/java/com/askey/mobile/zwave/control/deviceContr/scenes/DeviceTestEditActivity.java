package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceTestEditActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private final String TAG = DeviceTestEditActivity.class.getSimpleName();
    private Button updata,revove;
    private Spinner deviceBootModeSpinner, deviceInclusionStateSpinner;
    private ProvisionInfo provisionInfo;
    private String DSKStr, networkInclusionState, nodeId, bootModeText, deviceInclusionstate;
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

        initDskInfo();

        updata.setOnClickListener(this);
        revove.setOnClickListener(this);
        deviceBootModeSpinner.setOnItemSelectedListener(this);
        deviceInclusionStateSpinner.setOnItemSelectedListener(this);

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
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

    private void initDskInfo() {
        provisionInfo = getIntent().getParcelableExtra("provisionInfo");

        nodeId = provisionInfo.getNodeId();
        DSKStr = provisionInfo.getDsk();
        bootModeText = provisionInfo.getDeviceBootMode();
        networkInclusionState = provisionInfo.getNetworkInclusionState();
        deviceInclusionstate = provisionInfo.getDeviceInclusionState();

        nodeIdTextView.setText(nodeId);
        networkInclusionStateTestView.setText(networkInclusionState);
        dskTestView.setText(DSKStr);

        SharedPreferences sharedPreferences = getSharedPreferences("zwave", Context.MODE_PRIVATE);
        String version = sharedPreferences.getString(DSKStr,"");

       /*
         当从SharedPreferences中读出来的为“00”的时候，表示设备为S0，
         S0的DSK不能选BootMode， 所以将deviceBootModeSpinner隐藏，不让用户选择
         然后直接状态写成 Device boot mode:Security 2
         */
        if(version.equals(SECURITY_2)){
            deviceBootModeTextView.setText(String.format(getResources().getString(R.string.device_boot_mode),bootModeText));
            deviceBootModeSpinner.setVisibility(View.GONE);
        } else {

        }

        //设置Spinner初始化的值与接口获取到的保持一致
        Log.i(TAG, "----------bootModeText"+bootModeText);
        if(bootModeText.equals("Smart Start")){
            deviceBootModeSpinner.setSelection(1);
        }else {
            deviceBootModeSpinner.setSelection(0);
        }

        Log.i(TAG, "----------deviceInclusionstate"+deviceInclusionstate);
        if(deviceInclusionstate.equals("State Pending")){
            deviceInclusionStateSpinner.setSelection(0);
        } else if(deviceInclusionstate.equals("State Passive")){
            deviceInclusionStateSpinner.setSelection(1);
        } else {
            deviceInclusionStateSpinner.setSelection(2);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_updata:
                showWaitingDialog();

                String deviceInclusionStateStr = deviceInclusionStateSpinner.getSelectedItem().toString();
                String deviceBootModeStr = deviceBootModeSpinner.getSelectedItem().toString();

                String comm = LocalMqttData.editProvisionListEntry(DSKStr, deviceInclusionStateStr, deviceBootModeStr);
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, comm);
                ScenesFragment.newInstance().addDskResult();//通知UI更新ProvisionList
                finish();
                break;
            case R.id.btn_remove_dsk:
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.rmProvisionListEntry("rmProvisionListEntry", DSKStr));
                ScenesFragment.newInstance().addDskResult();//通知UI更新ProvisionList
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
