package com.ihs.booster.common.expandablelist.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ihs.booster.common.expandablelist.GroupExpandableListView;
import com.ihs.booster.common.expandablelist.data.BaseGroupData;
import com.ihs.booster.common.expandablelist.data.BaseItemData;

/**
 * Created by sharp on 16/1/14.
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public interface OnViewHolderClickedListener {
        void onGroupClicked(View view, int groupPosition, BaseGroupData groupData);

        void onGroupChecked(View view, int groupPosition, boolean isChecked, BaseGroupData groupData);

        void onChildClicked(View view, int groupPosition, int childPosition, BaseItemData viewData);
    }

    protected OnViewHolderClickedListener onViewHolderClickedListener;
    protected GroupExpandableListView listView;
    protected BaseItemData itemData;
    protected int childPosition = -1;
    protected int groupPosition = -1;
    public BaseViewHolder(View holderView) {
        super(holderView);
    }

    public BaseViewHolder(GroupExpandableListView listView, View holderView, OnViewHolderClickedListener onViewHolderClickedListener) {
        super(holderView);
        this.onViewHolderClickedListener = onViewHolderClickedListener;
        this.listView = listView;
        this.itemView.setClickable(true);
        this.itemView.setOnClickListener(this);
    }

    public void bindData(int groupPosition, int childPosition, BaseItemData viewData) {
        this.childPosition = childPosition;
        this.groupPosition = groupPosition;
        this.itemData = viewData;
    }


}
