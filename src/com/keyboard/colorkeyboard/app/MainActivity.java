package com.keyboard.colorkeyboard.app;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
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
import com.ihs.inputmethod.extended.api.HSKeyboardSettings;
import com.ihs.inputmethod.extended.eventrecorder.HSGoogleAnalyticsEvent;
import com.ihs.inputmethod.extended.eventrecorder.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.extended.theme.HSKeyboardThemeManager;
import com.keyboard.colorkeyboard.R;
import com.keyboard.colorkeyboard.utils.InputMethodManagerUtils;

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

    private View bt_step_one;
    private View bt_step_two;
    //    private View bt_step_one_content_view;
    //    private View bt_step_two_content_view;
    private TextView text_one;
    private TextView text_two;
    private Button bt_settings;
    private Button bt_languages;
    private ImageView img_rainbow;
    private ImageView img_enter_one;
    private ImageView img_enter_two;
    private ImageView img_choose_one;
    private ImageView img_choose_two;
    private EditText edit_text_test;
    private ImeSettingsContentObserver settingsContentObserver = new ImeSettingsContentObserver(new Handler());;

    private boolean isInStepOne;

    private CurrentUIStyle style;

    private BroadcastReceiver imeChangeRecevier = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) {
                if (InputMethodManagerUtils.isCurrentIMESelected(MainActivity.this)) {
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

        rootView = (View) this.findViewById(R.id.view_root);
        
        WindowManager wm = this.getWindowManager();
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        getWindow().setBackgroundDrawable(HSKeyboardThemeManager.getScaledBackgroundImage(getResources().getDrawable(R.drawable.app_bg), screenWidth, screenHeight));


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

        bt_settings = (Button) this.findViewById(R.id.bt_settings);
        bt_languages = (Button) this.findViewById(R.id.bt_languages);

        edit_text_test = (EditText) this.findViewById(R.id.edit_text_test);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
        registerReceiver(imeChangeRecevier, filter);



        if (getResources().getBoolean(R.bool.isTablet)) {

            int button_width = (int) (screenWidth * 0.5);
            float ratio_button_guide_settings = ((float) getResources().getDrawable(R.drawable.app_button_guide_settings_bg).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_settings_bg).getIntrinsicWidth());

            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(button_width, (int) (button_width * ratio_button_guide_settings));
            relativeParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            relativeParams.topMargin = (int) (screenHeight * 0.25);
            bt_settings.setLayoutParams(relativeParams);

            RelativeLayout.LayoutParams relativeParams2 = new RelativeLayout.LayoutParams(button_width, (int) (button_width * ratio_button_guide_settings));
            relativeParams2.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            relativeParams2.addRule(RelativeLayout.BELOW, R.id.bt_settings);

            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams((int) (button_width * ratio_button_guide_settings * 0.5), (int) (button_width
                    * ratio_button_guide_settings * 0.5));
            linearParams.gravity = Gravity.CENTER;
            linearParams.topMargin = 5;
            img_rainbow.setLayoutParams(linearParams);
            relativeParams2.topMargin = (int) (button_width * 0.07);
            bt_languages.setLayoutParams(relativeParams2);

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
        } else {
            int button_width = (int) (screenWidth * 0.7);
            float ratio_button_guide_settings = ((float) getResources().getDrawable(R.drawable.app_button_guide_settings_bg).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_settings_bg).getIntrinsicWidth());

            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(button_width, (int) (button_width * ratio_button_guide_settings));
            relativeParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            relativeParams.topMargin = (int) (screenHeight * 0.2);
            bt_settings.setLayoutParams(relativeParams);

            RelativeLayout.LayoutParams relativeParams2 = new RelativeLayout.LayoutParams(button_width, (int) (button_width * ratio_button_guide_settings));
            relativeParams2.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            relativeParams2.addRule(RelativeLayout.BELOW, R.id.bt_settings);
            relativeParams2.topMargin = (int) (button_width * 0.07);
            bt_languages.setLayoutParams(relativeParams2);
        }

        bt_step_one.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboardEnableDialog();
                HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_APP_STEP_ONE_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_NONE);
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

                HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_APP_STEP_TWO_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_NONE);
            }
        });

        bt_settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSKeyboardSettings.showMoreSettingsActivity();
                HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_APP_SETTINGS_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_NONE);
            }
        });

        bt_languages.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSKeyboardSettings.showLanguageSettingsActivity();
                HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_APP_LANGUAGES_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_NONE);
            }
        });

        if (getIntent().getBooleanExtra("isInStepOne", false)) {
            isInStepOne = true;
        }

        this.refreshUIState();
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
            if (InputMethodManagerUtils.isCurrentIMEEnabled(MainActivity.this)) {
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
                if (!InputMethodManagerUtils.isCurrentIMEEnabled(MainActivity.this)) {
                    getApplicationContext().getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_INPUT_METHODS), false,
                            settingsContentObserver);
                    refreshUIState();
                } else {
                    if (!InputMethodManagerUtils.isCurrentIMESelected(MainActivity.this)) {
                        if (isInStepOne) {
                            doSetpOneFinishAnimation();
                            style = CurrentUIStyle.UISTYLE_STEP_TWO;

                            HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_APP_STEP_ONE_ENABLED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_NONE);
                        } else {
                            refreshUIState();
                        }
                    } else {
                        refreshUIState();

                        HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_APP_STEP_TWO_ENABLED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_NONE);
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
        if (!InputMethodManagerUtils.isCurrentIMEEnabled(this)) {
            if (style == CurrentUIStyle.UISTYLE_STEP_ONE)
                return;
            rootView.setBackgroundColor(Color.TRANSPARENT);
            edit_text_test.setAlpha(0);
            edit_text_test.setFocusable(false);
            edit_text_test.setFocusableInTouchMode(false);

            bt_step_one.setVisibility(View.VISIBLE);
            bt_step_two.setVisibility(View.VISIBLE);
            bt_settings.setVisibility(View.INVISIBLE);
            bt_languages.setVisibility(View.INVISIBLE);
            bt_step_one.setClickable(true);
            bt_step_one.setBackgroundDrawable(getResources().getDrawable(R.drawable.guide_button_selector));
            bt_step_one.setAlpha(255);
            bt_step_two.setClickable(false);
            bt_step_two.setBackgroundDrawable(getResources().getDrawable(R.drawable.app_button_guide_disable_bg));
            bt_step_two.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);
            bt_settings.setAlpha(0);
            bt_languages.setAlpha(0);

            img_enter_one.setAlpha(255);
            img_enter_two.setAlpha(255);
            img_choose_one.setAlpha(0);
            img_choose_two.setAlpha(0);

            style = CurrentUIStyle.UISTYLE_STEP_ONE;
        } else if (!InputMethodManagerUtils.isCurrentIMESelected(this)) {
            if (style == CurrentUIStyle.UISTYLE_STEP_TWO)
                return;
            rootView.setBackgroundColor(Color.TRANSPARENT);
            //  restoreTitleImage();

            edit_text_test.setAlpha(0);
            edit_text_test.setFocusable(false);
            edit_text_test.setFocusableInTouchMode(false);

            bt_step_one.setVisibility(View.VISIBLE);
            bt_step_two.setVisibility(View.VISIBLE);
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
            bt_settings.setAlpha(0);
            bt_languages.setAlpha(0);

            img_enter_one.setAlpha(0);
            img_enter_two.setAlpha(255);
            img_choose_one.setAlpha(255);
            img_choose_two.setAlpha(0);

            style = CurrentUIStyle.UISTYLE_STEP_TWO;

        } else {
            edit_text_test.setAlpha(1);
            edit_text_test.setFocusable(true);
            edit_text_test.setFocusableInTouchMode(true);
            edit_text_test.requestFocus();
            //             getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(edit_text_test, InputMethodManager.SHOW_IMPLICIT);
            rootView.setBackgroundColor(getResources().getColor(R.color.bg_translucent_black));
            if (style == CurrentUIStyle.UISTYLE_STEP_THREE_NORMAL || style == CurrentUIStyle.UISTYLE_STEP_THREE_TEST)
                return;
            //scaleTitleImage();
            bt_step_one.setVisibility(View.GONE);
            bt_step_two.setVisibility(View.GONE);
            bt_settings.setVisibility(View.VISIBLE);
            bt_languages.setVisibility(View.VISIBLE);
            bt_settings.setAlpha(1);
            bt_languages.setAlpha(1);
            style = CurrentUIStyle.UISTYLE_STEP_THREE_NORMAL;
        }
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
        bt_settings.setAlpha(1);
        bt_languages.setAlpha(1);
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
                img_choose_one.setVisibility(255);
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
            }
        });
        img_choose_two.startAnimation(scaleAnimation);
    }
}
