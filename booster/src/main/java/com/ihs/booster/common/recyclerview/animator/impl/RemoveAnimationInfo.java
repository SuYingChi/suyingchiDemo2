
package com.ihs.booster.common.recyclerview.animator.impl;

import android.support.v7.widget.RecyclerView;

public class RemoveAnimationInfo extends ItemAnimationInfo {
    public RecyclerView.ViewHolder holder;

    public RemoveAnimationInfo(RecyclerView.ViewHolder holder) {
        this.holder = holder;
    }

    @Override
    public RecyclerView.ViewHolder getAvailableViewHolder() {
        return holder;
    }

    @Override
    public void clear(RecyclerView.ViewHolder item) {
        if (holder == item) {
            holder = null;
        }
    }

    @Override
    public String toString() {
        return "RemoveAnimationInfo{" +
                "holder=" + holder +
                '}';
    }
}
