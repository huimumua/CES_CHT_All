package com.askey.mobile.zwave.control.home.activity.addRoom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.home.adapter.RoomManageAdapter;
import com.askey.mobile.zwave.control.home.fragment.RoomsFragment;

import static com.askey.mobile.zwave.control.home.fragment.RoomsFragment.roomInfoList;

public class AddRoomActivity extends BaseActivity implements View.OnClickListener, RoomManageAdapter.OnItemClickListener {
    private RecyclerView room_recycler;
    private TextView add_room_tv;
    private RoomManageAdapter adapter;
    private static ModifyRoomListener modifyRoomListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        initView();
    }

    private void initView() {
        room_recycler = (RecyclerView) findViewById(R.id.room_recycler);
        add_room_tv = (TextView) findViewById(R.id.add_room_tv);
        add_room_tv.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        room_recycler.setLayoutManager(layoutManager);
        adapter = new RoomManageAdapter(roomInfoList);
        adapter.setOnItemClickListener(this);
        room_recycler.setAdapter(adapter);
    }

    @Override
    public void modifyNameClick(View view, int position) {
        Log.d("Click","modifyNameClick ");
    }

    @Override
    public void modifyNameConfirmClick(View view, String roomName) {
        Log.d("Click","modifyNameConfirmClick "+ roomName);
        if (roomName.equals("")) {
            Toast.makeText(this, "房间名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < roomInfoList.size(); i++) {
            if (roomInfoList.get(i).getRoomName().equals(roomName)) {
                Toast.makeText(this, "不能使用相同的房间名", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (adapter.getMode() == RoomManageAdapter.EDIT_MODE) {
            adapter.setMode(RoomManageAdapter.NORMAL_MODE);
            modifyRoomListener.addRoom(roomName);
            //需要等待modifyRoomListener.addRoom(roomName)成功之后再回调这里notify
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void deleteRoomClick(int position) {
        Log.d("Click","deleteRoomClick "+position);
        String choose_name = RoomsFragment.roomInfoList.get(position).getRoomName();
        Intent intent = new Intent(this, DeleteRoomActivity.class);
        intent.putExtra("name",choose_name);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_room_tv:
                if (adapter.getMode() == RoomManageAdapter.NORMAL_MODE) {
                    adapter.setMode(RoomManageAdapter.EDIT_MODE);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    public interface ModifyRoomListener {
        void addRoom(String roomName);
        void deleteRoom(int position);
    }

    public static void setModifyRoomListener(ModifyRoomListener listener) {
        modifyRoomListener = listener;
    }
}
