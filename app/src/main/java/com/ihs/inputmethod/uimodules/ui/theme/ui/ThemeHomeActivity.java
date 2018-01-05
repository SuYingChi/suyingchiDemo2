package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Browser;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.CPSettings;
import com.artw.lockscreen.LockerEnableDialog;
import com.artw.lockscreen.LockerSettings;
import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.app.framework.inner.HomeKeyTracker;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.theme.ThemeLockerBgUtil;
import com.ihs.inputmethod.uimodules.BuildConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.BaseCustomizeActivity;
import com.ihs.inputmethod.uimodules.ui.customize.util.BottomNavigationViewHelper;
import com.ihs.inputmethod.uimodules.ui.customize.view.CustomizeContentView;
import com.ihs.inputmethod.uimodules.ui.customize.view.LayoutWrapper;
import com.ihs.inputmethod.uimodules.ui.settings.activities.SettingsActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.widget.CustomDesignAlert;
import com.ihs.inputmethod.uimodules.widget.LockerGuideAlert;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.ihs.inputmethod.utils.CallAssistantConfigUtils;
import com.ihs.inputmethod.utils.ScreenLockerConfigUtils;
import com.ihs.keyboardutils.alerts.KCAlert;
import com.ihs.keyboardutils.permission.PermissionFloatWindow;
import com.ihs.keyboardutils.permission.PermissionTip;
import com.ihs.keyboardutils.permission.PermissionUtils;
import com.ihs.keyboardutils.utils.AlertShowingUtils;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.kc.commons.utils.KCCommonUtils;
import com.keyboard.common.KeyboardActivationGuideActivity;
import com.keyboard.common.SplashActivity;

import net.appcloudbox.ads.interstitialads.AcbInterstitialAdLoader;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jixiang on 16/8/17.
 */
