package com.ihs.inputmethod.latin;

import android.util.Log;

import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.uimodules.KeyboardPluginManager;

/**
 * Created by xu.zhang on 11/3/15.
 */
public class LatinIME extends HSUIInputMethodService {

    @Override
    public void onCreate() {
        Log.e("time log","time log service oncreated started");
        super.onCreate();
        KeyboardPluginManager.getInstance().onInputMethodServiceCreate();
        setPanelSwitcher(KeyboardPluginManager.getInstance());
        Log.e("time log","time log service oncreated started");
    }

}
