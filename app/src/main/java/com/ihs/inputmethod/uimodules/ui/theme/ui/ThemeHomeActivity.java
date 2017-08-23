package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.acb.interstitialads.AcbInterstitialAdLoader;
import com.artw.lockscreen.LockerEnableDialog;
import com.artw.lockscreen.LockerSettings;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.chargingscreen.activity.ChargingFullScreenAlertDialogActivity;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.HSUIInputMethod;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.theme.ThemeLockerBgUtil;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.utils.HSAppLockerUtils;
import com.ihs.inputmethod.uimodules.widget.CustomDesignAlert;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.alerts.HSAlertDialog;
import com.ihs.keyboardutils.alerts.KCAlert;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.permission.PermissionFloatWindow;
import com.ihs.keyboardutils.permission.PermissionTip;
import com.ihs.keyboardutils.permission.PermissionUtils;
import com.ihs.keyboardutils.utils.InterstitialGiftUtils;
import com.kc.commons.utils.KCCommonUtils;

import static android.view.View.GONE;
import static com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity.keyboardActivationFromCustom;
import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;

/**
 * Created by jixiang on 16/8/17.
 */
public class ThemeHomeActivity extends HSAppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, KeyboardActivationProcessor.OnKeyboardActivationChangedListener, TrialKeyboardDialog.OnTrialKeyboardStateChanged, View.OnClickListener {
    public final static String INTENT_KEY_SHOW_TRIAL_KEYBOARD = "SHOW_TRIAL_KEYBOARD";
    public final static String BUNDLE_AUTO_ENABLE_KEYBOARD = "BUNDLE_AUTO_ENABLE_KEYBOARD";

    private static final String SP_LAST_USAGE_ALERT_SESSION_ID = "SP_LAST_USAGE_ALERT_SESSION_ID";
    private final static String MY_THEME_FRAGMENT_TAG = "fragment_tag_my_theme";
    private final static String THEME_STORE_FRAGMENT_TAG = "fragment_tag_theme_store";

    private static final int keyboardActivationFromHome = 11;
    public static final int keyboardActivationFromHomeWithTrial = 12;

    private static final int LOAD_FULLSCREEN_AD_TIME = 5000;

    private static int HANDLER_SHOW_ACTIVE_DIALOG = 101;
    private static int HANDLER_SHOW_UPDATE_DIALOG = 102;
    private static int HANDLER_DISMISS_LOADING_FULLSCREEN_AD_DIALOG = 103;

    private AppBarLayout appbarLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String currentFragmentTag = THEME_STORE_FRAGMENT_TAG;
    private TrialKeyboardDialog trialKeyboardDialog;
    private boolean isFromUsageAccessActivity;
    private View enableTipTV;
    private boolean shouldShowActivationTip;
    private ThemeHomeActivity context = ThemeHomeActivity.this;
    private KeyboardActivationProcessor keyboardActivationProcessor;
    private View apkUpdateTip;
    private boolean isResumeOnCreate = true;

