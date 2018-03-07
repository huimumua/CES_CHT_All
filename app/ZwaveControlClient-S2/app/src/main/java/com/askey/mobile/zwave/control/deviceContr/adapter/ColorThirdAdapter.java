package com.askey.mobile.zwave.control.deviceContr.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by skysoft on 2017/10/27.
 */

public class  ColorThirdAdapter extends BaseAdapter {
    private Context mContext;
    private List<View> mList;

    public ColorThirdAdapter(Context context, List<View> mList) {
        mContext = context;
        this.mList = mList;
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mList.get(position);
    }


}
