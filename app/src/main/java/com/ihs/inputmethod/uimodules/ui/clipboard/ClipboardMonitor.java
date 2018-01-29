package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;


/**
 * Created by Arthur on 17/12/8.
 */

public class ClipboardMonitor {

    private static ClipboardMonitor instance = null;
    private SharedPreferences.Editor recentClipSpEditor;


    public static ClipboardMonitor getInstance() {
        if (instance == null) {
            instance = new ClipboardMonitor();
        }
        return instance;
    }

    public ClipboardMonitor() {

    }

    public void registerClipboardMonitor(ClipboardPresenter clipboardPresenter) {
        final ClipboardManager clipboard = (ClipboardManager) HSApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                public void onPrimaryClipChanged() {
                    CharSequence text = clipboard.getText();
                    if (!TextUtils.isEmpty(text)) {
                        String data = text.toString();
                        clipboardPresenter.recentDataOperate(data);
                        ClipboardPresenter.getInstance().saveArrayToSp(recentClipSpEditor);
                    }
                }
            });
        }
    }

}

