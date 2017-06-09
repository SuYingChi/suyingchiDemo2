package com.mobipioneer.lockerkeyboard.app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.HSDeepLinkActivity;
import com.ihs.inputmethod.api.HSUIInputMethod;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.permission.HSPermissionsManager;
import com.ihs.inputmethod.api.permission.HSPermissionsUtil;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.utils.Constants;
import com.ihs.keyboardutils.alerts.KCAlert;
import com.mobipioneer.lockerkeyboard.utils.ActivityUtils;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;
import static android.view.View.GONE;
import static com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor.PREF_THEME_HOME_SHOWED;
import static com.mobipioneer.lockerkeyboard.utils.MasterConstants.GA_PARAM_ACTION_APP_APPLOCKER_CLICKED;
import static com.mobipioneer.lockerkeyboard.utils.MasterConstants.GA_PARAM_ACTION_APP_HOME_CHARGING_SELECTED;
import static com.mobipioneer.lockerkeyboard.utils.MasterConstants.GA_PARAM_ACTION_APP_HOME_FIRSTTIME_SHOWED;

public class MainActivity extends HSDeepLinkActivity {

    private boolean versionFilterForRecordEvent;
    private final static String INSTRUCTION_SCREEN_VIEWED = "Instruction_screen_viewed";
    private final static String APP_STEP_ONE_HINT_CLICKED = "app_step_one_hint_clicked";
    private final static String APP_STEP_ONE_HINT = "app_step_one_hint";

    private SharedPreferences mPrefs;


    public enum CurrentUIStyle {
        UISTYLE_STEP_ONE,
        UISTYLE_STEP_TWO,
        UISTYLE_STEP_THREE_NORMAL,
        UISTYLE_STEP_THREE_TEST,
    }

    static float ratio = 0.7f;
    static float move = 0.15f;

    View rootView;

    private TextView protocolText;
    private ImageView img_title;
    private View bt_step_one;
    private View bt_step_two;
    private View bt_step_one_content_view;
    private View bt_step_two_content_view;
    private TextView text_one;
    private TextView text_two;
    private View bt_settings;
    private View bt_languages;
    private View bt_lock;
    private ImageView img_enter_one;
    private ImageView img_enter_two;
    private ImageView img_choose_one;
    private ImageView img_choose_two;
    private EditText edit_text_test;
    private ToggleButton fastChargeToggle;
    private ImeSettingsContentObserver settingsContentObserver = new ImeSettingsContentObserver(new Handler());
    ;

    boolean isInStepOne;
    // is phone dev
    boolean isPhoneDev = true;
    boolean isChargingShowEver = false;
    private final static String CHARGING_HAS_SHOW_PRE_KEY = "charging_has_show_pre_key";
    private final static String HOME_HAS_SHOW_PRE_KEY = "home_has_show_pre_key";

    CurrentUIStyle style;

    /**
     * 需要激活的主题包的PackageName，当点击主题片包的Apply时会传入
     */
    private String needActiveThemePkName = null;

