package com.askey.mobile.zwave.control.login.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.interf.FragmentPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LogInHomeActivity extends AppCompatActivity implements View.OnClickListener{

    private FrameLayout contentPager;
    public ImageView right,one,second,third;
    private List<Fragment> fragmentList;
    private int[] pointIds;
    private ImageView[] ivPointArray;
    private int curPage;
    private int preState;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    public boolean isNextPage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_home);

        initView();
        initFragment();
        initPoint();

    }


    private void initFragment() {
        fragmentList = new ArrayList<>();
        fragmentList.add(new LogInOneFragment());
        fragmentList.add(new LogInSecondFragment());
        fragmentList.add(new LogInThirdFragment());

        fragmentManager =getSupportFragmentManager();
        fragmentTransaction =fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content_pager, fragmentList.get(0),0 + "");
        fragmentTransaction.commit();


    }

    private void initView() {
        contentPager = (FrameLayout) findViewById(R.id.content_pager);
        right = (ImageView) findViewById(R.id.iv_right);
        one = (ImageView) findViewById(R.id.iv_one);
        second = (ImageView) findViewById(R.id.iv_second);
        third = (ImageView) findViewById(R.id.iv_third);

        right.setOnClickListener(this);
        right.setImageResource(R.drawable.vector_drawable_ic_66);
    }
    /**
     * 加载底部圆点
     */
    private void initPoint() {
        pointIds = new int[]{R.id.iv_one,R.id.iv_second,R.id.iv_third};
        ivPointArray = new ImageView[fragmentList.size()];
        for (int i = 0; i < fragmentList.size(); i++) {
            ivPointArray[i] = (ImageView) findViewById(pointIds[i]);
        }
        ivPointArray[0].setImageResource(R.drawable.vector_drawable_ic_slider_ponit_red);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_right:
                FragmentPage.getInstance().handlePage();
                break;
        }
    }

    public void goNextPage(Object object) {
        curPage++;
        if (curPage <= 2) {
            changePoint();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_pager, fragmentList.get(curPage),curPage + "");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        }else {
            Intent intent = new Intent(this,LogInActivity.class);
            HashMap<String,String> account = (HashMap)object;
            intent.putExtra("email", account.get("email"));
            intent.putExtra("password", account.get("password"));
            startActivity(intent);
            finish();
        }

        isNextPage = false;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (curPage > 0 && curPage <= fragmentList.size()) {
            curPage --;
            changePoint();

        }
    }
    private void changePoint () {
        for (int i = 0; i < fragmentList.size(); i++) {
            if (curPage == i) {
                ivPointArray[i].setImageResource(R.drawable.vector_drawable_ic_slider_ponit_red);
            } else {
                ivPointArray[i].setImageResource(R.drawable.vector_drawable_ic_slider_point_frame_red);
            }


        }
    }
}
