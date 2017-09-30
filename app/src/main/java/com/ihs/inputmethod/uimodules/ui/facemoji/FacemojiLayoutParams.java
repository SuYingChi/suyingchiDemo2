package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.stickerdeprecated.StickerManager;

class FacemojiLayoutParams {
	public int mEmojiPagerHeight;
	//private int mEmojiCategoryPageIdViewHeight;
	public int mKeyVerticalGap;

	private int mGridViewVerticalSpacing;
	private int mGridViewHorizontalSpacing;

	private int mGridViewColunmNumber;
	private int mGridViewRowNumber;
	private int mViewWidth;
	private int mViewHeight;


	private int orientation;
	public FacemojiLayoutParams(Resources res) {
		orientation=res.getConfiguration().orientation;

		int defaultKeyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(res)- (int) HSApplication.getContext().getResources().getDimension(R.dimen.emoticon_panel_actionbar_height);
		int defaultKeyboardWidth = res.getDisplayMetrics().widthPixels<res.getDisplayMetrics().heightPixels?res.getDisplayMetrics().widthPixels:res.getDisplayMetrics().heightPixels;

		mKeyVerticalGap = (int) res.getFraction(R.fraction.config_key_vertical_gap_pink,
				defaultKeyboardHeight, defaultKeyboardHeight);

		//mEmojiCategoryPageIdViewHeight = (int) (res.getDimension(R.dimen.config_emoji_category_page_id_height));



		mEmojiPagerHeight = defaultKeyboardHeight;
		//
		mGridViewVerticalSpacing = 30;
		mGridViewHorizontalSpacing = 30;

		// gridview
		mGridViewColunmNumber = FacemojiManager.FacemojiPalettesParam.COL;
		mGridViewRowNumber = FacemojiManager.FacemojiPalettesParam.ROW;

		// gif view width
		int horizontalGapTotal = (mGridViewColunmNumber + 1) * mGridViewHorizontalSpacing;
		mViewWidth = (defaultKeyboardWidth - horizontalGapTotal)/mGridViewColunmNumber;
		mViewHeight = (mEmojiPagerHeight - (mGridViewRowNumber + 1) * mGridViewHorizontalSpacing)/mGridViewRowNumber;

		if(mViewWidth>mViewHeight){
			mViewWidth = mViewHeight;
			mGridViewHorizontalSpacing = (defaultKeyboardWidth - mGridViewColunmNumber*mViewHeight)/(mGridViewColunmNumber+1);
		}
		else{
			mViewHeight = mViewWidth;
			mGridViewVerticalSpacing = (mEmojiPagerHeight - mGridViewRowNumber*mViewWidth)/(mGridViewRowNumber+1);
		}

		Log.e("FacemojiLayoutParams",mViewWidth+":"+mViewHeight);
		if(orientation== Configuration.ORIENTATION_LANDSCAPE){
			mGridViewRowNumber = FacemojiManager.FacemojiPalettesParam.ROW_LANDSCAPE;
			mGridViewColunmNumber = FacemojiManager.FacemojiPalettesParam.COL*FacemojiManager.FacemojiPalettesParam.ROW/mGridViewRowNumber;

			mGridViewHorizontalSpacing = 10;
			mViewWidth= (int)((res.getDisplayMetrics().widthPixels*1.0 - mGridViewHorizontalSpacing*(mGridViewColunmNumber+1))/mGridViewColunmNumber);
			mViewHeight=mViewWidth;
			mGridViewVerticalSpacing = (int)((mEmojiPagerHeight - mGridViewRowNumber*mViewHeight)/(mGridViewRowNumber + 1));
		}
		Log.e("FacemojiLayoutParams",mViewWidth+":"+mViewHeight);
		StickerManager.getInstance().saveSize(mGridViewRowNumber*mGridViewColunmNumber);
	}

	public void setPagerProperties(ViewPager vp) {
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) vp.getLayoutParams();
		lp.height = mEmojiPagerHeight;
		vp.setLayoutParams(lp);
	}

	public void setCategoryPageIdViewProperties(View v) {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
		//lp.height = mEmojiCategoryPageIdViewHeight;
		v.setLayoutParams(lp);
	}
	public int getViewWidth() {
		return mViewWidth;
	}

	public int getViewHeight() {return mViewHeight;}


	public void setPageGridViewProperties(View v) {
		FacemojiPageGridView gridView = (FacemojiPageGridView)v;
		gridView.setPadding(mGridViewHorizontalSpacing, mGridViewVerticalSpacing, mGridViewHorizontalSpacing, mGridViewVerticalSpacing);
		gridView.setVerticalSpacing(mGridViewVerticalSpacing);
		gridView.setHorizontalSpacing(mGridViewHorizontalSpacing);
		gridView.setNumColumns(mGridViewColunmNumber);
	}

}