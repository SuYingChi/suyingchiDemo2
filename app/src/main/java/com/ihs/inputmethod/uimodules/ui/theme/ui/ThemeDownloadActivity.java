package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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
import com.ihs.inputmethod.uimodules.ui.common.adapter.TabFragmentPagerAdapter;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.StickerHomeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.widget.CustomDesignAlert;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.alerts.HSAlertDialog;
import com.ihs.keyboardutils.permission.PermissionFloatWindow;
import com.ihs.keyboardutils.permission.PermissionTip;
import com.ihs.keyboardutils.permission.PermissionUtils;
import com.ihs.keyboardutils.utils.InterstitialGiftUtils;

import java.util.ArrayList;

import static android.view.View.GONE;
import static com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity.keyboardActivationFromCustom;
import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;

/**
 * Created by jixiang on 16/8/17.
 */
public class ThemeDownloadActivity extends HSAppCompatActivity implements KeyboardActivationProcessor.OnKeyboardActivationChangedListener, TrialKeyboardDialog.OnTrialKeyboardStateChanged, View.OnClickListener {
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
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<Fragment> fragments;
    private TabFragmentPagerAdapter tabFragmentPagerAdapter;
    private String currentFragmentTag = THEME_STORE_FRAGMENT_TAG;

    private TrialKeyboardDialog trialKeyboardDialog;
    private boolean isFromUsageAccessActivity;
    private View enableTipTV;
    private ThemeDownloadActivity context = ThemeDownloadActivity.this;
    private KeyboardActivationProcessor keyboardActivationProcessor;
    private boolean isResumeOnCreate = true;

