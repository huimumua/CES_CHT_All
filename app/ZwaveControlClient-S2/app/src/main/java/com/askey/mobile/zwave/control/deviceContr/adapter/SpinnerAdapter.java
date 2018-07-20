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
 * Created by Administrator on 2017/7/19.
 */

public class SpinnerAdapter extends BaseAdapter {
    private Context context;
    private List<String>list;

    public SpinnerAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MyViewHolder viewHolder;
        if (view==null){
            viewHolder=new MyViewHolder();

           view = LayoutInflater.from(context).inflate(R.layout.spinner_item, viewGroup, false);
            viewHolder.textView= (TextView) view.findViewById(R.id.spinner_item_text);
            view.setTag(viewHolder);
        }else{
           viewHolder = (MyViewHolder) view.getTag();
        }

        viewHolder.textView.setText(""+list.get(i));
       // viewHolder.imageView.setImageResource(person.getImageid());
        return view;
    }
    public static  class MyViewHolder{
        public TextView textView;
    }
}
