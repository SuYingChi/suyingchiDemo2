package com.ihs.booster.common.expandablelist.viewholder;

import android.view.View;

import com.ihs.booster.common.expandablelist.GroupExpandableListView;
import com.ihs.booster.common.expandablelist.data.BaseItemData;


/**
 * Created by sharp on 15/9/1.
 */

public class BaseItemViewHolder extends BaseViewHolder {
    protected boolean isLastChild;

    public BaseItemViewHolder(GroupExpandableListView listView, View holderView, OnViewHolderClickedListener listener) {
        super(listView, holderView, listener);
    }

    public void bindData(int groupPosition, int childPosition, boolean isLastChild, BaseItemData viewData) {
        super.bindData(groupPosition, childPosition, viewData);
        this.isLastChild = isLastChild;
    }

    @Override
    public void onClick(View view) {
        if (onViewHolderClickedListener != null) {
            onViewHolderClickedListener.onChildClicked(itemView, groupPosition, childPosition, itemData);
        }
    }
}
