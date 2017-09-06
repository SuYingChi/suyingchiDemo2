package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by guonan.lv on 17/9/6.
 */

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int mSpanCount;
    private int mSpacing;
    private boolean mIncludeEdge;
    private boolean mHasHeader;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge, boolean hasHeader) {
        mSpanCount = spanCount;
        mSpacing = spacing;
        mIncludeEdge = includeEdge;
        mHasHeader = hasHeader;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        if (mHasHeader) {
            if (position == 0) {
                outRect.bottom = mSpacing;
                return;
            }
            position -= 1;
        }
        int column = position % mSpanCount; // item column

        if (mIncludeEdge) {
            outRect.left = mSpacing - column * mSpacing / mSpanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * mSpacing / mSpanCount; // (column + 1) * ((1f / spanCount) * spacing)

            if (position < mSpanCount) { // top edge
                outRect.top = mSpacing;
            }
            outRect.bottom = mSpacing; // item bottom
        } else {
            outRect.left = column * mSpacing / mSpanCount; // column * ((1f / spanCount) * spacing)
            outRect.right = mSpacing - (column + 1) * mSpacing / mSpanCount; // spacing - (column + 1) * ((1f / spanCount) * spacing)
            if (position >= mSpanCount) {
                outRect.top = mSpacing; // item top
            }
        }
    }
}

