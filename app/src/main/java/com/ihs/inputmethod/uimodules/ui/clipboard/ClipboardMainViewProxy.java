package com.ihs.inputmethod.uimodules.ui.clipboard;


public interface ClipboardMainViewProxy {

    void notifyDeleteRecentAndSetPinsItemToTopDataSetChange(int lastPosition);

    void notifySetRecentItemToTopDataSetChange(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage, int position);

    void notifyAddRecentDataItemToTopChange();

    void notifyDeleteRecentAndAddPinsDataItemToTopChange();

    void notifyDeletePinsDataItem();

    void notifyUpdateRecentNoPined(int position);

    void clipboardDataBaseOperateFail();
}
