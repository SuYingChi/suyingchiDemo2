package com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by dsapphire on 16/2/27.
 */
public final class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

	private int spacing;
	private int headerNum;

	public GridSpacingItemDecoration(int spacing, int headerNum) {
		this.spacing = spacing;
		this.headerNum = headerNum;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		int position = parent.getChildAdapterPosition(view) - headerNum; // item position

		if (position >= 0) {
			outRect.left   = spacing;
			outRect.right  = spacing;
			outRect.top    = spacing;
			outRect.bottom = spacing;
		} else {
			outRect.left = 0;
			outRect.right = 0;
			outRect.top = 0;
			outRect.bottom = 0;
		}
	}
}