package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.dao.ZwaveDevice;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.mqtt.zwNodeMember;
import com.askey.firefly.zwave.control.utils.DeviceInfo;

import java.util.List;


/**
 * Created by chiapin on 2017/9/22.
 */

public class HomeActivity extends BaseActivity{

    private static String LOG_TAG = HomeActivity.class.getSimpleName();
    private ZwaveDeviceManager zwaveDeviceManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        zwaveDeviceManager = ZwaveDeviceManager.getInstance(this);

        GridView gvMember = (GridView) findViewById(R.id.gvMember);
        List<zwNodeMember> memberList = getMemberList();
        gvMember.setAdapter(new MemberAdapter(this, memberList));
        gvMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

            zwNodeMember zwNode = (zwNodeMember) parent.getItemAtPosition(position);
            String tmpType = zwNode.getDeviceType();

            buttonevent(tmpType,position);
            }
        });
    }

    private void buttonevent(final String type,final int position) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = null;
                switch (type) {
                    case "add":
                        intent = new Intent(mContext,AddDeviceActivity.class);
                        break;
                    case "remove":
                        intent = new Intent(mContext,RemoveDeviceActivity.class);
                        intent.putExtra("title", 1);
                        break;
                    case "BULB":
                        intent = new Intent(mContext,BulbActivity.class);
                        break;
                    case "DIMMER":
                        intent = new Intent(mContext,DimmerActivity.class);
                        break;
                    case "PLUG":
                        intent = new Intent(mContext,PlugActivity.class);
                        break;
                    case "SENSOR":
                        intent = new Intent(mContext,SensorActivity.class);
                        break;
                    case "OTHER":
                        break;
                }
                Log.i(LOG_TAG,"tap position = "+position);
                zwNodeMember member = DeviceInfo.memberList.get(position);
                intent.putExtra("NodeId", String.valueOf(member.getNodeId()));
                intent.putExtra("NodeInfoList",member.getNodeInfo());
                mContext.startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG,"===  onDestroy() ===");
    }

    private class MemberAdapter extends BaseAdapter {
        Context context;
        //List<zwNodeMember> memberList;

        MemberAdapter(Context context, List<zwNodeMember> memberList) {
            this.context = context;
            DeviceInfo.memberList = memberList;
        }

        @Override
        public int getCount() {
            return DeviceInfo.memberList.size();
        }

        @Override
        public View getView(int position, View itemView, ViewGroup parent) {
            if (itemView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                itemView = layoutInflater.inflate(R.layout.zwavenode_view, parent, false);
            }

            zwNodeMember member = DeviceInfo.memberList.get(position);
            ImageView ivImage = (ImageView) itemView
                    .findViewById(R.id.ivImage);
            TextView tvId = (TextView) itemView
                    .findViewById(R.id.nodeId);
            TextView tvName = (TextView) itemView
                    .findViewById(R.id.nodeName);

            String tmpType = member.getDeviceType();
            switch (tmpType) {
                case "OTHER":
                    ivImage.setImageResource(R.drawable.unknown);
                    break;
                case "BULB":
                    ivImage.setImageResource(R.drawable.bulb);
                    break;
                case "DIMMER":
                    ivImage.setImageResource(R.drawable.dimmer);
                    break;
                case "PLUG":
                    ivImage.setImageResource(R.drawable.plug);
                    break;
                case "SENSOR":
                    ivImage.setImageResource(R.drawable.sensor);
                    break;
                case "add":
                    ivImage.setImageResource(R.drawable.add);
                    break;
                case "remove":
                    ivImage.setImageResource(R.drawable.remove);
                    break;
            }
            if (tmpType == "add" || tmpType == "remove"){
                tvId.setText("");
            }else{
                tvId.setText("Name : " + String.valueOf(member.getDeviceType()));
            }
            tvName.setText(member.getName());
            return itemView;
        }

        @Override
        public Object getItem(int position) {
            return DeviceInfo.memberList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return DeviceInfo.memberList.get(position).getNodeId();
        }
    }

    public List<zwNodeMember> getMemberList() {

        DeviceInfo.memberList.clear();

        List<ZwaveDevice> list = zwaveDeviceManager.queryZwaveDeviceList();

        for (int idx = 1 ; idx< list.size(); idx++){
            //zwNodeMember(int nodeId,String homeId, String devType, String name)
            Log.i(LOG_TAG,"*** NodeId = "+list.get(idx).getNodeId()+" | HomeID = "+list.get(idx).getHomeId()
                    +"| devType="+list.get(idx).getDevType()+" | Name="+list.get(idx).getName()
                    +"| nodeInfo = "+list.get(idx).getNodeInfo());

            DeviceInfo.memberList.add(new zwNodeMember(list.get(idx).getNodeId(),list.get(idx).getHomeId(),
                    list.get(idx).getDevType(), list.get(idx).getName(),list.get(idx).getNodeInfo()));
        }

        DeviceInfo.memberList.add(new zwNodeMember(1,"","add", "Add Device",""));
        if (DeviceInfo.localSubTopiclist.size() > 1 ) {
            DeviceInfo.memberList.add(new zwNodeMember(1,"" ,"remove", "Remove Device",""));
        }
        return DeviceInfo.memberList;
    }

}