package com.ihs.inputmethod.latin;

import android.content.res.Configuration;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.framework.HSInputMethodService;
import com.keyboard.inputmethod.panels.gif.control.DataManager;
import com.keyboard.inputmethod.panels.gif.control.GifManager;
import com.keyboard.inputmethod.panels.gif.dao.base.LanguageDao;
import com.keyboard.inputmethod.panels.gif.emojisearch.ESManager;
import com.keyboard.inputmethod.panels.gif.ui.view.CustomSearchEditText;

/**
 * Created by xu.zhang on 11/3/15.
 */
public class LatinIME extends HSInputMethodService {
    public static final String HS_NOTIFICATION_SHOW_WINDOW="hs.keyboard.showWindow";
    public static final String HS_NOTIFICATION_DISCONNECT_INSIDE_CONNECTION="hs.keyboard.DISCONNECT_INSIDE_CONNECTION";
    public static final String HS_NOTIFICATION_SERVICE_DESTROY="hs.keyboard.SERVICE_DESTROY";
    public static final String HS_NOTIFICATION_SERVICE_START_INPUT_VIEW="hs.keyboard.SERVICE_START_INPUT_VIEW";
    public static final String HS_NOTIFICATION_PARAM_EDITOR_OWNER_PACKAGE_NAME = "editor_owner_package_name";
    private static InputConnection insideConnection=null;
    private volatile boolean isOnDestroy=false;

    @Override
    public void showWindow(boolean showInput) {
        if(showInput)
            HSGlobalNotificationCenter.sendNotification(HS_NOTIFICATION_SHOW_WINDOW);
        super.showWindow(showInput);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        GifManager.init();
        DataManager.getInstance().loadUserData();
        isOnDestroy=false;
    }

    public static void onStartInputInside(CustomSearchEditText editText) {
        HSInputMethodService inputMethodService= HSInputMethod.getInputService();
        inputMethodService.onFinishInputView(true);
        EditorInfo editorInfo=editText.getEditorInfo();
        insideConnection=editText.onCreateInputConnection(editorInfo);
        inputMethodService.onStartInputView(editorInfo, true);
    }

    public static void onFinishInputInside(){
        if(insideConnection!=null){
            HSInputMethodService inputMethodService= HSInputMethod.getInputService();
            inputMethodService.onFinishInput();
            inputMethodService.cleanupInternalStateForInsideEditText();
        }
    }

    @Override
    public InputConnection getCurrentInputConnection() {
        if(insideConnection!=null){
            return insideConnection;
        }
        return super.getCurrentInputConnection();
    }

    @Override
    public void onFinishInput() {
        if(insideConnection!=null){
            insideConnection=null;
            HSGlobalNotificationCenter.sendNotification(HS_NOTIFICATION_DISCONNECT_INSIDE_CONNECTION);
        }
        super.onFinishInput();
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        super.onStartInputView(editorInfo, restarting);
        // Broadcast event
        final HSBundle bundle = new HSBundle();
        bundle.putString(HS_NOTIFICATION_PARAM_EDITOR_OWNER_PACKAGE_NAME, editorInfo.packageName);
        HSGlobalNotificationCenter.sendNotification(HS_NOTIFICATION_SERVICE_START_INPUT_VIEW, bundle);
    }

    @Override
    public void onFinishInputView(final boolean finishingInput) {
        if (insideConnection != null) {
            insideConnection = null;
            HSGlobalNotificationCenter.sendNotification(HS_NOTIFICATION_DISCONNECT_INSIDE_CONNECTION);
        }
        if(!isOnDestroy){
            ESManager.getInstance().onFinishInputView();
        }
        super.onFinishInputView(finishingInput);
    }

    @Override
    public void onConfigurationChanged(Configuration conf) {
        ESManager.getInstance().onConfigurationChanged();
        super.onConfigurationChanged(conf);
    }

    @Override
    public void onDestroy() {
        isOnDestroy=true;
        super.onDestroy();
        HSGlobalNotificationCenter.sendNotification(HS_NOTIFICATION_SERVICE_DESTROY);
    }

    @Override
    public void loadKeyboard() {
        LanguageDao.updateCurrentLanguage();
        super.loadKeyboard();
    }
}
