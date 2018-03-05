package com.ihs.inputmethod.uimodules.ui.clipboard;

import java.util.List;


public interface ClipboardSQLiteOperate {
    List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getRecentAllContentFromTable();

    List<String> getPinsAllContentFromTable();

    void deleteItemInRecentTable(String item);

    void deleteItemInPinsTable(String item);

    void setItemPositionToBottomInRecentTable(String item);

    void addItemToBottomInPinsTable(String item);

    void addItemToBottomInRecentTable(String item);

    boolean queryItemExistsInRecentTable(String item);

    boolean queryItemExistsInPinsTable(String item);

    void clipDataOperateAddRecent(String item);

    void deleteRecentItemAndSetItemPositionToBottomInPins(String item);

    void deleteRecentItemAndAddToPins(String item);

    void deletePinsItemAndUpdateRecentItemNoPined(String item);

    void setOnDataBaseOperateFinishListener(OnClipboardDataBaseOperateFinishListener OnClipboardDataBaseOperateFinishListener);
}
