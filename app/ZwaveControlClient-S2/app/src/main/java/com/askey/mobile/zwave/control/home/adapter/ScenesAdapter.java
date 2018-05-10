package com.askey.mobile.zwave.control.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.ProvisionInfo;

import java.util.List;

/**
 * Created by skysoft on 2017/11/21.
 */

public class ScenesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener{

    private List<ProvisionInfo> dataList;
    private int flag = 0;
    private final static int ITEM = 0;
    private final static int PLUS = 1;
    private OnItemClickListener onItemClickListener = null;

    public ScenesAdapter(List<ProvisionInfo> dataList) {
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
            ProvisionInfo info = dataList.get(position);
            ((MyViewHolder)holder).dsk.setText(info.getDsk());
            ((MyViewHolder)holder).bootMode.setText(info.getDeviceBootMode());
            ((MyViewHolder)holder).inclusionState.setText(info.getDeviceInclusionState());
            ((MyViewHolder)holder).networkState.setText(info.getNetworkInclusionState());
            ((MyViewHolder)holder).linear.setTag(info);
            ((MyViewHolder)holder).linear.setOnClickListener(this);
            ((MyViewHolder)holder).linear.setOnLongClickListener(this);
            if(flag == ITEM){ //正常
                ((MyViewHolder)holder).deleteView.setVisibility(View.GONE);
            }else {
                ((MyViewHolder)holder).deleteView.setVisibility(View.VISIBLE);
                ((MyViewHolder)holder).deleteView.setTag(position); //如果这里setTag不传position进去， 在监听中就拿不到position
                ((MyViewHolder)holder).deleteView.setOnClickListener(this);
            }
        }

        if (holder instanceof AddItem) {
            ((AddItem)holder).itemView.setOnClickListener(this);
        }
    }

    /**
     * 设置是否显示“+”添加设备图标
     * @return 返回的dataList.size()+1,UI上会出现添加设备的图标（一个“+”图标），返回dataList.size()就没有“+”图标
     */
    @Override
    public int getItemCount() {
        return null == dataList ? 0 : flag == ITEM ? dataList.size() + 1 : dataList.size();
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
                //flag = 0表示是常模式，才会响应，否则是删除模式
                if(flag == ITEM)
                onItemClickListener.onItemClick(v, (ProvisionInfo) v.getTag());
                break;
            case R.id.delete_smart_start:
                //onItemClickListener.onItemClick(v, (ProvisionInfo) v.getTag());
                onItemClickListener.deleteItemClick((int)v.getTag());
                break;
            default:
                onItemClickListener.addItemClick();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.linear:
                onItemClickListener.onItemLongClick(v , (ProvisionInfo) v.getTag());
                break;
        }
        return true;
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, ProvisionInfo provisionInfo);

        void onItemLongClick(View view, ProvisionInfo provisionInfo);

        void addItemClick();

        void deleteItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
 
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView scene_name;
        private TextView dsk;
        private TextView bootMode;
        private TextView inclusionState;
        private TextView networkState;
        private LinearLayout linear;
        private ImageView deleteView;

        public MyViewHolder(View itemView) {
            super(itemView);
            dsk = (TextView) itemView.findViewById(R.id.dsk);
            inclusionState = (TextView) itemView.findViewById(R.id.inclusion_state);
            bootMode = (TextView) itemView.findViewById(R.id.boot_mode);
            networkState = (TextView) itemView.findViewById(R.id.network_state);
            linear = (LinearLayout) itemView.findViewById(R.id.linear);
            deleteView = (ImageView) itemView.findViewById(R.id.delete_smart_start);
        }
    }

    public static class AddItem extends RecyclerView.ViewHolder {
        private ImageView add_device;

        public AddItem(View itemView) {
            super(itemView);
            add_device = (ImageView) itemView.findViewById(R.id.add_device);
        }
    }

    public void setMode(int flag) {
        this.flag = flag;
    }

    public int getMode() {
        return flag;
    }
}
