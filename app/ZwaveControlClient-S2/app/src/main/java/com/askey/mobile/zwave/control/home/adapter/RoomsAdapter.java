package com.askey.mobile.zwave.control.home.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.List;

/**
 * Created by skysoft on 2017/10/23.
 */

public class RoomsAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> dataList;

    public RoomsAdapter(FragmentManager fm, List<Fragment> dataList) {
        super(fm);
        this.dataList = dataList;
    }

    @Override
    public Fragment getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}
