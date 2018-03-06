package com.ihs.inputmethod.uimodules.ui.clipboard;


public interface OnClipboardDataBaseOperateFinishListener {

    void addRecentItemSuccess();

    void setRecentItemToTopSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage,int position);

    void deletePinsItemSuccess();

    void deleteRecentItemAndSetItemPositionToBottomInPins(int lastPosition);

    void deleteRecentItemAndAddToPins();

    void deletePinsItemAndUpdateRecentItemNoPined(int position);

    void clipboardDataBaseOperateFail();
}
