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
import com.askey.mobile.zwave.control.deviceContr.model.ScheduleInfo;
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
    private String LOG_TAG = SceneActionsActivity.class.getSimpleName();
    private SwipeMenuRecyclerView mRecycleView;
    private ImageView ivAddAction;
    private SwipeMenuAdapter swipeMenuAdapter;
    private List<SceneActionInfo> datas;
    private LinearLayoutManager mLayoutManager;
    private int mActionId = -1;
    private SceneActionInfo sceneActionInfo;
    private String sceneIcon,sceneName,isFavorite,roomName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_action);

        datas = new ArrayList<>();
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
        sceneActionInfo = getIntent().getParcelableExtra("sceneActionInfo");
        Log.i(LOG_TAG, "=====getType===" + sceneActionInfo.getType());
        Log.i(LOG_TAG, "=====getAction===" + sceneActionInfo.getAction());
        Log.i(LOG_TAG, "=====getLightValue===" + sceneActionInfo.getLightValue());
        Log.i(LOG_TAG, "=====getName===" + sceneActionInfo.getName());
        Log.i(LOG_TAG, "=====getNodeId===" + sceneActionInfo.getNodeId());
        Log.i(LOG_TAG, "=====getTimer===" + sceneActionInfo.getTimer());
        Log.i(LOG_TAG, "=====getActionId===" + sceneActionInfo.getActionId() + "");

        if (mActionId == sceneActionInfo.getActionId()) {
            datas.get(mActionId).setAction(sceneActionInfo.getAction());
            datas.get(mActionId).setTimer(sceneActionInfo.getTimer());
            datas.get(mActionId).setLightValue(sceneActionInfo.getLightValue());
            datas.get(mActionId).setName(sceneActionInfo.getName());
            datas.get(mActionId).setNodeId(sceneActionInfo.getNodeId());
            datas.get(mActionId).setActionId(sceneActionInfo.getActionId());
            datas.get(mActionId).setType(sceneActionInfo.getType());
        } else {
            datas.add(sceneActionInfo);
        }
        swipeMenuAdapter.notifyDataSetChanged();
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

                Intent intent = new Intent(SceneActionsActivity.this,ActionSummaryActivity.class);
                intent.putExtra("from", SceneActionsActivity.class.getSimpleName());
                mActionId = position;
//                datas.get(position).setActionId(position);
                intent.putExtra("sceneActionInfo", datas.get(position));
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
                Log.i(LOG_TAG, "onClick");
                intent.putExtra("from", SceneActionsActivity.class.getSimpleName());
                SceneActionInfo sceneActionInfo = new SceneActionInfo();
//                datas.add(sceneActionInfo);
                sceneActionInfo.setActionId(datas.size());
                intent.putExtra("sceneActionInfo", sceneActionInfo);
                startActivity(intent);
              break;
    }        }

}
