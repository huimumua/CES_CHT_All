package com.askey.firefly.zwave.control.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.page.PageView;
import com.askey.firefly.zwave.control.page.devListPage;
import com.askey.firefly.zwave.control.page.sceneListPage;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chiapin on 2017/9/22.
 */

public class HomeActivity extends AppCompatActivity {

    private static String LOG_TAG = HomeActivity.class.getSimpleName();

    private TabLayout mTablayout;
    private ViewPager mViewPager;
    private List<PageView> pageList;
    private SamplePagerAdapter adapter;

    // Tab titles
    private String[] tabs = { "DEVICE LIST", "ROOM LIST" };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        pageList = new ArrayList<>();
        initData();
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        for(PageView view : pageList){
            view.refreshView();
        }
    }


    private void initData() {
        pageList.clear();
        pageList.add(new devListPage(HomeActivity.this));
        pageList.add(new sceneListPage(HomeActivity.this));

    }

    private void initView() {
        mTablayout = (TabLayout) findViewById(R.id.tabs);
        mTablayout.addTab(mTablayout.newTab().setText("DEVICE List"));
        mTablayout.addTab(mTablayout.newTab().setText("ROOM LIST"));
        adapter = new SamplePagerAdapter();

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);
        initListener();
    }

    private void initListener() {
        mTablayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTablayout));
    }

    public class SamplePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(pageList.get(position));
            return pageList.get(position);
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}