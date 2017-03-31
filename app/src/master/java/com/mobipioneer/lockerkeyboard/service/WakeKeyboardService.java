package com.mobipioneer.lockerkeyboard.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.dialogs.HSAlertDialog;
import com.ihs.inputmethod.uimodules.constants.Constants;
import com.mobipioneer.lockerkeyboard.SplashActivity;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by chenyuanming on 14/03/2017.
 */

public class WakeKeyboardService extends Service {
    Timer timer = new Timer();
    HSPreferenceHelper preferenceHelper = HSPreferenceHelper.create(this, "keyboard");
    private static final String SP_KEY_LAST_KEYBOARD_CHANGE = "lastKeyboardChange";
    private static final String SP_KEY_LAST_KEYBOARD_AlERT_SHOW = "lastKeyboardAlertShow";
    long keyboardChangeRemindInterval = (long) (HSConfig.optFloat(Integer.MAX_VALUE, "Application", "RemindChangeKeyboard", "RemindWhenNotActive") * 24 * 3600 * 1000);
    long keyboardAlertRemindInterval = (long) HSConfig.optFloat(Integer.MAX_VALUE, "Application", "RemindChangeKeyboard", "RemindInterval") * 24 * 3600 * 1000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    String defaultInputMethodServiceName = "";
    String defaultInputMethodPackageName = "";
    private static final int SHOW_KEY_BOARD = 1;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_KEY_BOARD:
                    onShowKeyboard();
                    break;
            }
        }
    };

    private void onShowKeyboard() {
        long lastKeyboardAlertShowTime = preferenceHelper.getLong(SP_KEY_LAST_KEYBOARD_AlERT_SHOW, System.currentTimeMillis());
        long lastKeyboardChangeTime = preferenceHelper.getLong(SP_KEY_LAST_KEYBOARD_CHANGE, System.currentTimeMillis());

        if (lastKeyboardAlertShowTime > lastKeyboardChangeTime) {
            long seconds = (System.currentTimeMillis() - lastKeyboardAlertShowTime) / 1000;
            HSLog.d("KeyboardService", String.format("键盘alert超过:%dd %dh %dm %ds", seconds/60/60/24, (seconds/60/60)%24,(seconds/60)%60,seconds));
            if (System.currentTimeMillis() - lastKeyboardAlertShowTime > keyboardAlertRemindInterval) {
                //键盘未切换过，点击later超过RemindInterval天
                showAlert();
            }
        } else {
            //从本键盘切换到别的键盘超过RemindWhenNotActive天
            long seconds = (System.currentTimeMillis() - lastKeyboardChangeTime) / 1000;
            HSLog.d("KeyboardService", String.format("键盘切走超过:%dd %dh %dm %ds", seconds/60/24, (seconds/60/60)%24,(seconds/60)%60,seconds));
            if (System.currentTimeMillis() - lastKeyboardChangeTime > keyboardChangeRemindInterval) {
                //键盘未切换过，点击later超过RemindInterval天
                showAlert();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean enable = HSConfig.optBoolean(false, "Application", "RemindChangeKeyboard", "Enable");
        if (!enable) {
            stopSelf();
        }
        if (preferenceHelper.getLong(SP_KEY_LAST_KEYBOARD_CHANGE, 0) == 0) {
            recordDefaultKeyboardChange();
        }
        if (preferenceHelper.getLong(SP_KEY_LAST_KEYBOARD_AlERT_SHOW, 0) == 0) {
            recordKeyboardShow();
        }
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();
                for (int i = 0; i < mInputMethodProperties.size(); i++) {
                    InputMethodInfo imi = mInputMethodProperties.get(i);
                    if (imi.getId().equals(Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {
                        //imi contains the information about the keyboard you are using
                        if (TextUtils.isEmpty(defaultInputMethodServiceName)) {
                            defaultInputMethodServiceName = imi.getServiceName();
                            defaultInputMethodPackageName = imi.getPackageName();
                        } else if (!imi.getServiceName().equals(defaultInputMethodServiceName)) {
                            if (getPackageName().equals(defaultInputMethodPackageName)) {
                                //键盘即将被切走
                                recordDefaultKeyboardChange();
                            }
                            defaultInputMethodServiceName = imi.getServiceName();
                            defaultInputMethodPackageName = imi.getPackageName();

                        }

                        if (getPackageName().equals(imi.getPackageName())) {
                            //当前键盘
                            return;
                        }
                        break;
                    }
                }

                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(100);
                if (runningServices != null) {
                    for (ActivityManager.RunningServiceInfo runningService : runningServices) {
                        String className = runningService.service.getClassName();
                        if (className.equals(defaultInputMethodServiceName)) {
                            if (!isEqual(runningServiceInfo, runningService) && runningService.clientCount > 1) {
                                runningServiceInfo = runningService;
                                handler.sendEmptyMessage(SHOW_KEY_BOARD);
                            }
                            break;
                        }
                    }
                }

            }
        };

        timer.schedule(timerTask, 0, 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    private void recordDefaultKeyboardChange() {
        HSLog.d("KeyboardService", "键盘切走时间:" + getDate());
        preferenceHelper.putLong(SP_KEY_LAST_KEYBOARD_CHANGE, System.currentTimeMillis());
    }

    private String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    }

    private void recordKeyboardShow() {
        HSLog.d("KeyboardService", "键盘Alert时间:" + getDate());
        preferenceHelper.putLong(SP_KEY_LAST_KEYBOARD_AlERT_SHOW, System.currentTimeMillis());
    }

    android.support.v7.app.AlertDialog alertDialog;
    private ActivityManager.RunningServiceInfo runningServiceInfo;

    private void showAlert() {
        if (alertDialog == null) {
            alertDialog = HSAlertDialog.build().setTitle("More Emojis")
                    .setMessage("Want to try more emojis and keyboard themes?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Context context = WakeKeyboardService.this;
                            Intent intent = new Intent(context, SplashActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(Constants.BUNDLE_AUTO_ENABLE_KEYBOARD,true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    }).setNegativeButton("Later", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            recordKeyboardShow();
                        }
                    }).create();
        }

        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
        recordKeyboardShow();

    }

 

    public boolean isEqual(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            return false;
        }
        Field[] fields1 = getDeclaredFields(obj1);
        Field[] fields2 = getDeclaredFields(obj2);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields1.length; i++) {
            Object val1 = getFieldValue(obj1, fields1, i);
            Object val2 = getFieldValue(obj2, fields2, i);
            if (val1 != null && val2 != null) {
                if (!val1.equals(val2)) {
                    String fieldName = getField(fields1, i).getName();
                    sb.append(String.format(",%s,%s-->%s", fieldName, val1.toString(), val2.toString()));
                }
            }

        }

        boolean result = sb.toString().length() == 0;
        if (!result) {
            System.out.println("key:" + sb.toString());
        }
        return result;
    }

    private Object getFieldValue(Object caller, Field[] fields, int index) {
        Field field = getField(fields, index);
        try {
            return field.get(caller);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Field getField(Field[] fields, int index) {
        Field field = fields[index];
        try {
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return field;
    }

    private Field[] getDeclaredFields(Object obj1) {
        return obj1.getClass().getDeclaredFields();
    }

}
