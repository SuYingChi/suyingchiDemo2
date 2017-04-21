package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.button;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.ads.AdsItem;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.ads.AdsProvider;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.ThemePageItem;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.ThemePageItem.CategoryItem;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.keyboard.core.themes.custom.elements.KCButtonShapeElement;
import com.keyboard.core.themes.custom.elements.KCButtonStyleElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Created by chenyuanming on 31/10/2016.
 */

public class ButtonFragment extends BaseThemeFragment {
    @Override
    protected ThemePageItem initiateThemePageItem() {

        List<KCButtonStyleElement> buttonStyles = new ArrayList<>();
        buttonStyles.addAll(KCCustomThemeManager.getInstance().getButtonStyleElements());
        Iterator<KCButtonStyleElement> iterator = buttonStyles.iterator();
        while (iterator.hasNext()) {
            KCButtonStyleElement kcButtonStyleElement = iterator.next();
            if ("none".equalsIgnoreCase(kcButtonStyleElement.getName())) {
                iterator.remove();
                break;
            }
        }
        return new ThemePageItem(Arrays.<CategoryItem<?>>asList(
                new CategoryItem<>(HSApplication.getContext().getString(R.string.custom_theme_title_button_shape), KCButtonShapeElement.class, new ButtonShapeProvider(this), KCCustomThemeManager.getInstance().getButtonShapeElements()),
                new CategoryItem<>(HSApplication.getContext().getString(R.string.custom_theme_title_button_style), AdsItem.class, new AdsProvider(), getAdsItems(true)),
                new CategoryItem<>(HSApplication.getContext().getString(R.string.custom_theme_title_button_style), KCButtonStyleElement.class, new ButtonStyleProvider(this), buttonStyles)
        ));
    }




}
