package com.ihs.inputmethod.uimodules.ui.facemoji.faceswitcher;

import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;

class FaceLayoutParams {

	public static int GRID_ROW_NUMBER      = 2;
	public static int GRID_COL_NUMBER      = 4;
	public static int GRID_ROW_NUMBER_LAND = 2;

	private int mEmojiKeyboardHeight;
	private int mEmojiPageIdViewHeight;

	private int mGridViewVerticalSpacing;
	private int mGridViewHorizontalSpacing;

	private int mGridViewColNumber;
	private int mGridViewRowNumber;
	private int mGridWidth;
	private int mGridHeight;

	public FaceLayoutParams(Resources res) {

		final int defaultKeyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(res);
		final int defaultKeyboardWidth = HSResourceUtils.getDefaultKeyboardWidth(res);

		mEmojiPageIdViewHeight = (int) (res.getDimension(R.dimen.config_gif_emoji_search_page_id_view_height));
		mGridViewHorizontalSpacing = 10;
		mGridViewVerticalSpacing = 10;
		// bottom margin to page id view

		final int orientation = res.getConfiguration().orientation;
		HSLog.d("orientation: " + orientation);
		mEmojiKeyboardHeight = defaultKeyboardHeight - mEmojiPageIdViewHeight;
		mGridViewColNumber = GRID_COL_NUMBER;
		mGridViewRowNumber = GRID_ROW_NUMBER;
		mGridWidth = (defaultKeyboardWidth- mGridViewHorizontalSpacing*(mGridViewColNumber+1))/ mGridViewColNumber;
		mGridHeight = (mEmojiKeyboardHeight- mGridViewVerticalSpacing*(mGridViewRowNumber+1))/mGridViewRowNumber;
		if(mGridWidth>mGridHeight){
			mGridWidth = mGridHeight;
			mGridViewHorizontalSpacing = (defaultKeyboardWidth - (mGridViewColNumber*mGridHeight))/(mGridViewColNumber+1);
		}else{
			mGridHeight = mGridWidth;
			mGridViewVerticalSpacing = (mEmojiKeyboardHeight - (mGridViewRowNumber*mGridWidth))/(mGridViewRowNumber+1);
		}
		HSLog.d("width: " + mGridWidth + " height: " + mGridHeight);
		HSLog.d("row: " + mGridViewRowNumber + " col: " + mGridViewColNumber);
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
		gridView.setPadding(mGridViewHorizontalSpacing, mGridViewVerticalSpacing, mGridViewHorizontalSpacing, mGridViewVerticalSpacing);
		gridView.setVerticalSpacing(mGridViewVerticalSpacing);
		gridView.setHorizontalSpacing(mGridViewHorizontalSpacing);
		gridView.setNumColumns(mGridViewColNumber);
	}
}