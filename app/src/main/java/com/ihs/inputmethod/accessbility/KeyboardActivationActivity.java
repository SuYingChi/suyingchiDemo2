package com.ihs.inputmethod.accessbility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.view.View.GONE;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_accessibility_guide_gotit_clicked;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_accessibility_guide_viewed;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_accessibility_setkey_screen_viewed;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_accessibility_setkey_success_page_viewed;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_alert_auto_setkey_showed;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_auto_setkey_clicked;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_manual_setkey_clicked;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_setting_up_page_viewed;
import static com.ihs.inputmethod.accessbility.AccGALogger.logOneTimeGA;
import static com.ihs.inputmethod.uimodules.R.id.bt_step_one;
import static com.ihs.inputmethod.uimodules.R.id.view_img_title;
import static com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor.PREF_THEME_HOME_SHOWED;


public class KeyboardActivationActivity extends HSActivity {
    public static final String ACTION_MAIN_ACTIVITY = HSApplication.getContext().getPackageName() + ".keyboard.main";
    private static final int GUIDE_DELAY = 300;
    private boolean shouldShowEnableDialog = false;

    private AccessibilityEventListener accessibilityEventListener;
    private Handler handler = new Handler();

    private int listenerKey = -1;

    private LinearLayout dialogView;
    private GivenSizeVideoView videoView;
    private CustomViewDialog customViewDialog;

    private boolean skipPage = false;
    private TextView protocolText;


