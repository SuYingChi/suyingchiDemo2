package com.ihs.inputmethod.uimodules.constants;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.dialogs.HSAlertDialog;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.uimodules.R;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

/**
 * Created by Arthur on 16/12/2.
 */

public class KeyboardActivationProcessor {


    public interface OnKeyboardActivationChangedListener {

        void activeDialogShowing();

        void keyboardSelected(int requestCode);

        void activeDialogCanceled();

        void activeDialogDismissed();
    }

    private static int activationRequestCode = -1;

    public final static String BUNDLE_ACTIVATION_CODE = "bundle_activation_code";
    public final static String PREF_THEME_HOME_SHOWED = "pref_theme_home_showed";

    private static final String GA_PARAM_ACTION_APP_STEP_ONE_CLICKED = "app_step_one_clicked";
    private static final String GA_PARAM_ACTION_APP_STEP_ONE_ENABLED = "app_step_one_enabled";
    private static final String GA_PARAM_ACTION_APP_STEP_TWO_CLICKED = "app_step_two_clicked";
    private static final String GA_PARAM_ACTION_APP_STEP_TWO_ENABLED = "app_step_two_enabled";
    private final static String APP_STEP_ONE_HINT_CLICKED = "app_step_one_hint_clicked";
    private final static String APP_STEP_ONE_HINT = "app_step_one_hint";
    private static final int HANDLER_SHOW_KEYBOARD_PICKER = 100;
    private static final int HANDLER_SHOW_PRIVACY_ALERT = 101;

    private Class activityClass;

    private Context context = HSApplication.getContext();

