package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ListMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.adapter.RecyclerAdapter;
import com.askey.mobile.zwave.control.deviceContr.adapter.SelectRoomAdapter;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectRoomActivity extends AppCompatActivity implements View.OnClickListener{
    private SwipeMenuRecyclerView mRecycleView;
    private LinearLayoutManager mLayoutManager;
    private SelectRoomAdapter mAdapter;
    private List<String> datas;
    private ImageView ivManageRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_room);

        initView();
    }

    private void initView() {

        mRecycleView = (SwipeMenuRecyclerView) findViewById(R.id.rv_list);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setHasFixedSize(true);
        ivManageRoom = (ImageView) findViewById(R.id.iv_manage_room);
        ivManageRoom.setOnClickListener(this);

        initData();
        mAdapter = new SelectRoomAdapter(this,datas);
        mRecycleView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });
    }

    private void initData() {
        datas = new ArrayList<>();

        datas.add("My Home");
        datas.add("Kitchen");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_manage_room:
                Intent intent = new Intent(this,ManageRoomActivity.class);
                startActivity(intent);
                break;
        }
    }
}
