package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.fragment.RoomsFragment;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.TimeUtils;
import com.askey.mobile.zwave.control.util.ToastShow;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class InstallSuccessActivity extends BaseActivity implements View.OnClickListener {
    public static String LOG_TAG = "InstallSuccessActivity";
    private TextView notify_msg, room_name_tv;
    private RelativeLayout linear_click;
    private ImageView device_icon;
    private EditText device_name;
    //    private Spinner roomSpinner;
    private CheckBox add_favorite;
    private Button done;
    private static AddDeviceSuccessListener addDeviceSuccessListener;
    private String brand = "";
    private String deviceType = "";
    private String nodeId = "";
    private String roomName = "";
    private String displsyName;
    private int isFavorite = 0;
    private final int CHOOSE_ROOM = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_success);

        initView();

        Intent intent =  getIntent();
        brand = intent.getStringExtra("brand");
        deviceType = intent.getStringExtra("deviceType");
        Log.i(LOG_TAG, "-----brand="+brand+" deviceType="+deviceType);

        nodeId = intent.getStringExtra("nodeId");

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);

        String nodeTopic = Const.subscriptionTopic +"Zwave"+ nodeId;
//        String nodeTopic = Const.subscriptionTopic;
        // 订阅新设备的topic为 sn + nodeId
        MQTTManagement.getSingInstance().subscribeToTopic(nodeTopic,null);

    }


    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=message="+result);
            if(result.contains("desired")){
                return;
            }
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //reNameDevice:{"Interface":"editNodeInfo","devType":"Zwave","NodeId":16,"Result":"true"}
                        JSONObject jsonObject = new JSONObject(result);
                        String reported = jsonObject.optString("reported");
                        JSONObject reportedObject = new JSONObject(reported);
                        String Interface = reportedObject.optString("Interface");
                        if(Interface.equals("editNodeInfo")){
                            String result = reportedObject.optString("result");
                            String NodeId = reportedObject.optString("nodeId");
                            if(result.equals("true")){
                                DeviceInfo deviceInfo =  new DeviceInfo();
                                deviceInfo.setDeviceId(NodeId);
                                deviceInfo.setRooms(roomName);
                                deviceInfo.setDeviceType(deviceType);
                                deviceInfo.setBrand(brand);
                                deviceInfo.setDisplayName(displsyName);
                                deviceInfo.setIsFavorite(isFavorite+"");
                                Logg.i(LOG_TAG,"=====startActivity=111=");

                                addDeviceSuccessListener.addDeviceSuccess(roomName, deviceInfo);
                                Logg.i(LOG_TAG,"=====startActivity==");
                                Intent intent = new Intent();
                                intent.setClass(mContext,HomeActivity.class);
                                startActivity(intent);
                                Const.setIsDataChange(true); //添加完了执行一下getDeviceList
                                finish();
                            }else{
                                ((Activity) mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mContext,"EditNodeInfo Fail ! ",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };


    private void initView() {
        linear_click = (RelativeLayout) findViewById(R.id.linear_click);
        linear_click.setOnClickListener(this);
        room_name_tv = (TextView) findViewById(R.id.room_name_tv);
        notify_msg = (TextView) findViewById(R.id.notify_msg);
        device_icon = (ImageView) findViewById(R.id.device_icon);
        device_name = (EditText) findViewById(R.id.device_name);
//        roomSpinner = (Spinner) findViewById(R.id.spinner);
        add_favorite = (CheckBox) findViewById(R.id.add_favorite);
        done = (Button) findViewById(R.id.done);
        done.setOnClickListener(this);

        device_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(count>=25){
                    ToastShow.showLong(mContext,getResources().getString(R.string.device_name_is_long));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });

//        "BULB","PLUG","WALLMOTE"
        if(deviceType.equals("BULB")){
            device_icon.setBackgroundResource(R.mipmap.bulb_icon);
        }else if(deviceType.equals("PLUG")){
            device_icon.setBackgroundResource(R.mipmap.switch_icon);
        }else if(deviceType.equals("WALLMOTE")){
            device_icon.setBackgroundResource(R.mipmap.wallmote_icon);
        }
//        RoomsFragment.getRoomList();
        if(RoomsFragment.roomInfoList!=null){
            for (int i = 0; i < RoomsFragment.roomInfoList.size(); i++) {
                if (RoomsFragment.roomInfoList.get(i).getRoomName().equals(Const.currentRoomName)) {
                    roomName = RoomsFragment.roomInfoList.get(i).getRoomName();
                    room_name_tv.setText(roomName);
                    break;
                }
            }
        }

        if (roomName.equals("")) {
            room_name_tv.setText("My Home");
        }


//        ArrayAdapter<String> source=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
//        roomSpinner.setAdapter(source);
//        roomSpinner.setSelection(targetPosition,true);
//
//        roomSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
//
//            @Override
//            public void onItemSelected(AdapterView<?> arg0, View arg1,
//                                       int arg2, long arg3) {
//                new AlertDialog.Builder(mContext)
//                        .setTitle("Prompt")
//                        .setMessage("Selected : "+items[arg2])
//                        .setPositiveButton("OK", null)
//                        .show();
//                roomName = items[arg2] ;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> arg0) {
//
//            }
//
//        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.done:
                displsyName = device_name.getText().toString();
                if(displsyName.equals("")){
                    Toast.makeText(mContext,"Device name is null",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(add_favorite.isChecked()){
                    isFavorite = 1;
                }
                if(roomName.equals("")){
                    roomName = "My Home" ;
                }

                Logg.i(LOG_TAG,"=====nodeId====="+nodeId);
                Logg.i(LOG_TAG,"=====displsyName====="+displsyName);
                Logg.i(LOG_TAG,"=====deviceType====="+deviceType);
                Logg.i(LOG_TAG,"=====roomName====="+roomName);
//                TcpClient.getInstance().getTransceiver().send("mobile_zwave:reNameDevice:"+":"+nodeId+":"+displsyName+":"+deviceType+":"+roomName+":"+isFavorite);
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.editNodeInfo(nodeId,roomName,isFavorite+"",displsyName,deviceType, TimeUtils.gettimeStamp()));
//                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.editNodeInfo(nodeId,roomName,isFavorite+"",displsyName,deviceType));

                break;

            case R.id.linear_click:
                startActivityForResult(new Intent(this, ChooseRoomActivity.class), CHOOSE_ROOM);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_ROOM:
                    String result = data.getStringExtra("roomName");
                    roomName = result;
                    room_name_tv.setText(result);
                    break;
            }
        }
    }

    public static interface AddDeviceSuccessListener {
        void addDeviceSuccess(String roomName, DeviceInfo deviceInfo);
    }
    public static void setAddDeviceListener(AddDeviceSuccessListener listener){
        addDeviceSuccessListener = listener;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logg.i(LOG_TAG,"===onStop=====");
        unrigister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG,"===onDestroy=====");
    }


    private void unrigister() {
        if(mMqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            Logg.i(LOG_TAG,"onTouchEvent=====");
            return true;
        }
        return true;
    }

    /**
     *监听返回按钮
     * 如果还没有给Device命名就点了返回，此时Device已添加成功，需要更新Devcie list
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent = new Intent();
            intent.setClass(mContext,HomeActivity.class);
            startActivity(intent);
            Const.setIsDataChange(true);
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
