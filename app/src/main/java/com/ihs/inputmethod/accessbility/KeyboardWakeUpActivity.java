package com.ihs.inputmethod.accessbility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;

import static android.view.View.GONE;


public class KeyboardWakeUpActivity extends HSActivity {

    private BroadcastReceiver methodChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkShouldAutoSelectKeyboard();
            }
        });
    }

    private void checkShouldAutoSelectKeyboard() {
        if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            if (HSAccessibilityService.isAvailable()) {

                final AccessibilityEventListener accessibilityEventListener = new AccessibilityEventListener(AccessibilityEventListener.MODE_SETUP_KEYBOARD);
                final int listenerKey = HSAccessibilityService.registerEventListener(accessibilityEventListener);

                final IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
                methodChangeReceiver = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) {
                            if (HSInputMethodListManager.isMyInputMethodSelected()) {
                                HSAccessibilityService.unregisterEvent(listenerKey);
                                accessibilityEventListener.onDestroy();


                                View coverView = HSFloatWindowManager.getInstance().getCoverView();

                                if (coverView != null) {
                                    coverView.findViewById(R.id.progressBar).setVisibility(GONE);
                                    coverView.findViewById(R.id.iv_succ).setVisibility(View.VISIBLE);
                                    ((TextView) coverView.findViewById(R.id.tv_settings_item)).setText(R.string.access_set_up_success);
                                }

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        HSFloatWindowManager.getInstance().removeAccessibilityCover();
                                    }
                                }, 1000);
                                finish();
                                startThemeHomeActivity();
                            }
                        }
                    }
                };
                registerReceiver(methodChangeReceiver, filter);
                try {
                    accessibilityEventListener.onAvailable();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    startThemeHomeActivity();
                }

            } else {
                startThemeHomeActivity();
            }
        }
    }

    private void startThemeHomeActivity() {
        Intent startThemeHomeIntent = getIntent();
        if (startThemeHomeIntent == null) {
            startThemeHomeIntent = new Intent();
        }
        startThemeHomeIntent.setClass(this, ThemeHomeActivity.class);
        startActivity(startThemeHomeIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if(methodChangeReceiver!=null){
            unregisterReceiver(methodChangeReceiver);
        }
        super.onDestroy();
    }
}
