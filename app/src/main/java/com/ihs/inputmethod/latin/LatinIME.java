package com.ihs.inputmethod.latin;

import android.content.res.Configuration;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.framework.HSKeyboardSwitcher;
import com.ihs.inputmethod.uimodules.KeyboardPluginManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.DataManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base.LanguageDao;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.emojisearch.ESManager;
import com.keyboard.rainbow.thread.AsyncThreadPools;

/**
 * Created by xu.zhang on 11/3/15.
 */
public class LatinIME extends HSUIInputMethodService {
    private volatile boolean isOnDestroy=false;

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
