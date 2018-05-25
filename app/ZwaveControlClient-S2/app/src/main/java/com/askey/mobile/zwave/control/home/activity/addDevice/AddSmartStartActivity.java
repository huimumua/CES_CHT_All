package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.home.fragment.ScenesFragment;
import com.askey.mobile.zwave.control.qrcode.CaptureActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by skysoft on 2018/3/8.
 */

public class AddSmartStartActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "AddSmartStartActivity";
    private EditText showQRCode;
    //private AppCompatEditText showQRCode;
    private ImageButton scanQr;
    private Button addSmartStartButton;
    private RadioGroup radioGroup;
    private RadioButton securityRadioBtn, smartStartRadioBtn;
    private RelativeLayout underline;
    private RelativeLayout editQrCodeLayout, editDskLayout;//edit_layout
    private TextView addModeHintTextView;

    private static final String SMART_START = "01";
    private static final String SECURITY_S2 = "00";

    private String brand = "";
    private String deviceType = "";
    private String nodeId = "";

    private String dskCode = "";
    private String qrCode = "";
    private String version = "";
    private String bootMode; //传给api：addProvisionListEntry的参数，bootMode = "1" 表示选择smart start, bootMode = "0" 表示选择 S2;
    private Context mContext;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_start);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        this.mContext = super.mContext;

        SharedPreferences sp = getSharedPreferences("zwave", Context.MODE_PRIVATE);
        editor = sp.edit();
        Log.i(TAG, "========onStart: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unrigister();
    }

    private void initView() {
        brand = getIntent().getStringExtra("brand");
        deviceType = getIntent().getStringExtra("deviceType");

        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        smartStartRadioBtn = (RadioButton) findViewById(R.id.smart_start_radio_button);
        securityRadioBtn = (RadioButton) findViewById(R.id.s2_radio_button);

        showQRCode = (EditText) findViewById(R.id.show_qr_code);
        scanQr = (ImageButton) findViewById(R.id.scan_qr_code);
        scanQr.setOnClickListener(this);

        addModeHintTextView = (TextView) findViewById(R.id.add_mode_text);//提示用户按按钮

        addSmartStartButton = (Button) findViewById(R.id.button_smart_start);
        addSmartStartButton.setOnClickListener(this);

        underline = (RelativeLayout) findViewById(R.id.qr_code_underline);

        //输入框内容改变的监听
        showQRCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 47 || start == 46) {
                    underline.setVisibility(View.VISIBLE);
                } else {
                    underline.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.smart_start_radio_button:
                        addModeHintTextView.setText(getResources().getString(R.string.automaticlly));
                        bootMode = SMART_START;
                        break;
                    case R.id.s2_radio_button:
                        addModeHintTextView.setText(getResources().getString(R.string.user_manully));
                        bootMode = SECURITY_S2;
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_qr_code:
                Intent smartStartintent = new Intent(AddSmartStartActivity.this, CaptureActivity.class);
                //startActivityForResult打开activity是为了配合onActivityResult返回数据，requestCode：1是区分标识
                startActivityForResult(smartStartintent, 71);
                break;
            case R.id.button_smart_start:
                String editText = showQRCode.getText().toString();
                if (editText.length() == 47) {
                    showWaitingDialog();
                    //调用mqtt接口，带参数
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.addProvisionList(dskCode, "47", qrCode, bootMode));
                } else {
                    if (editText == null || editText.length() < 1) {
                        Toast.makeText(mContext, "Please enter a qr code.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mContext, "Error in qr code.", Toast.LENGTH_LONG).show();
                    }
                }
                //MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.addProvisionList("30008-63926-16243-29736-05865-19168-33435-15670", "47", "",""));
        }
    }

    /**
     * 字符串截取
     * 扫描二维码获取到的数据过长，其中有一些不用的字符
     * 根据文档，当前只需要data中13到52们的字符，共40个字符
     *
     * @param qrData qr扫描到的字符串
     */
    private void subQrData(String qrData) {

        version = qrData.substring(2, 4); // version根据文档，Index2~3表示设备版本，00 表示仅支持Security s2,01 表示smart start

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

        SpannableString ss = new SpannableString(DSKData);
        //设置DSK前5个字符的字体颜色红色高亮。接口：setSpan(color, 字符启始位,字符结束位,前后包含);setSpan(Object what, int start, int end, int flags);
        ss.setSpan(new ForegroundColorSpan(Color.parseColor("#ff0000")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        showQRCode.setText(ss);

        Log.i(TAG, "~~~~~~~~~~~~~~~~~~bootMode: "+version);
        addModeHintTextView.setVisibility(View.VISIBLE);
        if(SMART_START.equals(version) ){
            radioGroup.setVisibility(View.VISIBLE);//选择 smart start / s2
            addModeHintTextView.setText(getResources().getString(R.string.automaticlly));
            bootMode = SMART_START;
        } else {
            radioGroup.setVisibility(View.GONE);//选择 smart start / s2
            addModeHintTextView.setText(getResources().getString(R.string.user_manully));
            bootMode = SECURITY_S2;
        }
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
            if (qrData != null && qrData.length() > 52) {
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
                    stopWaitDialog();
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String reported = jsonObject.optString("reported");
                        JSONObject reportedObject = new JSONObject(reported);
                        String mInterface = reportedObject.optString("Interface");
                        String result = reportedObject.optString("Result");
                        Log.i(TAG, "minterface : " + mInterface);
                        Log.i(TAG, "====result : " + result);
                        if (result.equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra("ADD_SMART_START_RESULT", result);
                            setResult(2, intent); //将result返回到ScenesFragment，然后再finish
                            ScenesFragment.newInstance().addDskResult();

                            editor.putString(dskCode,version);//将二维码的第2、3位保存起来，用作DeviceTestEditActivity中判断DSK类型
                            editor.commit();

                            finish();

                        } else {
                            Toast.makeText(mContext, "Add device failed", Toast.LENGTH_LONG).show();
                        }
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