package com.mobipioneer.lockerkeyboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.ihs.keyboardutils.alerts.KCAlert;
import com.mobipioneer.lockerkeyboard.accessbility.AccGALogger;
import com.mobipioneer.lockerkeyboard.accessbility.AccessibilityEventListener;
import com.mobipioneer.lockerkeyboard.accessbility.GivenSizeVideoView;
import com.mobipioneer.lockerkeyboard.app.CustomViewDialog;
import com.mobipioneer.lockerkeyboard.app.MainActivity;

import static android.view.View.GONE;
import static com.ihs.inputmethod.uimodules.R.id.view_img_title;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.app_accessibility_guide_gotit_clicked;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.app_accessibility_guide_viewed;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.app_accessibility_setkey_screen_viewed;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.app_accessibility_setkey_success_page_viewed;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.app_alert_auto_setkey_enable_clicked;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.app_alert_auto_setkey_showed;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.app_auto_setkey_clicked;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.app_manual_setkey_clicked;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.app_setting_up_page_viewed;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.logOneTimeGA;


public class KeyboardActivationActivity extends HSActivity {

    private static final int GUIDE_DELAY = 300;
    private boolean shouldShowEnableDialog = false;

    private AccessibilityEventListener accessibilityEventListener;
    private Handler handler = new Handler();

    private int listenerKey = -1;

    private LinearLayout dialogView;
    private GivenSizeVideoView videoView;
    private CustomViewDialog customViewDialog;

    private boolean skipPage = false;

    private BroadcastReceiver imeChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) {
                if (HSInputMethod.isCurrentIMESelected()) {

                    Intent actIntent = getIntent();
                    if (actIntent == null) {
                        actIntent = new Intent();
                    }
                    actIntent.setClass(HSApplication.getContext(), ThemeHomeActivity.class);
                    actIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    HSApplication.getContext().startActivity(actIntent);

                    View coverView = HSFloatWindowManager.getInstance().getCoverView();
                    logOneTimeGA(app_setting_up_page_viewed);


                    if (coverView != null) {
                        coverView.findViewById(R.id.progressBar).setVisibility(GONE);
                        coverView.findViewById(R.id.iv_succ).setVisibility(View.VISIBLE);
                        ((TextView) coverView.findViewById(R.id.tv_settings_item)).setText(R.string.access_set_up_success);
                        logOneTimeGA(app_accessibility_setkey_success_page_viewed);
                    }

                    KeyboardActivationActivity.this.finish();
                }
            }
        }
    };

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

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean oneTapPageViewed = AccGALogger.isOneTapPageViewed();
        if (oneTapPageViewed || Build.VERSION.SDK_INT < 17) {
            skipPage = true;
            Intent actIntent = getIntent();
            if (actIntent == null) {
                actIntent = new Intent();
            }

            if (oneTapPageViewed) {
                actIntent.setClass(HSApplication.getContext(), ThemeHomeActivity.class);
            } else {
                actIntent.setClass(HSApplication.getContext(), MainActivity.class);

            }

            actIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            HSApplication.getContext().startActivity(actIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_keyboard_activation);

        accessibilityEventListener = new AccessibilityEventListener(AccessibilityEventListener.MODE_SETUP_KEYBOARD);
        listenerKey = HSAccessibilityService.registerEventListener(accessibilityEventListener);

        View manulSetupBtn = findViewById(R.id.bt_step_two);
        manulSetupBtn.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.selector_keyactive_manual_normal));
        manulSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOneTimeGA(app_manual_setkey_clicked);

                Intent actIntent = getIntent();
                if (actIntent == null) {
                    actIntent = new Intent();
                }
                actIntent.setClass(HSApplication.getContext(), MainActivity.class);
                startActivity(actIntent);
            }
        });


        View setupAutoBtn = findViewById(R.id.bt_step_one);
        setupAutoBtn.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.selector_keyactive_onetap_normal));
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

        scaleTitleImage(findViewById(view_img_title));

        logOneTimeGA(app_accessibility_setkey_screen_viewed);
    }


    private void autoSetupKeyboard() {
        shouldShowEnableDialog = true;

        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, 0);

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

        if (shouldShowEnableDialog && !HSAccessibilityService.isAvailable()) {

            new KCAlert.Builder(this)
                    .setTitle(getString(R.string.alert_enable_access_warn_title))
                    .setMessage(getString(R.string.alert_enable_access_warn_content))
                    .setTopImageResource(R.drawable.accessibility_alert_top_image)
                    .setPositiveButton(getString(R.string.enable), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            logOneTimeGA(app_alert_auto_setkey_enable_clicked);
                            autoSetupKeyboard();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            logOneTimeGA(app_alert_auto_setkey_showed);
        }
    }

    public static void scaleTitleImage(View view) {

        Display display = HSFloatWindowManager.getInstance().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        if (HSApplication.getContext().getResources().getBoolean(R.bool.isTablet)) {
            width = (int) (width * 0.85);
        }
        view.getLayoutParams().width = view.getLayoutParams().height = width;
    }


}
