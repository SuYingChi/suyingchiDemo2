package com.ihs.booster.common.expandablelist.adapter;

import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.ihs.booster.common.expandablelist.GroupExpandableListView;
import com.ihs.booster.common.expandablelist.data.BaseGroupData;
import com.ihs.booster.common.expandablelist.data.BaseItemData;
import com.ihs.booster.common.expandablelist.viewholder.BaseGroupViewHolder;
import com.ihs.booster.common.expandablelist.viewholder.BaseItemViewHolder;
import com.ihs.booster.common.expandablelist.viewholder.BaseViewHolder.OnViewHolderClickedListener;

import java.util.ArrayList;
import java.util.List;


public class ExpandableAdapter extends BaseExpandableListAdapter {
    private List<Integer> groupLayoutIDList = new ArrayList<>();
    private List<Integer> childLayoutIDList = new ArrayList<>();
    private List<Class<? extends BaseGroupViewHolder>> groupHolderClassList = new ArrayList<>();
    private List<Class<? extends BaseItemViewHolder>> childHolderClassList = new ArrayList<>();
    private OnViewHolderClickedListener onViewHolderClickedListener;
    private GroupExpandableListView groupExpandableListView;
    private List<BaseGroupData> groupDataList = new ArrayList<>();
    private int backColorID = -1;

    public ExpandableAdapter(GroupExpandableListView groupExpandableListView, int groupLayoutID,
                             Class<? extends BaseGroupViewHolder> groupHolderClass, int childLayoutID,
                             Class<? extends BaseItemViewHolder> childHolderClass) {
        this.groupExpandableListView = groupExpandableListView;
        groupLayoutIDList.add(groupLayoutID);
        childLayoutIDList.add(childLayoutID);
        groupHolderClassList.add(groupHolderClass);
        childHolderClassList.add(childHolderClass);
    }

    public ExpandableAdapter(GroupExpandableListView groupExpandableListView, List<Integer> groupLayoutIDList,
                             List<Class<? extends BaseGroupViewHolder>> groupHolderClassList, List<Integer> childLayoutIDList,
                             List<Class<? extends BaseItemViewHolder>> childHolderClassList) {
        this.groupExpandableListView = groupExpandableListView;
        this.groupLayoutIDList = groupLayoutIDList;
        this.childLayoutIDList = childLayoutIDList;
        this.groupHolderClassList = groupHolderClassList;
        this.childHolderClassList = childHolderClassList;
    }


    public void setOnViewHolderClickedListener(OnViewHolderClickedListener onViewHolderClickedListener) {
        this.onViewHolderClickedListener = onViewHolderClickedListener;
    }

    public void setDataSource(List<BaseGroupData> groupDataList) {
        this.groupDataList = groupDataList;
        notifyDataSetChanged();
    }

    public void setBackgroundColor(@ColorInt int backColorID) {
        this.backColorID = backColorID;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return groupDataList.size();
    }

    @Override
    public BaseGroupData getGroup(int groupPosition) {
        return groupDataList.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        BaseGroupViewHolder holder = null;
        String holderClassName = getGroupClass(groupPosition).getClass().getName();
        if (convertView == null || !TextUtils.equals(convertView.getTag().getClass().getName(), holderClassName)) {
            try {
                convertView = LayoutInflater.from(parent.getContext()).inflate(getGroupLayout(groupPosition), parent, false);
                holder = getGroupClass(groupPosition).getConstructor(GroupExpandableListView.class, View.class, OnViewHolderClickedListener.class).newInstance
                        (groupExpandableListView, convertView, onViewHolderClickedListener);
                convertView.setTag(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder = (BaseGroupViewHolder) convertView.getTag();
        }
        if (holder != null) {
            holder.bindData(groupPosition, isExpanded, getGroup(groupPosition));
            holder.setBackgroundColor(backColorID);
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return getGroup(groupPosition).getChildrenCount();
    }

    @Override
    public BaseItemData getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).getChild(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        BaseItemViewHolder holder = null;
        String holderClassName = getChildClass(groupPosition).getName();
        if (convertView == null || !TextUtils.equals(convertView.getTag().getClass().getName(), holderClassName)) {
            try {
                convertView = LayoutInflater.from(parent.getContext()).inflate(getChildLayout(groupPosition), parent, false);
                holder = getChildClass(groupPosition).getConstructor(GroupExpandableListView.class, View.class, OnViewHolderClickedListener.class).newInstance
                        (groupExpandableListView, convertView, onViewHolderClickedListener);
                convertView.setTag(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder = (BaseItemViewHolder) convertView.getTag();
        }
        if (holder != null) {
            holder.bindData(groupPosition, childPosition, isLastChild, getChild(groupPosition, childPosition));
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private Class<? extends BaseGroupViewHolder> getGroupClass(int groupPosition) {
        int position = groupPosition > groupHolderClassList.size() - 1 ? groupHolderClassList.size() - 1 : groupPosition;
        return groupHolderClassList.get(position);
    }

    private Class<? extends BaseItemViewHolder> getChildClass(int groupPosition) {
        int position = groupPosition > childHolderClassList.size() - 1 ? childHolderClassList.size() - 1 : groupPosition;
        return childHolderClassList.get(position);
    }

    private int getGroupLayout(int groupPosition) {
        int postion = groupPosition > groupLayoutIDList.size() - 1 ? groupLayoutIDList.size() - 1 : groupPosition;
        return groupLayoutIDList.get(postion);
    }

    private int getChildLayout(int groupPosition) {
        int postion = groupPosition > childLayoutIDList.size() - 1 ? childLayoutIDList.size() - 1 : groupPosition;
        return childLayoutIDList.get(postion);
    }
}