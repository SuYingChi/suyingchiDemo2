package com.ihs.inputmethod.uimodules.ui.clipboard;

import java.util.List;

/**
 * Created by yingchi.su on 2018/3/7.
 */

public interface ClipboardContact {

    //数据层接口,目前只需要数据库操作，日后根据需要再增加数据层接口
    //由于数据操作都是同步操作，中间层可直接拿到数据层的数据处理结果，暂时不需要中间层接口回调通知处理结果
     //数据库表里的数据与UI显示的数据顺序是反转的。
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
    //数据库操作失败的话回调fail接口，目前只弹个toast，日后根据需要拓展
    interface ClipboardView {
//收藏里已有recent数据，再次收藏recent数据时，删除recent数据，收藏那边置顶
        void onDeleteRecentAndMovePinToTop(String lastPinItem);

        void onDeleteRecentAndMovePinToTopFail(String lastPinItem);
//收藏新的recent数据时，删除recent数据，并添加到收藏并置顶
        void onDeleteRecentAndAddPin();

        void onDeleteRecentAndAddPinFail();
//删除单条收藏
        void onDeletePin();

        void onDeletePinFail();
//删除单条收藏，recent那边如果有同样的数据，标明recent那边的数据为未被收藏
        void onDeletePinAndUnpinRecent(ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem);

        void onDeletePinAndUnpinRecentFail(ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem);


    }

}
