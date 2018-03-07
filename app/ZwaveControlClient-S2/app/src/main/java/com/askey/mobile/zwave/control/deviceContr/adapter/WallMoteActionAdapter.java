package com.askey.mobile.zwave.control.deviceContr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.WallMoteActionInfo;

import java.util.List;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/11/30 15:22
 * 修改人：skysoft
 * 修改时间：2017/11/30 15:22
 * 修改备注：
 */
public class WallMoteActionAdapter extends RecyclerView.Adapter<WallMoteActionAdapter.ViewHolder> implements View.OnClickListener{
    private Context mContext;
    //    private List<Map<String,String>> data;
    private List<WallMoteActionInfo> wallMoteActionList;

    private RecyclerAdapter.OnItemClickListener mOnItemClickListener = null;
    int[] icons = new int[]{ R.drawable.vector_drawable_wmplu,R.drawable.vector_drawable_wmpru,R.drawable.vector_drawable_wmpld,R.drawable.vector_drawable_wmprd,
            R.drawable.vector_drawable_wmslu,R.drawable.vector_drawable_wmsru,R.drawable.vector_drawable_wmsld,R.drawable.vector_drawable_wmsrd};

    public WallMoteActionAdapter(Context context, List<WallMoteActionInfo> wallMoteActionList) {
        mContext = context;
        this.wallMoteActionList = wallMoteActionList;
    }

    @Override
    public WallMoteActionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_set_up, parent, false);
        WallMoteActionAdapter.ViewHolder viewHolder = new WallMoteActionAdapter.ViewHolder(view);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WallMoteActionAdapter.ViewHolder holder, int position) {
        WallMoteActionInfo wallMoteActionInfo = wallMoteActionList.get(position);
        String type = wallMoteActionInfo.getType();
        int endpointId = Integer.parseInt(wallMoteActionInfo.getEndpointId());
        int groupId = Integer.parseInt(wallMoteActionInfo.getGroupId());
        switch (groupId) {
            case 2:
                holder.tvType.setText("SWITCH ON/OFF");
                holder.ivIcon.setImageResource(icons[0]);
                break;
            case 3:
                holder.tvType.setText("Dimmer Control");
                holder.ivIcon.setImageResource(icons[4]);
                break;
            case 4:
                holder.tvType.setText("SWITCH ON/OFF");
                holder.ivIcon.setImageResource(icons[1]);
                break;
            case 5:
                holder.tvType.setText("Dimmer Control");
                holder.ivIcon.setImageResource(icons[5]);
                break;
            case 6:
                holder.tvType.setText("SWITCH ON/OFF");
                holder.ivIcon.setImageResource(icons[2]);
                break;
            case 7:
                holder.tvType.setText("Dimmer Control");
                holder.ivIcon.setImageResource(icons[6]);
                break;
            case 8:
                holder.tvType.setText("SWITCH ON/OFF");
                holder.ivIcon.setImageResource(icons[3]);
                break;
            case 9:
                holder.tvType.setText("Dimmer Control");
                holder.ivIcon.setImageResource(icons[7]);
                break;
        }

        //将position保存在itemView的Tag中，以便点击时进行获取
        holder.tvName.setText(wallMoteActionInfo.getName());     //需要通过wallMoteActionInfo获取设备名称
        holder.itemView.setTag(position);
    }


    @Override
    public int getItemCount() {
        return wallMoteActionList.size();
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
        public TextView tvName,tvType;
        private ImageView ivIcon;

        public ViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvType = (TextView) view.findViewById(R.id.tv_type);
            ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
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
