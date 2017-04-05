package com.ihs.inputmethod.uimodules.ui.theme.ui.model;

import android.view.View;

import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeHomeModel {

	public HSKeyboardTheme keyboardTheme;
	public boolean deleteEnable;

	public boolean isAd;
	public int span;

	public boolean isTitle;
	public boolean titleClickable;
	public View.OnClickListener titleClickListener;
	public String title;
	public String rightButton;

	public boolean isBackground;

	public boolean isBanner;

	public boolean isBlankView;

	public boolean isCustomizedTitle;
	public boolean deleteButtonVisible;
	public String customizedTitle;
	public View.OnClickListener customizedTitleClickListener;

	public ThemeHomeModel() {
		keyboardTheme=null;
		deleteEnable=false;
		isAd=false;
		isTitle=false;
		titleClickable=false;
		titleClickListener=null;
		title=null;
		rightButton=null;
		isBackground=false;
		isBanner=false;
		isBlankView=false;
		isCustomizedTitle=false;
		deleteButtonVisible=false;
		customizedTitle=null;
		customizedTitleClickListener=null;
		span=1;
	}
}
