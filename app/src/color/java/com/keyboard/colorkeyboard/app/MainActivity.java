package com.keyboard.colorkeyboard.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
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
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.inputmethod.api.HSDeepLinkActivity;
import com.ihs.inputmethod.api.HSUIInputMethod;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.ihs.inputmethod.uimodules.widget.CustomDesignAlert;
import com.ihs.keyboardutils.utils.KCFeatureRestrictionConfig;
import com.keyboard.colorkeyboard.utils.Constants;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;
import static com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor.PREF_THEME_HOME_SHOWED;


public class MainActivity extends HSDeepLinkActivity {


    private final static String INSTRUCTION_SCREEN_VIEWED = "Instruction_screen_viewed";
    private final static String APP_STEP_ONE_HINT_CLICKED = "app_step_one_hint_clicked";
    private final static String APP_STEP_ONE_HINT = "app_step_one_hint";

    private final static float BUTTON_BACKGROUND_OPACITY_DISABLED = 0.7f;

    private final static float SCALE_MIN = 0.5f;
    private final static float SCALE_MAX = 1.5f;

    private boolean versionFilterForRecordEvent;

    public enum CurrentUIStyle {
        UISTYLE_STEP_ONE,
        UISTYLE_STEP_TWO,
        UISTYLE_STEP_THREE_TEST,
    }

    private View rootView;
    private SharedPreferences mPrefs;
    private View bt_step_one;
    private View bt_step_two;
    private RelativeLayout accessibilityButtonContainer;
    private TextView protocolText;
    private TextView bt_design_theme;
    private LinearLayout settings_languages_layout;
    private TextView bt_settings;
    private TextView bt_languages;
    private ImageView img_enter_one;
    private ImageView img_enter_two;
    private ImageView img_choose_one;
    private ImageView img_choose_two;
    private EditText edit_text_test;
    private ImeSettingsContentObserver settingsContentObserver = new ImeSettingsContentObserver(new Handler());
    private LottieAnimationView flashLottieAnimationView;
    private boolean isFlashLottieAnimationPlayed;

    private static final int TYPE_MANUAL = 0;
    private static final int TYPE_AUTO = 1;
    private int mode = TYPE_MANUAL; //根据Accessibility判断按钮内容

    private boolean isInStepOne;
    private boolean clickStepOne;
    /**
     * 需要激活的主题包的PackageName，当点击主题片包的Apply时会传入
     */
    private String needActiveThemePkName = null;

    private CurrentUIStyle style;

    private Handler handler = new Handler();

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
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onNewIntent(getIntent());

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        HSApplication.HSLaunchInfo firstLaunchInfo = HSApplication.getFirstLaunchInfo();
        versionFilterForRecordEvent = (firstLaunchInfo.appVersionCode >= HSApplication.getCurrentLaunchInfo().appVersionCode);

        if (versionFilterForRecordEvent && !isEventRecorded(INSTRUCTION_SCREEN_VIEWED)) {
            setEventRecorded(INSTRUCTION_SCREEN_VIEWED);
            HSGoogleAnalyticsUtils.getInstance().logAppEvent(INSTRUCTION_SCREEN_VIEWED);
        }
        rootView = this.findViewById(R.id.view_root);

        WindowManager wm = this.getWindowManager();
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        final int screenHeight = size.y;

