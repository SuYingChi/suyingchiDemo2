package com.mobipioneer.lockerkeyboard.accessbility;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.mobipioneer.lockerkeyboard.KeyboardActivationActivity;
import com.mobipioneer.lockerkeyboard.app.CustomViewDialog;
import com.mobipioneer.lockerkeyboard.app.MainActivity;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.mobipioneer.lockerkeyboard.KeyboardActivationActivity.scaleTitleImage;


/**
 * 本service只适用设置键盘 增加新功能需重新支持
 * <p>
 * Arthur 2016/12/24
 * <p>
 * API16及以上适用 4.1
 */
@TargetApi(16)
public class SetupKeyboardAccessibilityService {

    private static final int IME_STATE_NOT_ENABLE = -1;
    private static final int IME_STATE_ENABLED = 0;
    private static final int IME_STATE_SELECTED = 1;

    private ImeSettingsContentObserver settingsContentObserver = new ImeSettingsContentObserver();
    private boolean isScrolled;
    private int imeSettingState = IME_STATE_NOT_ENABLE;
    private boolean inputMethodEnabled;


    private AccessibilityNodeInfo rootWindow;


    private Context context;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    removeMessages(100);
                    retry();
                    break;
            }
        }
    };

    private boolean stopSelf = false;

    public SetupKeyboardAccessibilityService() {
        context = HSApplication.getContext();

        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_INPUT_METHODS), false,
                settingsContentObserver);

        onServiceConnected();
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (stopSelf) {
            return;
        }

        HSLog.e(event.toString());

        rootWindow = HSAccessibilityService.getInstance().getRootInActiveWindow();
        if (rootWindow == null) {
            return;
        }

        if (rootWindow.getChild(0) == null && event.getSource() != null) {
            rootWindow = event.getSource();
        }

        if (!inputMethodEnabled && HSInputMethodListManager.isMyInputMethodEnabled()) {
            inputMethodEnabled = true;
            imeSettingState++;
            InputMethodManager m = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            m.showInputMethodPicker();
            return;
        }

        if (!event.getPackageName().toString().contains("android")) {
            if (!HSInputMethodListManager.isMyInputMethodEnabled()) {
                Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_NO_HISTORY);
                HSApplication.getContext().startActivity(intent);
            }
            return;
        }

        String keyboardName = context.getString(R.string.english_service_label);
        if (!TextUtils.isEmpty(event.getClassName())) {
            String className = event.getClassName().toString().toLowerCase();
            //如果是对话框弹出 并且 内容包含键盘名字，则点击ok
            if (className.contains("dialog")) {
                List<AccessibilityNodeInfo> nodeInfoList = rootWindow.findAccessibilityNodeInfosByText(context.getString(R.string.english_ime_name));
                if (nodeInfoList.size() > 0) {
                    findAndPerformActionButton(context.getString(android.R.string.ok));
                }

            } else {

                List<AccessibilityNodeInfo> nodeInfos = rootWindow.findAccessibilityNodeInfosByText(keyboardName);
                AccessibilityNodeInfo andScroll = findAndScroll(rootWindow);
                int loopCount = 0;
                while (nodeInfos.size() < 1 && loopCount < 10) {
                    isScrolled = false;
                    if (andScroll != null) {
                        andScroll.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                    }
                    nodeInfos = rootWindow.findAccessibilityNodeInfosByText(keyboardName);
                    loopCount++;
                }


                if (nodeInfos.size() > 0) {
                    AccessibilityNodeInfo givenTextNode = nodeInfos.get(0);
                    checkFoundNode(givenTextNode);
                }
            }
        }

    }

    private AccessibilityNodeInfo findAndScroll(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null || isScrolled) {
            return null;
        }
        if (nodeInfo.isScrollable()) {
            isScrolled = true;
            return nodeInfo;
        } else {
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo andScroll = findAndScroll(nodeInfo.getChild(i));
                if (andScroll != null) {
                    return andScroll;
                }
            }
        }
        return null;
    }


    public void onInterrupt() {
        HSLog.e("onInterrupt");
    }


    public void onServiceConnected() {
        HSLog.e("onServiceConnected");
        HSFloatWindowManager instance = HSFloatWindowManager.getInstance();
        instance.showAccessibilityCover(R.layout.layout_accessbility_cover);
        scaleTitleImage(instance.getCoverView().findViewById(R.id.view_img_title));

        imeSettingState = IME_STATE_NOT_ENABLE;
        isScrolled = false;
        inputMethodEnabled = false;
        stopSelf = false;

        handler.sendEmptyMessageDelayed(100, 10000);
    }

    private void retry() {
        final View dialogView = View.inflate(context, R.layout.dialog_enable_accessbility_warn, null);


        final CustomViewDialog warnDialog = new CustomViewDialog(context);
        warnDialog.setContentView(dialogView);
        warnDialog.setCanceledOnTouchOutside(true);


        TextView tv_title = (TextView) dialogView.findViewById(R.id.alertTitle);
        tv_title.setText(R.string.access_set_up_failed);

        TextView tv_message = (TextView) dialogView.findViewById(R.id.message);
        tv_message.setText(R.string.dialog_set_up_manual_message);

        TextView tv_cancel = (TextView) dialogView.findViewById(R.id.tv_cancel);
        tv_cancel.setText(R.string.dialog_access_setup_manual);
        tv_cancel.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.settings_background_color));
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_alert_setup_failed_manual_clicked");

                stopSelf = true;
                handler.removeCallbacksAndMessages(null);
                HSFloatWindowManager.getInstance().removeAccessibilityCover();
                Intent intent = new Intent(HSApplication.getContext(), MainActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                warnDialog.dismiss();
            }
        });

        TextView tv_confirm = (TextView) dialogView.findViewById(R.id.tv_confirm);
        tv_confirm.setText(R.string.retry);
        tv_confirm.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.selector_keyactive_enable));
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_alert_setup_failed_retry_clicked");
                warnDialog.dismiss();
                onServiceConnected();
            }
        });

        warnDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                HSFloatWindowManager.getInstance().removeAccessibilityCover();
                Intent intent = new Intent(HSApplication.getContext(), KeyboardActivationActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                stopSelf = true;
                handler.removeCallbacksAndMessages(null);
            }
        });

        warnDialog.show();
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_alert_setup_failed_show");
    }

    private void findAndPerformActionButton(String text) {
        if (rootWindow == null)
            return;
        //通过文字找到当前的节点
        List<AccessibilityNodeInfo> nodes = rootWindow.findAccessibilityNodeInfosByText(text);
        for (int i = 0; i < nodes.size(); i++) {
            AccessibilityNodeInfo node = nodes.get(i);
            // 执行按钮点击行为
            if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }


    private class ImeSettingsContentObserver extends ContentObserver {


        private ImeSettingsContentObserver() {
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
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager m = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        m.showInputMethodPicker();
                    }
                }, 1);

                try {
                    if (settingsContentObserver != null) {
                        context.getContentResolver().unregisterContentObserver(settingsContentObserver);
//                        HSLog.d("unregister settings content observer");
                    }
                } catch (IllegalArgumentException ex) {
//                    HSLog.e("content observer not registered yet");
                }
            }
        }
    }

    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);

        try {
            if (settingsContentObserver != null)
                context.getContentResolver().unregisterContentObserver(settingsContentObserver);
        } catch (IllegalArgumentException ex) {
//            HSLog.e("content observer not registered yet");
        }
    }


    private void checkFoundNode(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        if (inputMethodEnabled && imeSettingState > IME_STATE_ENABLED) {
            return;
        } else if (!inputMethodEnabled && imeSettingState > IME_STATE_NOT_ENABLE) {
            return;
        }


        if (loopChildForCheckable(nodeInfo) == -1) {
            checkFoundNode(nodeInfo.getParent());
        }
    }

    private int loopChildForCheckable(AccessibilityNodeInfo nodeInfo) {

        if (inputMethodEnabled && imeSettingState > IME_STATE_ENABLED) {
            return 1;
        } else if (!inputMethodEnabled && imeSettingState > IME_STATE_NOT_ENABLE) {
            return 1;
        }

        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = nodeInfo.getChild(i);
            if (child.getClassName().toString().contains("ayout")) {
                int childResult = loopChildForCheckable(child);
                if (childResult != -1) {
                    return childResult;
                }
            } else if (child.isCheckable()) {
                if (!child.isChecked()) {
                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (!child.isChecked()) {
                        clickParent(child);
                    } else {
                        return 1;
                    }
                    return 1;
                } else {
                    if (!inputMethodEnabled) {
                        imeSettingState++;
                        if (imeSettingState == IME_STATE_ENABLED) {
                            inputMethodEnabled = true;
                        }

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager m = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                m.showInputMethodPicker();
                            }
                        }, 10);

                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        }
        return -1;
    }

    private void clickParent(AccessibilityNodeInfo child) {
        if (inputMethodEnabled && imeSettingState > IME_STATE_ENABLED) {
            return;
        } else if (!inputMethodEnabled && imeSettingState > IME_STATE_NOT_ENABLE) {
            return;
        }

        AccessibilityNodeInfo parent = child.getParent();
        if (parent.isClickable()) {
            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            imeSettingState++;
            if (imeSettingState == IME_STATE_ENABLED) {
                inputMethodEnabled = true;
            }
        } else {
            clickParent(parent);
        }
    }

}
