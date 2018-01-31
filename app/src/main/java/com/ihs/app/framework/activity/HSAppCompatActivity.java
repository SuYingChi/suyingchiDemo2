package com.ihs.app.framework.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSSessionMgr;

public class HSAppCompatActivity extends AppCompatActivity implements IDialogHolder {
    private boolean isBackPressed = false;
    private AlertDialog dialog;

    public HSAppCompatActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(1);
        HSSessionMgr.onActivityCreate(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        this.dismissDialog();
        HSSessionMgr.onActivityDestroy(this);
    }

    protected void onStart() {
        super.onStart();
        this.isBackPressed = false;
        HSSessionMgr.onActivityStart(this);
    }

    protected void onStop() {
        super.onStop();
        HSSessionMgr.onActivityStop(this, this.isBackPressed);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && event.getRepeatCount() == 0) {
            this.isBackPressed = false;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Exception var2) {
            HSAnalytics.logEvent("onBackPressedCrash");
            var2.printStackTrace();
        }

        this.isBackPressed = true;
    }

    public boolean showDialog(AlertDialog alertDialog) {
        this.dismissDialog();
        if (!this.isFinishing()) {
            this.dialog = alertDialog;
            this.dialog.show();
            return true;
        } else {
            HSAnalytics.logEvent("HSAppFramworkError", new String[]{"ShowAlertAcitivityError", this.getClass().getName()});
            return false;
        }
    }

    public void dismissDialog() {
        if (this.dialog != null) {
            this.dialog.dismiss();
            this.dialog = null;
        }

    }
}