    private OnKeyboardActivationChangedListener onKeyboardActivationChangedListener;
    private boolean versionFilterForRecordEvent;
    private SharedPreferences mPrefs;
    private ImeSettingsContentObserver settingsContentObserver;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_SHOW_KEYBOARD_PICKER) {
                InputMethodManager m = (InputMethodManager) HSApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                m.showInputMethodPicker();

                if (versionFilterForRecordEvent && !isEventRecorded(GA_PARAM_ACTION_APP_STEP_TWO_CLICKED)) {
                    //记第二步点击的时候，如果还没有记第一步点击或第一步enable, 就补上
                    if (!isEventRecorded(GA_PARAM_ACTION_APP_STEP_ONE_CLICKED)) {
                        return;
                    }
                    if (!isEventRecorded(GA_PARAM_ACTION_APP_STEP_ONE_ENABLED)) {
                        return;
                    }
                    if (!isEventRecorded(APP_STEP_ONE_HINT_CLICKED) || !isEventRecorded(APP_STEP_ONE_HINT)) {
                        return;
                    }
                    setEventRecorded(GA_PARAM_ACTION_APP_STEP_TWO_CLICKED);
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_PARAM_ACTION_APP_STEP_TWO_CLICKED);
                }

                Toast toast = Toast.makeText(context, R.string.toast_select_keyboard, Toast.LENGTH_LONG);
                toast.show();
            } else if (msg.what == HANDLER_SHOW_PRIVACY_ALERT) {
                showKeyboardEnableDialog();
            }
        }
    };


    public KeyboardActivationProcessor(Class activityClass, OnKeyboardActivationChangedListener onKeyboardActivationChangedListener) {
        this.activityClass = activityClass;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        HSApplication.HSLaunchInfo firstLaunchInfo = HSApplication.getFirstLaunchInfo();
        versionFilterForRecordEvent = (firstLaunchInfo.appVersionCode >= HSApplication.getCurrentLaunchInfo().appVersionCode);
        this.onKeyboardActivationChangedListener = onKeyboardActivationChangedListener;

        settingsContentObserver = new ImeSettingsContentObserver(new Handler());

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
        this.context.registerReceiver(imeChangeRecevier, filter);
    }


    public void activateKeyboard(Activity activity,boolean showActivationDialog, int activateRequestCode) {
        activationRequestCode = activateRequestCode;

        if (HSInputMethod.isCurrentIMESelected()) {
            onKeyboardActivationChangedListener.keyboardSelected(activationRequestCode);
            return;
        }

        if (showActivationDialog) {
            showApplyActiveKeyDialog(activity);
        } else {
            enableOrSelectKeyboard(activity);
        }
    }

    public static void setActivationRequestCode(int requestCode) {
        activationRequestCode = requestCode;
    }

    private void showApplyActiveKeyDialog(final Activity activity) {

        AlertDialog alertDialog = HSAlertDialog.build().setTitle(context.getString(R.string.dialog_title_select_keyboard_apply_rain))
                .setMessage(context.getResources().getString(R.string.dialog_msg_select_keyboard_apply_rain))
                .setPositiveButton(context.getResources().getString(R.string.dialog_confirm_select_keyboard_apply_rain), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("activate_alert_clicked");
                        enableOrSelectKeyboard(activity);
                    }
                }).create();
        alertDialog.show();
        onKeyboardActivationChangedListener.activeDialogShowing();
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onKeyboardActivationChangedListener.activeDialogCanceled();
            }
        });
        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("activate_alert_show");
    }

    private void enableOrSelectKeyboard(Activity activity) {

        if (!HSInputMethod.isCurrentIMEEnabled()) {
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_INPUT_METHODS), false,
                    settingsContentObserver);

            Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            if (activity != null) {
                activity.startActivity(intent);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_NO_HISTORY );
                context.startActivity(intent);
            }

            handler.sendEmptyMessageDelayed(HANDLER_SHOW_PRIVACY_ALERT, 500);

            if (versionFilterForRecordEvent && !isEventRecorded(GA_PARAM_ACTION_APP_STEP_ONE_CLICKED)) {
                setEventRecorded(GA_PARAM_ACTION_APP_STEP_ONE_CLICKED);
                HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_PARAM_ACTION_APP_STEP_ONE_CLICKED);
            }
        } else {
            handler.sendEmptyMessageDelayed(HANDLER_SHOW_KEYBOARD_PICKER, 200);
        }
    }


    private void showKeyboardEnableDialog() {
        AlertDialog alertDialog = HSAlertDialog.build().setTitle(context.getString(R.string.toast_enable_keyboard))
                .setMessage(context.getResources().getString(R.string.alert_attention_messenger))
                .setPositiveButton("GOT IT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (versionFilterForRecordEvent && !isEventRecorded(APP_STEP_ONE_HINT_CLICKED)) {
                            setEventRecorded(APP_STEP_ONE_HINT_CLICKED);
                            HSGoogleAnalyticsUtils.getInstance().logAppEvent(APP_STEP_ONE_HINT_CLICKED);
                        }

                        dialog.dismiss();


                        ImageView imageCodeProject = new ImageView(context);
                        imageCodeProject.setBackgroundResource(R.drawable.toast_enable_rain);
                        CustomViewDialog customViewDialog = new CustomViewDialog(imageCodeProject, 3000, Gravity.BOTTOM, 0, HSDisplayUtils.dip2px(20));
                        customViewDialog.show();
                    }
                }).create();
        alertDialog.show();
        if (versionFilterForRecordEvent && !isEventRecorded(APP_STEP_ONE_HINT)) {
            setEventRecorded(APP_STEP_ONE_HINT);
            HSGoogleAnalyticsUtils.getInstance().logAppEvent(APP_STEP_ONE_HINT);
        }
    }

    public void showHomePageActivationDialog(final Activity activity) {
        AlertDialog alertDialog = HSAlertDialog.build().setTitle(context.getString(R.string.dialog_title_enable_keyboard_home_rain))
                .setMessage(context.getResources().getString(R.string.dialog_msg_enable_keyboard_home_rain))
                .setPositiveButton(context.getResources().getString(R.string.dialog_confirm_select_keyboard_apply_rain), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("activate_alert_clicked");
                        enableOrSelectKeyboard(activity);
                    }
                }).create();
        alertDialog.show();

        onKeyboardActivationChangedListener.activeDialogShowing();

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onKeyboardActivationChangedListener.activeDialogCanceled();
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onKeyboardActivationChangedListener.activeDialogDismissed();
            }
        });
        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("activate_alert_show");
    }


    private class ImeSettingsContentObserver extends ContentObserver {

        private ImeSettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (HSInputMethod.isCurrentIMEEnabled()) {
                Intent i = new Intent(context, activityClass);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                HSToastUtils.cancel();
                context.startActivity(i);
                handler.sendEmptyMessageDelayed(HANDLER_SHOW_KEYBOARD_PICKER, 700);
                if (versionFilterForRecordEvent && !isEventRecorded(GA_PARAM_ACTION_APP_STEP_ONE_ENABLED)) {
                    setEventRecorded(GA_PARAM_ACTION_APP_STEP_ONE_ENABLED);
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_PARAM_ACTION_APP_STEP_ONE_ENABLED);
                }
                try {
                    if (settingsContentObserver != null) {
                        context.getContentResolver().unregisterContentObserver(settingsContentObserver);
                        HSLog.d("unregister settings content observer");
                    }
                } catch (IllegalArgumentException ex) {
                    HSLog.e("content observer not registered yet");
                }
            }
        }
    }

    private BroadcastReceiver imeChangeRecevier = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) {
                if (HSInputMethod.isCurrentIMESelected()) {
                    onKeyboardActivationChangedListener.keyboardSelected(activationRequestCode);
                    if (versionFilterForRecordEvent && !isEventRecorded(GA_PARAM_ACTION_APP_STEP_TWO_ENABLED)) {
                        if (isEventRecorded(GA_PARAM_ACTION_APP_STEP_ONE_CLICKED)
                                && isEventRecorded(GA_PARAM_ACTION_APP_STEP_ONE_ENABLED)
                                && isEventRecorded(APP_STEP_ONE_HINT_CLICKED)
                                && isEventRecorded(APP_STEP_ONE_HINT)
                                && isEventRecorded(GA_PARAM_ACTION_APP_STEP_TWO_CLICKED)) {
                            setEventRecorded(GA_PARAM_ACTION_APP_STEP_TWO_ENABLED);
                            HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_PARAM_ACTION_APP_STEP_TWO_ENABLED);
                        }
                    }
                }
            }
        }
    };

    public void release() {

        try {
            context.unregisterReceiver(imeChangeRecevier);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (settingsContentObserver != null) {
                context.getContentResolver().unregisterContentObserver(settingsContentObserver);
                HSLog.d("unregister settings content observer");
            }
            settingsContentObserver = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        handler.removeCallbacksAndMessages(null);
    }

    private void setEventRecorded(String pref_name) {
        mPrefs.edit().putBoolean(pref_name, true).apply();
    }

    private boolean isEventRecorded(String pref_name) {
        return mPrefs.getBoolean(pref_name, false);
    }

    public static class CustomViewDialog extends Dialog {
        private int gravity, xOffset, yOffset;

        public CustomViewDialog(View contentView, int duration) {
            super(HSApplication.getContext(), R.style.CustomToastDialog);
            init(contentView, duration, Gravity.CENTER, 0, 0);
        }


        public CustomViewDialog(View contentView, int duration, int gravity, int xOffset, int yOffset) {
            super(HSApplication.getContext(), R.style.CustomToastDialog);
            init(contentView, duration, gravity, xOffset, yOffset);
        }

        private void init(View contentView, int duration, int gravity, int xOffSet, int yOffSet) {
            setContentView(contentView);
            setTitle(null);
            contentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CustomViewDialog.this.cancel();
                }
            }, duration);
            setCanceledOnTouchOutside(true);

            this.gravity = gravity;
            this.xOffset = xOffSet;
            this.yOffset = yOffSet;
        }


        @Override
        public void show() {
            Window window = getWindow();
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                WindowManager.LayoutParams params = window.getAttributes();
                params.y = yOffset;
                params.x = xOffset;
                window.setGravity(gravity);
                window.setAttributes(params);
                window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            super.show();
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            return onTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            cancel();
            return false;
        }

    }
}
