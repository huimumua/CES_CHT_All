package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.home.activity.addRoom.AddRoomActivity;
import com.askey.mobile.zwave.control.home.adapter.SelectRoomAdapter;
import com.askey.mobile.zwave.control.home.fragment.RoomsFragment;

public class ChooseRoomActivity extends AppCompatActivity implements View.OnClickListener, SelectRoomAdapter.OnItemClickListener {

    private RecyclerView room_recycler;
    private TextView manage_room_tv;
    private SelectRoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_room);

        initView();
        initData();
    }

    private void initView() {
        room_recycler = (RecyclerView) findViewById(R.id.room_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        room_recycler.setLayoutManager(layoutManager);

        manage_room_tv = (TextView) findViewById(R.id.manage_room_tv);
        manage_room_tv.setOnClickListener(this);
    }

    private void initData() {
        adapter = new SelectRoomAdapter(RoomsFragment.roomInfoList);
        adapter.setOnItemClickListener(this);
        room_recycler.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.manage_room_tv:
                startActivity(new Intent(this, AddRoomActivity.class));
                break;
        }
    }

    @Override
    public void onItemClick(View view, String roomName) {
        Intent intent = new Intent(this, InstallSuccessActivity.class);
        intent.putExtra("roomName", roomName);
        setResult(RESULT_OK, intent);
        finish();
    }
}