    private AcbInterstitialAdLoader acbInterstitialAdLoader;
    private AlertDialog fullscreenAdLoadingDialog;
    private boolean fullscreenShowed = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_SHOW_ACTIVE_DIALOG) {
                if (!HSInputMethodListManager.isMyInputMethodSelected()) {
                    keyboardActivationProcessor.showHomePageActivationDialog(ThemeHomeActivity.this);
                }
            } else if (msg.what == HANDLER_SHOW_UPDATE_DIALOG) {
                checkAndShowApkUpdateAlert(false);
            } else if (msg.what == HANDLER_DISMISS_LOADING_FULLSCREEN_AD_DIALOG) {
                if (!fullscreenShowed) {
                    if (acbInterstitialAdLoader != null) {
                        acbInterstitialAdLoader.cancel();
                        acbInterstitialAdLoader = null;
                    }
                    dismissDialog(fullscreenAdLoadingDialog);
                    fullscreenAdLoadingDialog = null;
                    Toast.makeText(ThemeHomeActivity.this, R.string.locker_wallpaper_network_error, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (CustomThemeActivity
                    .NOTIFICATION_SHOW_TRIAL_KEYBOARD.equals(s)) {
                if (hsBundle != null) {
                    String showTrialKeyboardActivityName = hsBundle.getString(TrialKeyboardDialog.BUNDLE_KEY_SHOW_TRIAL_KEYBOARD_ACTIVITY, "");
                    int activationCode = hsBundle.getInt(KeyboardActivationProcessor.BUNDLE_ACTIVATION_CODE);
                    if (ThemeHomeActivity.class.getSimpleName().equals(showTrialKeyboardActivityName)) {
                        showTrialKeyboardDialog(activationCode);
                    }
                }
            } else if (NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
                Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.purchase_success), Toast.LENGTH_LONG).show();
                navigationView.getMenu().findItem(R.id.nav_no_ads).setVisible(false);//setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_theme_home);
        getWindow().setBackgroundDrawable(null);

        appbarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        String themeTitle = getResources().getString(R.string.theme_nav_theme_store);
        toolbar.setTitle(HSLog.isDebugging() ? themeTitle + " (Debug)" : themeTitle);
        setSupportActionBar(toolbar);

        View adTriggerView = findViewById(R.id.theme_home_interstitial_ad_trigger_view);
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            adTriggerView.setVisibility(View.GONE);
        } else {
            adTriggerView.setVisibility(View.VISIBLE);
            adTriggerView.setOnClickListener(this);
        }


        keyboardActivationProcessor = new KeyboardActivationProcessor(ThemeHomeActivity.class, ThemeHomeActivity.this);

        enableTipTV = findViewById(R.id.tv_enable_keyboard);
        enableTipTV.setVisibility(GONE);
        enableTipTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("activate_appbar_clicked");
                keyboardActivationProcessor.activateKeyboard(ThemeHomeActivity.this, false, keyboardActivationFromHome);
            }
        });
        if (getIntent() != null && getIntent().getBooleanExtra(BUNDLE_AUTO_ENABLE_KEYBOARD, false)) {
            keyboardActivationProcessor.activateKeyboard(ThemeHomeActivity.this, false, keyboardActivationFromHome);
        }
        findViewById(R.id.home_create_theme_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                String customEntry = THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag) ? "store_float_button" : "mytheme_float_button";
                bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                CustomThemeActivity.startCustomThemeActivity(bundle);

