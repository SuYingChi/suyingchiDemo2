package com.ihs.inputmethod.latin;

import android.util.Log;
import android.view.View;

import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.uimodules.KeyboardPanelManager;

/**
 * Created by xu.zhang on 11/3/15.
 */
public class LatinIME extends HSUIInputMethodService {
    KeyboardPanelManager keyboardPanelManager;
    @Override
    public void onCreate() {
        Log.e("time log","time log service oncreated started");
        super.onCreate();
        keyboardPanelManager = new KeyboardPanelManager();
        setPanelSwitcher(keyboardPanelManager);
// ;
        Log.e("time log","time log service oncreated started");
    }


    @Override
    public View onCreateInputView() {
        return keyboardPanelManager.createKeyboardPanelSwitchContainer(super.onCreateInputView());
    }
}
