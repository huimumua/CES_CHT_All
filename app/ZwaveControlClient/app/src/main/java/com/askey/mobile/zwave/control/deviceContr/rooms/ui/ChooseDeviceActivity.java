package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.adapter.ChooseActionAdapter;
import com.askey.mobile.zwave.control.deviceContr.adapter.RecyclerAdapter;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseDeviceActivity extends AppCompatActivity {
    private final String LOG_TAG = ActionChooseActivity.class.getSimpleName();
    private SwipeMenuRecyclerView mRecycleView;
    private LinearLayoutManager mLayoutManager;
    private ChooseActionAdapter mAdapter;
    private List<Map<String, Object>> datas;
    private Intent fromIntent;
    private String fromActivity;
    private TextView mTitle;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_choose);


        initView();

    }


    private void initView() {
        fromIntent = getIntent();
        mTitle = (TextView) findViewById(R.id.tv_title);
        mTitle.setText(getResources().getString(R.string.choose_device_title));

        mRecycleView = (SwipeMenuRecyclerView) findViewById(R.id.rv_choose_action);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setHasFixedSize(true);

        initData();
        mAdapter = new ChooseActionAdapter(this,datas);
        mRecycleView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Intent intent = null;
                fromActivity = fromIntent.getStringExtra("from");

                if (fromActivity != null && (ActionChooseActivity.class.getSimpleName()).equals(fromActivity)) {
                    intent = new Intent(ChooseDeviceActivity.this, DoActionActivity.class);

                    intent.putExtra("from", ChooseDeviceActivity.class.getSimpleName());
                    intent.putExtra("arr","arr");//需要从nodeid获取
                    intent.putExtra("nodeId", fromIntent.getStringExtra("nodeId"));
                    intent.putExtra("endpointId", fromIntent.getStringExtra("endpointId"));
                    intent.putExtra("groupId", fromIntent.getStringExtra("groupId"));
                    intent.putExtra("type",String.valueOf(datas.get(position).get("type")));
                    startActivity(intent);

                } else if (fromActivity != null && (ActionSummaryActivity.class.getSimpleName()).equals(fromActivity)) {
                    intent = new Intent(ChooseDeviceActivity.this, ActionSummaryActivity.class);

                    intent.putExtra("from", ChooseDeviceActivity.class.getSimpleName());
                    intent.putExtra("nodeId", fromIntent.getStringExtra("nodeId"));
                    intent.putExtra("endpointId", fromIntent.getStringExtra("endpointId"));
                    intent.putExtra("groupId", fromIntent.getStringExtra("groupId"));
                    intent.putExtra("arr","arr");//需要从nodeid获取
                    intent.putExtra("type", String.valueOf(datas.get(position).get("type")));
                    intent.putExtra("action", fromIntent.getStringExtra("action"));
                    intent.putExtra("timer", fromIntent.getStringExtra("timer"));
                    startActivity(intent);
                }
            }
        });
    }

    private void initData() {
        //获取出来data
        datas = new ArrayList<>();
        int[] icon = new int[]{R.drawable.vector_drawable_ic_117,R.drawable.vector_drawable_ic_80,R.drawable.vector_drawable_ic_81,R.drawable.vector_drawable_ic_device_79,R.drawable.vector_drawable_ic_65};
        Map<String, Object> one = new HashMap<>();
        one.put("icon", icon[0]);
        one.put("name", getResources().getString(R.string.espresso_machine));
        one.put("type", "plug");
        datas.add(one);

        one = new HashMap<>();
        one.put("icon", icon[1]);
        one.put("name", getResources().getString(R.string.power_strip));
        one.put("type", "bulb");
        datas.add(one);

        one = new HashMap<>();
        one.put("icon", icon[2]);
        one.put("name",  getResources().getString(R.string.tv_back));
        one.put("type", "bulb");
        datas.add(one);

        one = new HashMap<>();
        one.put("icon", icon[3]);
        one.put("name",  getResources().getString(R.string.dinner_table_1));
        one.put("type", "bulb");
        datas.add(one);

        one = new HashMap<>();
        one.put("icon", icon[4]);
        one.put("name",  getResources().getString(R.string.all_off));
        one.put("type", "bulb");
        datas.add(one);

        one = new HashMap<>();
        one.put("icon", icon[4]);
        one.put("name",  getResources().getString(R.string.evening_mood));
        one.put("type", "bulb");
        datas.add(one);
    }
}
