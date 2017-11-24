package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceListActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    public static String LOG_TAG = "DeviceListActivity";
    private TextView brand_name;
    private ImageView brand_icon;
    private GridView device_list;
    private ArrayList<HashMap<String,Object>> datas;
    private SimpleAdapter simpleAdapter;
    private static final int SMART_SWITCH = 0;
    private static final int LED_BULB = 1;
    private static final int WALLMOTE_QUAD = 2;
    private static final int RANGE_EXTENDER = 3;
    private static final int SENSOR = 4;
    private static final int DIMMER = 5;
    private String brand = "";
  private String[] items=new String[]{"SENSOR","BULB","DIMMER","PLUG","SWITCH","WALLMOTE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        brand_name = (TextView) findViewById(R.id.brand_name);
        brand_icon = (ImageView) findViewById(R.id.brand_icon);
        device_list = (GridView) findViewById(R.id.device_list);
        device_list.setOnItemClickListener(this);

        datas = new ArrayList<>();

        brand = getIntent().getStringExtra("brand");

        HashMap<String,Object> map0 = new HashMap<>();
        map0.put("image",R.drawable.ic_launcher);
        map0.put("device",items[0]);
        datas.add(map0);

        HashMap<String,Object> map1 = new HashMap<>();
        map1.put("image",R.drawable.ic_launcher);
        map1.put("device",items[1]);
        datas.add(map1);

        HashMap<String,Object> map2 = new HashMap<>();
        map2.put("image",R.drawable.ic_launcher);
        map2.put("device",items[2]);
        datas.add(map2);

        HashMap<String,Object> map3 = new HashMap<>();
        map3.put("image",R.drawable.ic_launcher);
        map3.put("device",items[3]);
        datas.add(map3);

        HashMap<String,Object> map4 = new HashMap<>();
        map4.put("image",R.drawable.ic_launcher);
        map4.put("device",items[4]);
        datas.add(map4);

        HashMap<String,Object> map5 = new HashMap<>();
        map5.put("image",R.drawable.ic_launcher);
        map5.put("device",items[5]);
        datas.add(map5);

        simpleAdapter = new SimpleAdapter(this,datas,R.layout.brand_item,new String[]{"image","device"},new int[]{R.id.brand_icon,R.id.brand_name});

        device_list.setAdapter(simpleAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //不确定具体需要传递什么信息
        Intent intent = new Intent();
        intent.setClass(this,InstallDeviceActivity.class);
        intent.putExtra("brand",brand);
        switch (position) {
            case SMART_SWITCH:
                intent.putExtra("deviceType",items[0]);
                break;
            case LED_BULB:
                intent.putExtra("deviceType",items[1]);
                break;
            case WALLMOTE_QUAD:
                intent.putExtra("deviceType",items[2]);
                break;
            case RANGE_EXTENDER:
                intent.putExtra("deviceType",items[3]);
                break;
            case SENSOR:
                intent.putExtra("deviceType",items[4]);
                break;
            case DIMMER:
                intent.putExtra("deviceType",items[5]);
                break;
        }
        startActivity(intent);
        finish();
    }




}
