package com.askey.mobile.zwave.control.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.RoomInfo;

import java.util.List;

/**
 * Created by skysoft on 2017/11/29.
 */

public class SelectRoomAdapter extends RecyclerView.Adapter<SelectRoomAdapter.MyViewHolder> implements View.OnClickListener{

    private List<RoomInfo> dataList;
    private OnItemClickListener onItemClickListener = null;

    public SelectRoomAdapter(List<RoomInfo> dataList) {
        this.dataList = dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.room_list_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RoomInfo info = dataList.get(position);
        holder.room_name.setText(info.getRoomName());
        holder.linear.setTag(info.getRoomName());
        holder.linear.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return null == dataList ? 0 : dataList.size();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.linear:
                onItemClickListener.onItemClick(v, (String) v.getTag());
                break;
        }

    }

    public static interface OnItemClickListener {
        void onItemClick(View view, String roomName);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linear;
        private TextView room_name;

        public MyViewHolder(View itemView) {
            super(itemView);
            linear = (LinearLayout) itemView.findViewById(R.id.linear);
            room_name = (TextView) itemView.findViewById(R.id.room_name);
        }
    }
}
