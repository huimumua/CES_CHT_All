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

public class DoActionActivity extends AppCompatActivity {

    private final String LOG_TAG = DoActionActivity.class.getSimpleName();
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
        mTitle.setText(getResources().getString(R.string.do_title));

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

                if (fromActivity != null && (ChooseDeviceActivity.class.getSimpleName()).equals(fromActivity)) {
                    if (datas.get(position).get("name").equals("Toggle")) {
                        intent = new Intent(DoActionActivity.this, ToggleActivity.class);

                    } else {
                        intent = new Intent(DoActionActivity.this, TimerActivity.class);
                    }

                    intent.putExtra("from", DoActionActivity.class.getSimpleName());
                    intent.putExtra("arr", fromIntent.getStringExtra("arr"));//需要从nodeid获取
                    intent.putExtra("nodeId", fromIntent.getStringExtra("nodeId"));
                    intent.putExtra("endpointId", fromIntent.getStringExtra("endpointId"));
                    intent.putExtra("groupId", fromIntent.getStringExtra("groupId"));
                    intent.putExtra("type", fromIntent.getStringExtra("type"));
                    intent.putExtra("action",String.valueOf(datas.get(position).get("name")));
                    startActivity(intent);

                } else if (fromActivity != null && (ActionSummaryActivity.class.getSimpleName()).equals(fromActivity)) {
                    intent = new Intent(DoActionActivity.this, ActionSummaryActivity.class);

                    intent.putExtra("from", DoActionActivity.class.getSimpleName());
                    intent.putExtra("nodeId", fromIntent.getStringExtra("nodeId"));
                    intent.putExtra("endpointId", fromIntent.getStringExtra("endpointId"));
                    intent.putExtra("groupId", fromIntent.getStringExtra("groupId"));
                    intent.putExtra("arr","arr");//需要从nodeid获取
                    intent.putExtra("type", fromIntent.getStringExtra("type"));
                    intent.putExtra("action", String.valueOf(datas.get(position).get("name")));
                    intent.putExtra("timer", fromIntent.getStringExtra("timer"));
                    startActivity(intent);
                }
            }
        });
    }

    private void initData() {
        datas = new ArrayList<>();
        Map<String, Object> one = new HashMap<>();
        one.put("icon", R.drawable.vector_drawable_ic_64);
        one.put("name", getResources().getString(R.string.switch_on_action));
        datas.add(one);

        one = new HashMap<>();
        one.put("icon", R.drawable.vector_drawable_ic_64);
        one.put("name", getResources().getString(R.string.switch_off_action));
        datas.add(one);
        one = new HashMap<>();
        one.put("icon", R.drawable.vector_drawable_ic_64);
        one.put("name", getResources().getString(R.string.toggle_action));
        datas.add(one);
    }
}
