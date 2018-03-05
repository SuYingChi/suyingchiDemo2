package com.ihs.inputmethod.uimodules.ui.clipboard;


public interface ClipboardMainViewListener {
    void notifyPinsDataSetChange();

    void notifyRecentDataSetChange();

    void changeToShowPinsView();

    void changeToShowRecentView();
}
