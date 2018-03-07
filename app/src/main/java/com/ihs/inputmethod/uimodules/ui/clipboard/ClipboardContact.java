package com.ihs.inputmethod.uimodules.ui.clipboard;

import java.util.List;

/**
 * Created by yingchi.su on 2018/3/7.
 */

public interface ClipboardContact {

    //由于数据操作都是同步操作，中间层可直接拿到数据层的数据处理结果，暂时不需要中间层接口，

    //数据层接口
    interface ClipboardSQLiteOperate {
        List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getRecentAllContentFromTable();

        List<String> getPinsAllContentFromTable();


        boolean deleteItemInPinsTable(String item);

        boolean setItemPositionToBottomInRecentTable(String item, int isPined);

        boolean queryItemExistsInRecentTable(String item);

        int queryItemExistsInPinsTable(String item);


        boolean deleteRecentItemAndSetItemPositionToBottomInPins(String item);

        boolean deleteRecentItemAndAddToPins(String item);

        boolean deletePinsItemAndUpdateRecentItemNoPined(String item);

        int queryItemInRecentTableReversePosition(String item);

        int queryItemInPinsTableReversePosition(String item);

        ClipboardRecentViewAdapter.ClipboardRecentMessage getRecentItemFromTable(String item);
    }
//视图层接口
    interface ClipboardView {

        void notifyDeleteRecentAndSetPinsItemToTopDataSetChange(int pinsLastPosition);

        void notifyDeleteRecentAndAddPinsDataItemToTopChange();

        void notifyDeletePinsDataItem();

        void notifyDeletePinsItemUpdateRecentNoPined(int position);

        void deletePinsDataItemFail();

        void deletePinsItemUpdateRecentNoPinedFail(int recentPosition);

        void deleteRecentAndSetPinsItemToTopFail(int pinsLastPosition);

        void deleteRecentAndAddPinsDataItemToTopFail();
    }

}