//                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("customize_entry_clicked", THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag) ? "store" : "mythemes");
                HSAnalytics.logEvent("customize_entry_clicked", THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag) ? "store" : "mythemes");
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                context, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                clearApkUpdateTip();
//                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_show", THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag) ? "store" : "mythemes");
                HSAnalytics.logEvent("sidebar_show", "fragment", THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag) ? "store" : "mythemes");
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);//去除自动染色，默认图标灰色，点击后变成colorAccent色
        navigationView.setItemTextColor(null);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        ViewGroup.LayoutParams layoutParams = headerView.getLayoutParams();
        layoutParams.height = (int) (getResources().getDisplayMetrics().widthPixels * 0.48f);

        //remove myThemeFragment if exist
        Fragment myThemeFragment = getFragmentManager().findFragmentByTag(MY_THEME_FRAGMENT_TAG);
        if (myThemeFragment != null) {
            getFragmentManager().beginTransaction().remove(myThemeFragment).commit();
        }

        //create storeFragment only if not exist
        Fragment storeFragment = getFragmentManager().findFragmentByTag(THEME_STORE_FRAGMENT_TAG);
        if (storeFragment == null) {
            storeFragment = new ThemeHomeFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.content_layout, storeFragment, THEME_STORE_FRAGMENT_TAG).commit();
        }
        currentFragmentTag = THEME_STORE_FRAGMENT_TAG;

        //init locker function
        boolean lockerEnable = getResources().getBoolean(R.bool.config_locker_drawer_visiable_enable) && HSAppLockerUtils.isLockerEnabled();
        navigationView.getMenu().findItem(R.id.nav_app_locker).setVisible(lockerEnable);

        // init update function
        navigationView.getMenu().findItem(R.id.nav_update).setVisible(ApkUtils.isUpdateEnabled());
        navigationView.getMenu().findItem(R.id.nav_no_ads).setVisible(getResources().getBoolean(R.bool.show_remove_ads_menu) && !RemoveAdsManager.getInstance().isRemoveAdsPurchased());
        apkUpdateTip = findViewById(R.id.apk_update_tip);


        //界面被启动 请求 扫描权限
        if (HSConfig.optBoolean(false, "Application", "AccessUsageAlert", "enable") && !PermissionUtils.isUsageAccessGranted() && shouldShowUsageAccessAlert()) {

            HSPreferenceHelper.getDefault().putInt(SP_LAST_USAGE_ALERT_SESSION_ID, HSSessionMgr.getCurrentSessionId());
            new KCAlert.Builder(this)
                    .setTitle(getString(R.string.dialog_app_usage_title))
                    .setMessage(getString(R.string.dialog_app_usage_tips))
                    .setTopImageResource(R.drawable.enable_keyboard_alert_top_bg)
                    .setPositiveButton(getString(R.string.dialog_agree).toUpperCase(), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !PermissionUtils.isUsageAccessGranted()) {
                                isFromUsageAccessActivity = true;
                            }
                            enableUsageAccessPermission();
                            // HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("appalert_usageaccess_agree_clicked");
                        }
                    })
                    .setNegativeButton(getString(R.string.dialog_disagree).toUpperCase(), null)
                    .show();
            // HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("appalert_usageaccess_showed");
        }

        HSGlobalNotificationCenter.addObserver(CustomThemeActivity.NOTIFICATION_SHOW_TRIAL_KEYBOARD, notificationObserver);

        //如果是第一次进入页面并且当前键盘没有被选为自己则弹框。
        if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            handler.sendEmptyMessageDelayed(HANDLER_SHOW_ACTIVE_DIALOG, 500);
        } else {
            handler.sendEmptyMessageDelayed(HANDLER_SHOW_UPDATE_DIALOG, 500);
        }

        onNewIntent(getIntent());
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

        boolean showTrial = intent.getBooleanExtra(INTENT_KEY_SHOW_TRIAL_KEYBOARD, false);
        if (showTrial) {
            handler.removeMessages(HANDLER_SHOW_ACTIVE_DIALOG);
            showTrialKeyboardDialog(keyboardActivationFromHomeWithTrial);
            getIntent().putExtra(INTENT_KEY_SHOW_TRIAL_KEYBOARD, false);
        } else {
            String from = intent.getStringExtra("From");
            if (trialKeyboardDialog != null && trialKeyboardDialog.isShowing() && from != null && from.equals("Keyboard")) {
                Toast.makeText(this, "Already in " + getResources().getString(R.string.theme_nav_theme_store), Toast.LENGTH_SHORT).show();
            }
        }
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_theme_store);
        onNavigationItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        newConfig.orientation = Configuration.ORIENTATION_PORTRAIT;
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HSFloatWindowManager.getInstance().removeAccessibilityCover();
            }
        }, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        restoreNavigationView();

        shouldShowActivationTip = true;

        if (isFromUsageAccessActivity) {
            isFromUsageAccessActivity = false;
            if (PermissionUtils.isUsageAccessGranted()) {
                // HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("permission_usage_access");
            }
        }

        refreshApkUpdateViews();
        HSThemeNewTipController.getInstance().removeNewTip(HSThemeNewTipController.ThemeTipType.NEW_TIP_THEME);

        // Place here to get a right session id from appframework
        if (isResumeOnCreate) {
            showEnableChargingAlertIfNeeded();
        }
        isResumeOnCreate = false;

