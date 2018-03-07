package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.adapter.ColorThirdAdapter;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.sdk.device.response.IoTDeviceInfoResponse;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BulbActivity extends BaseDeviceActivity {
    private static final String TAG = BulbActivity.class.getSimpleName();
    private static final String switchOn = "FF";
    private static final String switchOff = "00";
    private ColorPickViewOne pickOne;
    private ColorPickViewTwo pickTwo;
    private ColorPickViewThird pickThird;
    private CheckBox offDeviceTwo;
    private CheckBox offDeviceone, onoffIcon;
    private RelativeLayout colorOne,colorTwo,colorThird;
    private Button btnRightColor,btnLeftColor;
    private List<View> mList;
    private int colors[] = {Color.GREEN,Color.YELLOW,Color.WHITE,Color.RED,Color.CYAN,Color.MAGENTA};
    private int currentColor = 1;
    private LinearLayout llBlub;
    private TextView bulbExplain,tapCenterExplain, brightExplain,scheduleExplain,notifyExplain,vacationExplain;
    private SeekBar mSeekBar;
    private Context mContext;
    private int mColor;
    private boolean isStatus = false;
    private int red,green,blue;
    private IoTDeviceInfoResponse ioTDeviceInfoResponse;
    private String shadowTopic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulb_one);

        initView();
        initSetted();
        deviceName.setText("Bulb");

        initMyView();

        mContext = this;

        nodeId = getIntent().getStringExtra("nodeId");
        type = getIntent().getStringExtra("type");
        name = getIntent().getStringExtra("displayName");
        room = getIntent().getStringExtra("room");
        shadowTopic = getIntent().getStringExtra("shadowTopic");
        showWaitingDialog();
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        //获取灯泡状态
//            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,LocalMqttData.getSwitchStatus(nodeId));

        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,LocalMqttData.getBrigtness(nodeId));

        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,LocalMqttData.getLampColor(nodeId));

    }


    private void iotMessageResult(String s2) {
        try {
//            {"state":{"reported":{"deviceid":"e9491da46ac285b8","tstamp":"1513758340953","data":{"Interface":"getBrightness","devType":"Zwave","Node":"42","switchStatus":"off","brightness":"0"}}},"clientToken":"e9491da46ac285b8"}
            JSONObject  jsonObject = new JSONObject(s2);
            String state = jsonObject.optString("state");
            JSONObject stateObject = new JSONObject(state);
            String reported = stateObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String data = reportedObject.optString("data");
            JSONObject dataObject = new JSONObject(data);
            String Interface = dataObject.optString("Interface");
            if(Interface.equals("getSwitchStatus")){
                String switchStatus = dataObject.optString("switchStatus");
//                setSwitchUiStatus(switchStatus);
            }else if(Interface.equals("getBrightness")){
                String brigtness = dataObject.optString("brightness");
                String switchStatus = dataObject.optString("switchStatus");
                Logg.i(TAG,"==brigtness=="+brigtness);
                setSwitchUiStatus(switchStatus,brigtness);
            }else if(Interface.equals("getLampColor")){
                String componentId = dataObject.optString("component id");
                String value = dataObject.optString("value");
                String MessageType = dataObject.optString("MessageType");
                Logg.i(TAG,"====getLampColor==componentId==="+componentId);
                Logg.i(TAG,"====getLampColor==value==="+value);
                Logg.i(TAG,"====getLampColor==MessageType==="+MessageType);
            }else if(Interface.equals("setBrigtness")){
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onoffIcon.setChecked(true);
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(TAG,"=mqttMessageArrived=>=message="+result);

            if(result.contains("desired")){
                return;
            }
            mqttMessageResult(result);
            stopWaitDialog();
        }
    };

    //mqtt调用返回结果
    private void mqttMessageResult(String result) {
        try {
//            {"reported":{"MessageType":"Switch Color Report","Node id":27,"component id":"Blue","value":0,"Interface":"getLampColor","devType":"Zwave"}}
            JSONObject jsonObject = new JSONObject(result);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            if(Interface.equals("getSwitchStatus")){
                String switchStatus = reportedObject.optString("switchStatus");
//                setSwitchUiStatus(switchStatus);
            }else if(Interface.equals("getBrightness")){
                String brigtness = reportedObject.optString("brightness");
                String switchStatus = reportedObject.optString("switchStatus");
                setSwitchUiStatus(switchStatus,brigtness);
            }else if(Interface.equals("getLampColor")){
                String componentId = reportedObject.optString("component id");
                String value = reportedObject.optString("value");
                String MessageType = reportedObject.optString("MessageType");
                Logg.i(TAG,"====getLampColor==componentId==="+componentId);
                Logg.i(TAG,"====getLampColor==value==="+value);
                Logg.i(TAG,"====getLampColor==MessageType==="+MessageType);
            }else if(Interface.equals("setBrigtness")){
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onoffIcon.setChecked(true);
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setSwitchUiStatus(final String switchStatus, final String brigtness) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(switchStatus.equals("on")){
                    offDeviceone.setChecked(true);
                    offDeviceTwo.setChecked(true);
                    onoffIcon.setChecked(true);
                }else{
                    offDeviceone.setChecked(false);
                    offDeviceTwo.setChecked(false);
                    onoffIcon.setChecked(false);
                }
                mSeekBar.setProgress(Integer.valueOf(brigtness));
            }
        });
    }

    private void initMyView() {
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        //解释说明
//        llBlub = (LinearLayout) findViewById(R.id.activity_blub);
//        bulbExplain = (TextView) findViewById(R.id.tv_bulb_explain);
//        tapCenterExplain = (TextView) findViewById(R.id.tv_center_explain);
//        brightExplain = (TextView) findViewById(R.id.tv_bright_explain);
//        scheduleExplain = (TextView) findViewById(R.id.tv_scheduler_explain);
//        notifyExplain = (TextView) findViewById(R.id.tv_notify_explain);
//        vacationExplain = (TextView) findViewById(R.id.tv_vacation_explain);


//        btnRightColor = (Button) findViewById(R.id.btn_right_color);
        btnLeftColor = (Button) findViewById(R.id.btn_left_color);
//        btnRightColor.setOnClickListener(this);
        btnLeftColor.setOnClickListener(this);

        //中心button
        offDeviceone = (CheckBox) findViewById(R.id.off_one);
        offDeviceTwo = (CheckBox) findViewById(R.id.iv_off_two);
        offDeviceone.setOnClickListener(this);
        offDeviceTwo.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                showWaitingDialog();
                Logg.i(TAG,"==mSeekBar=="+String.valueOf(mSeekBar.getProgress()));
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,
                        LocalMqttData.setBrigtness(nodeId,String.valueOf(mSeekBar.getProgress())));
