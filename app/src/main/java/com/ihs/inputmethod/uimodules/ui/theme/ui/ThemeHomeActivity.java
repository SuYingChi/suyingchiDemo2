package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.app.Dialog;
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
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.CPSettings;
import com.acb.call.HomeKeyWatcher;
import com.acb.interstitialads.AcbInterstitialAdLoader;
import com.artw.lockscreen.LockerEnableDialog;
import com.artw.lockscreen.LockerSettings;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.theme.ThemeLockerBgUtil;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.TabFragmentPagerAdapter;
import com.ihs.inputmethod.uimodules.ui.customize.BaseCustomizeActivity;
import com.ihs.inputmethod.uimodules.ui.customize.util.BottomNavigationViewHelper;
import com.ihs.inputmethod.uimodules.ui.customize.view.CustomizeContentView;
import com.ihs.inputmethod.uimodules.ui.customize.view.LayoutWrapper;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.settings.activities.SettingsActivity2;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.widget.CustomDesignAlert;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.ihs.inputmethod.utils.CallAssistantConfigUtils;
import com.ihs.inputmethod.utils.ScreenLockerConfigUtils;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.alerts.KCAlert;
import com.ihs.keyboardutils.permission.PermissionFloatWindow;
import com.ihs.keyboardutils.permission.PermissionTip;
import com.ihs.keyboardutils.permission.PermissionUtils;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.InterstitialGiftUtils;
import com.kc.commons.utils.KCCommonUtils;
import com.keyboard.common.KeyboardActivationGuideActivity;

import java.util.ArrayList;
import java.util.Random;

import static android.view.View.GONE;

/**
 * Created by jixiang on 16/8/17.
 */
public class ThemeHomeActivity extends BaseCustomizeActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener,
        SettingsActivity2.GeneralHomePreferenceFragment.OnUpdateClickListener {
    public final static String EXTRA_SHOW_TRIAL_KEYBOARD = "EXTRA_SHOW_TRIAL_KEYBOARD";
    public final static String EXTRA_AUTO_ENABLE_KEYBOARD = "EXTRA_AUTO_ENABLE_KEYBOARD";

    private static final String SP_LAST_USAGE_ALERT_SESSION_ID = "SP_LAST_USAGE_ALERT_SESSION_ID";
    private static final String SP_TREBLE_FUNCTION_ALERT_SHOWED = "sp_treble_function_alert_showed";
    private final static String MY_THEME_FRAGMENT_TAG = "fragment_tag_my_theme";
    private final static String THEME_STORE_FRAGMENT_TAG = "fragment_tag_theme_store";
    public static int HOME_VIEWPAGER_STICKER_PAGE = 1;
    public static final String BUNDLE_KEY_HOME_INIT = "home_init_viewpager_page";

    private static final int KEYBOARD_ACTIVATION_FROM_HOME_ENTRY = 1;
    private static final int KEYBOARD_ACTIVATION_FROM_ENABLE_TIP = 2;
    private static final int KEYBOARD_ACTIVATION_FROM_SHOW_TRIAL_KEYBOARD_INTENT = 3;
    private static final int KEYBOARD_ACTIVATION_FROM_CUSTOM_THEME_FINISH = 4;

    private static final int LOAD_FULLSCREEN_AD_TIME = 5000;
    private static int HANDLER_SHOW_ACTIVE_DIALOG = 101;
    private static int HANDLER_SHOW_UPDATE_DIALOG = 102;
    private static int HANDLER_DISMISS_LOADING_FULLSCREEN_AD_DIALOG = 103;

    // customize
    private static final SparseIntArray ITEMS_INDEX_MAP = new SparseIntArray(5);
    private static final SparseArray<String> ITEMS_FLURRY_NAME_MAP = new SparseArray<>(5);

    public static final int TAB_INDEX_KEYBOARD = 0;
    public static final int TAB_INDEX_WALLPAPER = 1;
    public static final int TAB_INDEX_LOCKER = 2;
    public static final int TAB_INDEX_SETTINGS = 3;

    private int currentTabIndex = 0;

    static {
        ITEMS_INDEX_MAP.put(R.id.customize_bottom_bar_keyboard, TAB_INDEX_KEYBOARD);
        ITEMS_INDEX_MAP.put(R.id.customize_bottom_bar_wallpapers, TAB_INDEX_WALLPAPER);
        ITEMS_INDEX_MAP.put(R.id.customize_bottom_bar_call, TAB_INDEX_LOCKER);
        ITEMS_INDEX_MAP.put(R.id.customize_bottom_bar_setting, TAB_INDEX_SETTINGS);

        ITEMS_FLURRY_NAME_MAP.put(R.id.customize_bottom_bar_keyboard, "Keyboard");
        ITEMS_FLURRY_NAME_MAP.put(R.id.customize_bottom_bar_wallpapers, "Wallpaper");
        ITEMS_FLURRY_NAME_MAP.put(R.id.customize_bottom_bar_call, "Locker");
        ITEMS_FLURRY_NAME_MAP.put(R.id.customize_bottom_bar_setting, "Settings");
    }

    private CustomizeContentView mContent;
    private BottomNavigationView mBottomBar;

    private LayoutWrapper mLayoutWrapper;

    private int mViewIndex;
    public int mThemeTabIndex;
    public int mWallpaperTabIndex;
    private HomeKeyWatcher mHomeKeyWatcher;

    private AppBarLayout appbarLayout;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<Class> fragments;
    private TabFragmentPagerAdapter tabFragmentPagerAdapter;
    private String currentFragmentTag = THEME_STORE_FRAGMENT_TAG;

    private TrialKeyboardDialog trialKeyboardDialog;
    private boolean isFromUsageAccessActivity;
    private View enableTipTV;
    private ThemeHomeActivity context = ThemeHomeActivity.this;
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
                    Intent intent = new Intent(ThemeHomeActivity.this, KeyboardActivationGuideActivity.class);
                    intent.putExtra(KeyboardActivationGuideActivity.EXTRA_ACTIVATION_PROMPT_MESSAGE, getString(R.string.dialog_msg_enable_keyboard_home_rain));
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
                    .NOTIFICATION_CUSTOM_THEME_ACTIVITY_FINISH_SUCCESS.equals(s)) {
                if (!PreferenceManager.getDefaultSharedPreferences(ThemeHomeActivity.this).getBoolean("CUSTOM_THEME_SAVE", false)) {
                    PreferenceManager.getDefaultSharedPreferences(ThemeHomeActivity.this).edit().putBoolean("CUSTOM_THEME_SAVE", true).apply();
                }
                showTrialKeyboardDialog(KEYBOARD_ACTIVATION_FROM_CUSTOM_THEME_FINISH);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_theme_home);
