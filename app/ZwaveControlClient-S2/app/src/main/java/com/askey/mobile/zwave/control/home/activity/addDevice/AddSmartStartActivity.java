package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.qrcode.CaptureActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.welcome.ui.GuideActivity;
import com.askey.mobile.zwave.control.welcome.ui.NotificationActivity;
import com.fasterxml.jackson.databind.ser.Serializers;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by skysoft on 2018/3/8.
 */

public class AddSmartStartActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "AddSmartStartActivity";
    private EditText showQRCode;
    private ImageButton scanQr;
    private Button addDevice;
    //private CheckBox pin,dsk,no_dsk;
    private RadioGroup radioGroup;
    private RadioButton pin, dsk, no_dsk;

    private String brand = "";
    private String deviceType = "";
    private String nodeId = "";

    private String dskCode = "";
    private String qrCode = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_smart_start);
        initView();


        //TcpClient.getInstance().rigister(tcpReceive);
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unrigister();
    }

    private void initView() {
        brand = getIntent().getStringExtra("brand");
        deviceType = getIntent().getStringExtra("deviceType");

        showQRCode = (EditText) findViewById(R.id.show_qr_code);
        //showQRCode.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        scanQr = (ImageButton) findViewById(R.id.scan_qr_code);
        scanQr.setOnClickListener(this);
        addDevice = (Button) findViewById(R.id.smart_start_add);
        addDevice.setOnClickListener(this);

        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new MyradioBtnListener());
        pin = (RadioButton) findViewById(R.id.pin);
        dsk = (RadioButton) findViewById(R.id.dsk);
        no_dsk = (RadioButton) findViewById(R.id.no_dsk);

        if (pin.isChecked()) {
            showQRCode.setEnabled(true);
        } else {
            showQRCode.setEnabled(false);
        }
    }

    class MyradioBtnListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.pin:
                    showQRCode.setEnabled(true);
                    //showQRCode.setFocusable(true);
                    Log.i(TAG, "========onCheckedChanged:R.id.pin ");
                    break;
                case R.id.dsk:
                    showQRCode.setEnabled(false);
                    Log.i(TAG, "========onCheckedChanged:R.id.dsk ");
                    //showQRCode.setFocusable(false);
                    break;
                case R.id.no_dsk:
                    Log.i(TAG, "========onCheckedChanged:R.id.no_dsk ");
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_qr_code:
                Intent smartStartintent = new Intent(AddSmartStartActivity.this, CaptureActivity.class);
                //startActivityForResult打开activity是为了配合onActivityResult返回数据，requestCode：1是区分标识
                startActivityForResult(smartStartintent, 1);
                break;
            case R.id.smart_start_add:
                String editText = showQRCode.getText().toString();
                if (pin.isChecked()) {
                    dskCode = editText;
                    qrCode = "";
                }
                if (editText.length() == 47) {
                    showWaitingDialog();
                    //调用mqtt接口，带参数
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.addProvisionList(dskCode, "47", qrCode));
                } else {
                    if (editText == null || editText.length() < 1) {
                        Toast.makeText(mContext, "Please enter a qr code.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mContext, "Error in qr code.", Toast.LENGTH_LONG).show();
                    }
                }
                Log.i(TAG, "onClick: smart_start_add" + dskCode);
                break;
        }
    }

    private void installSuccessIntent() {
        Intent installintent = new Intent(AddSmartStartActivity.this, InstallSuccessActivity.class);
        //intent如果不传数据， 在InstallSuccessActivity中的网络接口会报错
        installintent.putExtra("brand", brand);
        installintent.putExtra("deviceType", deviceType);
        installintent.putExtra("nodeId", nodeId);
        startActivity(installintent);
    }

    /**
     * 字符串截取
     * 扫描二维码获取到的数据过长，其中有一些不用的字符
     * 根据文档，当前只需要data中13到52们的字符，共40个字符
     *
     * @param qrData qr扫描到的字符串
     */
    private void subQrData(String qrData) {
        //String str = "00000111112222233333aaaaa";
        String data = qrData.substring(12, 52); //根据文档，截取data中从12位开始至52位的字符，共40个字符。
        String data1 = data.substring(0, 5);//取字符串前5位
        String data2 = data.substring(5, 10);
        String data3 = data.substring(10, 15);
        String data4 = data.substring(15, 20);
        String data5 = data.substring(20, 25);
        String data6 = data.substring(25, 30);
        String data7 = data.substring(30, 35);
        String data8 = data.substring(35, 40);

        nodeId = data1;
        //将截取到的40位字符串每隔5位加一个“-”，这是DSK所需要的格式
        String DSKData = data1 + "-" + data2 + "-" + data3 + "-" + data4
                + "-" + data5 + "-" + data6 + "-" + data7 + "-" + data8;

        //55106-03713-41807-09806-27111-48391-14810-50406
        Log.i(TAG, "subQrData: " + DSKData);
        dskCode = DSKData;
        this.qrCode = qrData;
        showQRCode.setText(DSKData);
    }

    /**
     * 接收从另一个activity返回的参数
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String qrData = data.getExtras().getString("QR_CODE_DATA");
            Log.i(TAG, "=========onActivityResult: " + qrData);
            if (qrData != null && qrData.length() > 40) {
                subQrData(qrData);
            }
        }
    }


    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG, "===============topic=" + topic);
            Logg.i(TAG, "=============message=" + result);
            if (result.contains("desired")) {
                return;
            }

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String reported = jsonObject.optString("reported");
                        JSONObject reportedObject = new JSONObject(reported);
                        String mInterface = reportedObject.optString("Interface");
                        String result = reportedObject.optString("Result");
                        Log.i(TAG, "minterface : " + mInterface);
                        Log.i(TAG, "====result : " + result);
                        if(result.equals("true")){
                            installSuccessIntent();
                        }else{
                            Toast.makeText(mContext,"Add device failed",Toast.LENGTH_LONG).show();
                        }
                        stopWaitDialog();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Logg.i(TAG, "errorJson------>" + result);
                    }
                }
            });
        }

    };

    private void unrigister() {
        if (mMqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }
}