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
import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;
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
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class SceneActionsActivity extends AppCompatActivity implements View.OnClickListener{
    private String LOG_TAG = SceneActionsActivity.class.getSimpleName();
    private SwipeMenuRecyclerView mRecycleView;
    private ImageView ivAddAction;
    private SwipeMenuAdapter swipeMenuAdapter;
    private List<ScenesInfo> datas;
    private LinearLayoutManager mLayoutManager;
    private int mActionId = -1;
    private ScenesInfo scenesInfo;
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
        scenesInfo = getIntent().getParcelableExtra("scenesInfo");
        Log.i(LOG_TAG, "=====getType===" + scenesInfo.getCategory());
        Log.i(LOG_TAG, "=====getAction===" + scenesInfo.getTargetSatus());
        Log.i(LOG_TAG, "=====getLightValue===" + scenesInfo.getTargetColor());
        Log.i(LOG_TAG, "=====getName===" + scenesInfo.getDeviceName());
        Log.i(LOG_TAG, "=====getNodeId===" + scenesInfo.getNodeId());
        Log.i(LOG_TAG, "=====getTimer===" + scenesInfo.getTimer());
        Log.i(LOG_TAG, "=====getActionId===" + scenesInfo.getScenesId());

        if (mActionId == Integer.parseInt(scenesInfo.getScenesId())) {
            datas.get(mActionId).setTargetSatus(scenesInfo.getTargetSatus());
            datas.get(mActionId).setTimer(scenesInfo.getTimer());
            datas.get(mActionId).setTargetColor(scenesInfo.getTargetColor());
            datas.get(mActionId).setDeviceName(scenesInfo.getDeviceName());
            datas.get(mActionId).setNodeId(scenesInfo.getNodeId());
            datas.get(mActionId).setScenesId(scenesInfo.getScenesId());
            datas.get(mActionId).setCategory(scenesInfo.getCategory());
        } else {
            datas.add(scenesInfo);
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
                intent.putExtra("scenesInfo", datas.get(position));
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
                ScenesInfo scenesInfo = new ScenesInfo();
//                datas.add(scenesInfo);
                scenesInfo.setScenesId(String.valueOf(datas.size()));
                scenesInfo.setScenesName("test");
                scenesInfo.setIconName("test");
                intent.putExtra("scenesInfo", scenesInfo);
                startActivity(intent);
              break;
    }        }

}
