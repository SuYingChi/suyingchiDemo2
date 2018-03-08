package com.ihs.inputmethod.uimodules.ui.clipboard;

import java.util.List;

/**
 * Created by yingchi.su on 2018/3/7.
 */

public interface ClipboardContact {

    //由于数据操作都是同步操作，中间层可直接拿到数据层的数据处理结果，暂时不需要中间层接口，这么做在拿到处理是否成功的结果时注意再去拿一些别要的参数
    //数据层接口
    interface ClipboardSQLiteOperate {
        List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getRecentAllContentList();

        List<String> getPinsAllContentList();

        ClipboardRecentViewAdapter.ClipboardRecentMessage getRecentItem(String recentStringItem);

        int getRecentSize();

        int getPinsSize();

        boolean deletePinItem(String deletePinItem);

        boolean moveExistRecentItemToBottom(String existRecentItem, int isPined);

        boolean isRecentItemExists(String recentItem);

        int isPinItemExists(String pinItem);

        boolean insertRecentItem(String insertRecentItem, int isPined, int currentRecentSize);

        boolean deleteRecentItemAndSetItemPositionToBottomInPins(String deleteRecentItem);

        boolean deleteRecentItemAndAddToPins(String deleteRecentItem);

        boolean deletePinItemAndUnpinRecentItem(String deletePinItem);


    }

    //视图层接口
    interface ClipboardView {

        void onDeleteRecentAndMovePinToTop(String lastPinItem);

        void onDeleteRecentAndMovePinToTopFail(String lastPinItem);

        void onDeleteRecentAndAddPin();

        void onDeleteRecentAndAddPinFail();

        void onDeletePin();

        void onDeletePinFail();

        void onDeletePinAndUnpinRecent(ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem);

        void onDeletePinAndUnpinRecentFail(ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem);


    }

}
