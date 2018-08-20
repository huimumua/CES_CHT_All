package com.askey.firefly.zwave.control.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.page.PageView;
import com.askey.firefly.zwave.control.page.RoomListPage;
import com.askey.firefly.zwave.control.page.devListPage;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chiapin on 2017/9/22.
 */

public class HomeActivity extends BaseActivity {

    private static String LOG_TAG = HomeActivity.class.getSimpleName();
    private TabLayout mTablayout;
    private ViewPager mViewPager;
    private List<PageView> pageList;
    private SamplePagerAdapter adapter;
    private long clickTime = 0;
    initTask mTask;
    private ProgressBar progressBar;
    private TextView text;
    private FrameLayout rootFrameLayout;
    // Tab titles
    private String[] tabs = { "DEVICE LIST", "ROOM LIST" };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        pageList = new ArrayList<>();
        initData();
        initView();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        text = (TextView) findViewById(R.id.textView5);

        mTask = new initTask();
        mTask.execute();
    }

    class initTask extends AsyncTask<String, Integer, String> {

        protected void onPreExecute() {
            text.setText("loading");
        }



        @Override
        protected String doInBackground(String... params) {

            try {
                int count = 0;
                int length = 1;
                while (count< 99) {
                    count += length;
                    publishProgress(count);
                    Thread.sleep(60);
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progresses) {
            progressBar.setProgress(progresses[0]);
            text.setText("loading..." + progresses[0] + "%");
        }


        @Override
        protected void onPostExecute(String result) {
            text.setText("finish");
            mHandler.sendEmptyMessage(0);
        }

    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    hideProgressDialog();
                    break;
            }
        }
    };


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
        pageList.add(new RoomListPage(HomeActivity.this));
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

    @Override
    public void onBackPressed() {
        exit();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void exit() {
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            Toast.makeText(this, "Press back key again to exit", Toast.LENGTH_SHORT).show();
            clickTime = System.currentTimeMillis();
        } else {
            this.finish();
        }
    }
}