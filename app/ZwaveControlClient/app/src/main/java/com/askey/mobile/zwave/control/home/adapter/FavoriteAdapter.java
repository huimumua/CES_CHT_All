package com.askey.mobile.zwave.control.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;

import java.util.List;

/**
 * Created by skysoft on 2017/10/30.
 */

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.MyViewHolder> implements View.OnClickListener {

    private List<DeviceInfo> dataList;
    private OnItemClickListener onItemClickListener = null;
    public final static int NORMAL_MODE = 0;
    public final static int EDIT_MODE = 1;
    private int flag = 0;

    public FavoriteAdapter(List<DeviceInfo> dataList) {
        this.dataList = dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.device_info_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DeviceInfo info = dataList.get(position);
        holder.device_name.setText(info.getDisplayName());
        if (flag == NORMAL_MODE) {
            holder.delete_device.setVisibility(View.INVISIBLE);
            holder.choose_linear.setVisibility(View.INVISIBLE);
            holder.choose_linear.setOnClickListener(null);
            holder.linear.setTag(info);
            holder.linear.setOnClickListener(this);
        }
        if (flag == EDIT_MODE) {
            if ("0".equals(info.getIsFavorite())) {
                holder.delete_device.setVisibility(View.INVISIBLE);
                holder.delete_device.setOnClickListener(null);
                holder.choose_linear.setVisibility(View.VISIBLE);
                holder.choose_linear.setTag(position);
                holder.choose_linear.setOnClickListener(this);
            }
            if ("1".equals(info.getIsFavorite())) {
                holder.delete_device.setVisibility(View.VISIBLE);
                holder.delete_device.setTag(position);
                holder.delete_device.setOnClickListener(this);
                holder.choose_linear.setVisibility(View.GONE);
                holder.choose_linear.setOnClickListener(null);
            }
            holder.linear.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            switch (v.getId()) {
                case R.id.linear:
                    onItemClickListener.onItemClick(v, (DeviceInfo) v.getTag());
                    break;
                case R.id.delete_device:
                    onItemClickListener.removeFavoriteClick((int) v.getTag());
                    break;
                case R.id.choose_linear:
                    onItemClickListener.addFavoriteClick((int) v.getTag());
                    break;
            }
        }
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, DeviceInfo deviceInfo);

        void addFavoriteClick(int position);

        void removeFavoriteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView device_toggle_state, device_name, device_connect_state;
        private ImageView schedule, notify, location, device_img, delete_device;
        private LinearLayout linear, choose_linear;

        public MyViewHolder(View itemView) {
            super(itemView);
            linear = (LinearLayout) itemView.findViewById(R.id.linear);
            choose_linear = (LinearLayout) itemView.findViewById(R.id.choose_linear);
            device_toggle_state = (TextView) itemView.findViewById(R.id.device_toggle_state);
            device_name = (TextView) itemView.findViewById(R.id.device_name);
            device_connect_state = (TextView) itemView.findViewById(R.id.device_connect_state);
            schedule = (ImageView) itemView.findViewById(R.id.schedule);
            notify = (ImageView) itemView.findViewById(R.id.notify);
            location = (ImageView) itemView.findViewById(R.id.location);
            device_img = (ImageView) itemView.findViewById(R.id.device_img);
            delete_device = (ImageView) itemView.findViewById(R.id.delete_device);
        }
    }

    public void setMode(int flag) {
        this.flag = flag;
    }

    public int getMode() {
        return flag;
    }
}
