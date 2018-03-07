package com.askey.mobile.zwave.control.home.activity.addRoom;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.model.RoomInfo;
import com.askey.mobile.zwave.control.deviceContr.model.RoomInfo_state;
import com.askey.mobile.zwave.control.home.adapter.DeleteRoomAdapter;
import com.askey.mobile.zwave.control.home.fragment.RoomsFragment;

import java.util.ArrayList;
import java.util.List;

public class DeleteRoomActivity extends BaseActivity implements View.OnClickListener, DeleteRoomAdapter.OnItemClickListener{

    private String choose_room_name;//要删除的房间的名字，需要从本页面展示的list中移除
    private List<RoomInfo_state> dataList;
    private TextView room_notify;
    private RecyclerView room_recycler;
    private Button delete_btn;
    private DeleteRoomAdapter adapter;
    private RoomInfo will_delete_room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_room);

        initView();
        initData();
    }



    private void initView() {
        room_notify = (TextView) findViewById(R.id.room_notify);
        delete_btn = (Button) findViewById(R.id.delete_btn);
        delete_btn.setOnClickListener(this);

        room_recycler = (RecyclerView) findViewById(R.id.room_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        room_recycler.setLayoutManager(layoutManager);
    }
    private void initData(){
        dataList = new ArrayList<>();
        choose_room_name = getIntent().getStringExtra("name");
        room_notify.setText(choose_room_name);
        for (RoomInfo info : RoomsFragment.roomInfoList) {
            if (info.getRoomName().equals(choose_room_name)) {
                will_delete_room = info;
                continue;
            }
            RoomInfo_state roomInfo_state = new RoomInfo_state();
            if (info.getRoomName().equals("My Home") || info.getRoomName().equals("MyHome")) {
                roomInfo_state.setSelected(true);
            }
            roomInfo_state.setRoomId(info.getRoomId());
            roomInfo_state.setRoomName(info.getRoomName());
            dataList.add(roomInfo_state);
        }

        adapter = new DeleteRoomAdapter(dataList,room_recycler);
        adapter.setOnItemClickListener(this);
        room_recycler.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_btn:
                //在这里将will_delete_room从房间列表中删除，同时进行设备的分配

                break;
        }
    }

    @Override
    public void deleteItem(RoomInfo_state roomInfo_state) {
        //此处回调的是要把device分配到的房间信息,目前暂时将对象回调回来，后续可根据需求修改
    }
}