    private AcbInterstitialAdLoader acbInterstitialAdLoader;
    private AlertDialog fullscreenAdLoadingDialog;
    private boolean fullscreenShowed = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_SHOW_ACTIVE_DIALOG) {
                if (!HSInputMethodListManager.isMyInputMethodSelected()) {
                    keyboardActivationProcessor.showHomePageActivationDialog(ThemeDownloadActivity.this);
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
                    Toast.makeText(ThemeDownloadActivity.this, R.string.locker_wallpaper_network_error, Toast.LENGTH_LONG).show();
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
                    if (ThemeDownloadActivity.class.getSimpleName().equals(showTrialKeyboardActivityName)) {
                        showTrialKeyboardDialog(activationCode);
                    }
                }
            } else if (NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
                Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.purchase_success), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_theme_download);
        getWindow().setBackgroundDrawable(null);

        appbarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        String downloadTitle = getResources().getString(R.string.store_nav_download);
        toolbar.setTitle(downloadTitle);
        setSupportActionBar(toolbar);

        View adTriggerView = findViewById(R.id.download_page_trigger);
        adTriggerView.setOnClickListener(this);

        tabLayout = (TabLayout)  findViewById(R.id.store_tab);

        viewPager = (ViewPager) findViewById(R.id.fragment_view_pager);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        keyboardActivationProcessor = new KeyboardActivationProcessor(ThemeDownloadActivity.class, ThemeDownloadActivity.this);

        enableTipTV = findViewById(R.id.tv_enable_keyboard);
        enableTipTV.setVisibility(GONE);
        enableTipTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("activate_appbar_clicked");
                keyboardActivationProcessor.activateKeyboard(ThemeDownloadActivity.this, false, keyboardActivationFromHome);
            }
        });
        if (getIntent() != null && getIntent().getBooleanExtra(BUNDLE_AUTO_ENABLE_KEYBOARD, false)) {
            keyboardActivationProcessor.activateKeyboard(ThemeDownloadActivity.this, false, keyboardActivationFromHome);
        }
        findViewById(R.id.home_create_theme_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                String customEntry = "mytheme_float_button";
                bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                CustomThemeActivity.startCustomThemeActivity(bundle);

                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("customize_entry_clicked", "mythemes");
                HSAnalytics.logEvent("customize_entry_clicked", "mythemes");
            }
        });

        fragments = new ArrayList<>();
        Fragment themeHomeFragment = new ThemeHomeFragment();
        Fragment stickerHomeFragment = new StickerHomeFragment();
        Fragment myThemeFragment = new MyThemeFragment();
        fragments.add(themeHomeFragment);
        fragments.add(stickerHomeFragment);
        fragments.add(myThemeFragment);
        currentFragmentTag = THEME_STORE_FRAGMENT_TAG;

        tabFragmentPagerAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setOffscreenPageLimit(fragments.size());
        viewPager.setAdapter(tabFragmentPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);


        HSGlobalNotificationCenter.addObserver(CustomThemeActivity.NOTIFICATION_SHOW_TRIAL_KEYBOARD, notificationObserver);

        onNewIntent(getIntent());
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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        newConfig.orientation = Configuration.ORIENTATION_PORTRAIT;
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

        if (isFromUsageAccessActivity) {
            isFromUsageAccessActivity = false;
            if (PermissionUtils.isUsageAccessGranted()) {
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("permission_usage_access");
            }
        }

        HSThemeNewTipController.getInstance().removeNewTip(HSThemeNewTipController.ThemeTipType.NEW_TIP_THEME);

        // Place here to get a right session id from appframework
        if (isResumeOnCreate) {
            showEnableChargingAlertIfNeeded();
        }
        isResumeOnCreate = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.gc();
        if (homeKeyTracker.isHomeKeyPressed() && trialKeyboardDialog != null && trialKeyboardDialog.isShowing()) {
            trialKeyboardDialog.dismiss();
        }
    }

    private void showTrialKeyboardDialog(final int activationCode) { //在trialKeyboardDialog展示之前根据条件判断是否弹出一个全屏的Dialog来开启Locker
        final KeyboardActivationProcessor processor =
                new KeyboardActivationProcessor(ThemeDownloadActivity.this.getClass(), new KeyboardActivationProcessor.OnKeyboardActivationChangedListener() {
                    @Override
                    public void activeDialogShowing() {

                    }

                    @Override
                    public void keyboardSelected(int requestCode) {
                        if (requestCode == activationCode) {
                            if (LockerSettings.isLockerEnableShowSatisfied() && !isFinishing()) {
                                LockerEnableDialog.showLockerEnableDialog(ThemeDownloadActivity.this, ThemeLockerBgUtil.getInstance().getThemeBgUrl(HSKeyboardThemeManager.getCurrentThemeName()), new LockerEnableDialog.OnLockerBgLoadingListener() {
                                    @Override
                                    public void onFinish() {
                                        if (trialKeyboardDialog == null) {
                                            trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeDownloadActivity.class.getName()).create(context, ThemeDownloadActivity.this);
                                        }
                                        if (activationCode == keyboardActivationFromCustom) {
                                            trialKeyboardDialog.show(ThemeDownloadActivity.this, activationCode, false);
                                        } else {
                                            trialKeyboardDialog.show(ThemeDownloadActivity.this, activationCode, true);
                                        }
                                    }
                                });
                            } else {
                                if (trialKeyboardDialog == null) {
                                    trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeDownloadActivity.class.getName()).create(context, ThemeDownloadActivity.this);
                                }
                                if (activationCode == keyboardActivationFromCustom) {
                                    trialKeyboardDialog.show(ThemeDownloadActivity.this, activationCode, false);
                                } else {
                                    trialKeyboardDialog.show(ThemeDownloadActivity.this, activationCode, true);
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
        processor.activateKeyboard(ThemeDownloadActivity.this, true, activationCode);
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
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed", "themepackage");
                break;
            case keyboardActivationFromCustom:
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed", "customizetheme");
                break;
            default:
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed", "apply");
                break;
        }
    }

    @Override
    public void onTrailKeyPrevented() {
        if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            enableTipTV.setVisibility(View.VISIBLE);
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

    private boolean showEnableChargingAlertIfNeeded() {
        if (ChargingConfigManager.getManager().shouldShowEnableChargingAlert(true)) {
            ChargingConfigManager.getManager().increaseEnableAlertShowCount();
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("alert_charging_show");
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
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent("alert_charging_click");
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
            case R.id.download_page_trigger:
//                if (lottieAnimationView.isAnimating()) {
//                    lottieAnimationView.cancelAnimation();
//                    lottieAnimationView.setProgress(0f);
//                }
                /*loadFullscreenAd();
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_fullscreenAds_icon_mainscreencorner_clicked");
                HSAnalytics.logEvent("app_fullscreenAds_icon_mainscreencorner_clicked");*/
                switchToDownloads();
                break;
        }
    }

    private void switchToDownloads() {
        Intent intent = new Intent(this, ThemeDownloadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
        acbInterstitialAdLoader = KCInterstitialAd.loadAndShow(getString(R.string.placement_full_screen_open_keyboard), new KCInterstitialAd.OnAdShowListener() {
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
