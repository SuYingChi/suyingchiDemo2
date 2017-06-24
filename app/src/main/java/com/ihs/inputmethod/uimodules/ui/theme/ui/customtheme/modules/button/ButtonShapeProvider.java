package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.button;

import android.graphics.drawable.Drawable;

import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeItemProvider;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCBaseElement;
import com.keyboard.core.themes.custom.elements.KCButtonShapeElement;

/**
 * Created by chenyuanming on 31/10/2016.
 */

public class ButtonShapeProvider extends BaseThemeItemProvider<KCButtonShapeElement, BaseThemeItemProvider.BaseItemHolder, ButtonFragment> {
    private Drawable chosedBackgroundDrawable;

    public ButtonShapeProvider(ButtonFragment fragment) {
        super(fragment);
    }

    @Override
    protected boolean isCustomThemeItemSelected(KCBaseElement item) {
        return item instanceof KCButtonShapeElement &&
                fragment.getCustomThemeData().getButtonShapeElement().getName().equals(item.getName());
    }

    @Override
    protected Drawable getChosedBackgroundDrawable() {
        if(chosedBackgroundDrawable == null) {
            chosedBackgroundDrawable = KCElementResourseHelper.getButtonShapeChosedBackgroundDrawable();
        }
        return chosedBackgroundDrawable;
    }
}