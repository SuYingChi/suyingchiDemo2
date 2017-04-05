package com.ihs.booster.common.expandablelist.viewholder;

import android.support.annotation.ColorInt;
import android.view.View;

import com.ihs.booster.common.expandablelist.GroupExpandableListView;
import com.ihs.booster.common.expandablelist.data.BaseGroupData;


/**
 * Created by sharp on 15/9/1.
 */

public class BaseGroupViewHolder extends BaseViewHolder {
    protected boolean isExpanded = true;
    protected BaseGroupData groupData;

    public BaseGroupViewHolder(GroupExpandableListView listView, View holderView, OnViewHolderClickedListener listener) {
        super(listView, holderView, listener);
    }

    public void bindData(int groupPosition, boolean isExpanded, BaseGroupData viewData) {
        super.bindData(groupPosition, -1, viewData);
        groupData = (BaseGroupData) itemData;
        this.isExpanded = isExpanded;
    }

    public void setBackgroundColor(@ColorInt int backColorID) {
        if (backColorID != -1) {
            itemView.setBackgroundColor(backColorID);
        }
    }

    @Override
    public void onClick(View view) {
        if (onViewHolderClickedListener != null) {
            onViewHolderClickedListener.onGroupClicked(itemView, groupPosition, groupData);
        }
    }
}
