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
    private static final int BULB = 0;
    private static final int PLUG = 1;
    private static final int WALLMOTE = 2;
    private static final int EXTENDER = 3;
    private static final int DIMMER = 4;
    private static final int SENSOR = 5;
    private static final int PST02 = 6;
    private String brand = "";
    private String[] items=new String[]{"BULB","PLUG","WALLMOTE","EXTENDER", "DIMMER", "SENSOR", "PST02"};
    private int[] icon = new int[]{R.mipmap.bulb_icon,R.mipmap.switch_icon,R.mipmap.wallmote_icon,R.drawable.exdenter_product ,R.drawable.ic_zwgeneral};

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
//        if(brand.equals("Aeotec")){
//            brand_icon.setBackgroundResource(R.mipmap.aeotec_logo_big);
//        }

        HashMap<String,Object> map = new HashMap<>();
        map.put("image",icon[0]);
        map.put("device",items[0]);
        datas.add(map);

        HashMap<String,Object> map1 = new HashMap<>();
        map1.put("image",icon[1]);
        map1.put("device",items[1]);
        datas.add(map1);

        HashMap<String,Object> map2 = new HashMap<>();
        map2.put("image",icon[2]);
        map2.put("device",items[2]);
        datas.add(map2);

        HashMap<String,Object> map3 = new HashMap<>();
        map3.put("image",icon[3]);
        map3.put("device",items[3]);
        datas.add(map3);

        HashMap<String,Object> map4 = new HashMap<>();
        map4.put("image",icon[4]);
        map4.put("device",items[4]);
        datas.add(map4);

        HashMap<String,Object> map5 = new HashMap<>();
        map5.put("image",icon[4]);
        map5.put("device",items[5]);
        datas.add(map5);

        HashMap<String,Object> map6 = new HashMap<>();
        map6.put("image",icon[4]);
        map6.put("device",items[6]);
        datas.add(map6);

        simpleAdapter = new SimpleAdapter(this,datas,R.layout.brand_item,new String[]{"image","device"},new int[]{R.id.brand_icon,R.id.brand_name});

        device_list.setAdapter(simpleAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //不确定具体需要传递什么信息
        Intent intent = new Intent();
        intent.setClass(this,InstallGuideActivity.class);
        intent.putExtra("brand",brand);
        switch (position) {
            case BULB:
                intent.putExtra("deviceType",items[0]);
                intent.putExtra("deviceIcon",icon[0]);
                break;
            case PLUG:
                intent.putExtra("deviceType",items[1]);
                intent.putExtra("deviceIcon",icon[1]);
                break;
            case WALLMOTE:
                intent.putExtra("deviceType",items[2]);
                intent.putExtra("deviceIcon",icon[2]);
                break;
            case EXTENDER:
                intent.putExtra("deviceType",items[3]);
                intent.putExtra("deviceIcon",icon[3]);
                break;
            case DIMMER:
                //目前咩有引导页，暂时这样修改
//                intent.putExtra("deviceType",items[4]);
//                intent.putExtra("deviceIcon",icon[4]);
                Intent intent1 = new Intent(this, InstallDeviceActivity.class);
                intent1.putExtra("brand", brand);
                intent1.putExtra("deviceType",items[4]);
                startActivity(intent1);
                return;
            case SENSOR:
//                intent.putExtra("deviceType",items[5]);
//                intent.putExtra("deviceIcon",icon[4]);
                Intent intent2 = new Intent(this, InstallDeviceActivity.class);
                intent2.putExtra("brand", brand);
                intent2.putExtra("deviceType",items[5]);
                startActivity(intent2);
                return;
            case PST02:
//                intent.putExtra("deviceType",items[6]);
//                intent.putExtra("deviceIcon",icon[4]);
                Intent intent3 = new Intent(this, InstallDeviceActivity.class);
                intent3.putExtra("brand", brand);
                intent3.putExtra("deviceType",items[6]);
                startActivity(intent3);
                return;
        }
        startActivity(intent);
        finish();
    }




}
