package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

/**
 * Disable predictive animations. There is a bug in RecyclerView which causes views that
 * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
 * adapter size has decreased since the ViewHolder was recycled.
 */
public class SafeGridLayoutManager extends GridLayoutManager {

    public SafeGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}