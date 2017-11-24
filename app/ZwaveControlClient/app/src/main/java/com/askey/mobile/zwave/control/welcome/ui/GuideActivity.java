package com.askey.mobile.zwave.control.welcome.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.welcome.adapter.GuidePageAdapter;
import com.askey.mobile.zwave.control.deviceContr.ui.NotificationActivity;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class GuideActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout llPoint;
    private Button btnSkip;
    private List<View> viewList;
    private int []imageIdArray;//图片资源的数组
    private ImageView []ivPointArray;
    private int []pointIds;
    private int curPage,preState;
    public LinearLayout llActivityGuide;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        llActivityGuide = (LinearLayout) findViewById(R.id.activity_guide);
        mContext = this;

        initViewPager();
        initPoint();
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GuideActivity.this,NotificationActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewList = new ArrayList<>();
        View view1 = LayoutInflater.from(this).inflate(R.layout.welcome_guide_one,null);
        View view2 = LayoutInflater.from(this).inflate(R.layout.welcome_guide_second,null);
        View view3 = LayoutInflater.from(this).inflate(R.layout.welcome_guide_third,null);
        View view4 = LayoutInflater.from(this).inflate(R.layout.welcome_guide_fourth,null);
        viewList.add(view1);
        viewList.add(view2);
        viewList.add(view3);
        viewList.add(view4);

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
                            ivPointArray[i].setImageResource(R.drawable.vector_drawable_ic_slider_ponit);
                        } else {
                            ivPointArray[i].setImageResource(R.drawable.vector_drawable_ic_slider_point_frame);
                        }
                    }
            /*        switch (position) {
                        case 0:
                            llActivityGuide.setBackgroundResource(R.drawable.ic_boarding_pange_05);
                            break;
                        case 1:
                            llActivityGuide.setBackgroundResource(R.drawable.ic_boarding_pange_06);
                            break;
                        case 2:
                            llActivityGuide.setBackgroundResource(R.drawable.ic_boarding_pange_07);
                            break;
                        case 3:
                            llActivityGuide.setBackgroundResource(R.drawable.ic_boarding_pange_08);
                            break;
                    }*/


                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (preState == 1 && state == 0 && curPage == viewList.size() -1) {
                        Intent intent = new Intent(GuideActivity.this,NotificationActivity.class);
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
        llPoint = (LinearLayout) findViewById(R.id.ll_point);
        pointIds = new int[]{R.id.imageview1,R.id.imageview2,R.id.imageview3,R.id.imageview4};
        ivPointArray = new ImageView[viewList.size()];
        for (int i = 0; i < viewList.size(); i++) {
            ivPointArray[i] = (ImageView) findViewById(pointIds[i]);
        }
        ivPointArray[0].setImageResource(R.drawable.vector_drawable_ic_slider_ponit);
    }
}
