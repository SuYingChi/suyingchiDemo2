package com.ihs.inputmethod.home.HomeModel;

import android.view.View;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class HomeModel<T> {
    public T item;
    public boolean isAd;
    public int span;

    public String title;
    public String rightButtonText;
    public boolean isTitle;
    public boolean titleClickable;
    public View.OnClickListener titleClickListener;

    public boolean isMenu;
    public int menuTextResId;
    public int menuImageResId;

    public boolean isBanner;
    public boolean isCustomizedTitle;
    public boolean isSticker;
    public boolean isThemeList;
    public String customizedTitle;
    public View.OnClickListener customizedTitleClickListener;

    public HomeModel() {
        item = null;
        isAd = false;
        isTitle = false;
        titleClickable = false;
        titleClickListener = null;
        title = null;
        rightButtonText = null;
        isBanner = false;
        isThemeList = false;
        isCustomizedTitle = false;
        isMenu = false;
        menuTextResId = -1;
        menuImageResId = -1;
        isSticker = false;
        customizedTitle = null;
        customizedTitleClickListener = null;
        span = 1;
    }
}
