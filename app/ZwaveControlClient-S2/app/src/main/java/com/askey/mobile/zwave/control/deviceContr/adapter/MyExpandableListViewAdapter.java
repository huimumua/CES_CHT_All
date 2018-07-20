package com.askey.mobile.zwave.control.deviceContr.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.Info;
import com.askey.mobile.zwave.control.deviceContr.scenes.APITestActivity;
import com.askey.mobile.zwave.control.home.adapter.CommandListAdapter;

import java.util.List;

/**
 * Created by Administrator on 2017\10\10 0010.
 */

public class MyExpandableListViewAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private String nodeId;

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * 每个分组的名字的集合
     */
    private List<Info> groupList;

    public MyExpandableListViewAdapter(Context mContext, List<Info> groupList) {
        this.mContext = mContext;
        this.groupList = groupList;
    }

    public void setGroupList(List<Info> groupList) {
        this.groupList = groupList;
    }

    public List<Info> getGroupList() {
        return groupList;
    }


    public MyExpandableListViewAdapter(Context mContext) {
        this.mContext = mContext;
    }


    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        return groupList.get(childPosition).getData();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup
            parent) {
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.expandablelist_group, null);
        }
        // ImageView ivGroup = (ImageView) convertView.findViewById(R.id.iv_group);
        TextView tvGroup = (TextView) convertView.findViewById(R.id.tv_group);
        // 如果是展开状态，就显示展开的箭头，否则，显示折叠的箭头
        if (isExpanded) {
            //ivGroup.setImageResource(R.mipmap.drop_down_selected_icon);
        } else {
            // ivGroup.setImageResource(R.mipmap.drop_down_unselected_icon);
        }
        // 设置分组组名
        if (groupList.size() != 0) {
            tvGroup.setText("" + groupList.get(groupPosition).getName());
        }

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View
            convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.expandablelist_item, parent, false);
            viewHolder.recycleView = (RecyclerView) convertView.findViewById(R.id.comm_list);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // 因为 convertView 的布局就是一个 GridView，
        // 所以可以向下转型为 GridView

        // 创建 GridView 适配器
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        viewHolder.recycleView.setLayoutManager(layoutManager);
        CommandListAdapter commandListAdapter = new CommandListAdapter(groupList.get(groupPosition).getData());
        viewHolder.recycleView.setAdapter(commandListAdapter);
        if (nodeId.equals("1")) {
            commandListAdapter.setOnItemClickListener(null);
        } else {
            commandListAdapter.setOnItemClickListener(new CommandListAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, String str) {
                    Log.i("onItemClick", "====str=" + str);
                    Intent intent = new Intent();
                    intent.setClass(mContext, APITestActivity.class);
                    intent.putExtra("title", str);

                    intent.putExtra("nodeId", nodeId);
                    mContext.startActivity(intent);
                }
            });
        }

//        MyGridViewAdapter gridViewAdapter = new MyGridViewAdapter(mContext, classChild);
//        viewHolder.gridView.setAdapter(gridViewAdapter);
//        viewHolder.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                Toast.makeText(mContext, "点击了第" + (groupPosition + 1) + "组，第" +
////                        (position + 1) + "项", Toast.LENGTH_SHORT).show();
//                String classKey = classChild.get(position).getClassKey();
//               // String className = classChild.get(position).getClassName();
//                if (onClassName!=null){
//                    CourseTypeBean.DataListBean.ClassChildBeanX.ClassChildBean classChildBean = classChild.get(position);
//                    String classLeave = classChildBean.getClassLeave();
//                    onClassName.onClassName(classKey,classChildBean.getClassName(),classLeave);
//                }
//            }
//        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public class ViewHolder {
        private RecyclerView recycleView;
    }

}