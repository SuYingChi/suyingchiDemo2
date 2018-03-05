package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;


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
                        clipboardSQLiteOperate.clipDataOperateAddRecent(data);
                    }
                }
            });
        }
    }

}