        flashLottieAnimationView = (LottieAnimationView) this.findViewById(R.id.flash_lottie_animation);
        LottieComposition.Factory.fromAssetFileName(HSApplication.getContext(), "flash_animation.json", new OnCompositionLoadedListener() {
            @Override
            public void onCompositionLoaded(LottieComposition lottieComposition) {
                flashLottieAnimationView.setComposition(lottieComposition);
                flashLottieAnimationView.setProgress(0f);
            }
        });
        flashLottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                isFlashLottieAnimationPlayed = true;
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (false) {
                    startThemeHomeActivity();
                } else {
                    // 开始渐变动画
                    if (isAccessibilityEnable()) {
                        playAccessibilityButtonShowAnimation();
                    } else {
                        playManualButtonShowAnimation();
                    }
                }
                HSPreferenceHelper.getDefault().putBoolean(PREF_THEME_HOME_SHOWED, true);
            }
        });
        img_enter_one = (ImageView) this.findViewById(R.id.view_enter_one);
        img_enter_two = (ImageView) this.findViewById(R.id.view_enter_two);
        img_choose_one = (ImageView) this.findViewById(R.id.view_choose_one);
        img_choose_two = (ImageView) this.findViewById(R.id.view_choose_two);
        bt_settings = (TextView) this.findViewById(R.id.bt_settings);
        bt_languages = (TextView) this.findViewById(R.id.bt_languages);
        edit_text_test = (EditText) this.findViewById(R.id.edit_text_test);

        bt_design_theme = (TextView) this.findViewById(R.id.bt_design_theme);
        bt_design_theme.setBackgroundDrawable(HSDrawableUtils.getDimmedForegroundDrawable(BitmapFactory.decodeResource(HSApplication.getContext().getResources(), R.drawable.entrance_customize_button)));
        float density = getResources().getDisplayMetrics().density;
        bt_design_theme.setPadding((int) density * 20, (int) density * 10, (int) density * 20, (int) density * 10);
        LinearLayout.LayoutParams designThemeLayoutParam = (LinearLayout.LayoutParams) bt_design_theme.getLayoutParams();
        designThemeLayoutParam.topMargin = (int) (screenHeight * 0.09);

        settings_languages_layout = (LinearLayout) this.findViewById(R.id.settings_languages_layout);
        LinearLayout.LayoutParams settings_languages_layoutLayoutParams = (LinearLayout.LayoutParams) settings_languages_layout.getLayoutParams();
        settings_languages_layoutLayoutParams.topMargin = (int) (screenHeight * 0.03646);

        protocolText = (TextView) findViewById(R.id.privacy_policy_text);
        String serviceKeyText = getString(R.string.text_terms_of_service);
        String policyKeyText = getString(R.string.text_privacy_policy);
        String policyText = getResources().getString(R.string.keyboard_guide_privacy_policy, serviceKeyText, policyKeyText);
        SpannableString ss = new SpannableString(policyText);
        ss.setSpan(new URLSpan(HSConfig.optString("", "Application", "Policy", "TermsOfService")) {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(Color.BLACK);
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//                ds.setUnderlineText(true);
            }
        }, policyText.indexOf(serviceKeyText), policyText.indexOf(serviceKeyText) + serviceKeyText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new URLSpan(HSConfig.optString("", "Application", "Policy", "PrivacyPolicy")) {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(Color.BLACK);
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//                ds.setUnderlineText(true);
            }
        }, policyText.indexOf(policyKeyText), policyText.indexOf(policyKeyText) + policyKeyText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        protocolText.setText(ss);
        protocolText.setMovementMethod(LinkMovementMethod.getInstance());

        bt_step_one = this.findViewById(R.id.bt_step_one);
        bt_step_two = this.findViewById(R.id.bt_step_two);
        accessibilityButtonContainer = (RelativeLayout) findViewById(R.id.accessibility_button_container);
        bt_step_one.setBackgroundDrawable(RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(getResources().getColor(R.color.guide_bg_normal_color), getResources().getColor(R.color.guide_bg_disable_color),
                getResources().getDimension(R.dimen.guide_bg_radius)));
        bt_step_two.setBackgroundDrawable(RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(getResources().getColor(R.color.guide_bg_normal_color), getResources().getColor(R.color.guide_bg_disable_color),
                getResources().getDimension(R.dimen.guide_bg_radius)));
        accessibilityButtonContainer.setBackgroundDrawable(RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(getResources().getColor(R.color.guide_bg_normal_color), getResources().getColor(R.color.guide_bg_disable_color),
                getResources().getDimension(R.dimen.guide_bg_radius)));
        RelativeLayout.LayoutParams stepOneLayoutParams = (RelativeLayout.LayoutParams) bt_step_one.getLayoutParams();
        RelativeLayout.LayoutParams stepTwoLayoutParams = (RelativeLayout.LayoutParams) bt_step_two.getLayoutParams();
        RelativeLayout.LayoutParams accessibilityLayoutParams = (RelativeLayout.LayoutParams) accessibilityButtonContainer.getLayoutParams();

        if (getResources().getBoolean(R.bool.isTablet)) {
            stepOneLayoutParams.width = (int) (screenWidth * 0.45f);
            stepTwoLayoutParams.width = (int) (screenWidth * 0.45f);
            accessibilityLayoutParams.width = (int) (screenWidth * 0.45f);

            final float ratio_button_guide_settings = ((float) getResources().getDrawable(R.drawable.entrance_customize_button).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.entrance_customize_button).getIntrinsicWidth());

            bt_design_theme.post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout.LayoutParams designThemeLayouParam = (LinearLayout.LayoutParams) bt_design_theme.getLayoutParams();
                    designThemeLayouParam.height = (int) (bt_design_theme.getMeasuredWidth() * ratio_button_guide_settings);
                }
            });

            Paint p1 = new Paint();
            p1.setTextSize(getResources().getDimension(R.dimen.main_logo_title_textsize));
            Rect result = new Rect();
            p1.getTextBounds("RainBowKey", 0, "RainBowKey".length(), result);
        } else {
            stepOneLayoutParams.width = (int) (screenWidth * 0.75f);
            stepTwoLayoutParams.width = (int) (screenWidth * 0.75f);
            accessibilityLayoutParams.width = (int) (screenWidth * 0.75f);
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

        accessibilityButtonContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        bt_design_theme.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(MainActivity.this,"go to custom theme",Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(MainActivity.this,CustomThemeActivity.class));


//                IAPManager.getManager().startCustomThemeActivityIfSlotAvaiableFromActivity(MainActivity.this,null);

                HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_customize_entry_clicked");
            }
        });

        bt_settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSUIInputMethod.launchMoreLanguageActivity();
                HSGoogleAnalyticsUtils.getInstance().logAppEvent(Constants.GA_PARAM_ACTION_APP_SETTINGS_CLICKED);
            }
        });

        bt_languages.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSUIInputMethod.launchSettingsActivity();
                HSGoogleAnalyticsUtils.getInstance().logAppEvent(Constants.GA_PARAM_ACTION_APP_LANGUAGES_CLICKED);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
        registerReceiver(imeChangeRecevier, filter);

        this.refreshUIState();
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

            clickStepOne = true;

        }
    }

    private boolean isAccessibilityEnable() {
        boolean isAccessibilityEnabledInConfig = HSConfig.optBoolean(false, "Application", "AutoSetKeyEnable") && !KCFeatureRestrictionConfig.isFeatureRestricted("AccessibilityToEnableKeyboard");
        boolean isHSAccessibilityServiceAvailable = HSAccessibilityService.isAvailable();
        return isAccessibilityEnabledInConfig && !isHSAccessibilityServiceAvailable;
    }

    /**
     * Show keyboard enabling dialog
     */
    private void showKeyboardEnableDialog() {
        CustomDesignAlert dialog = new CustomDesignAlert(this);
        dialog.setTitle(getString(R.string.toast_enable_keyboard));
        dialog.setMessage(getString(R.string.alert_attention_messenger));
        dialog.setImageResource(R.drawable.enable_keyboard_alert_top_bg);
        dialog.setPositiveButton(getString(R.string.got_it), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (versionFilterForRecordEvent && !isEventRecorded(APP_STEP_ONE_HINT_CLICKED)) {
                    setEventRecorded(APP_STEP_ONE_HINT_CLICKED);
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent(APP_STEP_ONE_HINT_CLICKED);
                }

                ImageView imageCodeProject = new ImageView(getApplicationContext());
                imageCodeProject.setBackgroundResource(com.ihs.inputmethod.uimodules.R.drawable.toast_enable_rain);
                final KeyboardActivationProcessor.CustomViewDialog customViewDialog = new KeyboardActivationProcessor.CustomViewDialog(imageCodeProject, 3000, Gravity.BOTTOM, 0, HSDisplayUtils.dip2px(20));

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!MainActivity.this.isFinishing()) {
                            customViewDialog.show();
                        }
                    }
                }, 500);

                Intent intent = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
                intent.setFlags(FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                isInStepOne = true;
            }
        });
        dialog.show();

        if (versionFilterForRecordEvent && !isEventRecorded(APP_STEP_ONE_HINT)) {
            setEventRecorded(APP_STEP_ONE_HINT);
            HSGoogleAnalyticsUtils.getInstance().logAppEvent(APP_STEP_ONE_HINT);
        }
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
    protected void onResume() {
        super.onResume();

        if (edit_text_test != null) {
            edit_text_test.requestFocus();
            //            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //            imm.showSoftInput(edit_text_test, InputMethodManager.SHOW_IMPLICIT);
        }

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFlashLottieAnimationPlayed) {
                    flashLottieAnimationView.playAnimation();
                }
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
        if (clickStepOne) {
            bt_step_one.performClick();
            clickStepOne = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (edit_text_test != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit_text_test.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        if (flashLottieAnimationView.isAnimating()) {
            flashLottieAnimationView.cancelAnimation();
            flashLottieAnimationView.setProgress(0f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        needActiveThemePkName = null;
        try {
            if (settingsContentObserver != null) {
                getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
            }
            unregisterReceiver(imeChangeRecevier);
        } catch (IllegalArgumentException ex) {
            HSLog.e("content observer not registered yet");
        }
        getWindow().setBackgroundDrawable(null);
    }

    private void refreshUIState() {
        if (!HSInputMethodListManager.isMyInputMethodEnabled()) {
            if (style == CurrentUIStyle.UISTYLE_STEP_ONE)
                return;
            edit_text_test.setAlpha(0);
            edit_text_test.setFocusable(false);
            edit_text_test.setFocusableInTouchMode(false);

            bt_design_theme.setVisibility(View.INVISIBLE);
            bt_settings.setVisibility(View.INVISIBLE);
            bt_languages.setVisibility(View.INVISIBLE);

            bt_step_one.setEnabled(true);
            bt_step_one.setAlpha(1.0f);
            bt_step_two.setEnabled(false);
            bt_step_two.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);

            bt_design_theme.setAlpha(0);
            bt_settings.setAlpha(0);
            bt_languages.setAlpha(0);

            img_enter_one.setAlpha(255);
            img_enter_two.setAlpha(255);
            img_choose_one.setAlpha(0);
            img_choose_two.setAlpha(0);

            style = CurrentUIStyle.UISTYLE_STEP_ONE;
        } else if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            if (style == CurrentUIStyle.UISTYLE_STEP_TWO)
                return;

            edit_text_test.setAlpha(0);
            edit_text_test.setFocusable(false);
            edit_text_test.setFocusableInTouchMode(false);

            bt_design_theme.setVisibility(View.INVISIBLE);
            bt_settings.setVisibility(View.INVISIBLE);
            bt_languages.setVisibility(View.INVISIBLE);

            bt_step_one.setClickable(false);
            if (isInStepOne) {
                bt_step_one.setAlpha(1.0f);
                bt_step_one.setEnabled(false);
                bt_step_two.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);
            } else {
                bt_step_one.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);
                bt_step_one.setEnabled(false);
                bt_step_two.setAlpha(1.0f);
                bt_step_two.setEnabled(true);
            }

            bt_design_theme.setAlpha(0);
            bt_settings.setAlpha(0);
            bt_languages.setAlpha(0);

            img_enter_one.setAlpha(0);
            img_enter_two.setAlpha(255);
            img_choose_one.setAlpha(255);
            img_choose_two.setAlpha(0);

            style = CurrentUIStyle.UISTYLE_STEP_TWO;
        }
    }

    private void startThemeHomeActivity() {
        Intent startThemeHomeIntent = new Intent(MainActivity.this, ThemeHomeActivity.class);
        if (!TextUtils.isEmpty(needActiveThemePkName)) {
            final boolean setThemeSucceed = HSKeyboardThemeManager.setDownloadedTheme(needActiveThemePkName);

            if (setThemeSucceed) {
                startThemeHomeIntent.putExtra(ThemeHomeActivity.INTENT_KEY_SHOW_TRIAL_KEYBOARD, true);
            } else {
                HSKeyboardTheme keyboardTheme = HSKeyboardThemeManager.getDownloadedThemeByPackageName(needActiveThemePkName);
                if (keyboardTheme != null) {
                    String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                    HSToastUtils.toastCenterLong(String.format(failedString, keyboardTheme.getThemeShowName()));
                }
            }

            needActiveThemePkName = null;
        }
        startActivity(startThemeHomeIntent);
        finish();
    }

    private void playManualButtonShowAnimation() {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(bt_step_one, "alpha", 0f, 1.0f),
                ObjectAnimator.ofFloat(bt_step_two, "alpha", 0f, 1.0f),
                ObjectAnimator.ofFloat(protocolText, "alpha", 0f, 1.0f)
        );
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                bt_step_one.setVisibility(View.VISIBLE);
                bt_step_two.setVisibility(View.VISIBLE);
                protocolText.setVisibility(View.VISIBLE);
            }
        });
        set.setDuration(1000).start();
    }

    private void playAccessibilityButtonShowAnimation() {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(accessibilityButtonContainer, "alpha", 0f, 1.0f),
                ObjectAnimator.ofFloat(protocolText, "alpha", 0f, 1.0f)
        );
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                accessibilityButtonContainer.setVisibility(View.VISIBLE);
                protocolText.setVisibility(View.VISIBLE);
            }
        });
        set.setDuration(1000).start();
    }

    private void doSetpOneFinishAnimation() {

        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(SCALE_MIN, SCALE_MAX, SCALE_MIN, SCALE_MAX, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(350);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bt_step_one.setClickable(false);
                img_enter_one.setAlpha(0);
                img_choose_one.setVisibility(View.VISIBLE);
                img_choose_one.setAlpha(255);
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
        ScaleAnimation scaleAnimation = new ScaleAnimation(SCALE_MAX, 1.0f, SCALE_MAX, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(350);
        scaleAnimation.setFillAfter(true);
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

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, BUTTON_BACKGROUND_OPACITY_DISABLED);
        alphaAnimation.setDuration(350);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bt_step_one.setEnabled(false);
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
        AlphaAnimation alphaAnimation = new AlphaAnimation(BUTTON_BACKGROUND_OPACITY_DISABLED, 1.0f);
        alphaAnimation.setDuration(0);
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
                bt_step_two.setEnabled(true);
            }
        });
        bt_step_two.startAnimation(alphaAnimation);
    }

    private void doSetpTwoFinishAnimation() {
        bt_step_one.setClickable(false);
        bt_step_two.setClickable(false);
        bt_step_one.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AnimationSet animationSet = new AnimationSet(true);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                alphaAnimation.setDuration(300);
                animationSet.addAnimation(alphaAnimation);

                ScaleAnimation scaleAnimation = new ScaleAnimation(SCALE_MIN, SCALE_MAX, SCALE_MIN, SCALE_MAX, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(300);
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
        ScaleAnimation scaleAnimation = new ScaleAnimation(SCALE_MAX, 1.0f, SCALE_MAX, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(350);
        scaleAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startThemeHomeActivity();
            }
        });
        img_choose_two.startAnimation(scaleAnimation);
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
