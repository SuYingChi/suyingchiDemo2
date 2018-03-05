package com.ihs.inputmethod.uimodules.ui.clipboard;


public interface OnClipboardDataBaseOperateFinishListener {
    void addRecentItemSuccess();

    void setRecentItemToTopSuccess();

    void deletePinsItemSuccess();


    void deleteRecentItemAndSetItemPositionToBottomInPins();

    void deleteRecentItemAndAddToPins();

    void deletePinsItemAndUpdateRecentItemNoPined();
}
