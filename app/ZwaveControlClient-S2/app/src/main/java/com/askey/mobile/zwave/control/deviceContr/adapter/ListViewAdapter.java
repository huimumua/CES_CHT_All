package com.askey.mobile.zwave.control.deviceContr.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.askey.mobile.zwave.control.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017\9\14 0014.
 */

public class ListViewAdapter extends BaseAdapter {
    private LayoutInflater inflater;

    private List<String> list;


    public ListViewAdapter(Context context, List<String> list) {
        super();
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_item, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.spinner_item_text);
        tv.setText(list.get(position));
        return convertView;
    }
}
