package com.ihs.inputmethod.uimodules.ui.stickerdeprecated;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.api.utils.HSResourceUtils;

class StickerLayoutParams {
	private static int DEFAULT_KEYBOARD_ROWS = 4;
	
	public int mEmojiPagerHeight;
	private int mEmojiPagerBottomMargin;
	public int mEmojiKeyboardHeight;
	private int mEmojiCategoryPageIdViewHeight;
	public int mEmojiActionBarHeight;
	public int mKeyVerticalGap;
	private int mKeyHorizontalGap;
	private int mBottomPadding;
	private int mTopPadding;


//	//
	private int mPagerHeight;
	private int mPagerBottomMargin;
	private int mCategoryPageIdViewHeight;
	private int mGridViewVerticalSpacing;
	private int mGridViewHorizontalSpacing;
//
//	// gridview
	private int mGridViewColunmNumber;
	private int mGridViewRowNumber;
	private int mViewWidth;
	private int mViewHeight;
	private int mTabbarHeight;


	private int orientation;
	public StickerLayoutParams(Resources res) {
		orientation=res.getConfiguration().orientation;

		int defaultKeyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(res);
		int defaultKeyboardWidth = res.getDisplayMetrics().widthPixels<res.getDisplayMetrics().heightPixels?res.getDisplayMetrics().widthPixels:res.getDisplayMetrics().heightPixels;

		mKeyVerticalGap = (int) res.getFraction(R.fraction.config_key_vertical_gap_pink,
				defaultKeyboardHeight, defaultKeyboardHeight);
		mBottomPadding = (int) res.getFraction(R.fraction.config_keyboard_bottom_padding_pink,
				defaultKeyboardHeight, defaultKeyboardHeight);
		mTopPadding = (int) res.getFraction(R.fraction.config_keyboard_top_padding_pink,
				defaultKeyboardHeight, defaultKeyboardHeight);
		mKeyHorizontalGap = (int) (res.getFraction(R.fraction.config_key_horizontal_gap_pink,
				defaultKeyboardWidth, defaultKeyboardWidth));
		mEmojiCategoryPageIdViewHeight = (int) (res.getDimension(R.dimen.config_emoji_category_page_id_height));
		final int baseheight = defaultKeyboardHeight - mBottomPadding - mTopPadding
				+ mKeyVerticalGap;
		mEmojiActionBarHeight = baseheight / DEFAULT_KEYBOARD_ROWS
				- (mKeyVerticalGap - mBottomPadding) / 2;
		mEmojiPagerHeight = defaultKeyboardHeight - mEmojiActionBarHeight
				- mEmojiCategoryPageIdViewHeight;
		mEmojiPagerBottomMargin = 0;
		mEmojiKeyboardHeight = mEmojiPagerHeight - mEmojiPagerBottomMargin - 1;
		//
		mGridViewVerticalSpacing = 5;
		mGridViewHorizontalSpacing = 5;
		mCategoryPageIdViewHeight = (int) res.getDimension(R.dimen.config_emoji_category_page_id_height);

		///
		mTabbarHeight = res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height);
		mPagerHeight = defaultKeyboardHeight - mTabbarHeight - mCategoryPageIdViewHeight;
		mPagerBottomMargin = 0;

		// gridview
		mGridViewColunmNumber = StickerManager.StickerCategory.COL;
		mGridViewRowNumber = StickerManager.StickerCategory.ROW;

		// gif view width
		int horizontalGapTotal = (mGridViewColunmNumber - 1) * mGridViewHorizontalSpacing;
		mViewWidth = (defaultKeyboardWidth - horizontalGapTotal)/mGridViewColunmNumber;
		// gif view height
		int verticalGapTotal = (mGridViewRowNumber + 1) * mGridViewVerticalSpacing;
		mViewHeight =(mEmojiKeyboardHeight - verticalGapTotal)/mGridViewRowNumber;

		Log.e("FacemojiLayoutParams",mViewWidth+":"+mViewHeight);
		if(orientation== Configuration.ORIENTATION_LANDSCAPE){
			mGridViewRowNumber = StickerManager.StickerCategory.ROW_LANDSCAPE;
			mGridViewColunmNumber= (int) Math.ceil(res.getDisplayMetrics().widthPixels*1.0/mViewWidth);
			verticalGapTotal = (mGridViewRowNumber + 1) * mGridViewVerticalSpacing;
			mViewHeight =(mEmojiKeyboardHeight - verticalGapTotal)/mGridViewRowNumber;
			int addSpacing=mViewHeight-mViewWidth;
			mViewHeight=mViewWidth;
			mGridViewVerticalSpacing+=addSpacing/2;
		}
		Log.e("FacemojiLayoutParams",mViewWidth+":"+mViewHeight);
		StickerManager.getInstance().saveSize(mGridViewRowNumber*mGridViewColunmNumber);
	}

	public void setPagerProperties(ViewPager vp) {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vp.getLayoutParams();
		lp.height = mEmojiKeyboardHeight;
		lp.bottomMargin = mEmojiPagerBottomMargin;
		vp.setLayoutParams(lp);
	}

	public void setCategoryPageIdViewProperties(View v) {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
		lp.height = mEmojiCategoryPageIdViewHeight;
		v.setLayoutParams(lp);
	}
	public int getViewWidth() {
		return mViewWidth;
	}

	public int getViewHeight() {
		return mViewHeight;
	}

	public void setActionBarProperties(final LinearLayout ll) {
		final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll.getLayoutParams();
		lp.height = getActionBarHeight();
		ll.setLayoutParams(lp);
	}

	public void setPageGridViewProperties(View v) {
		StickerPageGridView gridView = (StickerPageGridView)v;
		gridView.setPadding(mGridViewVerticalSpacing, mGridViewVerticalSpacing, mGridViewVerticalSpacing, mGridViewVerticalSpacing);
		gridView.setVerticalSpacing(mGridViewVerticalSpacing);
		gridView.setHorizontalSpacing(mGridViewHorizontalSpacing);
		gridView.setNumColumns(mGridViewColunmNumber);
	}
	public int getActionBarHeight() {
		return mEmojiActionBarHeight - mBottomPadding;
	}
	public void setKeyProperties(final View v) {
		final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
		lp.leftMargin = mKeyHorizontalGap / 2;
		lp.rightMargin = mKeyHorizontalGap / 2;
		v.setLayoutParams(lp);
	}
}