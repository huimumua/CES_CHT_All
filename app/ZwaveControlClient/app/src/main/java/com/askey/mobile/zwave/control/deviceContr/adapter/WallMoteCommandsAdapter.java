package com.askey.mobile.zwave.control.deviceContr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;

import java.util.List;
import java.util.Map;

/**
 * Created by skysoft on 2017/7/20.
 */

public class WallMoteCommandsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{
    private static final int TYPE_BULB = 0;
    private static final int TYPE_PLUG = 1;
    private Context mContext;
    private List<Map<String, Object>> data;
    private OnItemClickListener mOnItemClickListener = null;
    int[] icons = new int[]{ R.drawable.vector_drawable_wmplu,R.drawable.vector_drawable_wmpru,R.drawable.vector_drawable_wmpld,R.drawable.vector_drawable_wmprd,
            R.drawable.vector_drawable_wmslu,R.drawable.vector_drawable_wmsru,R.drawable.vector_drawable_wmsld,R.drawable.vector_drawable_wmsrd};

    public WallMoteCommandsAdapter(Context context, List<Map<String, Object>> data) {
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
        int endpointId = Integer.parseInt((String) data.get(position).get("endpointId"));
        int groupId = Integer.parseInt((String) data.get(position).get("groupId"));
        switch (endpointId) {
            case 1:
                if (groupId == 2) {//touch left/up
                    ((BulbViewHolder) holder).ivIcon.setImageResource(icons[0]);
                    ((BulbViewHolder) holder).tvPressSlide.setText("Press"); //press / slide 根据ground
                } else if (groupId == 3) {//slide left/up
                    ((BulbViewHolder) holder).ivIcon.setImageResource(icons[4]);
                    ((BulbViewHolder) holder).tvPressSlide.setText("Slide"); //press / slide 根据ground
                }

                break;
            case 2:
                if (groupId == 2) {//touch right/up
                    ((BulbViewHolder) holder).ivIcon.setImageResource(icons[1]);
                    ((BulbViewHolder) holder).tvPressSlide.setText("Press"); //press / slide 根据ground
                } else if (groupId == 3) {//slide right/up
                    ((BulbViewHolder) holder).ivIcon.setImageResource(icons[5]);
                    ((BulbViewHolder) holder).tvPressSlide.setText("Slide"); //press / slide 根据ground
                }
                break;
            case 3:
                if (groupId == 2) {//touch left/down
                    ((BulbViewHolder) holder).ivIcon.setImageResource(icons[2]);
                    ((BulbViewHolder) holder).tvPressSlide.setText("Press"); //press / slide 根据ground
                } else if (groupId == 3) {//slide left/down
                    ((BulbViewHolder) holder).ivIcon.setImageResource(icons[6]);
                    ((BulbViewHolder) holder).tvPressSlide.setText("Slide"); //press / slide 根据ground
                }
                break;
            case 4:
                if (groupId == 2) {//touch right/up
                    ((BulbViewHolder) holder).ivIcon.setImageResource(icons[3]);
                    ((BulbViewHolder) holder).tvPressSlide.setText("Press"); //press / slide 根据ground
                } else if (groupId == 3) {//slide right/up
                    ((BulbViewHolder) holder).ivIcon.setImageResource(icons[7]);
                    ((BulbViewHolder) holder).tvPressSlide.setText("Slide"); //press / slide 根据ground
                }
                break;
        }


        if ("PLUG".equals(String.valueOf(data.get(position).get("type")))) {
            ((BulbViewHolder) holder).tvType.setText("ON/OFF control");//on/off /dimmer control
        } else {

            ((BulbViewHolder) holder).tvType.setText("Dimmer control");//on/off /dimmer control
        }




//            ((BulbViewHolder) holder).itemView.setOnClickListener(this);
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
    public static class BulbViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPressSlide,tvType;
        public ImageView ivIcon;

        public BulbViewHolder(View view) {
            super(view);
            tvPressSlide = (TextView) view.findViewById(R.id.tv_press_slide);
            tvType = (TextView) view.findViewById(R.id.tv_type);
            ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
        }
    }

    //define interface
    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {

        int viewType = -1;
        return viewType;
    }

    private RecyclerView.ViewHolder getViewHolderByViewType(int viewType) {

        View bulbView = View.inflate(mContext, R.layout.wallmote_command_list, null);

        RecyclerView.ViewHolder holder = null;
        holder = new BulbViewHolder(bulbView);
        return holder;
    }
}
