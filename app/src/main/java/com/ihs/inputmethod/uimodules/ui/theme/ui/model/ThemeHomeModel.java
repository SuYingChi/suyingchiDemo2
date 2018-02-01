package com.ihs.inputmethod.uimodules.ui.theme.ui.model;

import android.text.TextUtils;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeHomeModel implements ICondition {

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

	public String adPlacement;

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

	/**
	 * Check if is theme ad by ad placement
	 *
	 * All ad types:
	 * ThemeAd
	 * PreviewAd
	 * LastThemeAd
	 *
	 * @return true for ThemeAd
     */
	public boolean isThemeAd() {
        return isAd && !TextUtils.isEmpty(adPlacement) && adPlacement.equals(HSApplication.getContext().getString(R.string.theme_ad_placement_theme_ad));

    }

	@Override
	public boolean isDownloadLockerToUnlock() {
		if (keyboardTheme != null) {
			return keyboardTheme.isDownloadLockerToUnlock();
		}
		return false;
	}

	@Override
	public boolean isNeedNewVersionToUnlock() {
		if (keyboardTheme != null) {
			return keyboardTheme.isNeedNewVersionToUnlock();
		}
		return false;
	}

	@Override
	public boolean isRateToUnlock() {
		if (keyboardTheme != null) {
			return keyboardTheme.isRateToUnlock();
		}
		return false;
	}

	@Override
	public boolean isShareToUnlock() {
		if (keyboardTheme != null) {
			return keyboardTheme.isShareToUnlock();
		}
		return false;
	}

	@Override
	public boolean isDownloadInApp() {
		return HSConfig.optBoolean(false, "Application", "KeyboardTheme", "DownloadThemeZip", "DownloadInApp");
	}
}
