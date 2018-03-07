package com.askey.mobile.zwave.control.deviceContr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceList;
import com.askey.mobile.zwave.control.deviceContr.model.IotDeviceBean;
import com.askey.mobile.zwave.control.util.Const;

import java.util.ArrayList;

/**
 * Created by skysoft on 2017/7/20.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements View.OnClickListener{
    private Context mContext;
    private ArrayList<Object> data;
    private OnItemClickListener mOnItemClickListener = null;

    public RecyclerAdapter(Context context, ArrayList<Object> data) {
        mContext = context;
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (Const.isRemote) {
            holder.tvDeviceId.setText("Device Id : " + ((IotDeviceBean)data.get(position)).getDeviceId());
            holder.tvDeviceName.setText("Device Name : " + ((IotDeviceBean)data.get(position)).getDisplayName());
        } else {
            holder.tvDeviceId.setText("Device Id : " + ((DeviceList.NodeInfoList)data.get(position)).getNodeId());
            holder.tvDeviceName.setText("Device Name : " +  ((DeviceList.NodeInfoList)data.get(position)).getDeviceName());
        }

        //将position保存在itemView的Tag中，以便点击时进行获取
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
        public TextView tvDeviceId, tvDeviceName;

        public ViewHolder(View view) {
            super(view);
            tvDeviceId = (TextView) view.findViewById(R.id.tv_device_nodeid);
            tvDeviceName = (TextView) view.findViewById(R.id.tv_device_name);
        }
    }

    //define interface
    public static interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
