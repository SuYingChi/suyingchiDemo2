package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arthur on 17/12/8.
 */

public class ClipboardMonitor {

    private static ClipboardMonitor instance = null;
    private final SharedPreferences sp;
    private SharedPreferences.Editor recentClipSpEditor;
    private List<String> recentlist = new ArrayList<String>(10);


    public static ClipboardMonitor getInstance() {
        if (instance == null) {
            instance = new ClipboardMonitor();
        }
        return instance;
    }

    public ClipboardMonitor() {
        sp = HSApplication.getContext().getSharedPreferences("recentClip", Context.MODE_PRIVATE);
        recentClipSpEditor =  sp.edit();
        loadArray(sp,recentlist);

    }

    public void registerClipboardMonitor(ClipboardPresenter clipboardPresenter) {
        final ClipboardManager clipboard = (ClipboardManager) HSApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                public void onPrimaryClipChanged() {
                    CharSequence text = clipboard.getText();
                    if (!TextUtils.isEmpty(text)) {
                        String data = text.toString();
                        clipboardPresenter.recentDataOperate(recentlist,data);
                        saveArrayToSp(recentClipSpEditor,recentlist);
                    }
                }
            });
        }
    }



    public static boolean saveArrayToSp(SharedPreferences.Editor editor,List<String> list) {
        editor.putInt("clipSize", list.size());

        for (int i = 0; i < list.size(); i++) {
            editor.putString("clipValue" + i, list.get(i));
        }

        return editor.commit();
    }

        public static void loadArray(SharedPreferences sp,List<String> list) {

            list.clear();
            int size = sp.getInt("clipSize", 0);

            for(int i=0;i<size;i++) {
                list.add(sp.getString("clipValue" + i, null));
            }
        }

    public List<String> getRecentList() {
        return recentlist;
    }
}

