package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.adapter.RecyclerItemDecoration;
import com.askey.mobile.zwave.control.deviceContr.adapter.SwipeMenuAdapter;
import com.yanzhenjie.recyclerview.swipe.SwipeItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class SetupKeyActivity extends AppCompatActivity implements View.OnClickListener{
    private SwipeMenuRecyclerView mRecycleView;
    private ImageView ivAddAction;
    private SwipeMenuAdapter swipeMenuAdapter;
    private List<Map<String,String>> datas;
    private LinearLayoutManager mLayoutManager;
    private String nodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_key);

        initData();
        initView();


    }

    private void initView() {
        nodeId = getIntent().getStringExtra("nodeId");

        mRecycleView = (SwipeMenuRecyclerView) findViewById(R.id.rv_available_commands);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setHasFixedSize(true);
        ivAddAction = (ImageView) findViewById(R.id.iv_add_action);
        ivAddAction.setOnClickListener(this);
        mRecycleView.setSwipeItemClickListener(new SwipeItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(SetupKeyActivity.this,ActionSummaryActivity.class);
                startActivity(intent);
            }
        });

// 创建菜单：
        SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int viewType) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(SetupKeyActivity.this)
                        .setBackground(R.color.white)
                        .setImage(R.mipmap.ic_launcher)
                        .setWidth(100) // 宽度。
                        .setHeight(MATCH_PARENT); // 高度。
                 // 各种文字和图标属性设置。
                leftMenu.addMenuItem(deleteItem); // 在Item左侧添加一个菜单。
            }
        };


        SwipeMenuItemClickListener mMenuItemClickListener = new SwipeMenuItemClickListener() {
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge) {
                // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
                menuBridge.closeMenu();

                int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
                int adapterPosition = menuBridge.getAdapterPosition(); // RecyclerView的Item的position。
                int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
                datas.remove(menuBridge.getPosition());
                swipeMenuAdapter.notifyItemRemoved(adapterPosition);

            }
        };

        // 设置监听器。
        mRecycleView.setSwipeMenuCreator(mSwipeMenuCreator);
        // 菜单点击监听。
        mRecycleView.setSwipeMenuItemClickListener(mMenuItemClickListener);
        swipeMenuAdapter = new SwipeMenuAdapter(this,datas);
        mRecycleView.setAdapter(swipeMenuAdapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_add_action:
                Intent intent = new Intent(this,ActionChooseActivity.class);
                intent.putExtra("from", SetupKeyActivity.class.getSimpleName());
                intent.putExtra("nodeId",nodeId);
                startActivity(intent);
              break;
    }        }

    private List initData() {

        //test data 要根据命令显示图标  获取出来
        datas = new ArrayList<>();
        Map<String, String> one = new HashMap<>();
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
