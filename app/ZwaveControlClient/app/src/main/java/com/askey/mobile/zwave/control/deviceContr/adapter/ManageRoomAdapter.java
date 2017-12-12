package com.askey.mobile.zwave.control.deviceContr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;

import java.util.List;

/**
 * Created by skysoft on 2017/10/24.
 */

public class ManageRoomAdapter extends RecyclerView.Adapter<ManageRoomAdapter.ViewHolder> implements View.OnClickListener{
    private Context mContext;
    private List<String> data;
    private RecyclerAdapter.OnItemClickListener mOnItemClickListener = null;

    public ManageRoomAdapter(Context context, List<String> data) {
        mContext = context;
        this.data = data;
    }

    @Override
    public ManageRoomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_manage_room, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //将position保存在itemView的Tag中，以便点击时进行获取
        holder.tvName.setText(data.get(position));
        holder.itemView.setTag(position);
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(view,(int)view.getTag());
        }
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        private ImageView ivIcon;

        public ViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
        }
    }

    //define interface
    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(RecyclerAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
