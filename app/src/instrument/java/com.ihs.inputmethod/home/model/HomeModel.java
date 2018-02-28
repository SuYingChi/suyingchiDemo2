package com.ihs.inputmethod.home.model;

import android.view.View;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class HomeModel<T> {
    public T item;
    public boolean isAd;
    public int span;

    public boolean deleteEnable;

    public String title;
    public String rightButtonText;
    public boolean isTitle;
    public boolean titleClickable;
    public View.OnClickListener titleClickListener;

    public boolean isMenu;
    public boolean isTheme;
    public boolean isBackgroundBanner;
    public boolean isThemeBanner;
    public boolean isSticker;
    public View.OnClickListener customizedTitleClickListener;

    public HomeModel() {
        item = null;
        isAd = false;
        isTitle = false;
        deleteEnable = false;
        titleClickable = false;
        titleClickListener = null;
        title = null;
        rightButtonText = null;
        isBackgroundBanner = false;
        isThemeBanner = false;
        isMenu = false;
        isTheme = false;
        isSticker = false;
        customizedTitleClickListener = null;
        span = 1;
    }
}
