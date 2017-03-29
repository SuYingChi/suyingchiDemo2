
package com.ihs.booster.common.recyclerview.animator.impl;

import android.support.v7.widget.RecyclerView;

public abstract class ItemAnimationInfo {
    public abstract RecyclerView.ViewHolder getAvailableViewHolder();

    public abstract void clear(RecyclerView.ViewHolder holder);
}

