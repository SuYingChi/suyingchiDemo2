package com.ihs.customtheme.app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.customtheme.R;
import com.ihs.customtheme.app.utils.BitmapUtil;

/**
 * Created by dsapphire on 16/5/19.
 */
public class HSCommonHeaderView extends RelativeLayout {

	private TextView textCancel;
	private TextView textOK;
	private TextView textHead;

	private String titleCancel;
	private String titleOK;
	private String titleHead;
	private boolean backButtonVisible;
	private boolean nextButtonVisible;

	private OnHeadButtonClickListener headButtonClickListener;

	public HSCommonHeaderView(Context context) {
		this(context,null);
	}

	public HSCommonHeaderView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public HSCommonHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setText(final String cancel,final String title,final String ok){
		findViews();
		titleCancel=cancel;
		titleHead=title;
		titleOK=ok;
		updateTitle();
	}

	private void findViews() {
		if(textHead!=null){
			return;
		}
		textCancel = (TextView) findViewById(R.id.custom_theme_title_cancel);
		textOK = (TextView) findViewById(R.id.custom_theme_title_ok);
		textHead = (TextView) findViewById(R.id.custom_theme_title_title);
	}

	private void updateTitle() {
		findViews();
		if(textCancel==null||titleHead==null){
			return;
		}
		textHead.setText(titleHead);
		textOK.setText(titleOK);
		textCancel.setText(titleCancel);
	}

	public void setButtonVisibility(final boolean backButtonVisible,final boolean nextButtonVisible){
		findViews();
		this.backButtonVisible=backButtonVisible;
		this.nextButtonVisible=nextButtonVisible;
		updateButtonVisibility();
	}

	private void updateButtonVisibility() {
		if(textCancel==null){
			return;
		}
		if (backButtonVisible) {
			Drawable[] compoundDrawables = textCancel.getCompoundDrawables();
			Drawable buttonCancelLeftDrawable = null;
			if (compoundDrawables.length > 0) {
				buttonCancelLeftDrawable = compoundDrawables[0];
			}

			if (buttonCancelLeftDrawable == null) {
				Bitmap backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.navigationbar_arrow_back);
				int arrowBackWidth = (int) (backBitmap.getWidth() * 0.6);
				int arrowBackHeight = (int) (backBitmap.getHeight() * 0.6) + 6;

				Bitmap arrowBackBitmap = Bitmap.createScaledBitmap(backBitmap, arrowBackWidth, arrowBackHeight, true);
				BitmapDrawable arrowBackDrawable = new BitmapDrawable(HSApplication.getContext().getResources(), arrowBackBitmap);

				Bitmap pressedArrowBackBitmap = BitmapUtil.makeDarkBitmap(arrowBackBitmap,arrowBackWidth,arrowBackHeight,0.2f);

				BitmapDrawable pressedArrowBackBitmapDrawable = new BitmapDrawable(HSApplication.getContext().getResources(), pressedArrowBackBitmap);
				StateListDrawable stateListDrawable = new StateListDrawable();
				stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedArrowBackBitmapDrawable);
				stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedArrowBackBitmapDrawable);
				stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedArrowBackBitmapDrawable);
				stateListDrawable.addState(new int[]{}, arrowBackDrawable);

				textCancel.setCompoundDrawablesWithIntrinsicBounds(stateListDrawable, null, null, null);
				textCancel.setCompoundDrawablePadding(10);
			}
		} else {
			textCancel.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}

		if (nextButtonVisible) {
			Drawable[] compoundDrawables = textOK.getCompoundDrawables();
			Drawable buttonOKRightDrawable = null;
			if (compoundDrawables.length > 2) {
				buttonOKRightDrawable = compoundDrawables[2];
			}

			if (buttonOKRightDrawable == null) {
				Bitmap nextBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.navigationbar_arrow_next);
				int arrowNextWidth = (int) (nextBitmap.getWidth() * 0.6);
				int arrowNextHeight = (int) (nextBitmap.getHeight() * 0.6) + 6;

				Bitmap arrowNextBitmap = Bitmap.createScaledBitmap(nextBitmap, arrowNextWidth, arrowNextHeight, true);
				BitmapDrawable arrowNextDrawable = new BitmapDrawable(HSApplication.getContext().getResources(), arrowNextBitmap);

				Bitmap pressedArrowNextBitmap = BitmapUtil.makeDarkBitmap(arrowNextBitmap,arrowNextWidth,arrowNextHeight,0.2f);

				BitmapDrawable pressedArrowNextBitmapDrawable = new BitmapDrawable(HSApplication.getContext().getResources(), pressedArrowNextBitmap);
				StateListDrawable stateListDrawable = new StateListDrawable();
				stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedArrowNextBitmapDrawable);
				stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedArrowNextBitmapDrawable);
				stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedArrowNextBitmapDrawable);
				stateListDrawable.addState(new int[]{}, arrowNextDrawable);

				textOK.setCompoundDrawablesWithIntrinsicBounds(null, null, stateListDrawable, null);
				textOK.setCompoundDrawablePadding(10);
			}
		} else {
			textOK.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
	}

	public void setHeadButtonClickListener(final OnHeadButtonClickListener headButtonClickListener) {
		this.headButtonClickListener=headButtonClickListener;
		setListener();
	}

	private void setListener() {
		if(textCancel==null||headButtonClickListener==null){
			return;
		}
		textCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (headButtonClickListener != null) {
					headButtonClickListener.onCancelClick();
				}
			}
		});
		textOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (headButtonClickListener != null) {
					headButtonClickListener.onOKClick();
				}
			}
		});
	}

}