//                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + nodeId, "mobile_zwave:setBasic:" + nodeId + ":FF");
            }
        });

        //调色板
        colorOne = (RelativeLayout) findViewById(R.id.ll_color_one);
        colorTwo = (RelativeLayout) findViewById(R.id.ll_color_two);
        colorThird = (RelativeLayout) findViewById(R.id.ll_color_third);

        pickOne = (ColorPickViewOne) findViewById(R.id.color_picker_one);
        pickTwo = (ColorPickViewTwo) findViewById(R.id.color_picker_two);
        pickThird = (ColorPickViewThird) findViewById(R.id.color_picker_third);
        pickOne.setOnColorChangedListener(new ColorPickViewOne.OnColorChangedListener() {

            @Override
            public void onColorChange(int color) {
                Log.i("BulbActivity",color + "" );
                mColor = color;
                changeBulbColor("pickOne",color);

            }

        });

        pickTwo.setOnColorChangedListener(new ColorPickViewTwo.OnColorChangedListener() {

            @Override
            public void onColorChange(int color) {
                Log.i("BulbActivity",color + "onColorChange" );
                mColor = color;
                changeBulbColor("pickTwo",color);
            }

        });

        initMenuItem();
        pickThird.setAdapter(new ColorThirdAdapter(this,mList));

        pickThird.setOnItemClickListener(new ColorPickViewThird.OnItemClickListener() {
            @Override
            public void onItemClickListener(View v, int position) {
                if (position == mList.size() - 1) {
                    Log.i("BulbActivity","off" );
                    if (((CheckBox) mList.get(position)).isChecked()) {
                        isStatus = true;
                    } else {
                        isStatus = false;
                    }
                    changeSwitchStatus(isStatus);
                } else {
                    Log.i("BulbActivity",colors[position] + "onItemClickListener" );
                    mColor = colors[position];
                    changeBulbColor("pickThird",colors[position]);
                }

            }
        });
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.btn_right_color) {


        } else if (view.getId() == R.id.btn_left_color) {

        }
        switch (view.getId()) {
            case R.id.btn_right_color:
                changeColor();
                break;
            case R.id.btn_left_color:
                changeColor();
                break;
            case R.id.off_one:

            if (offDeviceone.isChecked()) {
                isStatus = true;
                //需验证ff 和 00  set无返回
            } else {
                isStatus = false;
            }
                changeSwitchStatus(isStatus);
                break;
            case R.id.iv_off_two:
//                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + nodeId, "mobile_zwave:setBasic:" + nodeId + ":FF");
                if (offDeviceTwo.isChecked()) {
                    isStatus = true;
                } else {
                    isStatus = false;
                }
                changeSwitchStatus(isStatus);
                break;
        }
    }

    private void changeSwitchStatus(boolean isStatus) {
        showWaitingDialog();
        if (isStatus) {
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic +"Zwave"+ nodeId, LocalMqttData.setSwitch(nodeId,"on"));
        } else {
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setBasic:" + nodeId + ":00" );
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic +"Zwave"+ nodeId, LocalMqttData.setSwitch(nodeId, "off"));

        }
    }

    private void changeColor() {
        if (currentColor == 1) {
            colorOne.setVisibility(View.GONE);
            colorTwo.setVisibility(View.VISIBLE);
            colorThird.setVisibility(View.GONE);
            currentColor++;
        } else if (currentColor == 2) {
            colorOne.setVisibility(View.VISIBLE);
            colorTwo.setVisibility(View.GONE);
            colorThird.setVisibility(View.GONE);
            currentColor++;
        } else if (currentColor == 3) {
            colorOne.setVisibility(View.GONE);
            colorTwo.setVisibility(View.GONE);
            colorThird.setVisibility(View.VISIBLE);
            currentColor = 1;
        }
    }

    // 初始化菜单项
    private void initMenuItem() {
        mList = new ArrayList<>();
        ImageView color;
//        {Color.CYAN,Color.YELLOW,Color.WHITE,Color.RED,Color.BLUE,Color.MAGENTA};
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlegreen);
        color.setColorFilter(colors[0]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlegreen);
        color.setColorFilter(colors[1]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlestrokes);
