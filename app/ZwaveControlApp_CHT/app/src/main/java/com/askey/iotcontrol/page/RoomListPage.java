package com.askey.iotcontrol.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.iotcontrol.R;
import com.askey.iotcontrol.dao.ZwaveDevice;
import com.askey.iotcontrol.dao.ZwaveDeviceManager;
import com.askey.iotcontrol.dao.ZwaveDeviceRoom;
import com.askey.iotcontrol.dao.ZwaveDeviceRoomManager;
import com.askey.iotcontrol.ui.RoomActivity;
import com.askey.iotcontrol.utils.DeviceInfo;

import java.util.List;

/**
 * Created by chiapin on 2017/10/24.
 */

public class RoomListPage extends PageView {

    private static String LOG_TAG = RoomListPage.class.getSimpleName();

    private ZwaveDeviceManager zwaveDeviceManager;
    private ZwaveDeviceRoomManager zwaveDeviceRoomManager;

    private Context mContext;

    public RoomListPage(Context context) {
        super(context);
        mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.gridview, null);
        addView(view);
        zwaveDeviceManager = ZwaveDeviceManager.getInstance(mContext);
        zwaveDeviceRoomManager = ZwaveDeviceRoomManager.getInstance(mContext);
    }


    @Override
    public void refreshView() {
        List<RoomMember> roomList = getRoomList();
        GridView gvMember = (GridView) findViewById(R.id.gvMember);

        gvMember.setAdapter(new RoomListPage.roomAdapter(mContext, roomList));

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

                RoomMember member = DeviceInfo.roomList.get(position);

                Intent intent = new Intent(mContext,RoomActivity.class);
                intent.putExtra("ScenceName", member.getRoomName());

                ZwaveDeviceRoom tmpRoom = zwaveDeviceRoomManager.getRoom(member.getRoomName());

                if (tmpRoom==null) {

                    intent.putExtra("RoomCondition", "null");
                    intent.putExtra("SensorNodeId", "null");
                } else {

                    if (tmpRoom.getSensorNodeId()==null ) {
                        intent.putExtra("RoomCondition", "null");
                        intent.putExtra("SensorNodeId", "null");
                    }else {
                        ZwaveDevice sensorNode = zwaveDeviceManager.queryZwaveDevices(tmpRoom.getSensorNodeId());
                        intent.putExtra("RoomCondition", tmpRoom.getCondition());
                        intent.putExtra("SensorNodeId", String.valueOf(tmpRoom.getSensorNodeId()));
                    }
                }
                mContext.startActivity(intent);
            }
        });

    }

    private class roomAdapter extends BaseAdapter {

        Context context;

        roomAdapter(Context context, List<RoomMember> roomList) {
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

            RoomMember member = DeviceInfo.roomList.get(position);
            ImageView ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            TextView tvName = (TextView) itemView.findViewById(R.id.roomName);

            String tmpRoom = member.getRoomName();

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
                    case "Kitchen Room":
                        ivImage.setImageResource(R.drawable.kitchenroom);
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
            return DeviceInfo.roomList.get(position).getRoomId();
        }
    }

    private List<RoomMember> getRoomList() {

        DeviceInfo.roomList.clear();

        List<String> tmpNodeList = zwaveDeviceManager.getRoomNameList();

        if (tmpNodeList!=null) {

            for (int idx = 0; idx < tmpNodeList.size(); idx++) {
                //Log.i(LOG_TAG, "*** tmpList[" + idx + "] = " + tmpNodeList.get(idx));
                ZwaveDeviceRoom newRoom = new ZwaveDeviceRoom();

                DeviceInfo.roomList.add(new RoomMember(idx, tmpNodeList.get(idx),
                        "", ""));
            }
        }
        tmpNodeList.clear();

        return DeviceInfo.roomList;
    }

}
