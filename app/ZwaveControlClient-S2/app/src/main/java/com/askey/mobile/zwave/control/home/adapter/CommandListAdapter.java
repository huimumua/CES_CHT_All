package com.askey.mobile.zwave.control.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.RoomInfo;
import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;

import java.util.List;

/**
 * Created by skysoft on 2017/11/29.
 */

public class CommandListAdapter extends RecyclerView.Adapter<CommandListAdapter.MyViewHolder> implements View.OnClickListener{

    private List<String> dataList;
    private OnItemClickListener onItemClickListener = null;

    public CommandListAdapter(List<String> dataList) {
        this.dataList = dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.command_list_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String commandText = dataList.get(position);
        holder.command.setText(commandText);
        holder.command.setTag(commandText);
        holder.command.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return null == dataList ? 0 : dataList.size();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_command:
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

        private TextView command;

        public MyViewHolder(View itemView) {
            super(itemView);
            command = (TextView) itemView.findViewById(R.id.tv_command);
        }
    }
}