    private BroadcastReceiver imeChangeRecevier = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) {
                if (HSInputMethodListManager.isMyInputMethodSelected()) {
                    if (versionFilterForRecordEvent && !isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_TWO_ENABLED)) {

                        if (isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED)
                                && isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED)
                                && isEventRecorded(APP_STEP_ONE_HINT_CLICKED)
                                && isEventRecorded(APP_STEP_ONE_HINT)
                                && isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_TWO_CLICKED)) {
                            setEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_TWO_ENABLED);
                            HSGoogleAnalyticsUtils.getInstance().logAppEvent(Constants.GA_PARAM_ACTION_APP_STEP_TWO_ENABLED);
                        }
                    }


                    MainActivity.this.doSetpTwoFinishAnimation();
                    style = CurrentUIStyle.UISTYLE_STEP_THREE_TEST;
                    setHasChargingShow();
                }
            }
        }
    };

    private boolean isSettingChargingClicked;//是否点击过charging的设置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int screenMetrics = getResources().getInteger(com.ihs.inputmethod.R.integer.config_screen_metrics);
        if (screenMetrics == com.ihs.inputmethod.framework.Constants.SCREEN_METRICS_SMALL_TABLET || screenMetrics == com.ihs.inputmethod.framework.Constants.SCREEN_METRICS_LARGE_TABLET) {
            isPhoneDev = false;
        }
        setContentView(R.layout.activity_main);

        View iv_back = findViewById(R.id.iv_back);

        if (iv_back != null) {
            if (Build.VERSION.SDK_INT < 17 || !getResources().getBoolean(R.bool.show_main_back)) {
                iv_back.setVisibility(GONE);
            }
            iv_back.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        onNewIntent(getIntent());
        if (shouldShowThemeHome() || (HSInputMethodListManager.isMyInputMethodSelected())) {
            startThemeHomeActivity();
            return;
        }
        //main 出现过一次之后就不再出现

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);


        HSApplication.HSLaunchInfo firstLaunchInfo = HSApplication.getFirstLaunchInfo();
        versionFilterForRecordEvent = (firstLaunchInfo.appVersionCode >= HSApplication.getCurrentLaunchInfo().appVersionCode);

        if (versionFilterForRecordEvent && !isEventRecorded(INSTRUCTION_SCREEN_VIEWED)) {
            setEventRecorded(INSTRUCTION_SCREEN_VIEWED);
            HSGoogleAnalyticsUtils.getInstance().logAppEvent(INSTRUCTION_SCREEN_VIEWED);
        }


        rootView = this.findViewById(R.id.view_root);

        protocolText = (TextView) findViewById(R.id.privacy_policy_text);
        String serviceKeyText = getString(R.string.text_terms_of_service);
        String policyKeyText = getString(R.string.text_privacy_policy);
        String policyText = getResources().getString(R.string.privacy_policy, serviceKeyText, policyKeyText);
        SpannableString ss = new SpannableString(policyText);
        ss.setSpan(new URLSpan(HSConfig.optString("", "Application", "Policy", "TermsOfService")) {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.white_standard));
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//                ds.setUnderlineText(true);
            }
        }, policyText.indexOf(serviceKeyText), policyText.indexOf(serviceKeyText) + serviceKeyText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new URLSpan(HSConfig.optString("", "Application", "Policy", "PrivacyPolicy")) {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.white_standard));
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//                ds.setUnderlineText(true);
            }
        }, policyText.indexOf(policyKeyText), policyText.indexOf(policyKeyText) + policyKeyText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        protocolText.setText(ss);
        protocolText.setMovementMethod(LinkMovementMethod.getInstance());

        img_title = (ImageView) this.findViewById(R.id.view_img_title);
        bt_step_one = this.findViewById(R.id.bt_step_one);
        bt_step_two = this.findViewById(R.id.bt_step_two);
        bt_step_one_content_view = this.findViewById(R.id.bt_step_one_content_view);
        bt_step_two_content_view = this.findViewById(R.id.bt_step_two_content_view);
        text_one = (TextView) this.findViewById(R.id.text_one);
        text_two = (TextView) this.findViewById(R.id.text_two);
        img_enter_one = (ImageView) this.findViewById(R.id.view_enter_one);
        img_enter_two = (ImageView) this.findViewById(R.id.view_enter_two);
        img_choose_one = (ImageView) this.findViewById(R.id.view_choose_one);
        img_choose_two = (ImageView) this.findViewById(R.id.view_choose_two);
        fastChargeToggle = (ToggleButton) findViewById(R.id.fast_charge_toggle);
        isSettingChargingClicked = ChargingConfigManager.getManager().isUserSetChargingToggle();
        fastChargeToggle.setToggleSwitchListener(new ToggleButton.IToggleButtonSwitchListener() {
            @Override
            public void onToggleSwitch(boolean isSwitchOn) {
                if (!isSettingChargingClicked) {//记录用户设置过charing选项
                    isSettingChargingClicked = true;
                    ChargingConfigManager.getManager().setUserChangeChargingToggle();
                }

//                if (isSwitchOn) {
//                    ChargingManagerUtil.enableCharging(false);
//                } else {
//                    ChargingManagerUtil.disableCharging();
//                }
            }
        });

        logHomeShowEvent();

        bt_settings = this.findViewById(R.id.bt_settings);
        bt_languages = this.findViewById(R.id.bt_languages);
        bt_lock = this.findViewById(R.id.bt_lock);

        edit_text_test = (EditText) this.findViewById(R.id.edit_text_test);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
        registerReceiver(imeChangeRecevier, filter);

        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, sessionEventObserver);

        if (false && getResources().getBoolean(R.bool.isTablet)) {
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int button_width = (int) (width * 0.5);

            int step_button_width = (int) (button_width * 1.1);

            float ratio_button_guide_one = ((float) getResources().getDrawable(R.drawable.app_button_guide_one_bg).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_one_bg).getIntrinsicWidth());
            float ratio_button_guide_two = ((float) getResources().getDrawable(R.drawable.app_button_guide_two_bg).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_two_bg).getIntrinsicWidth());
            float ratio_img_enter = ((float) getResources().getDrawable(R.drawable.app_button_guide_enter).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_enter).getIntrinsicWidth());
            float ratio_img_choose = ((float) getResources().getDrawable(R.drawable.app_button_guide_choose).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_choose).getIntrinsicWidth());

            RelativeLayout.LayoutParams step_one_relativeParamsInParent = new RelativeLayout.LayoutParams(step_button_width, (int) (step_button_width * ratio_button_guide_one));
            bt_step_one.setLayoutParams(step_one_relativeParamsInParent);

            RelativeLayout.LayoutParams step_one_relativeParamsInParent2 = new RelativeLayout.LayoutParams(step_button_width, (int) (step_button_width * ratio_button_guide_two));
            step_one_relativeParamsInParent2.topMargin = (int) (step_button_width * 0.07);
            bt_step_two.setLayoutParams(step_one_relativeParamsInParent2);

            ViewGroup.LayoutParams fastChargeToggleLayoutParams = fastChargeToggle.getLayoutParams();
            fastChargeToggleLayoutParams.width = step_button_width;

            RelativeLayout.LayoutParams step_one_relativeParams = new RelativeLayout.LayoutParams(-1, -2);
            step_one_relativeParams.topMargin = (int) (step_button_width * 0.070);
            bt_step_one_content_view.setLayoutParams(step_one_relativeParams);

            RelativeLayout.LayoutParams step_one_relativeParams2 = new RelativeLayout.LayoutParams(-2, -2);
            step_one_relativeParams2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            step_one_relativeParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            step_one_relativeParams2.leftMargin = (int) (step_button_width * 0.17);
            text_one.setLayoutParams(step_one_relativeParams2);

            RelativeLayout.LayoutParams step_one_relativeParams3 = new RelativeLayout.LayoutParams((int) (step_button_width * 0.035),
                    (int) (step_button_width * 0.035 * ratio_img_enter));
            step_one_relativeParams3.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            step_one_relativeParams3.leftMargin = (int) (step_button_width * 0.9);
            img_enter_one.setLayoutParams(step_one_relativeParams3);

            RelativeLayout.LayoutParams step_one_relativeParams4 = new RelativeLayout.LayoutParams((int) (step_button_width * 0.094),
                    (int) (step_button_width * 0.094 * ratio_img_choose));
            step_one_relativeParams4.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            step_one_relativeParams4.leftMargin = (int) (step_button_width * 0.87);
            img_choose_one.setLayoutParams(step_one_relativeParams4);

            RelativeLayout.LayoutParams step_two_relativeParams = new RelativeLayout.LayoutParams(-1, -2);
            step_two_relativeParams.topMargin = (int) (step_button_width * 0.070);
            bt_step_two_content_view.setLayoutParams(step_two_relativeParams);

            RelativeLayout.LayoutParams step_two_relativeParams2 = new RelativeLayout.LayoutParams(-2, -2);
            step_two_relativeParams2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            step_two_relativeParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            step_two_relativeParams2.leftMargin = (int) (step_button_width * 0.17);
            text_two.setLayoutParams(step_two_relativeParams2);

            RelativeLayout.LayoutParams step_two_relativeParams3 = new RelativeLayout.LayoutParams((int) (step_button_width * 0.035),
                    (int) (step_button_width * 0.035 * ratio_img_enter));
            step_two_relativeParams3.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            step_two_relativeParams3.leftMargin = (int) (step_button_width * 0.9);
            img_enter_two.setLayoutParams(step_two_relativeParams3);

            RelativeLayout.LayoutParams step_two_relativeParams4 = new RelativeLayout.LayoutParams((int) (step_button_width * 0.094),
                    (int) (step_button_width * 0.094 * ratio_img_choose));
            step_two_relativeParams4.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            step_two_relativeParams4.leftMargin = (int) (step_button_width * 0.87);
            img_choose_two.setLayoutParams(step_two_relativeParams4);
        } else {
            fastChargeToggle.post(new Runnable() {
                @Override
                public void run() {
                    fastChargeToggle.getLayoutParams().width = bt_step_one.getWidth();
                }
            });
        }

        bt_step_one.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboardEnableDialog();
                if (versionFilterForRecordEvent && !isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED)) {
                    setEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED);
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED);
                }
            }
        });

        bt_step_two.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager m = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                m.showInputMethodPicker();
                Toast toast = Toast.makeText(MainActivity.this, R.string.toast_select_keyboard, Toast.LENGTH_LONG);
                toast.show();
                //                MainActivity.this.doSetpTwoFinishAnimation();


                if (versionFilterForRecordEvent && !isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_TWO_CLICKED)) {


                    //记第二步点击的时候，如果还没有记第一步点击或第一步enable, 就补上

                    if (!isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED)) {
                        return;
                    }

                    if (!isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED)) {
                        return;
                    }

                    if (!isEventRecorded(APP_STEP_ONE_HINT_CLICKED) || !isEventRecorded(APP_STEP_ONE_HINT)) {
                        return;
                    }

                    setEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_TWO_CLICKED);
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent(Constants.GA_PARAM_ACTION_APP_STEP_TWO_CLICKED);
                }
            }
        });

        bt_settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.showMoreSettingsActivity();
                HSGoogleAnalyticsUtils.getInstance().logAppEvent(Constants.GA_PARAM_ACTION_APP_SETTINGS_CLICKED);
                HSAnalytics.logEvent(Constants.GA_PARAM_ACTION_APP_SETTINGS_CLICKED);
            }
        });

        bt_languages.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSUIInputMethod.launchMoreLanguageActivity();
                HSGoogleAnalyticsUtils.getInstance().logAppEvent(Constants.GA_PARAM_ACTION_APP_LANGUAGES_CLICKED);
                HSAnalytics.logEvent(Constants.GA_PARAM_ACTION_APP_LANGUAGES_CLICKED);
            }
        });

        bt_lock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                AppLockMgr.startLocker(getApplicationContext());
                HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_PARAM_ACTION_APP_APPLOCKER_CLICKED);
                HSAnalytics.logEvent(GA_PARAM_ACTION_APP_APPLOCKER_CLICKED);
            }
        });

        if (getIntent().getBooleanExtra("isInStepOne", false)) {
            isInStepOne = true;
        }

        this.refreshUIState();

        if (!HSPermissionsUtil.checkAllPermissionsGranted(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            HSPermissionsManager.getInstance().requestPermissions(null, null, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * 记录首页首次出现事件
     */
    private void logHomeShowEvent() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        boolean homeShowed = sp.getBoolean(HOME_HAS_SHOW_PRE_KEY, false);
        if (!homeShowed) {
            HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_PARAM_ACTION_APP_HOME_FIRSTTIME_SHOWED);
            HSAnalytics.logEvent(GA_PARAM_ACTION_APP_HOME_FIRSTTIME_SHOWED);
            sp.edit().putBoolean(HOME_HAS_SHOW_PRE_KEY, true).commit();
        }
    }

    /**
     * 设置是否显示charging的勾选框
     */
    private void jdugeShowChargingSelectedBox() {
        //未显示过Charging提示才能出现
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        isChargingShowEver = sp.getBoolean(CHARGING_HAS_SHOW_PRE_KEY, false);
//        if (ChargingConfigManager.getManager().enableChargingFunction() && !isChargingShowEver && style != CurrentUIStyle.UISTYLE_STEP_THREE_NORMAL) {
        fastChargeToggle.setVisibility(View.INVISIBLE);
        // set charge status
        fastChargeToggle.refreshSwitchState();
//        } else {
//            fastChargeToggle.setVisibility(View.INVISIBLE);
//        }
    }

    /**
     * Show keyboard enabling dialog
     */
    private void showKeyboardEnableDialog() {
        new KCAlert.Builder(this)
                .setTitle(getString(R.string.toast_enable_keyboard))
                .setMessage(getString(R.string.alert_attention_messenger))
                .setTopImageResource(R.drawable.enable_keyboard_alert_top_bg)
                .setPositiveButton(getString(R.string.got_it), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (versionFilterForRecordEvent && !isEventRecorded(APP_STEP_ONE_HINT_CLICKED)) {
                            setEventRecorded(APP_STEP_ONE_HINT_CLICKED);
                            HSGoogleAnalyticsUtils.getInstance().logAppEvent(APP_STEP_ONE_HINT_CLICKED);
                        }

                        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                        intent.setFlags(FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        isInStepOne = true;

                        ImageView imageCodeProject = new ImageView(getApplicationContext());
                        imageCodeProject.setBackgroundResource(com.ihs.inputmethod.uimodules.R.drawable.toast_enable_rain);
                        final KeyboardActivationProcessor.CustomViewDialog customViewDialog = new KeyboardActivationProcessor.CustomViewDialog(imageCodeProject, 3000, Gravity.BOTTOM, 0, HSDisplayUtils.dip2px(20));
                        imageCodeProject.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                customViewDialog.show();
                            }
                        }, 500);
                    }
                })
                .show();

        if (versionFilterForRecordEvent && !isEventRecorded(APP_STEP_ONE_HINT)) {
            setEventRecorded(APP_STEP_ONE_HINT);
            HSGoogleAnalyticsUtils.getInstance().logAppEvent(APP_STEP_ONE_HINT);
        }
    }


    private void startThemeHomeActivity() {
        Intent startThemeHomeIntent = getIntent();
        if (startThemeHomeIntent == null) {
            startThemeHomeIntent = new Intent();
        }
        startThemeHomeIntent.setClass(MainActivity.this, ThemeHomeActivity.class);

        if (!TextUtils.isEmpty(needActiveThemePkName)) {
            HSKeyboardThemeManager.setDownloadedTheme(needActiveThemePkName);
            startThemeHomeIntent.putExtra(ThemeHomeActivity.INTENT_KEY_SHOW_TRIAL_KEYBOARD, true);
            needActiveThemePkName = null;
        }
        startActivity(startThemeHomeIntent);
        finish();
    }


    public class ImeSettingsContentObserver extends ContentObserver {

        public ImeSettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (HSInputMethodListManager.isMyInputMethodEnabled()) {
                Intent i = new Intent(MainActivity.this, MainActivity.class);
                i.putExtra("isInStepOne", true);
                startActivity(i);
                try {
                    if (settingsContentObserver != null) {
                        getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
                        HSLog.d("unregister settings content observer");
                    }
                } catch (IllegalArgumentException ex) {
                    HSLog.e("content observer not registered yet");
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //HSAlertMgr.delayRateAlert();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //HSAlertMgr.showRateAlert();
        if (edit_text_test != null) {
            edit_text_test.requestFocus();
//            if (HSInputMethod.getInputService() != null) {
//                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            edit_text_test.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(edit_text_test, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 300);
//            }
        }

        jdugeShowChargingSelectedBox();

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!HSInputMethodListManager.isMyInputMethodEnabled()) {
                    getApplicationContext().getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_INPUT_METHODS), false,
                            settingsContentObserver);
                    refreshUIState();
                } else {
                    if (!HSInputMethodListManager.isMyInputMethodSelected()) {
                        if (isInStepOne) {
                            doSetpOneFinishAnimation();
                            style = CurrentUIStyle.UISTYLE_STEP_TWO;

                            if (versionFilterForRecordEvent && !isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED)) {
                                setEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED);
                                HSGoogleAnalyticsUtils.getInstance().logAppEvent(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED);
                            }
                        } else {
                            refreshUIState();
                        }
                    } else {
                        refreshUIState();

                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(Constants.GA_PARAM_ACTION_APP_STEP_TWO_ENABLED);
                        HSAnalytics.logEvent(Constants.GA_PARAM_ACTION_APP_STEP_TWO_ENABLED);
                    }
                    try {
                        if (settingsContentObserver != null)
                            getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
                    } catch (IllegalArgumentException ex) {
                        HSLog.e("content observer not registered yet");
                    }
                }
                isInStepOne = false;
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if (data != null) {
            String pkName = data.getQueryParameter("pkName");
            if (!TextUtils.isEmpty(pkName)) {
                HSLog.d("jx,收到激活主题的请求，包名:" + pkName);
                needActiveThemePkName = pkName;

                if (shouldShowThemeHome() || (HSInputMethodListManager.isMyInputMethodSelected())) {
                    startThemeHomeActivity();
                }
            }
        }
        if (getIntent().getBooleanExtra("isInStepOne", false)) {
            isInStepOne = true;
        }

        if (getIntent().getBooleanExtra("skip", false) && !isInStepOne && !HSInputMethodListManager.isMyInputMethodEnabled()) {

            Intent settingIntent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            settingIntent.setFlags(FLAG_ACTIVITY_NO_HISTORY);
            startActivity(settingIntent);
            isInStepOne = true;

            ImageView imageCodeProject = new ImageView(getApplicationContext());
            imageCodeProject.setBackgroundResource(com.ihs.inputmethod.uimodules.R.drawable.toast_enable_rain);
            final KeyboardActivationProcessor.CustomViewDialog customViewDialog = new KeyboardActivationProcessor.CustomViewDialog(imageCodeProject, 3000, Gravity.BOTTOM, 0, HSDisplayUtils.dip2px(20));
            imageCodeProject.postDelayed(new Runnable() {
                @Override
                public void run() {
                    customViewDialog.show();
                }
            }, 500);

        }

    }

    private INotificationObserver sessionEventObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                HSLog.d("jx onSession end");
                setHasChargingShow();
            }
        }
    };

    @Override
    protected void onStop() {

        super.onStop();
        if (edit_text_test != null) {
            edit_text_test.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit_text_test.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        HSLog.d("jx,MainActivity onstop hide input");
        HSInputMethod.hideWindow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSPreferenceHelper.getDefault().putBoolean(PREF_THEME_HOME_SHOWED, true);


        needActiveThemePkName = null;

        try {
            if (settingsContentObserver != null) {
                getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
            }
            unregisterReceiver(imeChangeRecevier);
        } catch (IllegalArgumentException ex) {
            HSLog.e("content observer not registered yet");
        }

        HSGlobalNotificationCenter.removeObserver(HSNotificationConstant.HS_SESSION_END, sessionEventObserver);
    }

    private void scaleTitleImage() {
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams((int) (width * ratio), (int) (width * ratio));
        relativeParams.topMargin = -(int) (width * move);
        relativeParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        img_title.setLayoutParams(relativeParams);
    }

    private void restoreTitleImage() {

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        if (getResources().getBoolean(R.bool.isTablet)) {
            width = (int) (width * 0.85);
        }
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(width, width);
        relativeParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        img_title.setLayoutParams(relativeParams);
    }

    private void refreshUIState() {
        if (!HSInputMethodListManager.isMyInputMethodEnabled()) {
            if (style == CurrentUIStyle.UISTYLE_STEP_ONE)
                return;

            restoreTitleImage();

            edit_text_test.setAlpha(0);
            edit_text_test.setFocusable(false);
            edit_text_test.setFocusableInTouchMode(false);

            bt_step_one.setVisibility(View.VISIBLE);
            bt_step_two.setVisibility(View.VISIBLE);
            if (!isChargingShowEver) {
                fastChargeToggle.setVisibility(View.VISIBLE);
            }
            bt_settings.setVisibility(View.INVISIBLE);
            bt_languages.setVisibility(View.INVISIBLE);
            bt_step_one.setClickable(true);
            bt_step_one.setAlpha(1.0f);
            bt_step_two.setClickable(false);
            bt_step_two.setAlpha(0.4f);
            bt_settings.setAlpha(0);
            bt_languages.setAlpha(0);
            if (isPhoneDev) {
                bt_lock.setVisibility(View.INVISIBLE);
                bt_lock.setAlpha(0);
            }

            img_enter_one.setAlpha(255);
            img_enter_two.setAlpha(255);
            img_choose_one.setAlpha(0);
            img_choose_two.setAlpha(0);

            rootView.setBackgroundColor(Color.parseColor("#2a9de8"));

            style = CurrentUIStyle.UISTYLE_STEP_ONE;
        } else if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            if (style == CurrentUIStyle.UISTYLE_STEP_TWO)
                return;

            restoreTitleImage();

            edit_text_test.setAlpha(0);
            edit_text_test.setFocusable(false);
            edit_text_test.setFocusableInTouchMode(false);

            bt_step_one.setVisibility(View.VISIBLE);
            bt_step_two.setVisibility(View.VISIBLE);
            if (!isChargingShowEver) {
                fastChargeToggle.setVisibility(View.VISIBLE);
            }
            bt_settings.setVisibility(View.INVISIBLE);
            bt_languages.setVisibility(View.INVISIBLE);

            bt_step_one.setClickable(false);
            if (isInStepOne) {
                bt_step_one.setAlpha(1.0f);
                bt_step_two.setClickable(false);
                bt_step_two.setAlpha(0.4f);
            } else {
                bt_step_one.setAlpha(0.4f);
                bt_step_two.setClickable(true);
                bt_step_two.setAlpha(1.0f);
            }
            bt_settings.setAlpha(0);
            bt_languages.setAlpha(0);
            if (isPhoneDev) {
                bt_lock.setVisibility(View.INVISIBLE);
                bt_lock.setAlpha(0);
            }

            img_enter_one.setAlpha(0);
            img_enter_two.setAlpha(255);
            img_choose_one.setAlpha(255);
            img_choose_two.setAlpha(0);

            rootView.setBackgroundColor(Color.parseColor("#2a9de8"));

            style = CurrentUIStyle.UISTYLE_STEP_TWO;

        } else {
            startThemeHomeActivity();

            HSLog.d("edit_text_test test");
            edit_text_test.setAlpha(1);
            edit_text_test.setFocusable(true);
            edit_text_test.setFocusableInTouchMode(true);
            edit_text_test.requestFocus();
            //             getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(edit_text_test, InputMethodManager.SHOW_IMPLICIT);

            if (style == CurrentUIStyle.UISTYLE_STEP_THREE_NORMAL || style == CurrentUIStyle.UISTYLE_STEP_THREE_TEST)
                return;
            scaleTitleImage();
            bt_step_one.setVisibility(GONE);
            bt_step_two.setVisibility(GONE);
//            fastChargeToggle.setVisibility(GONE);
            bt_settings.setVisibility(View.VISIBLE);
            bt_languages.setVisibility(View.VISIBLE);

            rootView.setBackgroundColor(Color.parseColor("#57bbfc"));
            bt_settings.setAlpha(1);
            bt_languages.setAlpha(1);
            if (isPhoneDev) {
                bt_lock.setVisibility(View.VISIBLE);
                bt_lock.setAlpha(1);
            }

            style = CurrentUIStyle.UISTYLE_STEP_THREE_NORMAL;
        }
    }

    private void doScaleAnimation() {
        scaleTitleImage();

        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, move,
                Animation.RELATIVE_TO_SELF, 0.0f);
        animationSet.addAnimation(translateAnimation);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.85f / ratio, 1.0f, 0.85f / ratio, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
        animationSet.addAnimation(scaleAnimation);

        animationSet.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                MainActivity.this.doAppearAnimation();
            }
        });
        animationSet.setDuration(500);
        this.img_title.startAnimation(animationSet);
    }

    private void doHideAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bt_step_one.setVisibility(GONE);
                bt_step_two.setVisibility(GONE);
