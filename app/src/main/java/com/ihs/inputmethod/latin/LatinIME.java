package com.ihs.inputmethod.latin;

import android.content.res.Configuration;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.framework.HSInputMethodService;
import com.ihs.inputmethod.framework.HSKeyboardSwitcher;
import com.ihs.inputmethod.uimodules.KeyboardPluginManager;
import com.ihs.inputmethod.uimodules.constants.Constants;
import com.ihs.inputmethod.uimodules.ui.gif.common.ui.view.CustomSearchEditText;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.DataManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base.LanguageDao;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.emojisearch.ESManager;
import com.keyboard.rainbow.thread.AsyncThreadPools;

/**
 * Created by xu.zhang on 11/3/15.
 */
public class LatinIME extends HSUIInputMethodService {
    public static final String HS_NOTIFICATION_DISCONNECT_INSIDE_CONNECTION="hs.keyboard.DISCONNECT_INSIDE_CONNECTION";
    public static final String HS_NOTIFICATION_SERVICE_DESTROY="hs.keyboard.SERVICE_DESTROY";
    public static final String HS_NOTIFICATION_SERVICE_START_INPUT_VIEW="hs.keyboard.SERVICE_START_INPUT_VIEW";
    public static final String HS_NOTIFICATION_PARAM_EDITOR_OWNER_PACKAGE_NAME = "editor_owner_package_name";
    private static InputConnection insideConnection=null;
    private volatile boolean isOnDestroy=false;

    private  INotificationObserver keyboardNotificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            if (eventName.equals(Constants.HS_NOTIFICATION_START_INPUT_INSIDE)) {
                CustomSearchEditText customSearchEditText = (CustomSearchEditText)notificaiton.getObject(Constants.HS_NOTIFICATION_CUSTOM_SEARCH_EDIT_TEXT);
                onStartInputInside(customSearchEditText);
            } else if (eventName.equals(Constants.HS_NOTIFICATION_FINISH_INPUT_INSIDE)) {
                onFinishInputInside();
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();


        Fresco.initialize(this);
        AsyncThreadPools.execute(new Runnable() {
            @Override
            public void run() {
                DataManager.getInstance().loadUserData();
            }
        });
        isOnDestroy=false;
        KeyboardPluginManager.getInstance().onInputMethodServiceCreate();
        setPanelSwitcher(KeyboardPluginManager.getInstance());
        HSKeyboardSwitcher.init(HSInputMethod.getInputService());
        HSLog.d("time log, input service on create finished");
    }


    @Override
    public void onFinishInputView(final boolean finishingInput) {
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
    }

    @Override
    public void loadKeyboard() {
        LanguageDao.updateCurrentLanguage();
        super.loadKeyboard();
    }
}
