package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by skysoft on 2018/6/25.
 */

public class VersionActivity extends BaseActivity {

    private static final String TAG = "VersionActivity";
    private Button exit;
    private TextView appVersion,
            servicesVersion,
            libraryType,
            protocolVersion,
            firmwareVersion,
            hardwareVersion,
            firmware1Version,
            firmware2Version;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);
        appVersion = (TextView) findViewById(R.id.app_version);
        servicesVersion = (TextView) findViewById(R.id.services_version);
        libraryType = (TextView) findViewById(R.id.library_type);
        protocolVersion = (TextView) findViewById(R.id.protocol_version);
        firmwareVersion = (TextView) findViewById(R.id.firmware_version);
        hardwareVersion = (TextView) findViewById(R.id.hardware_version);
        firmware1Version = (TextView) findViewById(R.id.firmware_1_version);
        firmware2Version = (TextView) findViewById(R.id.firmware_2_version);


        appVersion.setText(String.format(getResources().getString(R.string.app_version), "ISP0001MG.0.1.1"));//ISP0001MG.0.1.1

        exit = (Button) findViewById(R.id.version_exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson("getVersion"));
        showWaitingDialog();
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    message(result);
                }
            });
        }
    };

    private void message(String result) {

        try {
            JSONObject jsonObject = new JSONObject(result);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String messageType = reportedObject.optString("MessageType");
            if ("Version Messages".equals(messageType)) {
                String libType = reportedObject.optString("Z-wave Library Type");
                String proVersion = reportedObject.optString("Z-wave protocol Version");
                String firVersion = reportedObject.optString("Firmware 0 Version");
                String harVersion = reportedObject.optString("Hardware version");
                String firmware1VersionStr = reportedObject.optString("Firmware 1 Version");
                String firmware2VersionStr = reportedObject.optString("Firmware 2 Version");
                String servicesVersionStr = reportedObject.optString("services version");

                servicesVersion.setText(String.format(getResources().getString(R.string.services_version), servicesVersionStr));//ISP0001DA.281120.1.1
                libraryType.setText(String.format(getResources().getString(R.string.library_type), libType));
                protocolVersion.setText(String.format(getResources().getString(R.string.protocol_version), proVersion));
                firmwareVersion.setText(String.format(getResources().getString(R.string.firmware_version), firVersion));
                hardwareVersion.setText(String.format(getResources().getString(R.string.hardware_version), harVersion));
                firmware1Version.setText(String.format(getResources().getString(R.string.firmware1_version), firmware1VersionStr));
                firmware2Version.setText(String.format(getResources().getString(R.string.firmware2_version), firmware2VersionStr));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
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
