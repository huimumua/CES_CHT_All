package com.askey.firefly.zwave.control.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.askey.firefly.zwave.control.ui.AddDeviceActivity;
import com.askey.firefly.zwave.control.ui.BulbActivity;
import com.askey.firefly.zwave.control.ui.ControlActivity;
import com.askey.firefly.zwave.control.ui.DimmerActivity;
import com.askey.firefly.zwave.control.ui.PlugActivity;
import com.askey.firefly.zwave.control.ui.RemoveDeviceActivity;
import com.askey.firefly.zwave.control.ui.SensorActivity;
import com.askey.firefly.zwave.control.utils.DeviceInfo;

import java.util.List;

/**
 * Created by chiapin on 2017/10/24.
 */

public class devListPage extends PageView {

    private static String LOG_TAG = devListPage.class.getSimpleName();
    private ZwaveDeviceManager zwaveDeviceManager;
    private Context mContext;

    public devListPage(Context context) {

        super(context);
        mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.gridview, null);

        addView(view);
        zwaveDeviceManager = ZwaveDeviceManager.getInstance(mContext);

    }

    @Override
    public void refreshView() {

        Log.i(LOG_TAG,"refreshView() ");
        List<zwNodeMember> memberList = getMemberList();
        GridView gvMember = (GridView) findViewById(R.id.gvMember);

        gvMember.setAdapter(new MemberAdapter(mContext, memberList));
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
                    case "CONTROL":
                        intent = new Intent(mContext,ControlActivity.class);
                        break;
                    case "OTHER":
                        break;
                }

                zwNodeMember member = DeviceInfo.memberList.get(position);
                intent.putExtra("NodeId", String.valueOf(member.getNodeId()));
                intent.putExtra("NodeInfoList",member.getNodeInfo());
                mContext.startActivity(intent);
            }
        });
    }

    private class MemberAdapter extends BaseAdapter {

        Context context;

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
            ImageView ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            TextView tvId = (TextView) itemView.findViewById(R.id.nodeId);
            TextView tvName = (TextView) itemView.findViewById(R.id.nodeName);

            String tmpType = member.getDeviceType();

            // show device icon
            if (tmpType==null){
                ivImage.setImageResource(R.drawable.unknown);
            }
            else{
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
                    case "CONTROL":
                        ivImage.setImageResource(R.drawable.control);
                        break;
                    case "add":
                        ivImage.setImageResource(R.drawable.add);
                        break;
                    case "remove":
                        ivImage.setImageResource(R.drawable.remove);
                        break;
                }
            }

            // set device's name in device list page
            if (tmpType == "add" || tmpType == "remove") {
                tvId.setText("");
                tvName.setText(member.getName());
            } else {

                if (member.getName().equals(String.valueOf(member.getNodeId()))){
                    tvId.setText(member.getRoomName());
                    tvName.setText(member.getDeviceType()+member.getName());
                }else {
                    tvId.setText(member.getRoomName()+" : " + String.valueOf(member.getDeviceType()));
                    tvName.setText(member.getName());
                }
            }

            if (member.getName().equals(String.valueOf(member.getNodeId()))){
                tvName.setText(member.getDeviceType()+member.getName());
            }else {
                tvName.setText(member.getName());
            }
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

            DeviceInfo.memberList.add(new zwNodeMember(list.get(idx).getNodeId(),list.get(idx).getDevType(),
                    list.get(idx).getDevType(), list.get(idx).getName(),
                    list.get(idx).getRoomName(),false,list.get(idx).getNodeInfo()));
        }

        DeviceInfo.memberList.add(new zwNodeMember(1,"","add", "Add Device","",false,""));
        if (DeviceInfo.localSubTopiclist.size() > 1 ) {
            DeviceInfo.memberList.add(new zwNodeMember(1,"" ,"remove", "Remove Device","",false,""));
        }


        return DeviceInfo.memberList;
    }
}
