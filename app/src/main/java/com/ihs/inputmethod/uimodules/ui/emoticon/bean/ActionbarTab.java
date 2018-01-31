package com.ihs.inputmethod.uimodules.ui.emoticon.bean;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ActionbarTab {
    public String viewName;
    public RecyclerView view;
    public int iconResId;
    public Class panelClass;
    public String panelName;

    public ActionbarTab(String panelName, RecyclerView view, int iconResId) {
        this.viewName = panelName;
        this.view = view;
        this.iconResId = iconResId;
    }
    public ActionbarTab(String panelName, Class panelClass, int iconResId) {
        this.panelName = panelName;
        this.panelClass = panelClass;
        this.iconResId = iconResId;
    }
}
