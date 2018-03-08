package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;


public class ClipboardMonitor {

    private static volatile ClipboardMonitor instance = null;
    private ClipboardDataBaseOperateImpl clipboardDataBaseOperateImpl;
    private OnClipboardRecentDataChangeListener onClipboardRecentDataChangeListener;

    public static ClipboardMonitor getInstance() {
        if (instance == null) {
            synchronized (ClipboardMonitor.class) {
                if (instance == null) {
                    instance = new ClipboardMonitor();
                }
            }
        }
        return instance;
    }

    private ClipboardMonitor() {
        clipboardDataBaseOperateImpl = ClipboardDataBaseOperateImpl.getInstance();
    }

    public void registerClipboardMonitor() {
        final ClipboardManager clipboard = (ClipboardManager) HSApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                public void onPrimaryClipChanged() {
                    CharSequence text = clipboard.getText();
                    if (!TextUtils.isEmpty(text)) {
                        String item = text.toString();
                        int currentRecentSize = clipboardDataBaseOperateImpl.getRecentSize();
                        HSLog.d(ClipboardMonitor.class.getSimpleName(), "     ClipboardMonitor    add  new data      " + item);
                        //用户新增recent数据，与recent不重复，则添加并置顶
                        if (currentRecentSize <= ClipboardConstants.RECENT_TABLE_SIZE & !clipboardDataBaseOperateImpl.isRecentItemExists(item)) {
                            boolean isSuccess = clipboardDataBaseOperateImpl.insertRecentItem(item, clipboardDataBaseOperateImpl.isPinItemExists(item) ? 1 : 0, currentRecentSize);
                            ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem = clipboardDataBaseOperateImpl.getRecentItem(item);
                            if (onClipboardRecentDataChangeListener == null) {
                                return;
                            }
                            if (isSuccess) {
                                if (currentRecentSize == ClipboardConstants.RECENT_TABLE_SIZE) {
                                    onClipboardRecentDataChangeListener.onDeleteTheLastRecentAndNewRecentAddSuccess(recentItem);
                                } else {
                                    onClipboardRecentDataChangeListener.onNewRecentAddSuccess(recentItem);
                                }
                            } else {
                                if (currentRecentSize == ClipboardConstants.RECENT_TABLE_SIZE) {
                                    onClipboardRecentDataChangeListener.onDeleteTheLastRecentAndNewRecentAddFail(recentItem);
                                }
                                int isPined = clipboardDataBaseOperateImpl.isPinItemExists(item) ? 1 : 0;
                                onClipboardRecentDataChangeListener.onNewRecentAddFail(new ClipboardRecentViewAdapter.ClipboardRecentMessage(item, isPined));
                            }
                        }
                        //用户新增recent数据，与recent已有内容重复，则置顶重复内容
                        else if (clipboardDataBaseOperateImpl.isRecentItemExists(item)) {
                            ClipboardRecentViewAdapter.ClipboardRecentMessage lastRecentItem = clipboardDataBaseOperateImpl.getRecentItem(item);
                            if (lastRecentItem != null) {
                                boolean isSuccess = clipboardDataBaseOperateImpl.moveExistRecentItemToBottom(item, clipboardDataBaseOperateImpl.isPinItemExists(item) ? 1 : 0);
                                if (onClipboardRecentDataChangeListener == null) {
                                    return;
                                }
                                if (isSuccess) {
                                    onClipboardRecentDataChangeListener.onExistRecentAddSuccess(lastRecentItem);
                                } else {
                                    onClipboardRecentDataChangeListener.onExistRecentAddFail(lastRecentItem);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    void setOnClipboardRecentDataChangeListener(OnClipboardRecentDataChangeListener onClipboardRecentDataChangeListener) {
        this.onClipboardRecentDataChangeListener = onClipboardRecentDataChangeListener;
    }


    public interface OnClipboardRecentDataChangeListener {
        /**
         * 添加新的recent数据并置顶，数据库操作成功后回调UI刷新显示
         */
        void onNewRecentAddSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);

        /**
         * 添加新的recent数据并置顶，数据库操作失败后回调
         */
        void onNewRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);

        /**
         * 添加了已有的recent数据时，将已有的recent数据置顶，数据库操作成功后回调UI刷新显示
         *
         * @param exitsClipboardRecentMessage 要添加的已村在列表的recentItem
         */
        void onExistRecentAddSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage exitsClipboardRecentMessage);

        /**
         * 添加了已有的recent数据时，将已有的recent数据置顶，数据库操作失败后回调
         *
         * @param exitsClipboardRecentMessage 要添加的已村在列表的recentItem
         */
        void onExistRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage exitsClipboardRecentMessage);

        /**
         * 当recent数据已有10条时，再添加新的recent数据时，删除最后一条，并添加新数据并置顶，数据库操作成功后回调UI刷新显示
         *
         * @param clipboardRecentMessage 要添加的新recent数据
         */
        void onDeleteTheLastRecentAndNewRecentAddSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);

        /**
         * 当recent数据已有10条时，再添加新的recent数据时，删除最后一条，并添加新数据并置顶，数据库操作失败后回调
         *
         * @param clipboardRecentMessage 要添加的新recent数据
         */
        void onDeleteTheLastRecentAndNewRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);
    }
}