//                fastChargeToggle.setVisibility(GONE);
                MainActivity.this.doScaleAnimation();
                rootView.setBackgroundColor(Color.parseColor("#57bbfc"));
            }
        });
        alphaAnimation.setDuration(500);
        this.bt_step_one.startAnimation(alphaAnimation);
        this.bt_step_two.startAnimation(alphaAnimation);
    }

    private void doAppearAnimation() {
        bt_settings.setAlpha(1);
        bt_languages.setAlpha(1);
        bt_settings.setVisibility(View.VISIBLE);
        bt_languages.setVisibility(View.VISIBLE);
        if (isPhoneDev) {
            bt_lock.setAlpha(1);
            bt_lock.setVisibility(View.VISIBLE);
        }
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //                edit_text_test.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                edit_text_test.setAlpha(1);
                edit_text_test.setFocusable(true);
                edit_text_test.setFocusableInTouchMode(true);
                edit_text_test.requestFocus();
                ((InputMethodManager) edit_text_test.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(edit_text_test, 0);
            }
        });
        alphaAnimation.setDuration(500);
        this.bt_settings.startAnimation(alphaAnimation);
        this.bt_languages.startAnimation(alphaAnimation);
        if (isPhoneDev) {
            this.bt_lock.startAnimation(alphaAnimation);
        }
    }

    private void doSetpOneFinishAnimation() {
        bt_step_one.setClickable(false);
        //        bt_step_two.setClickable(true);
        //        bt_step_two.setAlpha(1);
        //        bt_step_one.setAlpha(0.4f);

        img_enter_one.setAlpha(0);
        img_choose_one.setAlpha(255);
        AnimationSet animationSet = new AnimationSet(true);
        //        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        //        alphaAnimation.setDuration(500);
        //        animationSet.addAnimation(alphaAnimation);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0.5f, 1.5f, 0.5f, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                MainActivity.this.doSetpOneFinishAnimation2();
            }
        });
        img_choose_one.startAnimation(animationSet);
    }

    private void doSetpOneFinishAnimation2() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1.0f, 1.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doSetpOneFinishAnimation3();
            }
        });
        img_choose_one.startAnimation(scaleAnimation);
    }

    private void doSetpOneFinishAnimation3() {

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.4f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doSetpOneFinishAnimation4();
            }
        });
        bt_step_one.startAnimation(alphaAnimation);
    }

    private void doSetpOneFinishAnimation4() {
        bt_step_two.setAlpha(1.0f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.4f, 1.0f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bt_step_two.setClickable(true);
            }
        });
        bt_step_two.startAnimation(alphaAnimation);
    }

    private void doSetpTwoFinishAnimation() {
        bt_step_one.setClickable(false);
        bt_step_two.setClickable(false);
        bt_step_one.setAlpha(0.4f);

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AnimationSet animationSet = new AnimationSet(true);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                alphaAnimation.setDuration(500);
                animationSet.addAnimation(alphaAnimation);

                ScaleAnimation scaleAnimation = new ScaleAnimation(0.5f, 1.5f, 0.5f, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(500);
                animationSet.addAnimation(scaleAnimation);
                animationSet.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        img_enter_two.setAlpha(0);
                        img_choose_two.setAlpha(255);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        MainActivity.this.doSetpTwoFinishAnimation2();
                    }
                });
                img_choose_two.startAnimation(animationSet);
            }
        });
    }

    private void doSetpTwoFinishAnimation2() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1.0f, 1.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                MainActivity.this.doHideAnimation();
                startThemeHomeActivity();
            }
        });
        img_choose_two.startAnimation(scaleAnimation);
    }

    /**
     * 设置charging已经显示过
     */
    private void setHasChargingShow() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        if (!sp.getBoolean(CHARGING_HAS_SHOW_PRE_KEY, false)) {
            sp.edit().putBoolean(CHARGING_HAS_SHOW_PRE_KEY, true).commit();

            boolean chargingScreenEnabled = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getBoolean(getString(R.string.config_charge_switchpreference_key), false);
            String isSelected = chargingScreenEnabled ? "true" : "false";
            HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_PARAM_ACTION_APP_HOME_CHARGING_SELECTED, isSelected);
            HSAnalytics.logEvent(GA_PARAM_ACTION_APP_HOME_CHARGING_SELECTED, "selected", isSelected);
        }
    }


    private boolean isEventRecorded(String pref_name) {
        return mPrefs.getBoolean(pref_name, false);
    }

    private void setEventRecorded(String pref_name) {
        mPrefs.edit().putBoolean(pref_name, true).apply();
    }

    private boolean shouldShowThemeHome() {
        return HSPreferenceHelper.getDefault().getBoolean(PREF_THEME_HOME_SHOWED, false);
    }
}
