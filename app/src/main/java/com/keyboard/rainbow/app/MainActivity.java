package com.keyboard.rainbow.app;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodCommonUtils;
import com.ihs.inputmethod.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.keyboard.rainbow.R;
import com.keyboard.rainbow.utils.Constants;

public class MainActivity extends HSActivity {

    private final static float BUTTON_BACKGROUND_OPACITY_DISABLED = 0.7f;
    private final static float BUTTON_BACKGROUND_OPACITY_ENABLED = 1f;
    private final static float BUTTON_BACKGROUND_OPACITY_INVISIBLE = 0f;

    public enum CurrentUIStyle {
        UISTYLE_STEP_ONE,
        UISTYLE_STEP_TWO,
        UISTYLE_STEP_THREE_NORMAL,
        UISTYLE_STEP_THREE_TEST,
    }

    private static float ratio = 0.7f;
    private static float move = 0.15f;

    private View rootView;

    private TextView view_title_text;
    private View view_logo_img;
    private View bt_step_one;
    private View bt_step_two;
    //    private View bt_step_one_content_view;
    //    private View bt_step_two_content_view;
    private TextView text_one;
    private TextView text_two;
    private TextView bt_design_theme;
    private LinearLayout settings_languages_layout;
    private TextView bt_settings;
    private TextView bt_languages;
    private ImageView img_rainbow;
    private ImageView img_enter_one;
    private ImageView img_enter_two;
    private ImageView img_choose_one;
    private ImageView img_choose_two;
    private EditText edit_text_test;
    private ImeSettingsContentObserver settingsContentObserver = new ImeSettingsContentObserver(new Handler());
    ;

    private boolean isInStepOne;

    private CurrentUIStyle style;

