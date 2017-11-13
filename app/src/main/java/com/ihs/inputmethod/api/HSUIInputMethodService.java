package com.ihs.inputmethod.api;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.adpanel.KeyboardGooglePlayAdManager;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.analytics.KeyboardAnalyticsReporter;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSEmojiSuggestionManager;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.suggestions.CustomSearchEditText;
import com.ihs.inputmethod.uimodules.KeyboardPanelManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base.LanguageDao;
import com.ihs.inputmethod.websearch.WebContentSearchManager;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.utils.KCFeatureRestrictionConfig;

import java.util.List;

/**
 * Created by xu.zhang on 11/3/15.
 */
public abstract class HSUIInputMethodService extends HSInputMethodService {
    public static final String ACTION_CLOSE_SYSTEM_DIALOGS = "android.intent.action.CLOSE_SYSTEM_DIALOGS";
    private static final String GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending";
    private static final String HS_SHOW_KEYBOARD_WINDOW = "hs.inputmethod.framework.api.SHOW_INPUTMETHOD";
    public static final String HS_NOTIFICATION_START_INPUT_INSIDE_CUSTOM_SEARCH_EDIT_TEXT = "CustomSearchEditText";

    private InputConnection insideConnection = null;
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
    private boolean isInputViewShowing = false;
    private String currentAppPackageName = "";
    private KeyboardFullScreenAd openFullScreenAd;
    private KeyboardFullScreenAd closeFullScreenAd;
    private KeyboardGooglePlayAdManager keyboardGooglePlayAdManager;
    private INotificationObserver keyboardNotificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            if (eventName.equals(HSInputMethod.HS_NOTIFICATION_START_INPUT_INSIDE)) {
                CustomSearchEditText customSearchEditText = (CustomSearchEditText) notificaiton.getObject(HS_NOTIFICATION_START_INPUT_INSIDE_CUSTOM_SEARCH_EDIT_TEXT);
                onStartInputInside(customSearchEditText);
            } else if (eventName.equals(HSInputMethod.HS_NOTIFICATION_FINISH_INPUT_INSIDE)) {
                onFinishInputInside();
            } else if (eventName.equals(HS_SHOW_KEYBOARD_WINDOW)) {
                if (inPlayStore()) {
                    getKeyboardPanelMananger().logCustomizeBarShowed();
                }
            }
        }
    };

    public static KeyboardPanelManager getKeyboardPanelMananger() {
        return (KeyboardPanelManager) keyboardPanelSwitcher;
    }

    @Override
    public void onCreate() {
        KeyboardAnalyticsReporter.getInstance().recordKeyboardOnCreateStart();
        super.onCreate();
        registerReceiver(this.receiver, new IntentFilter(ACTION_CLOSE_SYSTEM_DIALOGS));

        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_START_INPUT_INSIDE, keyboardNotificationObserver);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_FINISH_INPUT_INSIDE, keyboardNotificationObserver);
        HSGlobalNotificationCenter.addObserver(HS_SHOW_KEYBOARD_WINDOW, keyboardNotificationObserver);

        KeyboardAnalyticsReporter.getInstance().recordKeyboardOnCreateEnd();
        openFullScreenAd = new KeyboardFullScreenAd(getResources().getString(R.string.placement_full_screen_open_keyboard), "Open");
        closeFullScreenAd = new KeyboardFullScreenAd(getResources().getString(R.string.placement_full_screen_open_keyboard), "Close");
        keyboardGooglePlayAdManager = new KeyboardGooglePlayAdManager(HSApplication.getContext().getString(R.string.ad_placement_google_play_dialog_ad));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (isInputViewShowing) {
                getKeyboardPanelMananger().onBackPressed();
                if (!isInOwnApp()) {
                    if (!KCFeatureRestrictionConfig.isFeatureRestricted("AdKeyboardClose") && !RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                        closeFullScreenAd.show();
                    }
                }
                HSAnalytics.logGoogleAnalyticsEvent("app", "Trigger", "Spring_Trigger", "keyboard", null, null, null);
            } else {
                if (isInRightAppForBackAd()) {
                    HSAnalytics.logGoogleAnalyticsEvent("app", "Trigger", "Spring_Trigger", "normal", null, null, null);
                } else {
                    HSAnalytics.logGoogleAnalyticsEvent("app", "Trigger", "Spring_Trigger", "restricted", null, null, null);
                }

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

        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return false;
        }

        if (KCFeatureRestrictionConfig.isFeatureRestricted("AdBackButton")) {
            return false;
        }

        if (isInRightAppForBackAd()) {
            HSPreferenceHelper prefs = HSPreferenceHelper.create(this, "BackAd");

            long lastBackAdShowTimeMillis = prefs.getLong("LastBackAdShowTime", 0);
            long currentTimeMillis = System.currentTimeMillis();
            long backAdShowCountOfDay = prefs.getLong("BackAdShowCountOfDay", 0);
            if (!DateUtils.isToday(lastBackAdShowTimeMillis)) {
                backAdShowCountOfDay = 0;
                prefs.putLong("BackAdShowCountOfDay", 0);
            }

            float minIntervalByHour = HSConfig.optFloat(0, "Application", "InterstitialAds", "BackButton", "MinIntervalByHour");
            int maxCountPerDay = HSConfig.optInteger(0, "Application", "InterstitialAds", "BackButton", "MaxCountPerDay");

            if (currentTimeMillis - lastBackAdShowTimeMillis >= minIntervalByHour * 3600 * 1000 && backAdShowCountOfDay < maxCountPerDay) {
                boolean adShown = KCInterstitialAd.show(getString(R.string.placement_full_screen_open_keyboard), null, true);
                if (adShown) {
                    backAdShowCountOfDay++;
                    prefs.putLong("BackAdShowCountOfDay", backAdShowCountOfDay);
                    prefs.putLong("LastBackAdShowTime", currentTimeMillis);
                    return true;
                } else {
                    KCInterstitialAd.load(getString(R.string.placement_full_screen_open_keyboard));
                    return false;
                }
            } else {
                return false;
            }
        } else {
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
        // TODO: // How to judge current keyboard id & restart from text?
        boolean isFromText = editorInfo != null && (editorInfo.inputType & InputType.TYPE_CLASS_TEXT) > 0
                && (editorInfo.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) > 0
                && (editorInfo.inputType & InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) > 0;
        if (restarting && isFromText) {
            getKeyboardPanelMananger().showFunctionBarAd();
        }

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
                if (!KCFeatureRestrictionConfig.isFeatureRestricted("AdKeyboardOpen") && !RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                    openFullScreenAd.show();
                    openFullScreenAd.preLoad();
                }

                if (!KCFeatureRestrictionConfig.isFeatureRestricted("AdKeyboardClose") && !RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
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
        super.onDestroy();
        getKeyboardPanelMananger().onInputViewDestroy();
        HSGlobalNotificationCenter.removeObserver(keyboardNotificationObserver);
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
    }

    @Override
    public void onStartInput(EditorInfo editorInfo, boolean restarting) {
        super.onStartInput(editorInfo, restarting);
        if (restarting) {
            getKeyboardPanelMananger().resetKeyboardBarState();
        }

        if (editorInfo.packageName.equals(GOOGLE_PLAY_PACKAGE_NAME)
                && !currentAppPackageName.equals(this.getPackageName())
                && !currentAppPackageName.equals(GOOGLE_PLAY_PACKAGE_NAME)) { // 进入Google Play
            keyboardGooglePlayAdManager.loadAndShowAdIfConditionSatisfied();
        } else if (currentAppPackageName.equals(GOOGLE_PLAY_PACKAGE_NAME)
                && !editorInfo.packageName.equals(this.getPackageName())
                && !editorInfo.packageName.equals(GOOGLE_PLAY_PACKAGE_NAME)) { // 离开Google Play
            keyboardGooglePlayAdManager.cancel();
        }
        // 这里单独记了packageName，而没有通过getCurrentInputEditorInfo()方法
        // 因为这个方法在键盘出来后，一直返回的是键盘曾经出现过的那个App，而这里的editorInfo则对应实际进入的App
        currentAppPackageName = editorInfo.packageName;
    }

    @Override
    public void onKeyboardWindowShow() {
        super.onKeyboardWindowShow();
        if (!inPlayStore()) {
            getKeyboardPanelMananger().showBannerAdBar();
        }else {
            getKeyboardPanelMananger().showGoogleAdBar();
        }
    }

    @Override
    public void onKeyboardWindowHide() {
        HSFloatWindowManager.getInstance().removeGameTipView();
        if(!inPlayStore()){
            getKeyboardPanelMananger().removeCustomizeBar();
        }
    }

    private boolean inPlayStore() {
        return TextUtils.equals(currentAppPackageName, GOOGLE_PLAY_PACKAGE_NAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCodeInput(int codePoint, int x, int y, boolean isKeyRepeat) {
        try {
            if (codePoint == '\n' && currentAppPackageName.equals(GOOGLE_PLAY_PACKAGE_NAME)) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.getCurrentInputConnection().getTextBeforeCursor(100, 0));
                sb.append(this.getCurrentInputConnection().getTextAfterCursor(100, 0));
                String text = sb.toString();
                HSLog.d("Key enter pressed in google play.");
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_googleplay_search_content", text);
            }
        } catch (Exception e) {
            HSLog.d("Failed to log key enter in google play.");
        }

        super.onCodeInput(codePoint, x, y, isKeyRepeat);
    }
}
