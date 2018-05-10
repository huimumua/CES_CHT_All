package com.askey.mobile.zwave.control.deviceContr.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：ZwaveControlClient-S2
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/3/21 14:00
 * 修改人：skysoft
 * 修改时间：2018/3/21 14:00
 * 修改备注：
 */
public class ApiArrayAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> dataList;
    private LayoutInflater mInflater;

    //构造方法
    public ApiArrayAdapter(Context context, ArrayList<String> objects) {
        this.mContext = context;
        this.dataList = objects;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    //复写这个方法，使返回的数据没有最后一项
    @Override
    public int getCount() {
        // don't display last item. It is used as hint.
        int count = dataList.size();
//        return count > 0 ? count - 1 : count;
        return count ;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.api_spinner_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) convertView.findViewById(R.id.api_spinner_item_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Object item =  getItem(pos);
        viewHolder.mTextView.setText(dataList.get(position));

        return convertView;
    }

    public static class ViewHolder
    {
        public TextView mTextView;
    }

}
