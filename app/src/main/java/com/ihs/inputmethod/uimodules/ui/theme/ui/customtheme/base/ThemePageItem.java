package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base;


import com.keyboard.core.themes.custom.elements.KCBaseElement;

import java.util.List;

/**
 * Created by chenyuanming on 09/11/2016.
 */

public class ThemePageItem {
    public List<CategoryItem<? extends Object>> categories;

    public ThemePageItem(List<CategoryItem<? extends Object>> categories) {
        this.categories = categories;
    }
    public static class CategoryItem<I extends Object> {
        public String categoryName;
        public List<I> data;
        public BaseThemeItemProvider provider;
        public Class<I> itemClazz;

        public CategoryItem(String categoryName, Class<I> itemClazz, BaseThemeItemProvider provider, List<I> data) {
            this.categoryName = categoryName;
            this.data = data;
            this.provider = provider;
            this.itemClazz = itemClazz;
        }
    }
}
