package com.ihs.inputmethod.api;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAdSession;
import com.ihs.inputmethod.analytics.KeyboardAnalyticsReporter;
import com.ihs.inputmethod.api.framework.HSEmojiSuggestionManager;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.suggestions.CustomSearchEditText;
import com.ihs.inputmethod.uimodules.KeyboardPanelManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.Constants;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base.LanguageDao;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.websearch.WebContentSearchManager;

/**
 * Created by xu.zhang on 11/3/15.
 */
public abstract class HSUIInputMethodService extends HSInputMethodService {
    public static final String HS_NOTIFICATION_SERVICE_DESTROY = "hs.keyboard.SERVICE_DESTROY";
    public static final String HS_NOTIFICATION_SERVICE_START_INPUT_VIEW = "hs.keyboard.SERVICE_START_INPUT_VIEW";
    public static final String HS_NOTIFICATION_PARAM_EDITOR_OWNER_PACKAGE_NAME = "editor_owner_package_name";
    public static final String ACTION_CLOSE_SYSTEM_DIALOGS = "android.intent.action.CLOSE_SYSTEM_DIALOGS";

    private static InputConnection insideConnection = null;

    private boolean isInputViewShowing = false;

    private static KeyboardPanelManager getKeyboardPanelMananger() {
        return (KeyboardPanelManager) keyboardPanelSwitcher;
    }