    private BroadcastReceiver imeChangeRecevier = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) {
                if (HSInputMethodCommonUtils.isCurrentIMESelected(MainActivity.this)) {
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


        rootView = (View) this.findViewById(R.id.view_root);

        WindowManager wm = this.getWindowManager();
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        final int screenHeight = size.y;

        getWindow().setBackgroundDrawable(HSInputMethodCommonUtils.getScaledImage(getResources().getDrawable(R.drawable.app_bg), screenWidth, screenHeight));



        view_title_text = (TextView) this.findViewById(R.id.view_title_text);
        view_logo_img = (View) this.findViewById(R.id.view_logo_img);
        bt_step_one = (View) this.findViewById(R.id.bt_step_one);
        bt_step_two = (View) this.findViewById(R.id.bt_step_two);
        //        bt_step_one_content_view = (View) this.findViewById(R.id.bt_step_one_content_view);
        //        bt_step_two_content_view = (View) this.findViewById(R.id.bt_step_two_content_view);
        text_one = (TextView) this.findViewById(R.id.text_one);
        text_two = (TextView) this.findViewById(R.id.text_two);
        img_rainbow = (ImageView) this.findViewById(R.id.view_logo_img);
        img_enter_one = (ImageView) this.findViewById(R.id.view_enter_one);
        img_enter_two = (ImageView) this.findViewById(R.id.view_enter_two);
        img_choose_one = (ImageView) this.findViewById(R.id.view_choose_one);
        img_choose_two = (ImageView) this.findViewById(R.id.view_choose_two);

        bt_design_theme = (TextView) this.findViewById(R.id.bt_design_theme);
        settings_languages_layout = (LinearLayout) this.findViewById(R.id.settings_languages_layout);
        bt_settings = (TextView) this.findViewById(R.id.bt_settings);
        bt_languages = (TextView) this.findViewById(R.id.bt_languages);

        edit_text_test = (EditText) this.findViewById(R.id.edit_text_test);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
        registerReceiver(imeChangeRecevier, filter);

        LinearLayout.LayoutParams designThemeLayouParam = (LinearLayout.LayoutParams) bt_design_theme.getLayoutParams();
        designThemeLayouParam.topMargin = (int) (screenHeight * 0.09);

        LinearLayout.LayoutParams settings_languages_layoutLayoutParams = (LinearLayout.LayoutParams) settings_languages_layout.getLayoutParams();
        settings_languages_layoutLayoutParams.topMargin = (int) (screenHeight * 0.03646);

        if (getResources().getBoolean(R.bool.isTablet)) {

            int button_width = (int) (screenWidth * 0.5);
            final float ratio_button_guide_settings = ((float) getResources().getDrawable(R.drawable.entrance_customize_button).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.entrance_customize_button).getIntrinsicWidth());

            final float ratio_log_img = ((float) getResources().getDrawable(R.drawable.app_rainbow_logo).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_rainbow_logo).getIntrinsicWidth());


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
            int textHeight = (int) (result.height() * 0.8f);
            LinearLayout.LayoutParams logImgLayoutParams = (LinearLayout.LayoutParams) view_logo_img.getLayoutParams();
            logImgLayoutParams.height = textHeight;
            logImgLayoutParams.width = (int) (textHeight/ratio_log_img);
            logImgLayoutParams.topMargin = 0;

            int step_button_width = (int) (button_width * 1.1);

            float ratio_button_guide_one = ((float) getResources().getDrawable(R.drawable.app_button_guide_bg).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_bg).getIntrinsicWidth());
            float ratio_img_enter = ((float) getResources().getDrawable(R.drawable.app_button_guide_enter).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_enter).getIntrinsicWidth());
            float ratio_img_choose = ((float) getResources().getDrawable(R.drawable.app_button_guide_choose).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_choose).getIntrinsicWidth());

            RelativeLayout.LayoutParams step_one_linearParams = new RelativeLayout.LayoutParams(step_button_width, (int) (step_button_width * ratio_button_guide_one));
            step_one_linearParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            step_one_linearParams.topMargin = (int) (screenHeight * 0.7);
            bt_step_one.setLayoutParams(step_one_linearParams);

            RelativeLayout.LayoutParams step_one_linearParams2 = new RelativeLayout.LayoutParams(step_button_width, (int) (step_button_width * ratio_button_guide_one));
            step_one_linearParams2.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            step_one_linearParams2.addRule(RelativeLayout.BELOW, R.id.bt_step_one);
            step_one_linearParams2.topMargin = (int) (step_button_width * 0.07);
            bt_step_two.setLayoutParams(step_one_linearParams2);


            RelativeLayout.LayoutParams step_one_relativeParams2 = new RelativeLayout.LayoutParams(-2, -2);
            step_one_relativeParams2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            step_one_relativeParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            step_one_relativeParams2.leftMargin = (int) (step_button_width * 0.22);
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

            RelativeLayout.LayoutParams step_two_relativeParams2 = new RelativeLayout.LayoutParams(-2, -2);
            step_two_relativeParams2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            step_two_relativeParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            step_two_relativeParams2.leftMargin = (int) (step_button_width * 0.22);
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
        }
        bt_step_one.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboardEnableDialog();
                HSGoogleAnalyticsUtils.logAppEvent(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED);
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

                HSGoogleAnalyticsUtils.logAppEvent(Constants.GA_PARAM_ACTION_APP_STEP_TWO_CLICKED);
            }
        });

        bt_design_theme.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(MainActivity.this,"go to custom theme",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this,CustomThemeActivity.class));
            }
        });

        bt_settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.showMoreSettingsActivity();
                HSGoogleAnalyticsUtils.logAppEvent(Constants.GA_PARAM_ACTION_APP_SETTINGS_CLICKED);
            }
        });

        bt_languages.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.showLanguageSettingsActivity();
                HSGoogleAnalyticsUtils.logAppEvent(Constants.GA_PARAM_ACTION_APP_LANGUAGES_CLICKED);
            }
        });

        if (getIntent().getBooleanExtra("isInStepOne", false)) {
            isInStepOne = true;
        }

        this.refreshUIState();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if(data!=null) {
            String pkName = data.getQueryParameter("pkName");
            if (!TextUtils.isEmpty(pkName)) {
                HSLog.d("jx,收到激活主题的请求，包名:" + pkName);
                HSKeyboardThemeManager.setPluginTheme(pkName);
            }
        }
    }

    /**
     * Show keyboard enabling dialog
     */
    private void showKeyboardEnableDialog() {
        // Create custom dialog object
        final Dialog dialog = new Dialog(this);
        // hide to default title for Dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate the layout dialog_layout.xml and set it as contentView
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.enable_keyboard_dialog, null, false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        Button positiveBtn = (Button) dialog.findViewById(R.id.enable_alert_btn_ok);
        positiveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
                isInStepOne = true;
                Toast toast = Toast.makeText(MainActivity.this, R.string.toast_enable_keyboard, Toast.LENGTH_LONG);
                toast.show();
            }
        });

        dialog.show();
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
            if (HSInputMethodCommonUtils.isCurrentIMEEnabled(MainActivity.this)) {
                Intent i = new Intent(MainActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("isInStepOne", true);
                //                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
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
                if (!HSInputMethodCommonUtils.isCurrentIMEEnabled(MainActivity.this)) {
                    getApplicationContext().getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_INPUT_METHODS), false,
                            settingsContentObserver);
                    refreshUIState();
                } else {
                    if (!HSInputMethodCommonUtils.isCurrentIMESelected(MainActivity.this)) {
                        if (isInStepOne) {
                            doSetpOneFinishAnimation();
                            style = CurrentUIStyle.UISTYLE_STEP_TWO;
                            HSGoogleAnalyticsUtils.logAppEvent(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED);
                        } else {
                            refreshUIState();
                        }
                    } else {
                        refreshUIState();

                        HSGoogleAnalyticsUtils.logAppEvent(Constants.GA_PARAM_ACTION_APP_STEP_TWO_ENABLED);
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
    protected void onStop() {
        super.onStop();
        if (edit_text_test != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit_text_test.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (settingsContentObserver != null) {
                getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
            }
            unregisterReceiver(imeChangeRecevier);
        } catch (IllegalArgumentException ex) {
            HSLog.e("content observer not registered yet");
        }
    }

    private void refreshUIState() {
        if (!HSInputMethodCommonUtils.isCurrentIMEEnabled(this)) {
            if (style == CurrentUIStyle.UISTYLE_STEP_ONE)
                return;
            rootView.setBackgroundColor(Color.TRANSPARENT);
            edit_text_test.setAlpha(0);
            edit_text_test.setFocusable(false);
            edit_text_test.setFocusableInTouchMode(false);

            bt_step_one.setVisibility(View.VISIBLE);
            bt_step_two.setVisibility(View.VISIBLE);
            bt_design_theme.setVisibility(View.INVISIBLE);
            bt_settings.setVisibility(View.INVISIBLE);
            bt_languages.setVisibility(View.INVISIBLE);
            bt_step_one.setClickable(true);
            bt_step_one.setBackgroundDrawable(getResources().getDrawable(R.drawable.guide_button_selector));
            bt_step_one.setAlpha(1.0f);
            bt_step_two.setClickable(false);
            bt_step_two.setBackgroundDrawable(getResources().getDrawable(R.drawable.app_button_guide_disable_bg));
            bt_step_two.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);
            bt_design_theme.setAlpha(0);
            bt_settings.setAlpha(0);
            bt_languages.setAlpha(0);

            img_enter_one.setAlpha(255);
            img_enter_two.setAlpha(255);
            img_choose_one.setAlpha(0);
            img_choose_two.setAlpha(0);

            style = CurrentUIStyle.UISTYLE_STEP_ONE;
        } else if (!HSInputMethodCommonUtils.isCurrentIMESelected(this)) {
            if (style == CurrentUIStyle.UISTYLE_STEP_TWO)
                return;
            rootView.setBackgroundColor(Color.TRANSPARENT);
            //  restoreTitleImage();

            edit_text_test.setAlpha(0);
            edit_text_test.setFocusable(false);
            edit_text_test.setFocusableInTouchMode(false);

            bt_step_one.setVisibility(View.VISIBLE);
            bt_step_two.setVisibility(View.VISIBLE);
            bt_design_theme.setVisibility(View.INVISIBLE);
            bt_settings.setVisibility(View.INVISIBLE);
            bt_languages.setVisibility(View.INVISIBLE);
            bt_step_one.setClickable(false);
            if (isInStepOne) {
                bt_step_one.setAlpha(1.0f);
                bt_step_two.setClickable(false);
                bt_step_two.setBackgroundDrawable(getResources().getDrawable(R.drawable.app_button_guide_disable_bg));
                bt_step_two.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);
            } else {
                bt_step_one.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);
                bt_step_one.setBackgroundDrawable(getResources().getDrawable(R.drawable.app_button_guide_disable_bg));

                bt_step_two.setClickable(true);
                bt_step_two.setAlpha(1.0f);
                bt_step_two.setBackgroundDrawable(getResources().getDrawable(R.drawable.guide_button_selector));
            }
            bt_design_theme.setAlpha(0);
            bt_settings.setAlpha(0);
            bt_languages.setAlpha(0);

            img_enter_one.setAlpha(0);
            img_enter_two.setAlpha(255);
            img_choose_one.setAlpha(255);
            img_choose_two.setAlpha(0);

            style = CurrentUIStyle.UISTYLE_STEP_TWO;

        } else {
            startThemeHomeActivity();

            edit_text_test.setAlpha(1);
            edit_text_test.setFocusable(true);
            edit_text_test.setFocusableInTouchMode(true);
            edit_text_test.requestFocus();
            //             getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            edit_text_test.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(edit_text_test, InputMethodManager.SHOW_IMPLICIT);
                }
            },100);
            rootView.setBackgroundColor(getResources().getColor(R.color.bg_translucent_black));
            if (style == CurrentUIStyle.UISTYLE_STEP_THREE_NORMAL || style == CurrentUIStyle.UISTYLE_STEP_THREE_TEST)
                return;
            //scaleTitleImage();
            bt_step_one.setVisibility(View.GONE);
            bt_step_two.setVisibility(View.GONE);
            bt_design_theme.setVisibility(View.VISIBLE);
            bt_settings.setVisibility(View.VISIBLE);
            bt_languages.setVisibility(View.VISIBLE);
            bt_design_theme.setAlpha(1);
            bt_settings.setAlpha(1);
            bt_languages.setAlpha(1);
            style = CurrentUIStyle.UISTYLE_STEP_THREE_NORMAL;
        }
    }

    private void startThemeHomeActivity() {
        startActivity(new Intent(MainActivity.this,ThemeHomeActivity.class));
        finish();
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
                bt_step_one.setVisibility(View.GONE);
                bt_step_two.setVisibility(View.GONE);
                MainActivity.this.doAppearAnimation();
                // MainActivity.this.doScaleAnimation();
            }
        });
        alphaAnimation.setDuration(500);
        this.bt_step_one.startAnimation(alphaAnimation);
        this.bt_step_two.startAnimation(alphaAnimation);
    }

    private void doAppearAnimation() {
        rootView.setBackgroundColor(getResources().getColor(R.color.bg_translucent_black));
        bt_design_theme.setAlpha(1);
        bt_settings.setAlpha(1);
        bt_languages.setAlpha(1);
        bt_design_theme.setVisibility(View.VISIBLE);
        bt_settings.setVisibility(View.VISIBLE);
        bt_languages.setVisibility(View.VISIBLE);
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
        this.bt_design_theme.startAnimation(alphaAnimation);
        this.bt_settings.startAnimation(alphaAnimation);
        this.bt_languages.startAnimation(alphaAnimation);
    }

    private void doSetpOneFinishAnimation() {

        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 2.0f, 1f, 2.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bt_step_one.setClickable(false);
                img_enter_one.setAlpha(0);
                img_choose_one.setVisibility(View.VISIBLE);
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
        ScaleAnimation scaleAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
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
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bt_step_one.setBackgroundDrawable(getResources().getDrawable(R.drawable.app_button_guide_disable_bg));
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
                bt_step_two.setBackgroundDrawable(getResources().getDrawable(R.drawable.guide_button_selector));
                bt_step_two.setClickable(true);
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
                MainActivity.this.doHideAnimation();
                startThemeHomeActivity();
            }
        });
        img_choose_two.startAnimation(scaleAnimation);
    }
}
