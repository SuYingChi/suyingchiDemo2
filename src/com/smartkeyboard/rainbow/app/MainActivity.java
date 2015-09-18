package com.smartkeyboard.rainbow.app;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.extended.api.HSKeyboardSettings;
import com.ihs.inputmethod.extended.eventrecorder.HSGoogleAnalyticsEvent;
import com.ihs.inputmethod.extended.eventrecorder.HSGoogleAnalyticsUtils;
import com.smartkeyboard.rainbow.R;
import com.smartkeyboard.rainbow.utils.InputMethodManagerUtils;

public class MainActivity extends HSActivity {

    public enum CurrentUIStyle {
        UISTYLE_STEP_ONE,
        UISTYLE_STEP_TWO,
        UISTYLE_STEP_THREE_NORMAL,
        UISTYLE_STEP_THREE_TEST,
    }

    static float ratio = 0.7f;
    static float move = 0.15f;

    View rootView;

    private ImageView img_title;
    private View bt_step_one;
    private View bt_step_two;
    private View bt_step_one_content_view;
    private View bt_step_two_content_view;
    private TextView text_one;
    private TextView text_two;
    private Button bt_settings;
    private Button bt_languages;
    private ImageView img_enter_one;
    private ImageView img_enter_two;
    private ImageView img_choose_one;
    private ImageView img_choose_two;
    private EditText edit_text_test;
    private ImeSettingsContentObserver settingsContentObserver = new ImeSettingsContentObserver(new Handler());;

    boolean isInStepOne;
    boolean isTitleImageScaled;

    CurrentUIStyle style;

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
        img_title = (ImageView) this.findViewById(R.id.view_img_title);
        bt_step_one = (View) this.findViewById(R.id.bt_step_one);
        bt_step_two = (View) this.findViewById(R.id.bt_step_two);
        bt_step_one_content_view = (View) this.findViewById(R.id.bt_step_one_content_view);
        bt_step_two_content_view = (View) this.findViewById(R.id.bt_step_two_content_view);
        text_one = (TextView) this.findViewById(R.id.text_one);
        text_two = (TextView) this.findViewById(R.id.text_two);

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
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int button_width = (int) (width * 0.5);

