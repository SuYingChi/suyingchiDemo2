package com.ihs.inputmethod.uimodules.ui.emoticon.bean;

public class ActionbarTab {
    public String panelName;
    public Class panelClass;
    public int iconResId;

    public ActionbarTab(String panelName, Class panelClass, int iconResId) {
        this.panelName = panelName;
        this.panelClass = panelClass;
        this.iconResId = iconResId;
    }
}
