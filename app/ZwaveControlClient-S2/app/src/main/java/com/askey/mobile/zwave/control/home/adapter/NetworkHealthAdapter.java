package com.askey.mobile.zwave.control.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.NetworkHealthInfo;

import java.util.List;

/**
 * Created by skysoft on 2017/11/29.
 */

public class NetworkHealthAdapter extends RecyclerView.Adapter<NetworkHealthAdapter.MyViewHolder> implements View.OnClickListener{

    private static final String TAG = "NetworkHealthAdapter";
    private List<NetworkHealthInfo> dataList;
    private OnItemClickListener onItemClickListener = null;

    public NetworkHealthAdapter(List<NetworkHealthInfo> dataList) {
        this.dataList = dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (LayoutInflater.from(parent.getContext())).inflate(R.layout.network_health_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NetworkHealthInfo networkHealthInfo = dataList.get(position);
        Log.i(TAG, "----getDirectNodeId: "+networkHealthInfo.getDirectNodeId());
        Log.i(TAG, "----getNetworkHealth: "+networkHealthInfo.getNetworkHealth());
        Log.i(TAG, "----getRssiHopsValue: "+networkHealthInfo.getRssiHopsValue());
        holder.textViewNodeId.setText(networkHealthInfo.getDirectNodeId());
        holder.textViewNetworkHealth.setText(networkHealthInfo.getNetworkHealth());
        holder.textViewRSSIValue.setText(networkHealthInfo.getRssiHopsValue());
        //holder.textViewRSSIValue.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return null == dataList ? 0 : dataList.size();
    }

    @Override
    public void onClick(View v) {

    }

    public static interface OnItemClickListener {
        void onItemClick(View view, String roomName);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {


        private TextView textViewNodeId;
        private TextView textViewNetworkHealth;
        private TextView textViewRSSIValue;

        public MyViewHolder(View itemView) {
            super(itemView);
            textViewNodeId = (TextView) itemView.findViewById(R.id.direct_nodeid);
            textViewNetworkHealth = (TextView) itemView.findViewById(R.id.network_health);
            textViewRSSIValue = (TextView) itemView.findViewById(R.id.rssi_value);
        }
    }
}
