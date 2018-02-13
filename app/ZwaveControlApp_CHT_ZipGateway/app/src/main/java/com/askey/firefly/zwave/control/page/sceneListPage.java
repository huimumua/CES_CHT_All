package com.askey.firefly.zwave.control.page;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceSceneManager;
import com.askey.firefly.zwave.control.utils.DeviceInfo;

import java.util.List;

/**
 * Created by chiapin on 2017/10/24.
 */

public class sceneListPage extends PageView {

    private static String LOG_TAG = sceneListPage.class.getSimpleName();

    private ZwaveDeviceManager zwaveDeviceManager;
    private ZwaveDeviceSceneManager zwaveDeviceSceneManager;

    private Context mContext;

    public sceneListPage(Context context) {
        super(context);
        mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.gridview, null);
        addView(view);
        zwaveDeviceManager = ZwaveDeviceManager.getInstance(mContext);
        zwaveDeviceSceneManager = ZwaveDeviceSceneManager.getInstance(mContext);
    }


    @Override
    public void refreshView() {
        List<zwScenceMember> roomList = getRoomList();
        GridView gvMember = (GridView) findViewById(R.id.gvMember);

        gvMember.setAdapter(new sceneListPage.roomAdapter(mContext, roomList));

        gvMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                buttonevent(position);
            }
        });

    }

    private void buttonevent(final int position) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                /*
                zwScenceMember member = DeviceInfo.roomList.get(position);

                Intent intent = new Intent(mContext,RoomActivity.class);
                intent.putExtra("ScenceName", member.getScenceName());

                ZwaveDeviceScene tmpScene = zwaveDeviceSceneManager.getScene(member.getScenceName());

                if (tmpScene==null) {

                    intent.putExtra("RoomCondition", "null");
                    intent.putExtra("SensorNodeId", "null");
                } else {
                    ZwaveDevice sensorNode = zwaveDeviceManager.queryZwaveDevices(tmpScene.getSensorNodeId());

                    if (sensorNode==null ) {
                        intent.putExtra("RoomCondition", "null");
                        intent.putExtra("SensorNodeId", "null");
                    }else {
                        intent.putExtra("RoomCondition", tmpScene.getCondition());
                        intent.putExtra("SensorNodeId", String.valueOf(sensorNode.getNodeId()));
                    }
                }


                mContext.startActivity(intent);
                */
            }
        });
    }

    private class roomAdapter extends BaseAdapter {

        Context context;

        roomAdapter(Context context, List<zwScenceMember> roomList) {
            this.context = context;
            DeviceInfo.roomList = roomList;
        }

        @Override
        public int getCount() {
            return DeviceInfo.roomList.size();
        }

        @Override
        public View getView(int position, View itemView, ViewGroup parent) {
            if (itemView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                itemView = layoutInflater.inflate(R.layout.room_view, parent, false);
            }

            zwScenceMember member = DeviceInfo.roomList.get(position);
            ImageView ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            TextView tvName = (TextView) itemView.findViewById(R.id.roomName);

            String tmpRoom = member.getScenceName();

            if (tmpRoom==null){
                ivImage.setImageResource(R.drawable.unknown);
            }
            else{
                switch (tmpRoom) {
                    case "My Home":
                        ivImage.setImageResource(R.drawable.myhome);
                        break;
                    case "Living Room":
                        ivImage.setImageResource(R.drawable.livingroom);
                        break;
                    case "Bedroom":
                        ivImage.setImageResource(R.drawable.bedroom);
                        break;
                }
            }
            tvName.setText(tmpRoom);
            return itemView;
        }

        @Override
        public Object getItem(int position) {
            return DeviceInfo.roomList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return DeviceInfo.roomList.get(position).getScenceId();
        }
    }

    private List<zwScenceMember> getRoomList() {

        DeviceInfo.roomList.clear();

        /*
        List<String> tmpNodeList = zwaveDeviceManager.getSceneNameList();

        if (tmpNodeList!=null) {

            for (int idx = 0; idx < tmpNodeList.size(); idx++) {
                //Log.i(LOG_TAG, "*** tmpList[" + idx + "] = " + tmpNodeList.get(idx));
                ZwaveDeviceScene newScene = new ZwaveDeviceScene();

                ZwaveDeviceScene tmpScene = zwaveDeviceSceneManager.getScene(tmpNodeList.get(idx));
                if (tmpScene == null) {

                    newScene.setScene(tmpNodeList.get(idx));
                    newScene.setSensorNodeId(0);
                    newScene.setCondition("null");
                    zwaveDeviceSceneManager.insertDeviceScene(newScene);
                }

                DeviceInfo.roomList.add(new zwScenceMember(idx, tmpNodeList.get(idx),
                        "", ""));
            }
        }
        */
        
        return DeviceInfo.roomList;
    }

}
