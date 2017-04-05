package com.ihs.inputmethod.uimodules.ui.theme.ui.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ihs.inputmethod.uimodules.ui.theme.utils.CompatUtils;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class BackgroundItemDecoration extends RecyclerView.ItemDecoration {

	private int rowGap;
	private int columnGap;

	public BackgroundItemDecoration(int rowGap, int columnGap) {
		this.rowGap = CompatUtils.updateGapValueAccordingVersion(rowGap);
		this.columnGap = CompatUtils.updateGapValueAccordingVersion(columnGap);
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
		int leftPadding = 0, rightPadding = 0, topPadding , bottomPadding ;
		if (position == 0) {
			rightPadding = columnGap / 2;
		} else if (position == parent.getAdapter().getItemCount() - 1) {
			leftPadding = columnGap / 2;
		} else {
			leftPadding = columnGap / 2;
			rightPadding = columnGap / 2;
		}
		topPadding = rowGap / 2;
		bottomPadding = rowGap / 2;
		outRect.set(leftPadding, topPadding, rightPadding, bottomPadding);
	}
}