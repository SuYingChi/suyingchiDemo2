package com.ihs.inputmethod.uimodules.ui.emoticon.bean;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ActionbarTab {
    public int iconResId;
    public Class panelClass;
    public String panelName;

    public ActionbarTab(String panelName, Class panelClass, int iconResId) {
        this.panelName = panelName;
        this.panelClass = panelClass;
        this.iconResId = iconResId;
    }
}