//        color.setColorFilter(colors[2]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlegreen);
        color.setColorFilter(colors[3]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlegreen);
        color.setColorFilter(colors[4]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlegreen);
        color.setColorFilter(colors[5]);
        mList.add(color);
       onoffIcon = new CheckBox(this);
        onoffIcon.setButtonDrawable(null);
        onoffIcon.setBackgroundResource(R.drawable.device_on_off);
        mList.add(onoffIcon);
    }

    @Override
    public void info() {
        super.info();
        if (isDetailStatus) {

            llBlub.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            bulbExplain.setVisibility(View.INVISIBLE);
            tapCenterExplain.setVisibility(View.INVISIBLE);
            brightExplain.setVisibility(View.INVISIBLE);
            scheduleExplain.setVisibility(View.INVISIBLE);
            notifyExplain.setVisibility(View.INVISIBLE);
            vacationExplain.setVisibility(View.INVISIBLE);
        } else {

            llBlub.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
            bulbExplain.setVisibility(View.VISIBLE);
            tapCenterExplain.setVisibility(View.VISIBLE);
            brightExplain.setVisibility(View.VISIBLE);
            scheduleExplain.setVisibility(View.VISIBLE);
            notifyExplain.setVisibility(View.VISIBLE);
            vacationExplain.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unrigister();
    }

    private void unrigister() {
        if(mMqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }

    private void changeBulbColor(String tag,int color){
        //temp pos
        //设置某种颜色的亮度值 int ZwController_setSwitchColor(int deviceId, int color_id, int color_value);

//        ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x02,r_value);
//        ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x03,g_value);
//        ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x04,b_value);
//        ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x00,0);
//        ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x01,0);
        showWaitingDialog();
        switchRgb(color);

        Logg.i(TAG,"=changeBulbColor=red=="+red);
        Logg.i(TAG,"=changeBulbColor=green=="+green);
        Logg.i(TAG,"=changeBulbColor=blue=="+blue);
        if("pickTwo".equals(tag)){
            //warmWhite/coldWhite/RGB
            if (blue == 0) {
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + "Zwave" + nodeId,
                        LocalMqttData.setLampcolor(nodeId, "warmWhite", "0", String.valueOf(mSeekBar.getProgress()), red + "", green + "", blue + ""));
//                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + nodeId, "mobile_zwave:setBasic:" + nodeId + ":FF");
                Log.i(TAG,LocalMqttData.setLampcolor(nodeId, "warmWhite", "0", String.valueOf(mSeekBar.getProgress()), red + "", green + "", blue + ""));
            } else {
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,
                        LocalMqttData.setLampcolor(nodeId,"coldWhite","0",String.valueOf(mSeekBar.getProgress()),red+"",green+"",blue+""));
//                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + nodeId, "mobile_zwave:setBasic:" + nodeId + ":FF");
                Log.i(TAG,LocalMqttData.setLampcolor(nodeId, "coldWhite", "0", String.valueOf(mSeekBar.getProgress()), red + "", green + "", blue + ""));
            }
        }else{
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,
                    LocalMqttData.setLampcolor(nodeId,"RGB","0",String.valueOf(mSeekBar.getProgress()),red+"",green+"",blue+""));
//                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + nodeId, "mobile_zwave:setBasic:" + nodeId + ":FF");
        }

        //message=mobile_zwave:setSwitchColor:2:-139345:35
        Log.i(TAG, "mobile_zwave:setLampcolo                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                r :" + nodeId + ":" + color + ":" + mSeekBar.getProgress());
    }

    private void switchRgb(int color) {
        red = (color & 0xff0000) >> 16;
        green = (color & 0x00ff00) >> 8;
        blue = (color & 0x0000ff);
    }

}
