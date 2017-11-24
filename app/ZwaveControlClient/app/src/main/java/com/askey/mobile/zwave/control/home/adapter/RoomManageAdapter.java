package com.askey.mobile.zwave.control.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.RoomInfo;

import java.util.List;

import static com.askey.mobile.zwave.control.R.id.delete_room;

/**
 * Created by skysoft on 2017/11/13.
 */

public class RoomManageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    public final static int NORMAL_MODE = 0;
    public final static int EDIT_MODE = 1;
    private final static int ITEM = 0;
    private final static int PLUS = 1;
    private int flag = 0;
    private List<RoomInfo> dataList;
    private OnItemClickListener onItemClickListener = null;

    public RoomManageAdapter(List<RoomInfo> dataList) {
        this.dataList = dataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM) {
            View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.room_manage_item, null);
            return new MyViewHolder(view);
        }
        if (viewType == PLUS) {
            View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.room_add_item, null);
            return new AddRoomViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {

            RoomInfo info = dataList.get(position);
            ((MyViewHolder) holder).room_name.setText(info.getRoomName());
            if (flag == NORMAL_MODE) {
                if (info.getRoomName().equals("My Home")) {
                    ((MyViewHolder) holder).delete_room.setVisibility(View.INVISIBLE);
                } else {
                    ((MyViewHolder) holder).delete_room.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).delete_room.setOnClickListener(this);
                    ((MyViewHolder) holder).delete_room.setTag(position);
                }
                ((MyViewHolder) holder).edit_room_name.setOnClickListener(this);
                ((MyViewHolder) holder).edit_room_name.setTag(position);
            }
            if (flag == EDIT_MODE) {
                ((MyViewHolder) holder).delete_room.setVisibility(View.GONE);
                ((MyViewHolder) holder).delete_room.setOnClickListener(null);
            }
        }

        if (flag == EDIT_MODE && holder instanceof AddRoomViewHolder) {

            String roomName = ((AddRoomViewHolder) holder).room_name_ed.getText().toString();

            ((AddRoomViewHolder) holder).ed_confirm.setOnClickListener(this);
            ((AddRoomViewHolder) holder).ed_confirm.setTag(roomName);

        }


    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : flag == NORMAL_MODE ? dataList.size() : dataList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        //position的值是dataList.size()-1
        if (position == dataList.size() && flag == EDIT_MODE) {
            return PLUS;
        }
        return ITEM;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ed_confirm:
                onItemClickListener.modifyNameConfirmClick(v, AddRoomViewHolder.room_name_ed.getText().toString());
                break;
            case delete_room:
                onItemClickListener.deleteRoomClick((int) v.getTag());
                break;
            case R.id.edit_room_name:
                onItemClickListener.modifyNameClick(v, (int) v.getTag());
                break;
        }

    }

    public static interface OnItemClickListener {
        void modifyNameClick(View view, int position);

        void modifyNameConfirmClick(View view, String roomName);

        void deleteRoomClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout normal_mode;
        private ImageView delete_room, edit_room_name;
        private TextView room_name;

        public MyViewHolder(View itemView) {
            super(itemView);
            normal_mode = (LinearLayout) itemView.findViewById(R.id.normal_mode);

            delete_room = (ImageView) itemView.findViewById(R.id.delete_room);
            edit_room_name = (ImageView) itemView.findViewById(R.id.edit_room_name);
            room_name = (TextView) itemView.findViewById(R.id.room_name);
        }
    }

    public static class AddRoomViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout edit_mode;
        private ImageView ed_confirm;
        public static EditText room_name_ed;

        public AddRoomViewHolder(View itemView) {
            super(itemView);
            edit_mode = (LinearLayout) itemView.findViewById(R.id.edit_mode);
            ed_confirm = (ImageView) itemView.findViewById(R.id.ed_confirm);
            room_name_ed = (EditText) itemView.findViewById(R.id.room_name_ed);
        }
    }

    public void setMode(int flag) {
        this.flag = flag;
    }

    public int getMode() {
        return flag;
    }
}
