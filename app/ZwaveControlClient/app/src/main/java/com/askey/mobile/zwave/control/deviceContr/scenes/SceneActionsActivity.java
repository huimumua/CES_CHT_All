package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.adapter.SwipeMenuAdapter;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.ActionChooseActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.ActionSummaryActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.ChooseDeviceActivity;
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

public class SceneActionsActivity extends AppCompatActivity implements View.OnClickListener{
    private SwipeMenuRecyclerView mRecycleView;
    private ImageView ivAddAction;
    private SwipeMenuAdapter swipeMenuAdapter;
    private List<Map<String,String>> datas;
    private LinearLayoutManager mLayoutManager;
    private String sceneIcon,sceneName,isFavorite,roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_action);

        initData();
        initView();
        Intent intent = getIntent();
        sceneIcon = intent.getStringExtra("sceneIcon");
        sceneName = intent.getStringExtra("sceneName");
        isFavorite = intent.getStringExtra("isFavorite");

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if ("ActionSummaryActivity".equals(getIntent().getStringExtra("from"))) {
            Map<String, String> one = new HashMap<>();
            one.put("type", getIntent().getStringExtra("type"));
            one.put("name", getIntent().getStringExtra("name"));
            one.put("action", getIntent().getStringExtra("action"));
            one.put("lightvalue", "50");
            one.put("timer", getIntent().getStringExtra("timer"));
            datas.add(one);
            swipeMenuAdapter.notifyDataSetChanged();
        }

    }

    private void initView() {
        mRecycleView = (SwipeMenuRecyclerView) findViewById(R.id.rv_available_commands);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setHasFixedSize(true);
        ivAddAction = (ImageView) findViewById(R.id.iv_add_action);
        ivAddAction.setOnClickListener(this);
        mRecycleView.setSwipeItemClickListener(new SwipeItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Intent intent = new Intent(SceneActionsActivity.this,ChooseDeviceActivity.class);
                intent.putExtra("from", SceneActionsActivity.class.getSimpleName());
//                intent.putExtra("name", datas.get(position).get("name"));
//                intent.putExtra("type", datas.get(position).get("type"));
//                intent.putExtra("nodeId", datas.get(position).get("nodeId"));
//                intent.putExtra("action", datas.get(position).get("action"));
//                intent.putExtra("timer", datas.get(position).get("timer"));
                startActivity(intent);
            }
        });

// 创建菜单：
        SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int viewType) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(SceneActionsActivity.this)
                        .setBackground(R.color.white)
                        .setImage(R.drawable.ic_close)
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
                Intent intent = new Intent(this,ChooseDeviceActivity.class);
                intent.putExtra("from", SceneActionsActivity.class.getSimpleName());
                startActivity(intent);
              break;
    }        }

    private List initData() {

        //test data
        datas = new ArrayList<>();
        return datas;
    }
}
