package com.askey.mobile.zwave.control.home.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.home.adapter.FavoriteAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.ImageUtils;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.webservice.sdk.iot.callback.ShadowReceiveListener;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FavoriteEditActivity extends BaseActivity implements View.OnClickListener, FavoriteAdapter.OnItemClickListener {
    private static final String TAG = FavoriteEditActivity.class.getSimpleName();
    private List<DeviceInfo> deviceInfoList, favoriteList, unfavoriteLsit;
    private FavoriteAdapter adapter;
    private RecyclerView edit_favorite_recycler;
    private ImageView yes, no;
    private static EditFavoriteListener editFavoriteListener;
    private LinearLayout favorite_edit_linear;
    private List<String> addNodeIdList, removeNodeIdList;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_edit);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        initView();
        initData();

        if(Const.isRemote) {
            initIotMqttMessage();
        } else {
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        }

    }


    private void initIotMqttMessage() {
        //以下这句为注册监听
        AskeyIoTService.getInstance(this).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s=" + s);
                Logg.i(TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s1=" + s1);
                Logg.i(TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s2=" + s2);
                IotMqttManagement.getInstance().receiveMqttMessage(s,s1,s2);
                if(s2.contains("desired")){
                    return;
                }
                mqttMessageResult(s2);//要验s2格式

            }
        });
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

        }
    };

    //mqtt调用返回结果
    private void mqttMessageResult(final String result) {
        Logg.i(TAG,"=mqttMessageArrived=>=message="+result);
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //reNameDevice:{"Interface":"editNodeInfo","devType":"Zwave","NodeId":16,"Result":"true"}
//                    {"reported":{"Interface":"editFavoriteList","Result":"true"}}
                    JSONObject jsonObject = new JSONObject(result);
                    String reported = jsonObject.optString("reported");
                    JSONObject reportedObject = new JSONObject(reported);
                    String Interface = reportedObject.optString("Interface");
                    if(Interface.equals("editFavoriteList")){
                        String result = reportedObject.optString("Result");
                        if(result.equals("true")){
                            loading.setVisibility(View.GONE);
                            editFavoriteListener.editFavoriteClick(favoriteList);
                            finish();
                        }else{
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loading.setVisibility(View.GONE);
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

    private void initView() {
        loading = (ProgressBar) findViewById(R.id.loading);

        favorite_edit_linear = (LinearLayout) findViewById(R.id.favorite_edit_linear);
        byte[] bytes = getIntent().getByteArrayExtra("bg");
        Bitmap bitmap = ImageUtils.Bytes2Bimap(bytes);
        BitmapDrawable drawable = new BitmapDrawable(getResources(),bitmap);
        favorite_edit_linear.setBackground(drawable);

        yes = (ImageView) findViewById(R.id.yes);
        yes.setOnClickListener(this);
        no = (ImageView) findViewById(R.id.no);
        no.setOnClickListener(this);
        edit_favorite_recycler = (RecyclerView) findViewById(R.id.edit_favorite_recycler);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        edit_favorite_recycler.setLayoutManager(layoutManager);
    }

    private void initData() {
        addNodeIdList = new ArrayList<>();
        removeNodeIdList = new ArrayList<>();
        deviceInfoList = (List<DeviceInfo>) getIntent().getSerializableExtra("data");
        favoriteList = new ArrayList<>();
        unfavoriteLsit = new ArrayList<>();

        for (DeviceInfo deviceInfo : deviceInfoList) {
            if ("0".equals(deviceInfo.getIsFavorite())) {
                unfavoriteLsit.add(deviceInfo);
            }
            if ("1".equals(deviceInfo.getIsFavorite())) {
                favoriteList.add(deviceInfo);
            }
        }

        //将unfavorite的元素排到favorite后面,这里其实是所有的设备列表
        favoriteList.addAll(unfavoriteLsit);

        adapter = new FavoriteAdapter(favoriteList);
        adapter.setMode(FavoriteAdapter.EDIT_MODE);
        adapter.setOnItemClickListener(this);
        edit_favorite_recycler.setAdapter(adapter);
    }

    public static interface EditFavoriteListener {
        void editFavoriteClick(List<DeviceInfo> list);
    }

    public static void setEditFavoriteListener(EditFavoriteListener listener){
        editFavoriteListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes:
                //若用户没有操作本界面则直接返回
                if (addNodeIdList.size()>0 || removeNodeIdList.size()>0) {
                    loading.setVisibility(View.VISIBLE);
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.editNodeInfo(addNodeIdList,removeNodeIdList));
                } else {
                    finish();
                }
                //假设成功返回
//                editFavoriteListener.editFavoriteClick(favoriteList);
//                finish();
                break;
            case R.id.no:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(View view, DeviceInfo deviceInfo) {

    }

    @Override
    public void addFavoriteClick(int position) {
        DeviceInfo deviceInfo = favoriteList.get(position);
        Log.d("tag","add_deviceId-->"+deviceInfo.getDeviceId());
        for (int i = 0; i < removeNodeIdList.size(); i++) {
            if (deviceInfo.getDeviceId().equals(removeNodeIdList.get(i))) {
                removeNodeIdList.remove(i);
                break;
            }
        }
        addNodeIdList.add(deviceInfo.getDeviceId());
        favoriteList.get(position).setIsFavorite("1");
        adapter.notifyDataSetChanged();
        Log.d("tag","add-->" + addNodeIdList.toString()+"    remove-->" + removeNodeIdList.toString());
    }

    @Override
    public void removeFavoriteClick(int position) {
        DeviceInfo deviceInfo = favoriteList.get(position);
        Log.d("tag","remove_deviceId-->"+deviceInfo.getDeviceId());
        for (int i = 0; i < addNodeIdList.size(); i++) {
            if (deviceInfo.getDeviceId().equals(addNodeIdList.get(i))) {
                addNodeIdList.remove(i);
                break;
            }
        }
        removeNodeIdList.add(deviceInfo.getDeviceId());
        favoriteList.get(position).setIsFavorite("0");
        adapter.notifyDataSetChanged();
        Log.d("tag","add-->" + addNodeIdList.toString()+"    remove-->" + removeNodeIdList.toString());
//        if(Const.isRemote){
//            initIotMqttMessage();
//            if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
//                MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic+"Zwave"+deviceInfo.getDeviceId());
//                builder.setJsonString(CloudIotData.editNodeInfo(deviceInfo.getDeviceId(),deviceInfo.getRooms(),0+"",deviceInfo.getDisplayName(),deviceInfo.getDeviceType()));
//                AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);
//            }
//        }else{
//            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
//            //获取灯泡状态
//            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+deviceInfo.getDeviceId(), LocalMqttData.editNodeInfo(deviceInfo.getDeviceId(),deviceInfo.getRooms(),0+"",deviceInfo.getDisplayName(),deviceInfo.getDeviceType()));
//        }
    }

    @Override
    public void move2EditActivity() {

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

}
