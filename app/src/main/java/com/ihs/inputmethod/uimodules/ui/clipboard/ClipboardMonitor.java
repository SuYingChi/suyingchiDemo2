package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardPresenter.RECENT_TABLE_SIZE;


public class ClipboardMonitor {

    private static ClipboardMonitor instance = null;
    private ClipboardDataBaseOperateImpl clipboardDataBaseOperateImpl;
    private OnClipboardRecentDataChangeListener onClipboardRecentDataChangeListener;

    public static ClipboardMonitor getInstance() {
        if (instance == null) {
            instance = new ClipboardMonitor();
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
                        if (currentRecentSize <= RECENT_TABLE_SIZE & !clipboardDataBaseOperateImpl.isRecentItemExists(item)) {
                            boolean isSuccess = clipboardDataBaseOperateImpl.insertRecentItem(item, clipboardDataBaseOperateImpl.isPinItemExists(item), currentRecentSize);
                            ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem = clipboardDataBaseOperateImpl.getRecentItem(item);
                            if (onClipboardRecentDataChangeListener == null) {
                                return;
                            }
                            if (isSuccess) {
                                if (currentRecentSize == RECENT_TABLE_SIZE) {
                                    onClipboardRecentDataChangeListener.onDeleteTheLastRecentAndNewRecentAdd(recentItem);
                                } else {
                                    onClipboardRecentDataChangeListener.onNewRecentAdd(recentItem);
                                }
                            } else {
                                if (currentRecentSize == RECENT_TABLE_SIZE) {
                                    onClipboardRecentDataChangeListener.onDeleteTheLastRecentAndNewRecentAddFail(recentItem);
                                }
                                int isPined = clipboardDataBaseOperateImpl.isPinItemExists(item);
                                onClipboardRecentDataChangeListener.onNewRecentAddFail(new ClipboardRecentViewAdapter.ClipboardRecentMessage(item, isPined));
                            }
                        }
                        //用户新增recent数据，与recent已有内容重复，则置顶重复内容
                        else if (clipboardDataBaseOperateImpl.isRecentItemExists(item)) {
                            ClipboardRecentViewAdapter.ClipboardRecentMessage lastRecentItem = clipboardDataBaseOperateImpl.getRecentItem(item);
                            if (lastRecentItem != null) {
                                boolean isSuccess = clipboardDataBaseOperateImpl.moveExistRecentItemToBottom(item, clipboardDataBaseOperateImpl.isPinItemExists(item));
                                if (onClipboardRecentDataChangeListener == null) {
                                    return;
                                }
                                if (isSuccess) {
                                    onClipboardRecentDataChangeListener.onExistRecentAdd(lastRecentItem);
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

    //数据库操作失败的话回调fail接口，日后根据需要拓展
    public interface OnClipboardRecentDataChangeListener {
        //添加新的recent数据并置顶，收藏图标会根据该数据是否已被收藏选择是否高亮，
        void onNewRecentAdd(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);

        void onNewRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);

        //添加了已有的recent数据时，将已有的recent数据置顶
        void onExistRecentAdd(ClipboardRecentViewAdapter.ClipboardRecentMessage lastClipboardRecentMessage);

        void onExistRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);

        //当recent数据已有10条时，再添加新的recent数据时，删除最后一条，并添加新数据并置顶
        void onDeleteTheLastRecentAndNewRecentAdd(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);

        void onDeleteTheLastRecentAndNewRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);
    }
}
