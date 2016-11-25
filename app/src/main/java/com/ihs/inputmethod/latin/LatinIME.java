package com.ihs.inputmethod.latin;

import android.util.Log;
import android.view.View;

import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.uimodules.KeyboardPanelManager;

/**
 * Created by xu.zhang on 11/3/15.
 */
public class LatinIME extends HSUIInputMethodService {

    @Override
    public void onCreate() {
        Log.e("time log","time log service oncreated started");
        super.onCreate();
        KeyboardPanelManager.getInstance().onInputMethodServiceCreate();
        setPanelSwitcher(KeyboardPanelManager.getInstance());
// ;
        Log.e("time log","time log service oncreated started");
    }


    @Override
    public View onCreateInputView() {
        return KeyboardPanelManager.getInstance().createKeyboardPanelSwitchContainer(super.onCreateInputView());
    }
}
