package com.ihs.customtheme.app.ui;

import com.ihs.inputmethod.theme.HSCustomThemeItemBase;

/**
 * Created by dsapphire on 16/5/7.
 */
public interface CustomThemeItemView {

	void selectedThemeItem();
	void unSelectedThemeItem();
	void lockedThemeItem();
	void unLockedThemeItem();
	HSCustomThemeItemBase getCustomThemeItem();
}
