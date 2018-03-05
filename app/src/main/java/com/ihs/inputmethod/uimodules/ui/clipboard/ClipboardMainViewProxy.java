package com.ihs.inputmethod.uimodules.ui.clipboard;


public interface ClipboardMainViewProxy {
    void notifyPinsDataSetChange();

    void notifyRecentDataSetChange();

    void changeToShowPinsView();

    void changeToShowRecentView();
}
