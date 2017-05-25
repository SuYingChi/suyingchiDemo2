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

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
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
import com.ihs.inputmethod.websearch.WebContentSearchManager;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.utils.KCFeatureRestrictionConfig;

import java.util.Calendar;
import java.util.List;

/**
 * Created by xu.zhang on 11/3/15.
 */
public abstract class HSUIInputMethodService extends HSInputMethodService {
    public static final String HS_NOTIFICATION_SERVICE_DESTROY = "hs.keyboard.SERVICE_DESTROY";
    public static final String HS_NOTIFICATION_PARAM_EDITOR_OWNER_PACKAGE_NAME = "editor_owner_package_name";
    public static final String ACTION_CLOSE_SYSTEM_DIALOGS = "android.intent.action.CLOSE_SYSTEM_DIALOGS";
    private static final String GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending";


    private static InputConnection insideConnection = null;
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
            if (Intent.ACTION_DATE_CHANGED.equals(action)) {
                KeyboardFullScreenAdSession.resetKeyboardFullScreenAdSessionIndex();
                KeyboardFullScreenAd.resetKeyboardFullScreenAdSessions();
            }
        }
    };
    private boolean isInputViewShowing = false;
    private String currentAppPackageName;
    private KeyboardFullScreenAd openFullScreenAd;
    private KeyboardFullScreenAd closeFullScreenAd;
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

    private static KeyboardPanelManager getKeyboardPanelMananger() {
        return (KeyboardPanelManager) keyboardPanelSwitcher;
    }

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
        closeFullScreenAd = new KeyboardFullScreenAd(getResources().getString(R.string.placement_full_screen_open_keyboard), "Close");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (isInputViewShowing) {
                getKeyboardPanelMananger().onBackPressed();
                if (!isInOwnApp()) {
                    if (!KCFeatureRestrictionConfig.isFeatureRestricted("AdKeyboardClose")) {
                        closeFullScreenAd.show();
                    }
                }
                HSAnalytics.logGoogleAnalyticsEvent("app", "Trigger", "Spring_Trigger", "keyboard", null, null, null);
            } else {
                showBackAdIfNeeded();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private boolean showBackAdIfNeeded() {
        boolean enabled = HSConfig.optBoolean(false, "Application", "InterstitialAds", "BackButton", "Show");

        if (!enabled) {
            return false;
        }

        if (KCFeatureRestrictionConfig.isFeatureRestricted("AdBackButton")) {
            return false;
        }

        if (isInRightAppForBackAd()) {
            HSAnalytics.logGoogleAnalyticsEvent("app", "Trigger", "Spring_Trigger", "normal", null, null, null);
            HSPreferenceHelper prefs = HSPreferenceHelper.create(this, "BackAd");

            long lastBackTimeMillis = prefs.getLong("LastBackTime", 0);
            int backCount = prefs.getInt("BackCount", 0);
            long currentBackTimeMillis = System.currentTimeMillis();

            Calendar lastBackCalendar = Calendar.getInstance();
            lastBackCalendar.setTimeInMillis(lastBackTimeMillis);

            Calendar currentBackCalendar = Calendar.getInstance();
            currentBackCalendar.setTimeInMillis(currentBackTimeMillis);

            int hitBackCount = prefs.getInt("HitBackIndex", -1);

            if (currentBackCalendar.get(Calendar.YEAR) == lastBackCalendar.get(Calendar.YEAR) &&
                    currentBackCalendar.get(Calendar.DAY_OF_YEAR) == lastBackCalendar.get(Calendar.DAY_OF_YEAR)) {
                backCount++;
            } else {
                backCount = 1;
                hitBackCount = -1;
                prefs.putInt("HitBackCount", hitBackCount);
            }
            prefs.putLong("LastBackTime", currentBackTimeMillis);
            prefs.putInt("BackCount", backCount);

            List<Integer> backCountList = (List<Integer>) HSConfig.getList("Application", "InterstitialAds", "BackButton", "BackCountOfDay");
            for (int targetBackCount : backCountList) {
                if (backCount >= targetBackCount && hitBackCount < targetBackCount) {
                    boolean adShown = KCInterstitialAd.show(getString(R.string.placement_full_screen_open_keyboard), null, true);
                    if (adShown) {
                        hitBackCount = backCount;
                        prefs.putInt("HitBackIndex", hitBackCount);
                        return true;
                    } else {
                        KCInterstitialAd.load(getString(R.string.placement_full_screen_open_keyboard));
                    }
                    break;
                }
            }
            return false;
        } else {
            HSAnalytics.logGoogleAnalyticsEvent("app", "Trigger", "Spring_Trigger", "restricted", null, null, null);
            return false;
        }
    }

    private boolean isInRightAppForBackAd() {
        if (isInOwnApp() || isInputViewShowing) {
            return false;
        }

        if (currentAppPackageName == null) {
            return false;
        }

        List<String> packageNameBlackList = (List<String>) HSConfig.getList("Application", "InterstitialAds", "BackButton", "PackageNameExclude");
        if (packageNameBlackList == null) {
            return true;
        }

        for (String blockedPackageName : packageNameBlackList) {
            if (currentAppPackageName.contains(blockedPackageName)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInOwnApp() {
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        if (editorInfo == null) {
            return false;
        } else if (TextUtils.equals(currentAppPackageName, getPackageName())) {
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
            if (!isInOwnApp()) {
                if (!KCFeatureRestrictionConfig.isFeatureRestricted("AdKeyboardOpen")) {
                    openFullScreenAd.show();
                    openFullScreenAd.preLoad();
                }

                if (!KCFeatureRestrictionConfig.isFeatureRestricted("AdKeyboardClose")) {
                    closeFullScreenAd.preLoad();
                }
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
    public void showWindow(boolean showInput) {
        super.showWindow(showInput);
        if (currentAppPackageName.equals(GOOGLE_PLAY_PACKAGE_NAME)){
            getKeyboardPanelMananger().logCustomizeBarShowed();
        }
    }

    @Override
    public void onStartInput(EditorInfo editorInfo, boolean restarting) {
        super.onStartInput(editorInfo, restarting);
        if (restarting) {
            getKeyboardPanelMananger().resetKeyboardBarState();
        }

        // 这里单独记了packageName，而没有通过getCurrentInputEditorInfo()方法
        // 因为这个方法在键盘出来后，一直返回的是键盘曾经出现过的那个App，而这里的editorInfo则对应实际进入的App
        currentAppPackageName = editorInfo.packageName;
        if (currentAppPackageName.equals(GOOGLE_PLAY_PACKAGE_NAME)) {
            getKeyboardPanelMananger().showCustomizeBar();
        }else{
            getKeyboardPanelMananger().removeCustomizeBar();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
