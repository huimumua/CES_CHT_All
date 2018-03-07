package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.adapter.CommandsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailableActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CommandsAdapter mCommandsAdapter;
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
        mCommandsAdapter = new CommandsAdapter(this,datas);
        mRecyclerView.setAdapter(mCommandsAdapter);
    }

    private List initData() {

        //test data
        datas = new ArrayList<>();
        Map<String, Object> one = new HashMap<>();
        one.put("type", "bulb");
        one.put("device_name", "Dinner table 1");
        one.put("action", "Toggle");
        one.put("lightvalue", "50");
        one.put("timmer", "15");
        datas.add(one);

        one = new HashMap<>();
        one.put("type", "bulb");
        one.put("device_name", "Dinner table 1");
        one.put("action", "Toggle");
        one.put("lightvalue", "50");
        one.put("timmer", "15");
        datas.add(one);

        one = new HashMap<>();
        one.put("type", "bulb");
        one.put("device_name", "Dinner table 1");
        one.put("action", "Toggle");
        one.put("lightvalue", "50");
        one.put("timmer", "15");
        datas.add(one);
        return datas;
    }

}