public class ThemeHomeActivity extends BaseCustomizeActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener,
        SettingsActivity.GeneralHomePreferenceFragment.OnUpdateClickListener {
    public final static String EXTRA_SHOW_TRIAL_KEYBOARD = "EXTRA_SHOW_TRIAL_KEYBOARD";
    public final static String EXTRA_SHOW_AD_ON_TRIAL_KEYBOARD_DISMISS = "EXTRA_SHOW_AD_ON_TRIAL_KEYBOARD_DISMISS";
    public final static String EXTRA_AUTO_ENABLE_KEYBOARD = "EXTRA_AUTO_ENABLE_KEYBOARD";

    private static final String SP_LAST_USAGE_ALERT_SESSION_ID = "SP_LAST_USAGE_ALERT_SESSION_ID";
    private static final String SP_TREBLE_FUNCTION_ALERT_SHOWED = "sp_treble_function_alert_showed";
    public static final String BUNDLE_KEY_HOME_MAIN_PAGE_TAB = "home_main_page_tab";
    public static final String BUNDLE_KEY_HOME_INNER_PAGE_TAB = "home_inner_page_tab";

    private static final int KEYBOARD_ACTIVATION_FROM_HOME_ENTRY = 1;
    private static final int KEYBOARD_ACTIVATION_FROM_ENABLE_TIP = 2;
    private static final int REQUEST_CODE_KEYBOARD_ACTIVATION_FROM_CUSTOM_THEME = 3;
    private static final int REQUEST_CODE_START_CUSTOM_THEME = 4;

    private static final int LOAD_FULLSCREEN_AD_TIME = 5000;
    private static int HANDLER_SHOW_ACTIVE_DIALOG = 101;
    private static int HANDLER_SHOW_UPDATE_DIALOG = 102;
    private static int HANDLER_DISMISS_LOADING_FULLSCREEN_AD_DIALOG = 103;

    // customize
    private static final List<Integer> ITEMS_INDEX_RESOURCE_ID_LIST = new ArrayList<>();

    public static final int TAB_INDEX_KEYBOARD = 0;

    private int currentTabIndex = 0;

    static {
        ITEMS_INDEX_RESOURCE_ID_LIST.add(R.id.customize_bottom_bar_keyboard);
        ITEMS_INDEX_RESOURCE_ID_LIST.add(R.id.customize_bottom_bar_wallpapers);
        ITEMS_INDEX_RESOURCE_ID_LIST.add(R.id.customize_bottom_bar_call);
        ITEMS_INDEX_RESOURCE_ID_LIST.add(R.id.customize_bottom_bar_setting);
    }

    private CustomizeContentView mContent;
    private BottomNavigationView mBottomBar;

    private LayoutWrapper mLayoutWrapper;

    private int mViewIndex;
    public int mWallpaperTabIndex;

    private TrialKeyboardDialog trialKeyboardDialog;
    private boolean isFromUsageAccessActivity;
    private View enableTipTV;
    private ThemeHomeActivity context = ThemeHomeActivity.this;
    private View apkUpdateTip;
    private boolean isResumeOnCreate = true;

    private AcbInterstitialAdLoader acbInterstitialAdLoader;
    private AlertDialog fullscreenAdLoadingDialog;
    private boolean fullscreenShowed = false;

    private int splashJumpCode = -1;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_SHOW_ACTIVE_DIALOG) {
                if (!HSInputMethodListManager.isMyInputMethodSelected()) {
                    Intent intent = new Intent(ThemeHomeActivity.this, KeyboardActivationGuideActivity.class);
                    intent.putExtra(KeyboardActivationGuideActivity.EXTRA_ACTIVATION_PROMPT_MESSAGE, getString(R.string.dialog_msg_enable_keyboard_home_rain, getString(R.string.app_name)));
                    startActivityForResult(intent, KEYBOARD_ACTIVATION_FROM_HOME_ENTRY);
                }
            } else if (msg.what == HANDLER_SHOW_UPDATE_DIALOG) {
                checkAndShowApkUpdateAlert(false);
            } else if (msg.what == HANDLER_DISMISS_LOADING_FULLSCREEN_AD_DIALOG) {
                if (!fullscreenShowed) {
                    if (acbInterstitialAdLoader != null) {
                        acbInterstitialAdLoader.cancel();
                        acbInterstitialAdLoader = null;
                    }
                    if (fullscreenAdLoadingDialog != null) {
                        KCCommonUtils.dismissDialog(fullscreenAdLoadingDialog);
                        fullscreenAdLoadingDialog = null;
                    }
                    Toast.makeText(ThemeHomeActivity.this, R.string.locker_wallpaper_network_error, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_INPUT_METHOD_CHANGED)) {
                boolean isKeyboardSelected = HSInputMethodListManager.isMyInputMethodSelected();
                enableTipTV.setVisibility(isKeyboardSelected ? View.GONE : View.VISIBLE);
            }
        }
    };

    public static void bindScrollListener(Context context, RecyclerView recyclerView, boolean hasBottom) {
        if (context instanceof ThemeHomeActivity) {
            ((ThemeHomeActivity) context).getLayoutWrapper().attachToRecyclerView(recyclerView, hasBottom);
        }
    }

    public LayoutWrapper getLayoutWrapper() {
        return mLayoutWrapper;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        mContent.onServiceConnected(mService);
        mBottomBar.setOnNavigationItemSelectedListener(ThemeHomeActivity.this);

        if (mHasPendingTheme) {
//            setSystemTheme();
        }
    }

    public void hideBottomBar() {
        mBottomBar.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("tabIndex", currentTabIndex);
        savedInstanceState.putInt("wallpaperIndex", mWallpaperTabIndex);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        currentTabIndex = bundle.getInt("tabIndex");
        mWallpaperTabIndex = bundle.getInt("wallpaperIndex");
    }

    private HomeKeyTracker homeKeyTracker = new HomeKeyTracker(HSApplication.getContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(0, 0);

        setContentView(R.layout.activity_theme_home);
        getWindow().setBackgroundDrawable(null);

        mContent = ViewUtils.findViewById(this, R.id.content_layout);

        mBottomBar = ViewUtils.findViewById(this, R.id.bottom_bar);
        BottomNavigationViewHelper.disableShiftMode(mBottomBar);
        mLayoutWrapper = new LayoutWrapper(mBottomBar, getResources().getDimensionPixelSize(R.dimen.bottom_bar_default_height), CommonUtils.pxFromDp(3.3f));

        enableTipTV = findViewById(R.id.tv_enable_keyboard);
        ((TextView) enableTipTV).setText(getString(R.string.tv_enable_keyboard_tip, getString(R.string.app_name)));
        enableTipTV.setVisibility(HSInputMethodListManager.isMyInputMethodSelected() ? View.GONE : View.VISIBLE);
        enableTipTV.setOnClickListener(v -> {
            Intent intent = new Intent(ThemeHomeActivity.this, KeyboardActivationGuideActivity.class);
            startActivityForResult(intent, KEYBOARD_ACTIVATION_FROM_ENABLE_TIP);
        });

        apkUpdateTip = findViewById(R.id.apk_update_tip);

        //界面被启动 请求 扫描权限
        if (HSConfig.optBoolean(false, "Application", "AccessUsageAlert", "enable") && !PermissionUtils.isUsageAccessGranted() && shouldShowUsageAccessAlert()) {

            HSPreferenceHelper.getDefault().putInt(SP_LAST_USAGE_ALERT_SESSION_ID, HSSessionMgr.getCurrentSessionId());
            new KCAlert.Builder(this)
                    .setTitle(getString(R.string.dialog_app_usage_title))
                    .setMessage(getString(R.string.dialog_app_usage_tips))
                    .setTopImageResource(R.drawable.enable_keyboard_alert_top_bg)
                    .setPositiveButton(getString(R.string.dialog_agree).toUpperCase(), view -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !PermissionUtils.isUsageAccessGranted()) {
                            isFromUsageAccessActivity = true;
                        }
                        enableUsageAccessPermission();
                    })
                    .setNegativeButton(getString(R.string.dialog_disagree).toUpperCase(), null)
                    .show();
        }

        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_INPUT_METHOD_CHANGED));

        //如果是第一次进入页面并且当前键盘没有被选为自己则弹框。
        if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            handler.sendEmptyMessageDelayed(HANDLER_SHOW_ACTIVE_DIALOG, 500);
        } else {
            handler.sendEmptyMessageDelayed(HANDLER_SHOW_UPDATE_DIALOG, 500);
        }

        splashJumpCode = getIntent().getIntExtra(SplashActivity.JUMP_TAG, -1);
        onNewIntent(getIntent());
        if (splashJumpCode == -1) {
            KeyboardFullScreenAd.showSessionOneTimeAd("appOpened");
        }
    }

    private void enableUsageAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !PermissionUtils.isUsageAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            HSApplication.getContext().startActivity(intent);
            PermissionFloatWindow.getInstance().createPermissionTip(PermissionTip.TYPE_TEXT_USAGE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean showTrial = intent.getBooleanExtra(EXTRA_SHOW_TRIAL_KEYBOARD, false);
        if (showTrial) {
            handler.removeMessages(HANDLER_SHOW_ACTIVE_DIALOG);
            showTrialKeyboardDialog();
            getIntent().putExtra(EXTRA_SHOW_TRIAL_KEYBOARD, false);
        } else {
            String from = intent.getStringExtra("From");
            if (trialKeyboardDialog != null && trialKeyboardDialog.isShowing() && from != null && from.equals("Keyboard")) {
                Toast.makeText(this, "Already in " + getResources().getString(R.string.default_themes, getResources().getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
            }
        }

        //mark bug
        if (mLayoutWrapper != null) {
            mLayoutWrapper.show();
        }

        int innerTabIndex = 0;
        // keyboard中点击sticker panel加号，设定viewpager当前页为sticker home页
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            if (bundle.containsKey(BUNDLE_KEY_HOME_MAIN_PAGE_TAB)) {
                currentTabIndex = bundle.getInt(BUNDLE_KEY_HOME_MAIN_PAGE_TAB);
            }
            if (bundle.containsKey(BUNDLE_KEY_HOME_INNER_PAGE_TAB)) {
                innerTabIndex = bundle.getInt(BUNDLE_KEY_HOME_INNER_PAGE_TAB);
            }

        }
        mBottomBar.setSelectedItemId(ITEMS_INDEX_RESOURCE_ID_LIST.get(currentTabIndex));
        mContent.setWithChildTabSelected(currentTabIndex, innerTabIndex);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        newConfig.orientation = Configuration.ORIENTATION_PORTRAIT;
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        handler.postDelayed(() -> HSFloatWindowManager.getInstance().removeAccessibilityCover(), 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (splashJumpCode != -1) {
            Intent intent = new Intent();
            switch (splashJumpCode) {
                case SplashActivity.JUMP_TO_FACEMOJI_CAMERA:
                    try {
                        intent.setClass(this, Class.forName("com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity"));
                    } catch (ClassNotFoundException e) {
                        HSLog.e(e.getMessage());
                    }
                    break;
                case SplashActivity.JUMP_TO_CUSTOM_THEME:
                    intent.setClass(this, CustomThemeActivity.class);
                    break;
            }
            KeyboardFullScreenAd.showSessionOneTimeAd("appOpened", new KeyboardFullScreenAd.OneTimeAdListener() {
                @Override
                public void onAdClose() {
                    if (splashJumpCode == SplashActivity.JUMP_TO_FACEMOJI_CAMERA || splashJumpCode == SplashActivity.JUMP_TO_CUSTOM_THEME) {
                        try {
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    splashJumpCode = -1;
                }
            });
            return;
        }

        restoreNavigationView();
        mContent.setChildSelected(currentTabIndex);

        if (isFromUsageAccessActivity)

        {
            isFromUsageAccessActivity = false;
        }

//        refreshApkUpdateViews();
        HSThemeNewTipController.getInstance().

                removeNewTip(HSThemeNewTipController.ThemeTipType.NEW_TIP_THEME);

        if (mLayoutWrapper != null)

        {
            mLayoutWrapper.show();
        }

        // Place here to get a right session id from appframework
        if (isResumeOnCreate)

        {
            showOpenAlertIfNeeded();
        }

        isResumeOnCreate = false;

    }

    @Override
    protected void onStart() {
        super.onStart();
        homeKeyTracker.startTracker();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (homeKeyTracker.isHomeKeyPressed()) {
            HSAnalytics.logEvent("app_quit_way", "app_quit_way", "home");
        }
        homeKeyTracker.stopTracker();

        if (trialKeyboardDialog != null) {
            KCCommonUtils.dismissDialog(trialKeyboardDialog);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        currentTabIndex = ITEMS_INDEX_RESOURCE_ID_LIST.indexOf(id);
        if (mViewIndex != currentTabIndex) {
            mViewIndex = currentTabIndex;
        }
        mContent.setChildSelected(currentTabIndex);
        // reset icon to origins
        return true;
    }

    private void showTrialKeyboardDialog() {
        if (HSInputMethodListManager.isMyInputMethodSelected()) {
            boolean showAd = getIntent().getBooleanExtra(EXTRA_SHOW_AD_ON_TRIAL_KEYBOARD_DISMISS, true);
            if (LockerSettings.isLockerEnableShowSatisfied() && !isFinishing()) {

                LockerEnableDialog.showLockerEnableDialog(ThemeHomeActivity.this, ThemeLockerBgUtil.getInstance().getThemeBgUrl(HSKeyboardThemeManager.getCurrentThemeName()),
                        getString(R.string.locker_enable_title_has_text),
                        () -> {
                            if (trialKeyboardDialog == null) {
                                trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeHomeActivity.this).create();
                            }

                            trialKeyboardDialog.show(showAd);
                        });
            } else {
                if (trialKeyboardDialog == null) {
                    trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeHomeActivity.this).create();
                }
                trialKeyboardDialog.show(showAd);
            }
        } else {
            Intent intent = new Intent(this, KeyboardActivationGuideActivity.class);
            startActivityForResult(intent, REQUEST_CODE_KEYBOARD_ACTIVATION_FROM_CUSTOM_THEME);
        }
    }

    public void showCustomThemeActivity(Bundle bundle) {
        final Intent intent = new Intent(this, CustomThemeActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_START_CUSTOM_THEME);
    }

    @Override
    protected void onDestroy() {
        if (trialKeyboardDialog != null) {
            KCCommonUtils.dismissDialog(trialKeyboardDialog);
            trialKeyboardDialog = null;
        }

        unregisterReceiver(broadcastReceiver);
        super.onDestroy();

        KCCommonUtils.fixSystemLeaks(this);
    }

    @Override
    public void finish() {
        super.finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        }

    }


    private boolean shouldShowUsageAccessAlert() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int currentSessionId = HSSessionMgr.getCurrentSessionId();
            if (HSPreferenceHelper.getDefault().getInt(SP_LAST_USAGE_ALERT_SESSION_ID, 0) == 0) {
                HSPreferenceHelper.getDefault().putInt(SP_LAST_USAGE_ALERT_SESSION_ID, currentSessionId);
                return true;
            } else {
                if (currentSessionId - HSPreferenceHelper.getDefault().getInt(SP_LAST_USAGE_ALERT_SESSION_ID, 0)
                        >= HSConfig.optInteger(0, "Application", "AccessUsageAlert", "AskInterval")) {
                    HSPreferenceHelper.getDefault().putInt(SP_LAST_USAGE_ALERT_SESSION_ID, currentSessionId);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_KEYBOARD_ACTIVATION_FROM_CUSTOM_THEME == requestCode) {
            if (resultCode == RESULT_OK) {
                showTrialKeyboardDialog();
            }

        } else if (requestCode == REQUEST_CODE_START_CUSTOM_THEME) {
            if (resultCode == RESULT_OK) {
                showTrialKeyboardDialog();
            }
        }
    }

    private void restoreNavigationView() {

    }

    private void checkAndShowApkUpdateAlert(final boolean force) {
        if (ApkUtils.checkAndShowUpdateAlert(force)) {
            return;
        }

        if (force) {
            HSToastUtils.toastCenterLong(getResources().getString(R.string.apk_update_to_date_tip));
        }
    }

    private void showApkUpdateTip() {
        apkUpdateTip.setVisibility(View.VISIBLE);
    }

    private boolean shouldShowApkUpdateTip(final int versionCode) {
        // Get update apk version code from local storage and remote
        final int versionCodeOfLocalStorage = ApkUtils.getUpdateApkVersionCode();

        HSLog.d("Local storage version code to update: " + versionCodeOfLocalStorage + ", latest version code: " + versionCode);

        // If we have record version code and not less than latest, means we already show the dot
        if (versionCodeOfLocalStorage >= versionCode) {
            return false;
        }

        return true;
    }

    private Random random = new Random();

    private void showOpenAlertIfNeeded() {
        if (!HSPreferenceHelper.getDefault().getBoolean(SP_TREBLE_FUNCTION_ALERT_SHOWED, false)
                && ChargingConfigManager.getManager().shouldShowEnableChargingAlert(false)) {
            if (AlertShowingUtils.isShowingAlert()) {
                return;
            }

            if (isFinishing()) {
                return;
            }

            AlertShowingUtils.startShowingAlert();
            CustomDesignAlert multiFunctionDialog = new CustomDesignAlert(HSApplication.getContext());
            multiFunctionDialog.setEnablePrivacy(true, v -> startBrowsePrivacy());
            multiFunctionDialog.setTitle(getString(R.string.multi_function_alert_title));
            multiFunctionDialog.setMessage(getString(R.string.multi_function_alert_message));
            multiFunctionDialog.setImageResource(R.drawable.enable_tripple_alert_top_image);
            multiFunctionDialog.setCancelable(true);
            multiFunctionDialog.setPositiveButton(getString(R.string.enable), view -> {
                HSAnalytics.logEvent("alert_multi_function_click", "size", "half_screen", "occasion", "open_app");
                ChargingManagerUtil.enableCharging(false);
                enableLocker();
                CPSettings.setCallAssistantModuleEnabled(true);
                CPSettings.setScreenFlashModuleEnabled(true, true);
            });
            multiFunctionDialog.setOnDismissListener(dialog -> AlertShowingUtils.stopShowingAlert());
            KCCommonUtils.showDialog(multiFunctionDialog);
            HSAnalytics.logEvent("alert_multi_function_show", "size", "half_screen", "occasion", "open_app");
            HSPreferenceHelper.getDefault().putBoolean(SP_TREBLE_FUNCTION_ALERT_SHOWED, true);
        } else {
            int priority = random.nextInt(3); // 0:Charging 1:Locker 2:Call Assistant
            HSLog.d("OpenAlert priority  = " + priority);
            switch (priority) {
                case 0:
                    if (!showChargingDialog() && !showScreenLockerDialog() && !showCallAssistantDialog()) {
                        HSLog.d("OpenAlert priority  = " + priority + " show nothing.");
                    }
                    break;
                case 1:
                    if (!showScreenLockerDialog() && !showCallAssistantDialog() && !showChargingDialog()) {
                        HSLog.d("OpenAlert priority  = " + priority + " show nothing.");
                    }
                    break;
                case 2:
                    if (!showCallAssistantDialog() && !showChargingDialog() && !showScreenLockerDialog()) {
                        HSLog.d("OpenAlert priority  = " + priority + " show nothing.");
                    }
                    break;
                default:
                    HSLog.w("OpenAlert priority wrong");
                    break;
            }
        }
    }

    private void startBrowsePrivacy() {
        Uri uri = Uri.parse(HSConfig.optString("", "Application", "Policy", "PrivacyPolicy"));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w("URLSpan", "Activity was not found for intent, " + intent.toString());
        }
    }

    private boolean showChargingDialog() {
        if (ChargingConfigManager.getManager().shouldShowEnableChargingAlert(true)) {
            if (AlertShowingUtils.isShowingAlert()) {
                return false;
            }
            if (isFinishing()) {
                return false;
            }

            AlertShowingUtils.startShowingAlert();
            CustomDesignAlert dialog = new CustomDesignAlert(HSApplication.getContext());
            dialog.setTitle(getString(R.string.charging_alert_title));
            dialog.setMessage(getString(R.string.charging_alert_message));
            dialog.setImageResource(R.drawable.enable_charging_alert_top_image);
            dialog.setCancelable(true);
            dialog.setPositiveButton(getString(R.string.enable), view -> {
                ChargingManagerUtil.enableCharging(false);
                HSAnalytics.logEvent("alert_charging_click", "size", "half_screen", "occasion", "open_app");
            });
            KCCommonUtils.showDialog(dialog);
            dialog.setOnDismissListener(dialog1 -> AlertShowingUtils.stopShowingAlert());
            HSAnalytics.logEvent("alert_charging_show", "size", "half_screen", "occasion", "open_app");
            ChargingConfigManager.getManager().increaseEnableAlertShowCount();
            return true;
        } else {
            return false;
        }
    }

    private boolean showScreenLockerDialog() {
        if (BuildConfig.LOCKER_APP_GUIDE && !LockerAppGuideManager.getInstance().isLockerInstall()) {
            if (!HSPreferenceHelper.getDefault().getBoolean("locker_guide_app_open_showed", false)) {
                HSPreferenceHelper.getDefault().putBoolean("locker_guide_app_open_showed", true);
                showLockerGuideAlert();
                return true;
            } else {
                return false;
            }
        }

        if (ScreenLockerConfigUtils.shouldShowScreenLockerAlert(true)) {
            if (AlertShowingUtils.isShowingAlert()) {
                return false;
            }
            if (isFinishing()) {
                return false;
            }

            if (BuildConfig.LOCKER_APP_GUIDE) {
                showLockerGuideAlert();
            } else {
                AlertShowingUtils.startShowingAlert();
                CustomDesignAlert lockerDialog = new CustomDesignAlert(HSApplication.getContext());
                lockerDialog.setTitle(getString(R.string.locker_alert_title));
                lockerDialog.setMessage(getString(R.string.locker_alert_message));
                lockerDialog.setImageResource(R.drawable.enable_tripple_alert_top_image);//locker image
                lockerDialog.setCancelable(true);

                lockerDialog.setPositiveButton(getString(R.string.enable), view -> {
                    HSAnalytics.logEvent("alert_locker_click", "size", "half_screen", "occasion", "open_app");
                    enableLocker();
                });
                KCCommonUtils.showDialog(lockerDialog);
                lockerDialog.setOnDismissListener(dialog -> AlertShowingUtils.stopShowingAlert());
                HSAnalytics.logEvent("alert_locker_show", "size", "half_screen", "occasion", "open_app");
                ScreenLockerConfigUtils.increaseEnableAlertShowCount();
            }
            return true;
        } else {
            return false;
        }
    }

    private void showLockerGuideAlert() {
        if (HSConfig.optBoolean(false, "Application", "DownloadScreenLocker", "AppOpen", "enable")) {
            LockerGuideAlert lockerDialog = new LockerGuideAlert(this);
            lockerDialog.setCancelable(true);
            KCCommonUtils.showDialog(lockerDialog);
            AutopilotEvent.logTopicEvent("topic-1512033355055", "locker_alert_show");
        }
    }

    private boolean showCallAssistantDialog() {
        if (CallAssistantConfigUtils.shouldShowCallAssistantAlert(true)) {
            if (AlertShowingUtils.isShowingAlert()) {
                return false;
            }
            if (isFinishing()) {
                return false;
            }
            AlertShowingUtils.startShowingAlert();
            CustomDesignAlert callAssistantDialog = new CustomDesignAlert(HSApplication.getContext());
            callAssistantDialog.setTitle(getString(R.string.call_assistant_alert_title));
            callAssistantDialog.setMessage(getString(R.string.call_assistant_alert_message));
            callAssistantDialog.setImageResource(R.drawable.enable_callflash_alert_top_image);// call image
            callAssistantDialog.setCancelable(true);
            callAssistantDialog.setEnablePrivacy(true, v -> startBrowsePrivacy());
            callAssistantDialog.setPositiveButton(getString(R.string.enable), view -> {
                HSAnalytics.logEvent("alert_call_assistant_click", "size", "half_screen", "occasion", "open_app");
                CPSettings.setCallAssistantModuleEnabled(true);
                CPSettings.setScreenFlashModuleEnabled(true, true);
            });
            KCCommonUtils.showDialog(callAssistantDialog);
            callAssistantDialog.setOnDismissListener(dialog -> AlertShowingUtils.stopShowingAlert());
            HSAnalytics.logEvent("alert_call_assistant_show", "size", "half_screen", "occasion", "open_app");
            CallAssistantConfigUtils.increaseAlertShowCount();
            return true;
        } else {
            return false;
        }
    }

    private void enableLocker() {
        LockerSettings.setLockerEnabled(true);
    }

    @Override
    public void updateClick() {
        handler.removeMessages(HANDLER_SHOW_UPDATE_DIALOG);
        checkAndShowApkUpdateAlert(true);
        handler.post(this::restoreNavigationView);

    }
}
