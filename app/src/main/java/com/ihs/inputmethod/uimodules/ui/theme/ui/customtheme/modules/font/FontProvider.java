package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.font;

import android.graphics.drawable.Drawable;

import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeItemProvider;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCBaseElement;
import com.keyboard.core.themes.custom.elements.KCFontElement;

/**
 * Created by chenyuanming on 31/10/2016.
 */

public class FontProvider extends BaseThemeItemProvider<KCFontElement, BaseThemeItemProvider.BaseItemHolder, FontFragment> {

    public FontProvider(FontFragment fragment) {
        super(fragment);
    }

    @Override
    protected boolean isCustomThemeItemSelected(KCBaseElement item) {
        return item instanceof KCFontElement &&
                fragment.getCustomThemeData().getFontElement().getName().equals(item.getName());
    }


    @Override
    protected Drawable getChosedBackgroundDrawable() {
        return KCElementResourseHelper.getFontChosedBackgroundDrawable();
    }

    @Override
    protected Drawable getLockedDrawable() {
        return KCElementResourseHelper.getFontLockedDrawable();
    }

    @Override
    protected Drawable getNewMarkDrawable() {
        return KCElementResourseHelper.getFontNewMarkDrawable();
    }

    @Override
    protected Drawable getBackgroundDrawable(Object item) {
        return KCElementResourseHelper.getFontBackgroundDrawable();
    }

    @Override
    protected Drawable getPlaceHolderDrawable() {
        return KCElementResourseHelper.getFontPlaceHolderDrawable();
    }
}