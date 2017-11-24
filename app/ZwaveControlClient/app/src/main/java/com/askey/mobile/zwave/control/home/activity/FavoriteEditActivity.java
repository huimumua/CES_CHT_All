package com.askey.mobile.zwave.control.home.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.home.adapter.FavoriteAdapter;
import com.askey.mobile.zwave.control.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class FavoriteEditActivity extends BaseActivity implements View.OnClickListener, FavoriteAdapter.OnItemClickListener {

    private List<DeviceInfo> deviceInfoList, favoriteList, unfavoriteLsit;
    private FavoriteAdapter adapter;
    private RecyclerView edit_favorite_recycler;
    private ImageView yes, no;
    private static EditFavoriteListener editFavoriteListener;
    private LinearLayout favorite_edit_linear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_edit);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        initView();
        initDta();
    }

    private void initView() {

        favorite_edit_linear = (LinearLayout) findViewById(R.id.favorite_edit_linear);
        byte[] bytes = getIntent().getByteArrayExtra("bg");
        Bitmap bitmap = ImageUtils.Bytes2Bimap(bytes);
        BitmapDrawable drawable = new BitmapDrawable(getResources(),bitmap);
        favorite_edit_linear.setBackground(drawable);

        yes = (ImageView) findViewById(R.id.yes);
        yes.setOnClickListener(this);
        no = (ImageView) findViewById(R.id.no);
        no.setOnClickListener(this);
        edit_favorite_recycler = (RecyclerView) findViewById(R.id.edit_favorite_recycler);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        edit_favorite_recycler.setLayoutManager(layoutManager);
    }

    private void initDta() {
        deviceInfoList = (List<DeviceInfo>) getIntent().getSerializableExtra("data");
        favoriteList = new ArrayList<>();
        unfavoriteLsit = new ArrayList<>();

        for (DeviceInfo deviceInfo : deviceInfoList) {
            if ("0".equals(deviceInfo.getIsFavorite())) {
                unfavoriteLsit.add(deviceInfo);
            }
            if ("1".equals(deviceInfo.getIsFavorite())) {
                favoriteList.add(deviceInfo);
            }
        }

        //将unfavorite的元素排到favorite后面,这里其实是所有的设备列表
        favoriteList.addAll(unfavoriteLsit);

        adapter = new FavoriteAdapter(favoriteList);
        adapter.setMode(FavoriteAdapter.EDIT_MODE);
        adapter.setOnItemClickListener(this);
        edit_favorite_recycler.setAdapter(adapter);
    }

    public static interface EditFavoriteListener {
        void editFavoriteClick(List<DeviceInfo> list);
    }

    public static void setEditFavoriteListener(EditFavoriteListener listener){
        editFavoriteListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes:
                editFavoriteListener.editFavoriteClick(favoriteList);
                finish();
                break;
            case R.id.no:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(View view, DeviceInfo deviceInfo) {

    }

    @Override
    public void addFavoriteClick(int position) {
        favoriteList.get(position).setIsFavorite("1");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void removeFavoriteClick(int position) {
        favoriteList.get(position).setIsFavorite("0");
        adapter.notifyDataSetChanged();
    }
}
