package com.askey.mobile.zwave.control.guideSetting.ui;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.ui.NotificationActivity;
import com.askey.mobile.zwave.control.welcome.adapter.GuidePageAdapter;
import com.askey.mobile.zwave.control.welcome.ui.GuideActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class DeviceGuideActivity extends AppCompatActivity implements View.OnClickListener{
    private ViewPager viewPager;
    private List<View> viewList;
    private int curPage;
    private int preState;
    private ImageView[] ivPointArray;
    private int[] pointIds;
    private ImageView right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_guide);

        initViewPager();
        initPoint();
        right = (ImageView) findViewById(R.id.iv_right);
        right.setOnClickListener(this);
    }


    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.content_pager);
        viewList = new ArrayList<>();
       View view1 = LayoutInflater.from(this).inflate(R.layout.device_guide_one,null);
       View view2 = LayoutInflater.from(this).inflate(R.layout.device_guide_second,null);
       View view3 = LayoutInflater.from(this).inflate(R.layout.device_guide_third,null);
       View view4 = LayoutInflater.from(this).inflate(R.layout.device_guide_fourth,null);
       View view5 = LayoutInflater.from(this).inflate(R.layout.device_guide_fifth,null);
        viewList.add(view1);
        viewList.add(view2);
        viewList.add(view3);
        viewList.add(view4);
        viewList.add(view5);

            viewPager.setAdapter(new GuidePageAdapter(viewList));
            //设置滑动监听
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                    curPage = position;
                    for (int i = 0; i < pointIds.length; i++) {
                        if (position == i) {
                            ivPointArray[i].setImageResource(R.drawable.vector_drawable_ic_slider_ponit_red);
                        } else {
                            ivPointArray[i].setImageResource(R.drawable.vector_drawable_ic_slider_point_frame_red);
                        }

                    }


                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (preState == 1 && state == 0 && curPage == viewList.size() -1) {
                        Intent intent = new Intent(DeviceGuideActivity.this,DeviceGuideHomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    preState = state;
                }
            });
        }

    /**
     * 加载底部圆点
     */
    private void initPoint() {
        pointIds = new int[]{R.id.iv_one,R.id.iv_second,R.id.iv_third,R.id.iv_fourth,R.id.iv_fifth};
        ivPointArray = new ImageView[viewList.size()];
        for (int i = 0; i < viewList.size(); i++) {
            ivPointArray[i] = (ImageView) findViewById(pointIds[i]);
        }
        ivPointArray[0].setImageResource(R.drawable.vector_drawable_ic_slider_ponit_red);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_right:
                curPage++;
                if (curPage >= viewList.size()) {
                    Intent intent = new Intent(DeviceGuideActivity.this, DeviceGuideHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    viewPager.setCurrentItem(curPage);
                }
                break;
        }
    }
}