    private INotificationObserver keyboardNotificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            if (eventName.equals(HSInputMethod.HS_NOTIFICATION_START_INPUT_INSIDE)) {
                CustomSearchEditText customSearchEditText = (CustomSearchEditText) notificaiton.getObject(Constants.HS_NOTIFICATION_CUSTOM_SEARCH_EDIT_TEXT);
                onStartInputInside(customSearchEditText);
            } else if (eventName.equals(HSInputMethod.HS_NOTIFICATION_FINISH_INPUT_INSIDE)) {
                onFinishInputInside();
            } else if (eventName.equals(Constants.HS_NOTIFICATION_RESET_EDIT_INFO)) {
                resetEditInfo();
            }
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                if (reason != null && reason.equals("homekey")) {
                    getKeyboardPanelMananger().onHomePressed();
                }
            }
        }
    };

    private final BroadcastReceiver dateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Intent.ACTION_DATE_CHANGED.equals(action)) {
                KeyboardFullScreenAdSession.resetKeyboardFullScreenAdSessionIndex();
                KeyboardFullScreenAd.resetKeyboardFullScreenAdSessions();
            }
        }
    };

    private KeyboardFullScreenAd openFullScreenAd;
    private KeyboardFullScreenAd closeFullScreenAd;


    @Override
    public void onCreate() {
        KeyboardAnalyticsReporter.getInstance().recordKeyboardOnCreateStart();
        super.onCreate();
        KeyboardFullScreenAdSession.getInstance();
        registerReceiver(this.dateReceiver, new IntentFilter(Intent.ACTION_DATE_CHANGED));
        registerReceiver(this.receiver, new IntentFilter(ACTION_CLOSE_SYSTEM_DIALOGS));

        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_START_INPUT_INSIDE, keyboardNotificationObserver);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_FINISH_INPUT_INSIDE, keyboardNotificationObserver);
        HSGlobalNotificationCenter.addObserver(Constants.HS_NOTIFICATION_RESET_EDIT_INFO, keyboardNotificationObserver);

        KeyboardAnalyticsReporter.getInstance().recordKeyboardOnCreateEnd();
        openFullScreenAd = new KeyboardFullScreenAd(getResources().getString(R.string.placement_full_screen_open_keyboard), "Open");
        closeFullScreenAd = new KeyboardFullScreenAd(getResources().getString(R.string.placement_full_screen_close_keyboard), "Close");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isInputViewShowing) {
            getKeyboardPanelMananger().onBackPressed();
            if (!isKeyboardInItsOwnApp()) {
                closeFullScreenAd.show();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isKeyboardInItsOwnApp() {
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        if (editorInfo == null) {
            return false;
        } else if (TextUtils.equals(editorInfo.packageName, getPackageName())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public View onCreateInputView() {
        KeyboardAnalyticsReporter.getInstance().recordKeyboardStartTime("CreateAndStartInputView");
        return super.onCreateInputView();
    }

    public void onStartInputInside(CustomSearchEditText editText) {

        /**
         * 标志调用该方法时键盘不是真正的收起。
         */
        super.onFinishInputView(true);
        EditorInfo editorInfo = editText.getEditorInfo();
        insideConnection = editText.onCreateInputConnection(editorInfo);
        HSSpecialCharacterManager.onConnectInnerEditor();
        /**
         * 设置编辑框类型为自定义搜索类型，从而启动对应的键盘布局。
         */
        HSInputMethod.setCustomSearchInputType(editorInfo);
        onStartInputView(editorInfo, true);
    }

    public void onFinishInputInside() {
        if (insideConnection != null) {
            onFinishInput();
            cleanupInternalStateForInsideEditText();
            resetEditInfo();
        }

    }

    private void resetEditInfo() {
        /**
         * Fix - Suggestions failed when we quit from inner editor.
         * Root - Not update input attributes in settings for old editor.
         * Resolution - Let settings update input attributes for old editor.
         */
        onStartInputView(getCurrentInputEditorInfo(), false);
    }

    @Override
    public InputConnection getCurrentInputConnection() {
        if (insideConnection != null) {
            return insideConnection;
        }
        return super.getCurrentInputConnection();
    }

    @Override
    public void onFinishInput() {
        if (insideConnection != null) {
            insideConnection = null;
        }
        super.onFinishInput();
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        isInputViewShowing = true;
        Log.e("time log", "time log service onstartInputView started");
        KeyboardAnalyticsReporter.getInstance().recordKeyboardStartTime("StartInputView");
        super.onStartInputView(editorInfo, restarting);
        getKeyboardPanelMananger().beforeStartInputView();

        if (insideConnection == null && restarting) {
            getKeyboardPanelMananger().showKeyboardWithMenu();
        }
//        // Broadcast event
//        final HSBundle bundle = new HSBundle();
//        if (editorInfo != null) {
//            bundle.putString(HS_NOTIFICATION_PARAM_EDITOR_OWNER_PACKAGE_NAME, editorInfo.packageName);
//        }

//        HSGlobalNotificationCenter.sendNotification(HS_NOTIFICATION_SERVICE_START_INPUT_VIEW, bundle);
        Log.e("time log", "time log service onstartInputView finished");

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ApkUtils.checkAndShowUpdateAlert();
            }
        }, 1000);

        KeyboardAnalyticsReporter.getInstance().onKeyboardSessionStart();
        KeyboardAnalyticsReporter.getInstance().recordKeyboardEndTime();

        if (!restarting) {
            if (!isKeyboardInItsOwnApp()) {
                openFullScreenAd.show();

                openFullScreenAd.preLoad();
                closeFullScreenAd.preLoad();
            }
        }
    }

    @Override
    public void onFinishInputView(final boolean finishingInput) {
        isInputViewShowing = false;
        if (WebContentSearchManager.ControlStripState.PANEL_CONTROL != WebContentSearchManager.stripState) {
            HSGlobalNotificationCenter.sendNotificationOnMainThread(WebContentSearchManager.SHOW_CONTROL_PANEL_STRIP_VIEW);
        }

        if (insideConnection != null) {
            insideConnection = null;
        }
        HSEmojiSuggestionManager.cleanupFollowEmojiForTypedWords();

        KeyboardAnalyticsReporter.getInstance().onKeyboardSessionEnd();
        super.onFinishInputView(finishingInput);
    }

    @Override
    public void onDestroy() {
        WebContentSearchManager.getInstance().storeHistory();

        unregisterReceiver(this.receiver);
        unregisterReceiver(this.dateReceiver);
        super.onDestroy();
        getKeyboardPanelMananger().onInputViewDestroy();
        HSGlobalNotificationCenter.sendNotification(HS_NOTIFICATION_SERVICE_DESTROY);
    }

    @Override
    public void loadKeyboard() {
        LanguageDao.updateCurrentLanguage();
        super.loadKeyboard();
    }

    @Override
    public void setInputView(View view) {
        super.setInputView(view);

        WebContentSearchManager.stripState = WebContentSearchManager.ControlStripState.PANEL_CONTROL;
    }

    @Override
    public void hideWindow() {
        super.hideWindow();
        getKeyboardPanelMananger().resetKeyboardBarState();
        HSLog.e("keyboard lifecycle ----hide window----");
    }

    @Override
    public void onStartInput(EditorInfo editorInfo, boolean restarting) {
        super.onStartInput(editorInfo, restarting);
        if(restarting){
            getKeyboardPanelMananger().resetKeyboardBarState();
        }
    }
}