            float ratio_button_guide_settings = ((float) getResources().getDrawable(R.drawable.app_button_guide_settings_bg).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_settings_bg).getIntrinsicWidth());

            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(button_width, (int) (button_width * ratio_button_guide_settings));
            relativeParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            bt_settings.setLayoutParams(relativeParams);

            RelativeLayout.LayoutParams relativeParams2 = new RelativeLayout.LayoutParams(button_width, (int) (button_width * ratio_button_guide_settings));
            relativeParams2.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            relativeParams2.addRule(RelativeLayout.BELOW, R.id.bt_settings);

            relativeParams2.topMargin = (int) (button_width * 0.07);
            bt_languages.setLayoutParams(relativeParams2);

            int step_button_width = (int) (button_width * 1.1);

            float ratio_button_guide_one = ((float) getResources().getDrawable(R.drawable.app_button_guide_one_bg).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_one_bg).getIntrinsicWidth());
            float ratio_button_guide_two = ((float) getResources().getDrawable(R.drawable.app_button_guide_two_bg).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_two_bg).getIntrinsicWidth());
            float ratio_img_enter = ((float) getResources().getDrawable(R.drawable.app_button_guide_enter).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_enter).getIntrinsicWidth());
            float ratio_img_choose = ((float) getResources().getDrawable(R.drawable.app_button_guide_choose).getIntrinsicHeight())
                    / ((float) getResources().getDrawable(R.drawable.app_button_guide_choose).getIntrinsicWidth());

            LinearLayout.LayoutParams step_one_linearParams = new LayoutParams(step_button_width, (int) (step_button_width * ratio_button_guide_one));
            step_one_linearParams.gravity = Gravity.CENTER;
            bt_step_one.setLayoutParams(step_one_linearParams);

            LinearLayout.LayoutParams step_one_linearParams2 = new LayoutParams(step_button_width, (int) (step_button_width * ratio_button_guide_two));
            step_one_linearParams2.gravity = Gravity.CENTER;
            step_one_linearParams2.topMargin = (int) (step_button_width * 0.07);
            bt_step_two.setLayoutParams(step_one_linearParams2);

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
        }

        bt_step_one.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboardEnableDialog();

                //                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                //                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                //
                //                    @Override
                //                    public void onClick(DialogInterface dialog, int which) {
                //                        startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
                //                        isInStepOne = true;
                //                        Toast toast = Toast.makeText(MainActivity.this, R.string.toast_enable_keyboard, Toast.LENGTH_LONG);
                //                        toast.show();
                //                    }
                //                });
                //                builder.setMessage(R.string.alert_attention_messenger);
                //                builder.show();

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

    private void scaleTitleImage() {
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        LinearLayout.LayoutParams linearParams = new LayoutParams((int) (width * ratio), (int) (width * ratio));
        linearParams.gravity = Gravity.CENTER;
        linearParams.topMargin = -(int) (width * move);
        img_title.setLayoutParams(linearParams);
    }

    private void restoreTitleImage() {

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        if (getResources().getBoolean(R.bool.isTablet)) {
            width = (int) (width * 0.85);
        }
        LinearLayout.LayoutParams linearParams = new LayoutParams(width, width);
        linearParams.gravity = Gravity.CENTER;
        img_title.setLayoutParams(linearParams);
    }

    private void refreshUIState() {
        if (!InputMethodManagerUtils.isCurrentIMEEnabled(this)) {
            if (style == CurrentUIStyle.UISTYLE_STEP_ONE)
                return;

            restoreTitleImage();

            edit_text_test.setAlpha(0);
            edit_text_test.setFocusable(false);
            edit_text_test.setFocusableInTouchMode(false);

            bt_step_one.setVisibility(View.VISIBLE);
            bt_step_two.setVisibility(View.VISIBLE);
            bt_settings.setVisibility(View.INVISIBLE);
            bt_languages.setVisibility(View.INVISIBLE);
            bt_step_one.setClickable(true);
            bt_step_one.setAlpha(1.0f);
            bt_step_two.setClickable(false);
            bt_step_two.setAlpha(0.4f);
            bt_settings.setAlpha(0);
            bt_languages.setAlpha(0);

            img_enter_one.setAlpha(255);
            img_enter_two.setAlpha(255);
            img_choose_one.setAlpha(0);
            img_choose_two.setAlpha(0);

            rootView.setBackgroundColor(Color.parseColor("#2a9de8"));

            style = CurrentUIStyle.UISTYLE_STEP_ONE;
        } else if (!InputMethodManagerUtils.isCurrentIMESelected(this)) {
            if (style == CurrentUIStyle.UISTYLE_STEP_TWO)
                return;

            restoreTitleImage();

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
                bt_step_two.setAlpha(0.4f);
            } else {
                bt_step_one.setAlpha(0.4f);
                bt_step_two.setClickable(true);
                bt_step_two.setAlpha(1.0f);
            }
            bt_settings.setAlpha(0);
            bt_languages.setAlpha(0);

            img_enter_one.setAlpha(0);
            img_enter_two.setAlpha(255);
            img_choose_one.setAlpha(255);
            img_choose_two.setAlpha(0);

            rootView.setBackgroundColor(Color.parseColor("#2a9de8"));

            style = CurrentUIStyle.UISTYLE_STEP_TWO;

        } else {
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
            bt_step_one.setVisibility(View.GONE);
            bt_step_two.setVisibility(View.GONE);
            bt_settings.setVisibility(View.VISIBLE);
            bt_languages.setVisibility(View.VISIBLE);
            rootView.setBackgroundColor(Color.parseColor("#57bbfc"));
            bt_settings.setAlpha(1);
            bt_languages.setAlpha(1);

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
                bt_step_one.setVisibility(View.GONE);
                bt_step_two.setVisibility(View.GONE);
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
                MainActivity.this.doHideAnimation();
            }
        });
        img_choose_two.startAnimation(scaleAnimation);
    }
}
