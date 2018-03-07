package com.askey.mobile.zwave.control.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.RoomInfo_state;

import java.util.List;

/**
 * Created by skysoft on 2017/11/15.
 */

public class DeleteRoomAdapter extends RecyclerView.Adapter<DeleteRoomAdapter.MyViewHolder>{

    private List<RoomInfo_state> dataList;
    private RecyclerView recyclerView;
    private int mSelectedPos = -1;
    private OnItemClickListener onItemClickListener = null;

    public DeleteRoomAdapter(List<RoomInfo_state> dataList, RecyclerView recyclerView) {
        this.dataList = dataList;
        this.recyclerView = recyclerView;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).isSelected()) {
                mSelectedPos = i;
            }
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.delete_room_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        RoomInfo_state roomInfo_state = dataList.get(position);
        holder.room_name.setText(roomInfo_state.getRoomName());
        holder.delete_choose.setSelected(roomInfo_state.isSelected());

        holder.delete_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyViewHolder viewHolder = (MyViewHolder) recyclerView.findViewHolderForLayoutPosition(mSelectedPos);
                if (null != viewHolder) {
                    viewHolder.delete_choose.setSelected(false);
                } else {
                    notifyItemChanged(mSelectedPos);
                }
                dataList.get(mSelectedPos).setSelected(false);
                mSelectedPos = position;
                Log.d("mSelectedPos",mSelectedPos+"");
                onItemClickListener.deleteItem(dataList.get(mSelectedPos));
                dataList.get(mSelectedPos).setSelected(true);
                holder.delete_choose.setSelected(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    public static interface OnItemClickListener {
        void deleteItem(RoomInfo_state roomInfo_state);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView delete_choose;
        private TextView room_name;

        public MyViewHolder(View itemView) {
            super(itemView);
            delete_choose = (ImageView) itemView.findViewById(R.id.delete_choose);
            room_name = (TextView) itemView.findViewById(R.id.room_name);
        }
    }
}