//        getWindow().setBackgroundDrawable(null);

        mContent = ViewUtils.findViewById(this, R.id.content_layout);
        mContent.setChildSelected(currentTabIndex);
        mBottomBar = ViewUtils.findViewById(this, R.id.bottom_bar);
        BottomNavigationViewHelper.disableShiftMode(mBottomBar);
        mLayoutWrapper = new LayoutWrapper(mBottomBar, getResources().getDimensionPixelSize(R.dimen.bottom_bar_default_height), CommonUtils.pxFromDp(3.3f));

        enableTipTV = findViewById(R.id.tv_enable_keyboard);
        ((TextView) enableTipTV).setText(getString(R.string.tv_enable_keyboard_tip, getString(R.string.app_name)));
        enableTipTV.setVisibility(HSInputMethodListManager.isMyInputMethodSelected() ? View.GONE : View.VISIBLE);
        enableTipTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemeHomeActivity.this, KeyboardActivationGuideActivity.class);
                startActivityForResult(intent, KEYBOARD_ACTIVATION_FROM_ENABLE_TIP);
            }
        });

        currentFragmentTag = THEME_STORE_FRAGMENT_TAG;

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
                        }
                    })
                    .setNegativeButton(getString(R.string.dialog_disagree).toUpperCase(), null)
                    .show();
        }

        HSGlobalNotificationCenter.addObserver(CustomThemeActivity.NOTIFICATION_CUSTOM_THEME_ACTIVITY_FINISH_SUCCESS, notificationObserver);

        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_INPUT_METHOD_CHANGED));

        //如果是第一次进入页面并且当前键盘没有被选为自己则弹框。
        if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            handler.sendEmptyMessageDelayed(HANDLER_SHOW_ACTIVE_DIALOG, 500);
        } else {
            handler.sendEmptyMessageDelayed(HANDLER_SHOW_UPDATE_DIALOG, 500);
        }

        onNewIntent(getIntent());
        Menu menu = mBottomBar.getMenu();
        setMenuItemIconDrawable(menu, R.id.customize_bottom_bar_keyboard, R.drawable.customize_keyboard_h);
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
            showTrialKeyboardDialog(KEYBOARD_ACTIVATION_FROM_SHOW_TRIAL_KEYBOARD_INTENT);
            getIntent().putExtra(EXTRA_SHOW_TRIAL_KEYBOARD, false);
        } else {
            String from = intent.getStringExtra("From");
            if (trialKeyboardDialog != null && trialKeyboardDialog.isShowing() && from != null && from.equals("Keyboard")) {
                Toast.makeText(this, "Already in " + getResources().getString(R.string.theme_nav_theme_store, getResources().getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
            }
        }

        //mark bug
        if (mLayoutWrapper != null) {
            mLayoutWrapper.show();
        }
        // keyboard中点击sticker panel加号，设定viewpager当前页为sticker home页
        Bundle extras = intent.getExtras();
        if (extras != null && extras.getInt(BUNDLE_KEY_HOME_INIT) == HOME_VIEWPAGER_STICKER_PAGE) {
            currentTabIndex = TAB_INDEX_KEYBOARD;
        }
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
        mContent.setChildSelected(currentTabIndex);

        if (isFromUsageAccessActivity) {
            isFromUsageAccessActivity = false;
        }

//        refreshApkUpdateViews();
        HSThemeNewTipController.getInstance().removeNewTip(HSThemeNewTipController.ThemeTipType.NEW_TIP_THEME);

        if (mLayoutWrapper != null) {
            mLayoutWrapper.show();
        }

        // Place here to get a right session id from appframework
        if (isResumeOnCreate) {
            showOpenAlertIfNeeded();
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

    private void resetBottomMenu() {
        Menu menu = mBottomBar.getMenu();
        setMenuItemIconDrawable(menu, R.id.customize_bottom_bar_wallpapers, R.drawable.customize_wallpaper);
        setMenuItemIconDrawable(menu, R.id.customize_bottom_bar_keyboard, R.drawable.customize_keyboard);
        setMenuItemIconDrawable(menu, R.id.customize_bottom_bar_call, R.drawable.customize_call);
        setMenuItemIconDrawable(menu, R.id.customize_bottom_bar_setting, R.drawable.customize_settings);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        currentTabIndex = ITEMS_INDEX_MAP.get(id);
        if (mViewIndex != currentTabIndex) {
            mViewIndex = currentTabIndex;
        }
        mContent.setChildSelected(currentTabIndex);
        // reset icon to origins
        resetBottomMenu();

        switch (item.getItemId()) {
            case R.id.customize_bottom_bar_wallpapers:
                HSAnalytics.logEvent("app_tab_bottom_clicked", "tabName", "wallpaper");
                item.setIcon(R.drawable.customize_wallpaper_h);
                break;
            case R.id.customize_bottom_bar_keyboard:
                HSAnalytics.logEvent("app_tab_bottom_clicked", "tabName", "keyboard");
                item.setIcon(R.drawable.customize_keyboard_h);
                break;
            case R.id.customize_bottom_bar_call:
                HSAnalytics.logEvent("app_tab_bottom_clicked", "tabName", "callflash");
                item.setIcon(R.drawable.customize_call_h);
                break;
            case R.id.customize_bottom_bar_setting:
                HSAnalytics.logEvent("app_tab_bottom_clicked", "tabName", "settings");
                item.setIcon(R.drawable.customize_settings_h);
                break;
        }
        return true;
    }

    private void setMenuItemIconDrawable(Menu menu, @IdRes int itemId, @DrawableRes int drawableId) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setIcon(drawableId);
        }
    }

    private void showTrialKeyboardDialog(final int activationCode) {
        if (HSInputMethodListManager.isMyInputMethodSelected()) {
            if (LockerSettings.isLockerEnableShowSatisfied() && !isFinishing()) {

                LockerEnableDialog.showLockerEnableDialog(ThemeHomeActivity.this, ThemeLockerBgUtil.getInstance().getThemeBgUrl(HSKeyboardThemeManager.getCurrentThemeName()),
                        getString(R.string.locker_enable_title_has_text),
                        "from",
                        (LockerEnableDialog.OnLockerBgLoadingListener) () -> {
                    if (trialKeyboardDialog == null) {
                        trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeHomeActivity.this).create();
                    }
                    if (activationCode == KEYBOARD_ACTIVATION_FROM_CUSTOM_THEME_FINISH) {
                        trialKeyboardDialog.show(false);
                    } else {
                        trialKeyboardDialog.show(true);
                    }
                });
            } else {
                if (trialKeyboardDialog == null) {
                    trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeHomeActivity.this).create();
                }
                if (activationCode == KEYBOARD_ACTIVATION_FROM_CUSTOM_THEME_FINISH) {
                    trialKeyboardDialog.show(false);
                } else {
                    trialKeyboardDialog.show(true);
                }
            }
        } else {
            Intent intent = new Intent(this, KeyboardActivationGuideActivity.class);
            startActivityForResult(intent, activationCode);
        }
    }

    @Override
    protected void onDestroy() {
        if (trialKeyboardDialog != null) {
            trialKeyboardDialog.dismiss();
            trialKeyboardDialog = null;
        }

        HSGlobalNotificationCenter.removeObserver(notificationObserver);
        unregisterReceiver(broadcastReceiver);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (KEYBOARD_ACTIVATION_FROM_SHOW_TRIAL_KEYBOARD_INTENT == requestCode || KEYBOARD_ACTIVATION_FROM_CUSTOM_THEME_FINISH == requestCode) {
            showTrialKeyboardDialog(requestCode);
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
        if (!HSPreferenceHelper.getDefault().getBoolean(SP_TREBLE_FUNCTION_ALERT_SHOWED, false) && ChargingConfigManager.getManager().shouldShowEnableChargingAlert(false)) {
            CustomDesignAlert multiFunctionDialog = new CustomDesignAlert(HSApplication.getContext());
            multiFunctionDialog.setEnablePrivacy(true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBrowsePrivacy();
                }
            });
            multiFunctionDialog.setTitle(getString(R.string.multi_function_alert_title));
            multiFunctionDialog.setMessage(getString(R.string.multi_function_alert_message));
            multiFunctionDialog.setImageResource(R.drawable.enable_tripple_alert_top_image);
            multiFunctionDialog.setCancelable(true);
            multiFunctionDialog.setPositiveButton(getString(R.string.enable), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HSAnalytics.logEvent("alert_multi_function_click", "size", "half_screen", "occasion", "open_app");
                    ChargingManagerUtil.enableCharging(false);
                    enableLocker();
                    CPSettings.setScreenFlashModuleEnabled(true);
                }
            });
            multiFunctionDialog.show();
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
            CustomDesignAlert dialog = new CustomDesignAlert(HSApplication.getContext());
            dialog.setTitle(getString(R.string.charging_alert_title));
            dialog.setMessage(getString(R.string.charging_alert_message));
            dialog.setImageResource(R.drawable.enable_charging_alert_top_image);
            dialog.setCancelable(true);
            dialog.setPositiveButton(getString(R.string.enable), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChargingManagerUtil.enableCharging(false);
                    HSAnalytics.logEvent("alert_charging_click", "size", "half_screen", "occasion", "open_app");
                }
            });
            dialog.show();
            HSAnalytics.logEvent("alert_charging_show", "size", "half_screen", "occasion", "open_app");
            ChargingConfigManager.getManager().increaseEnableAlertShowCount();
            return true;
        } else {
            return false;
        }
    }

    private boolean showScreenLockerDialog() {
        if (ScreenLockerConfigUtils.shouldShowScreenLockerAlert(true)) {
            CustomDesignAlert lockerDialog = new CustomDesignAlert(HSApplication.getContext());
            lockerDialog.setTitle(getString(R.string.locker_alert_title));
            lockerDialog.setMessage(getString(R.string.locker_alert_message));
            lockerDialog.setImageResource(R.drawable.enable_tripple_alert_top_image);//locker image
            lockerDialog.setCancelable(true);
            lockerDialog.setPositiveButton(getString(R.string.enable), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HSAnalytics.logEvent("alert_locker_click", "size", "half_screen", "occasion", "open_app");
                    enableLocker();
                }
            });
            lockerDialog.show();
            HSAnalytics.logEvent("alert_locker_show", "size", "half_screen", "occasion", "open_app");
            ScreenLockerConfigUtils.increaseEnableAlertShowCount();
            return true;
        } else {
            return false;
        }
    }

    private boolean showCallAssistantDialog() {
        if (CallAssistantConfigUtils.shouldShowCallAssistantAlert(true)) {
            CustomDesignAlert callAssistantDialog = new CustomDesignAlert(HSApplication.getContext());
            callAssistantDialog.setTitle(getString(R.string.call_assistant_alert_title));
            callAssistantDialog.setMessage(getString(R.string.call_assistant_alert_message));
            callAssistantDialog.setImageResource(R.drawable.enable_charging_alert_top_image);// call image
            callAssistantDialog.setCancelable(true);
            callAssistantDialog.setEnablePrivacy(true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBrowsePrivacy();
                }
            });
            callAssistantDialog.setPositiveButton(getString(R.string.enable), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HSAnalytics.logEvent("alert_call_assistant_click", "size", "half_screen", "occasion", "open_app");
                    CPSettings.setScreenFlashModuleEnabled(true);
                }
            });
            callAssistantDialog.show();
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

    private void dismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing() && !isFinishing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void updateClick() {
        handler.removeMessages(HANDLER_SHOW_UPDATE_DIALOG);
        checkAndShowApkUpdateAlert(true);
        handler.post(new Runnable() {
            @Override
            public void run() {
                restoreNavigationView();
            }
        });

    }
}
