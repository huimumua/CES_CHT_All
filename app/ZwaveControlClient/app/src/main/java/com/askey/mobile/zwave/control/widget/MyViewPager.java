package com.askey.mobile.zwave.control.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by skysoft on 2017/11/7.
 */

public class MyViewPager extends ViewPager {

    private boolean isPagingEnable = true;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPagingEnable(boolean isPagingEnable) {
        this.isPagingEnable = isPagingEnable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return this.isPagingEnable && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.isPagingEnable && super.onInterceptTouchEvent(ev);
    }
}
