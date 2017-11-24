package com.askey.mobile.zwave.control.deviceContr.adapter;

import android.content.Context;
import android.support.v7.view.menu.MenuAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;

import java.util.List;
import java.util.Map;

/**
 * Created by skysoft on 2017/10/23.
 */

public class SwipeMenuAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{
    private static final int TYPE_BULB = 0;
    private static final int TYPE_PLUG = 1;
    private Context mContext;
    private List<Map<String, String>> data;
    private CommandsAdapter.OnItemClickListener mOnItemClickListener = null;

    public SwipeMenuAdapter(Context context, List<Map<String, String>> data) {
        mContext = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        RecyclerView.ViewHolder holder = getViewHolderByViewType(viewType);
//        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (data.get(position).get("type").equals("bulb")) {

            ((CommandsAdapter.BulbViewHolder) holder).tvName.setText(data.get(position).get("device_name"));
            ((CommandsAdapter.BulbViewHolder) holder).tvAction.setText(data.get(position).get("action"));
            ((CommandsAdapter.BulbViewHolder) holder).tvLightVaule.setText(data.get(position).get("lightvalue") + "%");
            ((CommandsAdapter.BulbViewHolder) holder).tvTimmer.setText("timmer:" + data.get(position).get("timmer"));
            ((CommandsAdapter.BulbViewHolder) holder).ivLightIcon.setImageResource(R.mipmap.point);
//            ((BulbViewHolder) holder).itemView.setOnClickListener(this);
        }
        holder.itemView.setTag(position);
    }


    @Override
    public int getItemCount() {
        Log.i("aaaaaaaaa", "dd" + data.size());
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
    public static class BulbViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName, tvAction,tvLightVaule,tvTimmer;
        public ImageView ivLightIcon;

        public BulbViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvAction = (TextView) view.findViewById(R.id.tv_action);
            tvLightVaule = (TextView) view.findViewById(R.id.tv_light_value);
            tvTimmer = (TextView) view.findViewById(R.id.tv_timmer);
            ivLightIcon = (ImageView) view.findViewById(R.id.iv_light_icon);
        }
    }

    //define interface
    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(CommandsAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {

        int viewType = -1;
        if (data.get(position).get("type").equals("bulb")) {
            viewType = TYPE_BULB;//0
        } else {
            viewType = TYPE_PLUG;
        }

        return viewType;
    }

    private RecyclerView.ViewHolder getViewHolderByViewType(int viewType) {

        View bulbView = View.inflate(mContext, R.layout.list_commands, null);

        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case TYPE_BULB:
                holder = new CommandsAdapter.BulbViewHolder(bulbView);
                break;
            case TYPE_PLUG:
                break;
        }
        return holder;
    }
}
