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

public class FavoriteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private static final String TAG = FavoriteAdapter.class.getSimpleName();
    private List<DeviceInfo> dataList;
    private OnItemClickListener onItemClickListener = null;
    public final static int NORMAL_MODE = 0;
    public final static int EDIT_MODE = 1;
    private final static int ITEM = 2;
    private final static int PLUS = 3;
    private int flag = 0;

    public FavoriteAdapter(List<DeviceInfo> dataList) {
        this.dataList = dataList;
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
            DeviceInfo info = dataList.get(position);
            String deviceType = info.getDeviceType();
            Logg.i(TAG,"=====deviceType==="+deviceType);

            if ("BULB".equals(deviceType)) {
                ((MyViewHolder) holder).device_img.setBackgroundResource(R.drawable.ic_grid_blub);
                ((MyViewHolder) holder).device_toggle_state.setVisibility(View.VISIBLE);
            } else if ("PLUG".equals(deviceType)) {
                ((MyViewHolder) holder).device_img.setBackgroundResource(R.drawable.ic_grid_swith_on);
                ((MyViewHolder) holder).device_toggle_state.setVisibility(View.VISIBLE);
            } else if ("WALLMOTE".equals(deviceType)) {
                ((MyViewHolder) holder).device_img.setBackgroundResource(R.drawable.ic_grid_wallmote);
                ((MyViewHolder) holder).device_toggle_state.setVisibility(View.INVISIBLE);
            } else if ("EXTENDER".equals(deviceType)) {
                ((MyViewHolder) holder).device_img.setBackgroundResource(R.drawable.ic_grid_square);
                ((MyViewHolder) holder).device_toggle_state.setVisibility(View.INVISIBLE);
            }else if ("DIMMER".equals(deviceType)) {
                ((MyViewHolder) holder).device_img.setBackgroundResource(R.drawable.ic_zwgeneral);
                ((MyViewHolder) holder).device_toggle_state.setVisibility(View.INVISIBLE);
            } else if ("SENSOR".equals(deviceType)) {
                ((MyViewHolder) holder).device_img.setBackgroundResource(R.drawable.ic_zwgeneral);
                ((MyViewHolder) holder).device_toggle_state.setVisibility(View.INVISIBLE);
            } else if ("PST02".equals(deviceType)) {
                ((MyViewHolder) holder).device_img.setBackgroundResource(R.drawable.ic_zwgeneral);
                ((MyViewHolder) holder).device_toggle_state.setVisibility(View.INVISIBLE);
            }
            ((MyViewHolder)holder).device_name.setText(info.getDisplayName());
            if (flag == NORMAL_MODE) {
                ((MyViewHolder)holder).delete_device.setVisibility(View.INVISIBLE);
                ((MyViewHolder)holder).choose_linear.setVisibility(View.INVISIBLE);
                ((MyViewHolder)holder).choose_linear.setOnClickListener(null);
                ((MyViewHolder)holder).linear.setTag(info);
                ((MyViewHolder)holder).linear.setOnClickListener(this);
            }
            if (flag == EDIT_MODE) {
                if ("0".equals(info.getIsFavorite())) {
                    ((MyViewHolder)holder).delete_device.setVisibility(View.INVISIBLE);
                    ((MyViewHolder)holder).delete_device.setOnClickListener(null);
                    ((MyViewHolder)holder).choose_linear.setVisibility(View.VISIBLE);
                    ((MyViewHolder)holder).choose_linear.setTag(position);
                    ((MyViewHolder)holder).choose_linear.setOnClickListener(this);
                }
                if ("1".equals(info.getIsFavorite())) {
                    ((MyViewHolder)holder).delete_device.setVisibility(View.VISIBLE);
                    ((MyViewHolder)holder).delete_device.setTag(position);
                    ((MyViewHolder)holder).delete_device.setOnClickListener(this);
                    ((MyViewHolder)holder).choose_linear.setVisibility(View.GONE);
                    ((MyViewHolder)holder).choose_linear.setOnClickListener(null);
                }
                ((MyViewHolder)holder).linear.setOnClickListener(null);
            }
        }
        if (flag == NORMAL_MODE && holder instanceof AddItem) {
            ((AddItem) holder).itemView.setOnClickListener(this);
        }
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : flag == NORMAL_MODE ? dataList.size()+1 : dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == dataList.size() && flag == NORMAL_MODE) {
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
                    onItemClickListener.removeFavoriteClick((int) v.getTag());
                    break;
                case R.id.choose_linear:
                    onItemClickListener.addFavoriteClick((int) v.getTag());
                    break;
                default:
                    onItemClickListener.move2EditActivity();
            }
        }
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, DeviceInfo deviceInfo);

        void addFavoriteClick(int position);

        void removeFavoriteClick(int position);

        void move2EditActivity();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView device_toggle_state, device_name;
        private ImageView schedule, notify, location, device_img, delete_device;
        private LinearLayout linear, choose_linear;

        public MyViewHolder(View itemView) {
            super(itemView);
            linear = (LinearLayout) itemView.findViewById(R.id.linear);
            choose_linear = (LinearLayout) itemView.findViewById(R.id.choose_linear);
            device_toggle_state = (TextView) itemView.findViewById(R.id.device_toggle_state);
            device_name = (TextView) itemView.findViewById(R.id.device_name);
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
