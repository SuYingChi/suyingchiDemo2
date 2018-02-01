package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.font;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.ads.AdsItem;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.ads.AdsProvider;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.ThemePageItem;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.ThemePageItem.CategoryItem;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.keyboard.core.themes.custom.elements.KCFontElement;
import com.keyboard.core.themes.custom.elements.KCTextColorElement;

import java.util.Arrays;


/**
 * Created by chenyuanming on 31/10/2016.
 */

public class FontFragment extends BaseThemeFragment {

    @Override
    public void onResume() {
        super.onResume();
        HSThemeNewTipController.getInstance().removeNewTip(HSThemeNewTipController.ThemeTipType.NEW_TIP_FONT); // 清除对应元素new mark
    }

    @Override
    protected ThemePageItem initiateThemePageItem() {
        return new ThemePageItem(Arrays.asList(
                new CategoryItem<>(HSApplication.getContext().getString(R.string.custom_theme_title_font), AdsItem.class, new AdsProvider(), getAdsItems(1, 0.75f, false)),
                new CategoryItem<>(HSApplication.getContext().getString(R.string.custom_theme_title_font), KCFontElement.class, new FontProvider(this), KCCustomThemeManager.getInstance().getFontElements()),
                new CategoryItem<>(HSApplication.getContext().getString(R.string.custom_theme_title_font_color), KCTextColorElement.class, new FontColorProvider(this), KCCustomThemeManager.getInstance().getTextColorElements())
        ));
    }

}