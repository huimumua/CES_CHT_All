package com.askey.mobile.zwave.control.deviceContr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;

import java.util.List;

/**
 * Created by skysoft on 2017/10/24.
 */

public class SwipeMenuAdapter extends RecyclerView.Adapter<SwipeMenuAdapter.ViewHolder> implements View.OnClickListener{
    private Context mContext;
    private List<ScenesInfo> data;
    private RecyclerAdapter.OnItemClickListener mOnItemClickListener = null;
    int[] icons = new int[]{ R.drawable.vector_drawable_wmplu,R.drawable.vector_drawable_wmpru,R.drawable.vector_drawable_wmpld,R.drawable.vector_drawable_wmprd,
            R.drawable.vector_drawable_wmslu,R.drawable.vector_drawable_wmsru,R.drawable.vector_drawable_wmsld,R.drawable.vector_drawable_wmsrd};

    public SwipeMenuAdapter(Context context, List<ScenesInfo> data) {
        mContext = context;
        this.data = data;
    }

    @Override
    public SwipeMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_commands, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        if ("BULB".equals(data.get(position).get("type"))) {
//           holder.tvType.setText("SWITCH ON/OFF");
//        } else if ("PLUG".equals(data.get(position).get("type"))) {
//            holder.tvType.setText("Dimmer Control");
//        } else {
//            holder.tvType.setText("Dimmer Control");
//        }
//        int endpointId = Integer.parseInt((String) data.get(position).get("endpointId"));
//        int groupId = Integer.parseInt((String) data.get(position).get("groupId"));
//        switch (endpointId) {
//            case 1:
//                if (groupId == 2) {//touch left/up
//                    holder.ivIcon.setImageResource(icons[0]);
//                } else if (groupId == 3) {//slide left/up
//                    holder.ivIcon.setImageResource(icons[4]);
//                }
//
//                break;
//            case 2:
//                if (groupId == 2) {//touch right/up
//                    holder.ivIcon.setImageResource(icons[1]);
//                } else if (groupId == 3) {//slide right/up
//                    holder.ivIcon.setImageResource(icons[5]);
//                }
//                break;
//            case 3:
//                if (groupId == 2) {//touch left/down
//                    holder.ivIcon.setImageResource(icons[2]);
//                } else if (groupId == 3) {//slide left/down
//                    holder.ivIcon.setImageResource(icons[6]);
//                }
//                break;
//            case 4:
//                if (groupId == 2) {//touch right/up
//                    holder.ivIcon.setImageResource(icons[3]);
//                } else if (groupId == 3) {//slide right/up
//                    holder.ivIcon.setImageResource(icons[7]);
//                }
//                break;
//        }

        if ("BULB".equals(data.get(position).getCategory())) {
            holder.ivIcon.setImageResource(R.drawable.vector_drawable_ic_device_79);
        } else if ("PLUG".equals(data.get(position).getCategory())) {
            holder.ivIcon.setImageResource(R.drawable.vector_drawable_ic_81);
        } else {
            holder.ivIcon.setImageResource(R.drawable.vector_drawable_ic_65);
        }

        //将position保存在itemView的Tag中，以便点击时进行获取
        holder.tvName.setText(data.get(position).getDeviceName());
        holder.tvAction.setText(data.get(position).getTargetSatus());
        holder.tvTimmer.setText("Timer:" + data.get(position).getTimer());
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
        public TextView tvName,tvTimmer,tvAction;
        private ImageView ivIcon;

        public ViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvTimmer = (TextView) view.findViewById(R.id.tv_timmer);
            tvAction = (TextView) view.findViewById(R.id.tv_action);
            ivIcon = (ImageView) view.findViewById(R.id.iv_command_icon);
        }
    }

    //define interface
    public static interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public void setOnItemClickListener(RecyclerAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
