package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.adapter.ChooseActionAdapter;
import com.askey.mobile.zwave.control.deviceContr.adapter.RecyclerAdapter;
import com.askey.mobile.zwave.control.deviceContr.model.WallMoteActionInfo;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WallMoteActionActivity extends BaseActivity {
    private final String LOG_TAG = WallMoteActionActivity.class.getSimpleName();
    private SwipeMenuRecyclerView mRecycleView;
    private LinearLayoutManager mLayoutManager;
    private ChooseActionAdapter mAdapter;
    private List<Map<String, Object>> datas;
    private String nodeId;
    private Intent fromIntent;
    private String fromActivity;
    private TextView mTitle;
    private WallMoteActionInfo  wallMoteActionInfo;

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

        Intent intent = this.getIntent();
         wallMoteActionInfo=(WallMoteActionInfo)intent.getSerializableExtra("wallMoteActionInfo");

        initData();
        mAdapter = new ChooseActionAdapter(this,datas);
        mRecycleView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = null;
                intent = new Intent(mContext,WallMoteDeviceActivity.class);
                intent.putExtra("nodeId",fromIntent.getStringExtra("nodeId"));
                intent.putExtra("endpointId",String.valueOf(datas.get(position).get("endpointId")));
                intent.putExtra("groupId",String.valueOf(datas.get(position).get("groupId")));
                intent.putExtra("icon",String.valueOf(datas.get(position).get("icon")));

                Bundle bundle = new Bundle();
                bundle.putSerializable("wallMoteActionInfo", (Serializable) wallMoteActionInfo);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });
    }

    private void initData() {
        datas = new ArrayList<>();
        int[] icon = new int[]{ R.drawable.vector_drawable_wmplu,R.drawable.vector_drawable_wmpru,R.drawable.vector_drawable_wmpld,R.drawable.vector_drawable_wmprd,
        R.drawable.vector_drawable_wmslu,R.drawable.vector_drawable_wmsru,R.drawable.vector_drawable_wmsld,R.drawable.vector_drawable_wmsrd
               };
        Map<String, Object> one = new HashMap<>();
        one.put("icon", icon[0]);
        one.put("name", getResources().getString(R.string.touch_left_up));
        one.put("endpointId", 0);//添加到哪一块
        one.put("groupId", 2);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon", icon[1]);//此处缺一个图
        one.put("name", getResources().getString(R.string.touch_right_up));
        one.put("endpointId", 0);
        one.put("groupId", 4);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon",  icon[2]);
        one.put("name", getResources().getString(R.string.touch_left_down));
        one.put("endpointId", 0);
        one.put("groupId", 6);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon",  icon[3]);
        one.put("name", getResources().getString(R.string.touch_right_down));
        one.put("endpointId", 0);
        one.put("groupId", 8);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon",  icon[4]);
        one.put("name", getResources().getString(R.string.slide_left_up));
        one.put("endpointId", 0);
        one.put("groupId", 3);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon",  icon[5]);
        one.put("name", getResources().getString(R.string.slide_right_up));
        one.put("endpointId", 0);
        one.put("groupId", 5);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon",  icon[6]);
        one.put("name", getResources().getString(R.string.slide_left_down));
        one.put("endpointId", 0);
        one.put("groupId", 7);// on/off
        datas.add(one);

        one = new HashMap<>();
        one.put("icon",  icon[7]);
        one.put("name", getResources().getString(R.string.slide_right_down));
        one.put("endpointId", 0);
        one.put("groupId", 9);// on/off
        datas.add(one);
    }
}
