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

public class ActionChooseActivity extends AppCompatActivity {
    private final String LOG_TAG = ActionChooseActivity.class.getSimpleName();
    private SwipeMenuRecyclerView mRecycleView;
    private LinearLayoutManager mLayoutManager;
    private ChooseActionAdapter mAdapter;
    private List<Map<String, Object>> datas;
    private String nodeId;
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
        mTitle.setText(getResources().getString(R.string.choose_action_title));
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
                if (fromActivity != null && (SetupKeyActivity.class.getSimpleName()).equals(fromActivity)) {
                    intent = new Intent(ActionChooseActivity.this,ChooseDeviceActivity.class);

                    intent.putExtra("from", ActionChooseActivity.class.getSimpleName());
                    intent.putExtra("nodeId",fromIntent.getStringExtra("nodeId"));
                    intent.putExtra("endpointId",String.valueOf(datas.get(position).get("endpointId")));
                    intent.putExtra("groupId",String.valueOf(datas.get(position).get("groupId")));
                    startActivity(intent);

                } else if (fromActivity != null && (ActionSummaryActivity.class.getSimpleName()).equals(fromActivity)) {
                    intent = new Intent(ActionChooseActivity.this,ActionSummaryActivity.class);

                    intent.putExtra("from", ActionChooseActivity.class.getSimpleName());
                    intent.putExtra("nodeId",fromIntent.getStringExtra("nodeId"));
                    intent.putExtra("endpointId",String.valueOf(datas.get(position).get("endpointId")));
                    intent.putExtra("groupId",String.valueOf(datas.get(position).get("groupId")));
                    intent.putExtra("arr",fromIntent.getStringExtra("arr"));
                    intent.putExtra("type",fromIntent.getStringExtra("type"));
                    intent.putExtra("action",fromIntent.getStringExtra("action"));
                    intent.putExtra("timer",fromIntent.getStringExtra("timer"));
                    startActivity(intent);
                }

            }
        });
    }

    private void initData() {
        datas = new ArrayList<>();
        int[] icon = new int[]{ R.drawable.vector_drawable_ic_99,R.drawable.ic_launcher,R.drawable.vector_drawable_ic_100,R.drawable.vector_drawable_ic_106,
                R.drawable.vector_drawable_ic_105,R.drawable.vector_drawable_ic_107,R.drawable.vector_drawable_ic_108};
        Map<String, Object> one = new HashMap<>();
        one.put("icon", icon[0]);
        one.put("name", getResources().getString(R.string.touch));
        one.put("endpointId", 3);//添加到哪一块
        one.put("groupId", 2);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon", icon[1]);//此处缺一个图
        one.put("name", getResources().getString(R.string.touch));
        one.put("endpointId", 4);
        one.put("groupId", 2);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon",  icon[2]);
        one.put("name", getResources().getString(R.string.swipe));
        one.put("endpointId", 1);
        one.put("groupId", 3);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon",  icon[3]);
        one.put("name", getResources().getString(R.string.swipe));
        one.put("endpointId", 2);
        one.put("groupId", 3);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon",  icon[4]);
        one.put("name", getResources().getString(R.string.swipe));
        one.put("endpointId", 3);
        one.put("groupId", 3);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon",  icon[5]);
        one.put("name", getResources().getString(R.string.touch_hold));
        one.put("endpointId", 1);
        one.put("groupId", 2);// on/off
        datas.add(one);
    }
}