    private BroadcastReceiver imeChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) {
                if (HSInputMethodListManager.isMyInputMethodSelected()) {

                    Intent actIntent = getIntent();
                    if (actIntent == null) {
                        actIntent = new Intent();
                    }
                    actIntent.setClass(HSApplication.getContext(), ThemeHomeActivity.class);
                    actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
                    KeyboardActivationActivity.this.startActivity(actIntent);

                    View coverView = HSFloatWindowManager.getInstance().getCoverView();
                    logOneTimeGA(app_setting_up_page_viewed);


                    if (coverView != null) {
                        coverView.findViewById(R.id.progressBar).setVisibility(GONE);
                        coverView.findViewById(R.id.iv_succ).setVisibility(View.VISIBLE);
                        ((TextView) coverView.findViewById(R.id.tv_settings_item)).setText(R.string.access_set_up_success);
                        logOneTimeGA(app_accessibility_setkey_success_page_viewed);
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            KeyboardActivationActivity.this.finish();
                        }
                    }, 200);
                }
            }
        }
    };
    private boolean alertDialogShowing;

    @Override
    protected void onDestroy() {
        try {
            if (!skipPage) {
                HSAccessibilityService.unregisterEvent(listenerKey);
                unregisterReceiver(imeChangeReceiver);
                accessibilityEventListener.onDestroy();
            }
        } catch (Exception e) {
        }

        if (videoView != null) {
            try {
                videoView.stopPlayback();
                videoView = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean oneTapPageViewed = AccGALogger.isOneTapPageViewed();
        if (oneTapPageViewed || Build.VERSION.SDK_INT < 17 || HSPreferenceHelper.getDefault().contains(PREF_THEME_HOME_SHOWED)) {
            skipPage = true;
            Intent actIntent = new Intent();
            actIntent.putExtras(getIntent());
            if (oneTapPageViewed) {
                actIntent.setClass(HSApplication.getContext(), ThemeHomeActivity.class);
            } else {
                actIntent.setAction(ACTION_MAIN_ACTIVITY);
            }
            startActivity(actIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_keyboard_activation);

        accessibilityEventListener = new AccessibilityEventListener(AccessibilityEventListener.MODE_SETUP_KEYBOARD);
        listenerKey = HSAccessibilityService.registerEventListener(accessibilityEventListener);

        if (findViewById(view_img_title) != null) {
            scaleTitleImage(findViewById(view_img_title));
        } else {
            WindowManager wm = this.getWindowManager();
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenWidth = size.x;
            final int screenHeight = size.y;
//            Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.app_bg)).getBitmap();
//            getWindow().setBackgroundDrawable(new BitmapDrawable(HSBitmapScaleUtils.centerCrop(bitmap, screenWidth, screenHeight)));
        }


        View setupAutoBtn = findViewById(bt_step_one);
        ((Button)setupAutoBtn).setText(getString(R.string.tv_1_tap_to_set_up_keyboard, getString(R.string.app_name)));
        if (findViewById(R.id.text_one) != null) {
            setupAutoBtn.setBackgroundDrawable(RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(getResources().getColor(R.color.guide_bg_normal_color), getResources().getColor(R.color.guide_bg_disable_color),
                    getResources().getDimension(R.dimen.guide_bg_radius)));
        } else {
            setupAutoBtn.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.selector_keyactive_onetap_normal));
        }
        setupAutoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                logOneTimeGA(app_auto_setkey_clicked);

                if (HSAccessibilityService.isAvailable()) {
                    try {
                        accessibilityEventListener.onAvailable();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    autoSetupKeyboard();
                }

            }
        });


        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
        registerReceiver(imeChangeReceiver, filter);


        logOneTimeGA(app_accessibility_setkey_screen_viewed);

        initPrivacy();
    }

    private void initPrivacy() {
        protocolText = (TextView) findViewById(R.id.privacy_policy_text);
        String serviceKeyText = getString(R.string.text_terms_of_service);
        String policyKeyText = getString(R.string.text_privacy_policy);
        String policyText = getResources().getString(R.string.keyboard_guide_privacy_policy, serviceKeyText, policyKeyText);
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
    }


    private void autoSetupKeyboard() {
        shouldShowEnableDialog = true;

        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, 100);

        if (dialogView == null) {
            dialogView = (LinearLayout) View.inflate(getApplicationContext(), R.layout.dialog_enable_accessbility_guide, null);

            videoView = (GivenSizeVideoView) dialogView.findViewById(R.id.videoview_guide);
            Uri uri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.accesibility_guide);
            videoView.setVideoURI(uri);

            videoView.setZOrderOnTop(true);

            customViewDialog = new CustomViewDialog(HSApplication.getContext());
            customViewDialog.setContentView(dialogView);

            customViewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    videoView.stopPlayback();
                }
            });

            customViewDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    int width = dialogView.getMeasuredWidth();
                    if (width != videoView.getMeasuredWidth()) {
                        videoView.setViewSize(width, width * 588 / 948);
                    }
                    dialogView.forceLayout();
                }
            });

            dialogView.findViewById(R.id.tv_confirm).setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.selector_keyactive_enable));
            dialogView.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logOneTimeGA(app_accessibility_guide_gotit_clicked);

                    customViewDialog.dismiss();
                }
            });
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                videoView.start();
                logOneTimeGA(app_accessibility_guide_viewed);

                customViewDialog.show();
            }
        }, GUIDE_DELAY);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (skipPage) {
            return;
        }

        if (!alertDialogShowing && shouldShowEnableDialog && !HSAccessibilityService.isAvailable()) {

            AlertDialog.Builder alertDialogBuilder;
            alertDialogBuilder = new AlertDialog.Builder(this, R.style.AppCompactDialogStyle);
            alertDialogBuilder.setTitle(getString(R.string.alert_enable_access_warn_title));//设置标题
            alertDialogBuilder.setMessage(getString(R.string.alert_enable_access_warn_content));//设置显示文本
            alertDialogBuilder.setPositiveButton(getString(R.string.alert_enable_access_warn_confirm), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logOneTimeGA(app_manual_setkey_clicked);
                    Intent actIntent = new Intent();
                    actIntent.putExtras(getIntent());
                    actIntent.putExtra("skip", true);
                    actIntent.setAction(ACTION_MAIN_ACTIVITY);
                    startActivity(actIntent);
                    dialog.dismiss();
                    finish();
                }
            });
            alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    alertDialogShowing = false;
                }
            });

            alertDialogBuilder.show();
            alertDialogShowing = true;

            logOneTimeGA(app_alert_auto_setkey_showed);
        }
    }

    public static void scaleTitleImage(View view) {
        if (view == null) {
            return;
        }

        Display display = HSFloatWindowManager.getInstance().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        if (HSApplication.getContext().getResources().getBoolean(R.bool.isTablet)) {
            width = (int) (width * 0.85);
        }
        view.getLayoutParams().width = view.getLayoutParams().height = width;
    }


}