//        if (!isAdTriggerLottieAnimationPlayed) {
////            lottieAnimationView.loop(true);
////            lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
////                /**
////                 * {@inheritDoc}
////                 *
////                 * @param animation
////                 */
////                @Override
////                public void onAnimationRepeat(Animator animation) {
////                    super.onAnimationRepeat(animation);
////                    if (++adTriggerLottieAnimationPlayedTimes >= GIFT_AD_TRIGGER_ANIMATION_PLAY_TIME) {
////                        lottieAnimationView.cancelAnimation();
////                    }
////                }
////            });
//            isAdTriggerLottieAnimationPlayed = true;
////            lottieAnimationView.playAnimation();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.gc();
        if (homeKeyTracker.isHomeKeyPressed() && trialKeyboardDialog != null && trialKeyboardDialog.isShowing()) {
            trialKeyboardDialog.dismiss();
        }
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_theme_store) {
            if (!currentFragmentTag.equals(THEME_STORE_FRAGMENT_TAG)) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment myThemeFragment = getFragmentManager().findFragmentByTag(MY_THEME_FRAGMENT_TAG);
                Fragment themeStoreFragment = getFragmentManager().findFragmentByTag(THEME_STORE_FRAGMENT_TAG);
                if (myThemeFragment != null) {
                    transaction.hide(myThemeFragment);
                }
                if (themeStoreFragment == null) {
                    themeStoreFragment = new ThemeHomeFragment();
                    transaction.add(R.id.content_layout, themeStoreFragment, THEME_STORE_FRAGMENT_TAG);
                }
                transaction.show(themeStoreFragment).commit();
                appbarLayout.setExpanded(true);
                toolbar.setTitle(R.string.theme_nav_theme_store);
                // HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_store_clicked");
            }
            currentFragmentTag = THEME_STORE_FRAGMENT_TAG;
        } else if (id == R.id.nav_my_themes) {
            if (!currentFragmentTag.equals(MY_THEME_FRAGMENT_TAG)) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment myThemeFragment = getFragmentManager().findFragmentByTag(MY_THEME_FRAGMENT_TAG);
                Fragment themeStoreFragment = getFragmentManager().findFragmentByTag(THEME_STORE_FRAGMENT_TAG);
                if (themeStoreFragment != null) {
                    transaction.hide(themeStoreFragment);
                }
                if (myThemeFragment == null) {
                    myThemeFragment = new MyThemeFragment();
                    transaction.add(R.id.content_layout, myThemeFragment, MY_THEME_FRAGMENT_TAG);
                }
                transaction.show(myThemeFragment).commit();
                appbarLayout.setExpanded(true);
                toolbar.setTitle(R.string.theme_nav_my_themes);
//                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_mythemes_clicked");
                HSAnalytics.logEvent("sidebar_mythemes_clicked");
            }
            currentFragmentTag = MY_THEME_FRAGMENT_TAG;
        } else if (id == R.id.nav_language) {
            HSUIInputMethod.launchMoreLanguageActivity();
//            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_languages_clicked");
            HSAnalytics.logEvent("sidebar_languages_clicked");
        } else if (id == R.id.nav_setting) {
            HSUIInputMethod.launchSettingsActivity();
//            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_settings_clicked");
            HSAnalytics.logEvent("sidebar_settings_clicked");
        } else if (id == R.id.nav_app_locker) {
            HSGlobalNotificationCenter.sendNotificationOnMainThread(HSUIInputMethod.HS_NOTIFICATION_LOCKER_CLICK);
            // HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_applocker_clicked");
        } else if (id == R.id.nav_update) {
            handler.removeMessages(HANDLER_SHOW_UPDATE_DIALOG);
            checkAndShowApkUpdateAlert(true);
            // HSGoogleAnalyticsUtils.getInstance().logAppEvent("sidebar_update_clicked");

            handler.post(new Runnable() {
                @Override
                public void run() {
                    restoreNavigationView();
                }
            });
        } else if (id == R.id.nav_no_ads) {
//            HSGoogleAnalyticsUtils.getInstance().logAppEvent("removeAds_clicked");
            HSAnalytics.logEvent("removeAds_clicked");
            RemoveAdsManager.getInstance().purchaseRemoveAds();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showTrialKeyboardDialog(final int activationCode) { //在trialKeyboardDialog展示之前根据条件判断是否弹出一个全屏的Dialog来开启Locker
        final KeyboardActivationProcessor processor =
                new KeyboardActivationProcessor(ThemeHomeActivity.this.getClass(), new KeyboardActivationProcessor.OnKeyboardActivationChangedListener() {
                    @Override
                    public void activeDialogShowing() {

                    }

                    @Override
                    public void keyboardSelected(int requestCode) {
                        if (requestCode == activationCode) {
                            if (LockerSettings.isLockerEnableShowSatisfied() && !isFinishing()) {
                                LockerEnableDialog.showLockerEnableDialog(ThemeHomeActivity.this, ThemeLockerBgUtil.getInstance().getThemeBgUrl(HSKeyboardThemeManager.getCurrentThemeName()), new LockerEnableDialog.OnLockerBgLoadingListener() {
                                    @Override
                                    public void onFinish() {
                                        if (trialKeyboardDialog == null) {
                                            trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeHomeActivity.class.getName()).create(context, ThemeHomeActivity.this);
                                        }
                                        if (activationCode == keyboardActivationFromCustom) {
                                            trialKeyboardDialog.show(ThemeHomeActivity.this, activationCode, false);
                                        } else {
                                            trialKeyboardDialog.show(ThemeHomeActivity.this, activationCode, true);
                                        }
                                    }
                                });
                            } else {
                                if (trialKeyboardDialog == null) {
                                    trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeHomeActivity.class.getName()).create(context, ThemeHomeActivity.this);
                                }
                                if (activationCode == keyboardActivationFromCustom) {
                                    trialKeyboardDialog.show(ThemeHomeActivity.this, activationCode, false);
                                } else {
                                    trialKeyboardDialog.show(ThemeHomeActivity.this, activationCode, true);
                                }
                            }
                        }
                    }

                    @Override
                    public void activeDialogCanceled() {

                    }

                    @Override
                    public void activeDialogDismissed() {

                    }
                });
        processor.activateKeyboard(ThemeHomeActivity.this, true, activationCode);
    }

    @Override
    protected void onDestroy() {
        if (trialKeyboardDialog != null) {
            trialKeyboardDialog.dismiss();
            trialKeyboardDialog = null;
        }

        HSGlobalNotificationCenter.removeObserver(notificationObserver);
        keyboardActivationProcessor.release();
        keyboardActivationProcessor = null;
        super.onDestroy();

        KCCommonUtils.fixInputMethodManagerLeak(this);
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
    public void activeDialogShowing() {
        enableTipTV.setVisibility(GONE);
    }

    public void keyboardSelected(int requestCode) {
        if (keyboardActivationFromHomeWithTrial == requestCode || keyboardActivationFromCustom == requestCode) {
            showTrialKeyboardDialog(requestCode);
        }
        enableTipTV.setVisibility(GONE);
    }

    @Override
    public void activeDialogCanceled() {
    }

    @Override
    public void activeDialogDismissed() {
        if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            enableTipTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTrialKeyShow(int requestCode) {
        enableTipTV.setVisibility(GONE);

        switch (requestCode) {
            case keyboardActivationFromHomeWithTrial:
                // HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed", "themepackage");
                HSAnalytics.logEvent("keyboard_theme_try_viewed", "from", "themePackage");
                break;
            case keyboardActivationFromCustom:
                // HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed", "customizetheme");
                HSAnalytics.logEvent("keyboard_theme_try_viewed", "from", "customizetheme");
                break;
            default:
                // HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed", "apply");
                HSAnalytics.logEvent("keyboard_theme_try_viewed", "from", "apply");
                break;
        }
    }

    @Override
    public void onTrailKeyPrevented() {
        if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            enableTipTV.setVisibility(View.VISIBLE);
        }
    }

    private void restoreNavigationView() {
        if (THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag)) {
            navigationView.setCheckedItem(R.id.nav_theme_store);
            if (shouldShowActivationTip && !HSInputMethodListManager.isMyInputMethodSelected()) {
                enableTipTV.setVisibility(View.VISIBLE);
            } else {
                enableTipTV.setVisibility(GONE);
            }

        } else if (MY_THEME_FRAGMENT_TAG.equals(currentFragmentTag)) {
            navigationView.setCheckedItem(R.id.nav_my_themes);
        }
    }

    private void resetApkUpdateViews() {
        apkUpdateTip.setVisibility(View.GONE);
        navigationView.getMenu().findItem(R.id.nav_update).getActionView().setVisibility(View.GONE);
    }

    private void refreshApkUpdateViews() {
        resetApkUpdateViews();

        if (ApkUtils.shouldUpdate()) {
            if (shouldShowApkUpdateTip(ApkUtils.getLatestVersionCode())) {
                showApkUpdateTip();
            }

            showApkUpdateMenuItemIndicationIcon();
        }
    }

    private void checkAndShowApkUpdateAlert(final boolean force) {
        if (ApkUtils.checkAndShowUpdateAlert(force)) {
            return;
        }

        if (force) {
            HSToastUtils.toastCenterLong(getResources().getString(R.string.apk_update_to_date_tip));
        }
    }

    private void clearApkUpdateTip() {
        if (apkUpdateTip.getVisibility() == View.VISIBLE) {
            apkUpdateTip.setVisibility(View.GONE);
            ApkUtils.saveUpdateApkVersionCode();
        }
    }

    private void showApkUpdateTip() {
        apkUpdateTip.setVisibility(View.VISIBLE);
        // HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_menu_reddot_show");
    }

    private void showApkUpdateMenuItemIndicationIcon() {
        navigationView.getMenu().findItem(R.id.nav_update).getActionView().setVisibility(View.VISIBLE);
        // HSGoogleAnalyticsUtils.getInstance().logAppEvent("sidebar_update_icon_show");
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

    private boolean showEnableChargingAlertIfNeeded() {
        if (ChargingConfigManager.getManager().shouldShowEnableChargingAlert(true)) {
            ChargingConfigManager.getManager().increaseEnableAlertShowCount();
//            // HSGoogleAnalyticsUtils.getInstance().logAppEvent("alert_charging_show");
            HSAnalytics.logEvent("alert_show");
            if (HSConfig.optInteger(0, "Application", "ChargeLocker", "EnableAlertStyle") == 0) {
                CustomDesignAlert dialog = new CustomDesignAlert(HSApplication.getContext());
                dialog.setTitle(getString(R.string.charging_alert_title));
                dialog.setMessage(getString(R.string.charging_alert_message));
                dialog.setImageResource(R.drawable.enable_charging_alert_top_image);
                dialog.setCancelable(true);
                dialog.setPositiveButton(getString(R.string.enable), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ChargingManagerUtil.enableCharging(false);
//                        HSGoogleAnalyticsUtils.getInstance().logAppEvent("alert_charging_click");
                        HSAnalytics.logEvent("alert_clicked");
                    }
                });
                dialog.show();
            } else {
                Intent intent = new Intent(HSApplication.getContext(), ChargingFullScreenAlertDialogActivity.class);
                intent.putExtra("type", "charging");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                HSApplication.getContext().startActivity(intent);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.theme_home_interstitial_ad_trigger_view:
//                if (lottieAnimationView.isAnimating()) {
//                    lottieAnimationView.cancelAnimation();
//                    lottieAnimationView.setProgress(0f);
//                }
                loadFullscreenAd();
                // HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_fullscreenAds_icon_mainscreencorner_clicked");
                HSAnalytics.logEvent("app_fullscreenAds_icon_mainscreencorner_clicked");
                break;
        }
    }


    private void dismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing() && !isFinishing()) {
            dialog.dismiss();
        }
    }

    private void loadFullscreenAd() {
        if (!InterstitialGiftUtils.isNetworkAvailable(-1)) {
            Toast.makeText(this, R.string.no_network_connection, Toast.LENGTH_LONG).show();
            return;
        }

        fullscreenShowed = false;
        acbInterstitialAdLoader = KCInterstitialAd.loadAndShow(getString(R.string.placement_full_screen_open_keyboard),
                getString(R.string.interstitial_ad_title_home_gift_button),
                getString(R.string.interstitial_ad_subtitle_home_gift_button),
                new KCInterstitialAd.OnAdShowListener() {
            @Override
            public void onAdShow(boolean b) {
                fullscreenShowed = b;
                dismissDialog(fullscreenAdLoadingDialog);
                fullscreenAdLoadingDialog = null;
                handler.removeMessages(HANDLER_DISMISS_LOADING_FULLSCREEN_AD_DIALOG);
            }
        }, null);

        fullscreenAdLoadingDialog = HSAlertDialog.build(this).setView(R.layout.dialog_loading).setCancelable(false).create();
        fullscreenAdLoadingDialog.show();

        handler.sendEmptyMessageDelayed(HANDLER_DISMISS_LOADING_FULLSCREEN_AD_DIALOG, LOAD_FULLSCREEN_AD_TIME);
    }


}
