package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.adapter.WallMoteCommandsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WallMoteCommandsActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private WallMoteCommandsAdapter mCommandsAdapter;
    private List<Map<String,Object>> datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_available_commands);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        //test
        initData();
        mCommandsAdapter = new WallMoteCommandsAdapter(this,datas);
        mRecyclerView.setAdapter(mCommandsAdapter);
    }

    private List initData() {

        //test data
        datas = new ArrayList<>();
        Map<String, Object> one = new HashMap<>();
        one.put("type", "BULB");
        one.put("groupId", "2");
        one.put("endpointId", "1");
        datas.add(one);

        one = new HashMap<>();
        one.put("type", "PLUG");
        one.put("groupId", "2");
        one.put("endpointId", "1");
        datas.add(one);

        one = new HashMap<>();
        one.put("type", "BULB");
        one.put("groupId", "2");
        one.put("endpointId", "1");
        datas.add(one);
        return datas;
    }

}
