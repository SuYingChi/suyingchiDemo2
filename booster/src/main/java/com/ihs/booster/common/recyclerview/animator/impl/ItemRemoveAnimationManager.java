
package com.ihs.booster.common.recyclerview.animator.impl;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.ihs.booster.common.recyclerview.animator.BaseItemAnimator;


public abstract class ItemRemoveAnimationManager extends BaseItemAnimationManager<RemoveAnimationInfo> {
    private static final String TAG = "ARVItemRemoveAnimMgr";

    public ItemRemoveAnimationManager(BaseItemAnimator itemAnimator) {
        super(itemAnimator);
    }

    @Override
    public long getDuration() {
        return mItemAnimator.getRemoveDuration();
    }

    @Override
    public void setDuration(long duration) {
        mItemAnimator.setRemoveDuration(duration);
    }

    @Override
    public void dispatchStarting(RemoveAnimationInfo info, RecyclerView.ViewHolder item) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchRemoveStarting(" + item + ")");
        }
        mItemAnimator.dispatchRemoveStarting(item);
    }

    @Override
    public void dispatchFinished(RemoveAnimationInfo info, RecyclerView.ViewHolder item) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchRemoveFinished(" + item + ")");
        }
        mItemAnimator.dispatchRemoveFinished(item);
    }

    @Override
    protected boolean endNotStartedAnimation(RemoveAnimationInfo info, RecyclerView.ViewHolder item) {
        if ((info.holder != null) && ((item == null) || (info.holder == item))) {
            onAnimationEndedBeforeStarted(info, info.holder);
            dispatchFinished(info, info.holder);
            info.clear(info.holder);
            return true;
        } else {
            return false;
        }
    }

    public abstract boolean addPendingAnimation(RecyclerView.ViewHolder holder);
}
