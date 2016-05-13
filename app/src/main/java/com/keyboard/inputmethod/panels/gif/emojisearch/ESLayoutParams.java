package com.keyboard.inputmethod.panels.gif.emojisearch;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.ihs.inputmethod.base.utils.ResourceUtils;
import com.keyboard.rainbow.R;

final class ESLayoutParams {

	private int mEmojiKeyboardHeight;

	private int mGridViewVerticalSpacing;
	private int mGridViewHorizontalSpacing;

	private int mGridViewColNumber;
	private int mGridWidth;
	private int mGridHeight;

	public ESLayoutParams(Resources res) {

		final int defaultKeyboardHeight = ResourceUtils.getDefaultKeyboardHeight(res);
		final int defaultKeyboardWidth  = ResourceUtils.getDefaultKeyboardWidth(res);

		int mEmojiPageIdViewHeight = (int) (res.getDimension(R.dimen.config_gif_emoji_search_page_id_view_height));

		mEmojiKeyboardHeight = defaultKeyboardHeight - mEmojiPageIdViewHeight;

		mGridViewVerticalSpacing   = 10;
		mGridViewHorizontalSpacing = 10;

		// bottom margin to page id view
		int mGridViewBottomPadding = mGridViewVerticalSpacing * 2;

		final int orientation = res.getConfiguration().orientation;

		int mGridViewRowNumber;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			// row & col
			mGridViewColNumber = 7;
			mGridViewRowNumber = 5;

			// gif view width
			final int hSpacingTotal = (mGridViewColNumber - 1) * mGridViewHorizontalSpacing;
			mGridWidth = (defaultKeyboardWidth - hSpacingTotal) / mGridViewColNumber;

			// gif view height
			final int vSpacingTotal = (mGridViewRowNumber - 1) * mGridViewVerticalSpacing;
			mGridHeight = (mEmojiKeyboardHeight - vSpacingTotal - mGridViewBottomPadding) / mGridViewRowNumber;
		} else {
			// gif view height
			mGridViewRowNumber = 3;
			final int vSpacingTotal = (mGridViewRowNumber - 1) * mGridViewVerticalSpacing;
			mGridHeight = (mEmojiKeyboardHeight - vSpacingTotal - mGridViewBottomPadding) / mGridViewRowNumber;

			// gif view width
			mGridViewColNumber = (res.getDisplayMetrics().widthPixels / mGridHeight) - 1;
			final int hSpacingTotal = (mGridViewColNumber - 1) * mGridViewHorizontalSpacing;
			mGridWidth = (defaultKeyboardWidth - hSpacingTotal) / mGridViewColNumber;
		}


		// set max page item count
		ESManager.getInstance().setMaxPageItemCount(mGridViewRowNumber * mGridViewColNumber);
	}

	public int getGridWidth() {
		return mGridWidth;
	}

	public int getGridHeight() {
		return mGridHeight;
	}

	public void setPagerProperties(ViewPager vp) {
		final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vp.getLayoutParams();
		lp.height = mEmojiKeyboardHeight;
		vp.setLayoutParams(lp);
	}

	public void setPageGridViewProperties(GridView gridView) {
		gridView.setVerticalSpacing(mGridViewVerticalSpacing);
		gridView.setHorizontalSpacing(mGridViewHorizontalSpacing);
		gridView.setNumColumns(mGridViewColNumber);
	}
}