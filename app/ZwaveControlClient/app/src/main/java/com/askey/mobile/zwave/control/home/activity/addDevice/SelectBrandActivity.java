package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectBrandActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    public static String LOG_TAG = "SelectBrandActivity";
    private GridView brand_list;
    private ArrayList<HashMap<String,Object>> datas;
    private SimpleAdapter simpleAdapter;
    private static final int AEOTEC = 0;
    private static final int GUNITECH = 1;
    private static final int PHILIPS = 2;
    private static final int MYSTROM = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        brand_list = (GridView) findViewById(R.id.brand_list);
        brand_list.setOnItemClickListener(this);

        datas = new ArrayList<>();

        HashMap<String,Object> map0 = new HashMap<>();
        map0.put("image",R.drawable.ic_launcher);
        map0.put("brand",getString(R.string.aeotec));
        datas.add(map0);

        HashMap<String,Object> map1 = new HashMap<>();
        map1.put("image",R.drawable.ic_launcher);
        map1.put("brand",getString(R.string.gunitech));
        datas.add(map1);

        HashMap<String,Object> map2 = new HashMap<>();
        map2.put("image",R.drawable.ic_launcher);
        map2.put("brand",getString(R.string.philips));
        datas.add(map2);

        HashMap<String,Object> map3 = new HashMap<>();
        map3.put("image",R.drawable.ic_launcher);
        map3.put("brand",getString(R.string.mystrom));
        datas.add(map3);


        simpleAdapter = new SimpleAdapter(this,datas,R.layout.brand_item,new String[]{"image","brand"},new int[]{R.id.brand_icon,R.id.brand_name});

        brand_list.setAdapter(simpleAdapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this,DeviceListActivity.class);
        switch (position) {
            case AEOTEC:
                intent.putExtra("brand",getString(R.string.aeotec));
                break;
            case GUNITECH:
                intent.putExtra("brand",getString(R.string.gunitech));
                break;
            case PHILIPS:
                intent.putExtra("brand",getString(R.string.philips));
                break;
            case MYSTROM:
                intent.putExtra("brand",getString(R.string.mystrom));
                break;

        }
        startActivity(intent);
        finish();
    }
}
