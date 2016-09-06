package com.ihs.inputmethod.latin;

import android.content.res.Configuration;
import android.util.Log;

import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.uimodules.KeyboardPluginManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base.LanguageDao;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.emojisearch.ESManager;

/**
 * Created by xu.zhang on 11/3/15.
 */
public class LatinIME extends HSUIInputMethodService {
    private volatile boolean isOnDestroy=false;

    @Override
    public void onCreate() {
        Log.e("time log","time log service oncreated started");
        super.onCreate();
//        Fresco.initialize(this);
//        AsyncThreadPools.execute(new Runnable() {
//            @Override
//            public void run() {
//                DataManager.getInstance().loadUserData();
//            }
//        });
        isOnDestroy=false;
        KeyboardPluginManager.getInstance().onInputMethodServiceCreate();
        setPanelSwitcher(KeyboardPluginManager.getInstance());
        //HSKeyboardSwitcher.init(HSInputMethod.getInputService());
        Log.e("time log","time log service oncreated started");
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
