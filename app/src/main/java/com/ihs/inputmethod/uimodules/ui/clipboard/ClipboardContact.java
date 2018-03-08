package com.ihs.inputmethod.uimodules.ui.clipboard;

import java.util.List;

/**
 * Created by yingchi.su on 2018/3/7.
 */

public interface ClipboardContact {

    /**
     * 由于数据操作都是同步操作，中间层可直接拿到数据层的数据处理结果，暂时不需要中间层接口回调通知处理结果
     */
    interface ClipboardSQLiteOperate {
        /**
         * @return List<ClipboardRecentViewAdapter.ClipboardRecentMessage> 数据库的recent表里的内容反转后的集合
         */
        List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getRecentAllContentList();

        /**
         * @return List<String> 数据库的Pins表里的内容反转后的集合
         */
        List<String> getPinsAllContentList();

        /**
         * 根据文本获取recent表里对应的item
         *
         * @param recentItemString recentItem的文本
         * @return recentItem
         */
        ClipboardRecentViewAdapter.ClipboardRecentMessage getRecentItem(String recentItemString);

        /**
         * @return recent表的item总数
         */
        int getRecentSize();

        /**
         * @return pins表的item总数
         */

        int getPinsSize();

        /**
         * 根据pinItem的文本删除pin表对应的item
         *
         * @param deletePinItemString pinItem的文本
         * @return 删除是否成功
         */
        boolean deletePinItem(String deletePinItemString);

        /**
         * 添加recent表已有的数据时，将现有的recentItem删除并添加到最底下
         *
         * @param existRecentItemString 添加的recent表里已有数据的文本内容
         * @param isPined               添加的recent表里已有数据是否被收藏的标记变量
         * @return
         */
        boolean moveExistRecentItemToBottom(String existRecentItemString, int isPined);

        /**
         * 根据文本查询recent表里是否已存在该文本
         *
         * @param recentItemString recent文本
         * @return 该文本是否在recent表里已存在
         */
        boolean isRecentItemExists(String recentItemString);

        /**
         * 根据文本查询pin表里是否已存在该文本
         *
         * @param pinItem pin文本
         * @return 该文本是否在pin表里已存在
         */
        boolean isPinItemExists(String pinItem);

        /**
         * 新增RecentItem到recent表里
         *
         * @param insertRecentItemString 新增的recentItem的文本
         * @param isPined                新增的recentItem是否已被收藏的标志变量
         * @param currentRecentSize      当前recent表里的item总数
         * @return 是否新增成功
         */
        boolean insertRecentItem(String insertRecentItemString, int isPined, int currentRecentSize);

        /**
         * 当收藏的文本在收藏表里已有了，根据recentItem的文本删除recent表对应的item并在pin表里将对应的内容删除并添加到最底下
         *
         * @param deleteRecentItem 需要删除的
         * @return 数据操作是否成功
         */
        boolean deleteRecentItemAndMoveItemToBottomInPins(String deleteRecentItem);

        /**
         * 当收藏的文本在收藏表里已有了，根据recentItem的文本删除recent表对应的item并在pin表里将对应的内容删除并添加到最底下
         *
         * @param deleteRecentItem 收藏的文本
         * @return 数据操作是否成功
         */
        boolean deleteRecentItemAndAddToBottomPins(String deleteRecentItem);

        /**
         * 删除收藏里的item时，如果recent表里也有该文本，则在recent表里标明该item已不被收藏
         *
         * @param deletePinItem 删除收藏里的item
         * @return 操作是否成功
         */
        boolean deletePinItemAndUnpinRecentItem(String deletePinItem);


    }

    /**
     * 视图层接口,数据库操作失败的话回调fail接口，目前只弹个toast，日后根据需要拓展
     */
    interface ClipboardView {

        /**
         * 收藏里已有recent数据，再次收藏recent数据时，删除recent数据，收藏那边已有数据置顶，数据库操作成功后回调UI刷新显示
         *
         * @param exitsPinItem 新增收藏已有的pinItem
         * @param exitsPinItemPosition  收藏已有的pinItem的原有在UI列表的位置
         */
        void onDeleteRecentAndMovePinToTopSuccess(String exitsPinItem, int exitsPinItemPosition);
        /**
         * 收藏里已有recent数据，再次收藏recent数据时，删除recent数据，收藏那边已有数据置顶，数据库操作失败后回调
         *
         * @param exitsPinItem 新增收藏已有的pinItem
         * @param exitsPinItemPosition  收藏已有的pinItem的原有在UI列表的位置
         */
        void onDeleteRecentAndMovePinToTopFail(String exitsPinItem,int exitsPinItemPosition);

        /**
         * 收藏新的recent数据时，删除recent数据，并添加到收藏并置顶，数据库操作成功后回调UI刷新显示
         * @param selectedRecentItem  收藏的recent数据
         * @param selectedRecentItemPosition  收藏的recent数据在UI列表里的位置
         */
        void onDeleteRecentAndAddPinSuccess(String selectedRecentItem, int selectedRecentItemPosition);
        /**
         * 收藏新的recent数据时，删除recent数据，并添加到收藏并置顶，数据库操作失败后回调
         * @param selectedRecentItem  收藏的recent数据
         * @param selectedRecentItemPosition  收藏的recent数据在UI列表里的位置
         */
        void onDeleteRecentAndAddPinFail(String selectedRecentItem,int selectedRecentItemPosition);

        /**
         * 删除单条收藏
         * @param selectPinItemPosition 删除的pinItem的在列表里的位置，数据库操作成功后回调UI刷新显示
         */
        void onDeletePinSuccess(int selectPinItemPosition);
        /**
         * 删除单条收藏
         * @param selectPinItemPosition 删除的pinItem的在列表里的位置，数据库操作失败后回调
         */
        void onDeletePinFail(int selectPinItemPosition);

        /**
         * 删除单条收藏，recent那边如果有同样的数据，标明recent那边的数据未被收藏，数据库操作成功后回调UI刷新显示
         *
         * @param recentItem 需要更新的recent表的item
         * @param selectPinItemPosition  删除的pinItem在列表的position
         */
        void onDeletePinAndUnpinRecentSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem, int selectPinItemPosition);
        /**
         * 删除单条收藏，recent那边如果有同样的数据，标明recent那边的数据未被收藏，数据库操作失败后回调
         *
         * @param recentItem 需要更新的recent表的item
         * @param selectPinItemPosition  删除的pinItem在列表的position
         */
        void onDeletePinAndUnpinRecentFail(ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem,int selectPinItemPosition);


    }

}
