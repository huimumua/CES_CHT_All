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
import com.askey.mobile.zwave.control.util.Logg;

import java.util.List;

/**
 * Created by skysoft on 2017/10/30.
 */

public class RecentlyAdapter extends RecyclerView.Adapter<RecentlyAdapter.MyViewHolder> implements View.OnClickListener{
    private static final String TAG = RecentlyAdapter.class.getSimpleName();
    private List<DeviceInfo> dataList;
    private OnItemClickListener onItemClickListener = null;

    public RecentlyAdapter(List<DeviceInfo> dataList) {
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
        String deviceType = info.getDeviceType();
        Logg.i(TAG,"=====deviceType==="+deviceType);
        if ("BULB".equals(deviceType)) {
            holder.device_img.setBackgroundResource(R.mipmap.bulb_icon);
            holder. device_toggle_state.setVisibility(View.VISIBLE);
        } else if ("PLUG".equals(deviceType)) {
            holder.device_img.setBackgroundResource(R.mipmap.switch_icon);
            holder. device_toggle_state.setVisibility(View.VISIBLE);
        } else if ("WALLMOTE".equals(deviceType)) {
            holder.device_img.setBackgroundResource(R.mipmap.wallmote_icon);
            holder. device_toggle_state.setVisibility(View.GONE);
        } else if ("EXTENDER".equals(deviceType)) {
            holder.device_img.setBackgroundResource(R.drawable.ic_zwgeneral);
            holder. device_toggle_state.setVisibility(View.GONE);
        }
        holder.device_name.setText(info.getDisplayName());
        holder.linear.setTag(dataList.get(position));
        holder.linear.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linear:
                onItemClickListener.onItemClick(v, (DeviceInfo) v.getTag());
                break;
        }
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, DeviceInfo deviceInfo);
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
}
