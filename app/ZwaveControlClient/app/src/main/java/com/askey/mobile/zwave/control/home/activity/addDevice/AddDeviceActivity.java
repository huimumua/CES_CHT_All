package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.dao.DeviceDao;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.home.adapter.SimpleDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class AddDeviceActivity extends BaseActivity implements View.OnClickListener, SimpleDeviceAdapter.OnItemClickListener{
    private ImageView search_icon;
    private RecyclerView device_recyclerview;
    private ProgressBar scan_progress;
    private TextView notify_msg;
    private Button search_btn, select_btn;
    private static Handler handler;
    private static final int CHECK_COMPLETE = 1;
    private static final int SEARCH_AGAIN = 2;
    private List<DeviceInfo> devcieList = new ArrayList<DeviceInfo>();
    private SimpleDeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        initView();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    //此处逻辑需优化，不能每次都创建adapter
                    case CHECK_COMPLETE:
                        scan_progress.setVisibility(View.GONE);
                        search_btn.setVisibility(View.VISIBLE);
                        if (devcieList != null && devcieList.size() > 0) {
                            search_icon.setVisibility(View.GONE);
                            device_recyclerview.setVisibility(View.VISIBLE);
                            notify_msg.setText(R.string.device_found);
                            adapter = new SimpleDeviceAdapter(devcieList);
                            adapter.setOnItemClickListener(AddDeviceActivity.this);
                            device_recyclerview.setAdapter(adapter);
                        } else {
                            notify_msg.setText(R.string.no_devices);
                        }
                        break;
                }
            }
        };
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initView() {
        search_icon = (ImageView) findViewById(R.id.search_icon);
        device_recyclerview = (RecyclerView) findViewById(R.id.device_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        device_recyclerview.setLayoutManager(layoutManager);
        scan_progress = (ProgressBar) findViewById(R.id.scan_progress);
        notify_msg = (TextView) findViewById(R.id.notify_msg);

        search_btn = (Button) findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);
        select_btn = (Button) findViewById(R.id.select_btn);
        select_btn.setOnClickListener(this);
    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    devcieList = DeviceDao.getAllDeviceInfo();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(CHECK_COMPLETE);
            }
        }).start();
    }

    private void searchAgain() {
        scan_progress.setVisibility(View.VISIBLE);
        device_recyclerview.setVisibility(View.GONE);
        search_btn.setVisibility(View.GONE);
        notify_msg.setText(R.string.scanning_for_devices);
        search_icon.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    devcieList = DeviceDao.getAllDeviceInfo();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(CHECK_COMPLETE);
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_btn:
                searchAgain();
                break;
            case R.id.select_btn:
                startActivity(new Intent(this,SelectBrandActivity.class));
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(View view, DeviceInfo deviceInfo) {
        startActivity(new Intent(this,InstallDeviceActivity.class));
        finish();
    }
}
