package com.askey.mobile.zwave.control.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;

import java.util.List;

/**
 * Created by skysoft on 2017/11/21.
 */

public class ScenesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener{

    private List<ScenesInfo> dataList;
    private final static int ITEM = 0;
    private final static int PLUS = 1;
    private OnItemClickListener onItemClickListener = null;

    public ScenesAdapter(List<ScenesInfo> dataList) {
        this.dataList = dataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM) {
            View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.scene_info_item,null);
            return new MyViewHolder(view);
        }
        if (viewType == PLUS) {
            View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.device_add_item,null);
            return new AddItem(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ScenesInfo info = dataList.get(position);
            ((MyViewHolder)holder).scene_name.setText(info.getScenesName());
            ((MyViewHolder)holder).linear.setTag(info);
            ((MyViewHolder)holder).linear.setOnClickListener(this);
            ((MyViewHolder)holder).linear.setOnLongClickListener(this);
        }

        if (holder instanceof AddItem) {
            ((AddItem)holder).itemView.setOnClickListener(this);
        }
    }

    @Override
    public int getItemCount() {
        return null == dataList ? 0 : dataList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == dataList.size()) {
            return PLUS;
        }
        return ITEM;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linear:
                onItemClickListener.onItemClick(v, (ScenesInfo) v.getTag());
                break;
            default:
                onItemClickListener.addItemClick();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.linear:
                onItemClickListener.onItemLongClick(v , (ScenesInfo) v.getTag());
                break;
        }
        return true;
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, ScenesInfo scenesInfo);

        void onItemLongClick(View view, ScenesInfo scenesInfo);

        void addItemClick();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView scene_name;
        private LinearLayout linear;

        public MyViewHolder(View itemView) {
            super(itemView);
            scene_name = (TextView) itemView.findViewById(R.id.scene_name);
            linear = (LinearLayout) itemView.findViewById(R.id.linear);
        }
    }

    public static class AddItem extends RecyclerView.ViewHolder {
        private ImageView add_device;

        public AddItem(View itemView) {
            super(itemView);
            add_device = (ImageView) itemView.findViewById(R.id.add_device);
        }
    }
}
