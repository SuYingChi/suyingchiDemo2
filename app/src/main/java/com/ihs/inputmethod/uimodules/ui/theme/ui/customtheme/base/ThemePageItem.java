package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base;


import java.util.List;

import me.drakeet.multitype.ItemViewProvider;

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
        public ItemViewProvider provider;
        public Class<I> itemClazz;

        public CategoryItem(String categoryName, Class<I> itemClazz, ItemViewProvider provider, List<I> data) {
            this.categoryName = categoryName;
            this.data = data;
            this.provider = provider;
            this.itemClazz = itemClazz;
        }
    }
}
