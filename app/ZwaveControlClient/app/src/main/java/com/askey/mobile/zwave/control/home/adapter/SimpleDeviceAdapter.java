package com.askey.mobile.zwave.control.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;

import java.util.List;

/**
 * Created by skysoft on 2017/10/25.
 */

public class SimpleDeviceAdapter extends RecyclerView.Adapter<SimpleDeviceAdapter.MyViewHolder> implements View.OnClickListener{

    private List<DeviceInfo> dataList;
    private OnItemClickListener onItemClickListener;
    public SimpleDeviceAdapter(List<DeviceInfo> dataList) {
        this.dataList = dataList;
    }

    @Override
    public SimpleDeviceAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.device_item, null);
        view.setOnClickListener(this);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SimpleDeviceAdapter.MyViewHolder holder, int position) {
        DeviceInfo deviceInfo = dataList.get(position);
        String deviceType = deviceInfo.getDeviceType();
        if(deviceType.equals("")){

        }

        holder.device_icon.setImageResource(R.drawable.ic_launcher);
        holder.device_name.setText(deviceInfo.getDisplayName());
        holder.itemView.setTag(deviceInfo);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onItemClick(v, (DeviceInfo) v.getTag());
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView device_name;
        private ImageView device_icon;

        public MyViewHolder(View itemView) {
            super(itemView);
            device_icon = (ImageView) itemView.findViewById(R.id.device_icon);
            device_name = (TextView) itemView.findViewById(R.id.device_name);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, DeviceInfo deviceInfo);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
