
package com.ihs.booster.common.recyclerview.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ihs.booster.common.recyclerview.expandable.ExpandableItemViewHolder;
import com.ihs.booster.common.recyclerview.expandable.annotation.ExpandableItemStateFlags;


public abstract class AbstractExpandableItemViewHolder extends RecyclerView.ViewHolder implements ExpandableItemViewHolder {
    @ExpandableItemStateFlags
    private int mExpandStateFlags;

    public AbstractExpandableItemViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void setExpandStateFlags(@ExpandableItemStateFlags int flags) {
        mExpandStateFlags = flags;
    }

    @Override
    @ExpandableItemStateFlags
    public int getExpandStateFlags() {
        return mExpandStateFlags;
    }
}
