package com.mobipioneer.lockerkeyboard.app;

import android.view.inputmethod.EditorInfo;

import com.ihs.inputmethod.api.HSUIInputMethodService;

/**
 * Created by xu.zhang on 11/15.
 */
public class MyInputMethodService extends HSUIInputMethodService {

    public static final String HS_NOTIFICATION_SHOW_WINDOW = "hs.keyboard.showWindow";
    public static final String HS_NOTIFICATION_SERVICE_START_INPUT_VIEW = "hs.keyboard.SERVICE_START_INPUT_VIEW";

    @Override
    public void onCreate() {
//        HSKeyboardSwitcher.init(this);
        super.onCreate();
//        KeyboardPluginManager.getInstance().onInputMethodServiceCreate();
//        setPanelSwitcher(KeyboardPluginManager.getInstance());
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        super.onStartInputView(editorInfo, restarting);
    }

}
