package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardPresenter.RECENT_TABLE_SIZE;


public class ClipboardMonitor {

    private static ClipboardMonitor instance = null;
    private ClipboardSQLiteOperate clipboardSQLiteOperate;

    public static ClipboardMonitor getInstance() {
        if (instance == null) {
            instance = new ClipboardMonitor();
        }
        return instance;
    }

    private ClipboardMonitor() {
        clipboardSQLiteOperate = ClipboardDataBaseOperateImpl.getInstance();
    }

    public void registerClipboardMonitor() {
        final ClipboardManager clipboard = (ClipboardManager) HSApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                public void onPrimaryClipChanged() {
                    CharSequence text = clipboard.getText();
                    if (!TextUtils.isEmpty(text)) {
                        String data = text.toString();
                        HSLog.d(ClipboardMonitor.class.getSimpleName(), "     ClipboardMonitor    add  new data      " + data);
                        if (clipboardSQLiteOperate.getRecentAllContentFromTable().size() <= RECENT_TABLE_SIZE & !clipboardSQLiteOperate.queryItemExistsInRecentTable(data)) {
                            clipboardSQLiteOperate.addItemToBottomInRecentTable(data);
                        }
                        //用户新增recent数据，与recent已有内容重复，则置顶重复内容
                        else if (clipboardSQLiteOperate.queryItemExistsInRecentTable(data)) {
                            clipboardSQLiteOperate.setItemPositionToBottomInRecentTable(data);
                        }
                    }
                }
            });
        }
    }
}
