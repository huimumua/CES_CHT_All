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
 * Created by skysoft on 2017/10/23.
 */

public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private List<DeviceInfo> deviceInfoList;
    private int flag = 0;
    private OnItemClickListener onItemClickListener = null;
    public final static int NORMAL_MODE = 0;
    public final static int EDIT_MODE = 1;
    private final static int ITEM = 0;
    private final static int PLUS = 1;


    public List<DeviceInfo> getDeviceInfoList() {
        return deviceInfoList;
    }

    public void setDeviceInfoList(List<DeviceInfo> deviceInfoList) {
        this.deviceInfoList = deviceInfoList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM) {
            View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.device_info_item, null);
            return new MyViewHolder(view);
        }
        if (viewType == PLUS) {
            View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.device_add_item, null);
            return new AddItem(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            DeviceInfo info = deviceInfoList.get(position);
            ((MyViewHolder) holder).device_name.setText(info.getDisplayName());
            if (flag == NORMAL_MODE) {
                ((MyViewHolder) holder).delete_device.setVisibility(View.INVISIBLE);
                ((MyViewHolder) holder).linear.setTag(deviceInfoList.get(position));
                ((MyViewHolder) holder).linear.setOnClickListener(this);
            }
            if (flag == EDIT_MODE) {
                ((MyViewHolder) holder).delete_device.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).delete_device.setTag(position);
                ((MyViewHolder) holder).delete_device.setOnClickListener(this);
                ((MyViewHolder) holder).linear.setOnClickListener(null);
            }
        }
        if (flag == NORMAL_MODE && holder instanceof AddItem) {
            ((AddItem) holder).itemView.setOnClickListener(this);
        }
    }

    @Override
    public int getItemCount() {
        //判断是否加载addDevice的item
        return deviceInfoList == null ? 0 : flag == NORMAL_MODE ? deviceInfoList.size() + 1 : deviceInfoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == deviceInfoList.size() && flag == NORMAL_MODE) {
            return PLUS;
        }
        return ITEM;
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            switch (v.getId()) {
                case R.id.linear:
                    onItemClickListener.onItemClick(v, (DeviceInfo) v.getTag());
                    break;
                case R.id.delete_device:
                    onItemClickListener.deleteItemClick((int) v.getTag());
                    break;
                default:
                    onItemClickListener.addItemClick();
            }
        }
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, DeviceInfo deviceInfo);

        void deleteItemClick(int position);

        void addItemClick();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView device_toggle_state, device_name, device_connect_state;
        private ImageView schedule, notify, location, device_img, delete_device;
        private LinearLayout linear;

        public MyViewHolder(View itemView) {
            super(itemView);
            linear = (LinearLayout) itemView.findViewById(R.id.linear);
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
