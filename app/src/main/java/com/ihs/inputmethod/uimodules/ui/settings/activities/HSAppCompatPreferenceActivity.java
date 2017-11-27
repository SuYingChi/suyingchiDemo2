package com.ihs.inputmethod.uimodules.ui.settings.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;

import com.ihs.app.framework.HSSessionMgr;
import com.ihs.app.framework.activity.IDialogHolder;
import com.kc.commons.utils.KCCommonUtils;

/**
 * Created by xu.zhang on 9/23/16.
 */

public class HSAppCompatPreferenceActivity extends AppCompatPreferenceActivity implements IDialogHolder {
    protected boolean isBackPressed = false;
    private AlertDialog dialog;


    /**
     * called on HSActivity created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSSessionMgr.onActivityCreate(this);
    }

    /**
     * called on HSActivity destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialog();
        HSSessionMgr.onActivityDestroy(this);
    }


    /**
     * called on HSActivity started
     */
    @Override
    protected void onStart() {
        super.onStart();
        isBackPressed = false;
        HSSessionMgr.onActivityStart(this);
    }


    /**
     * called on HSActivity stopped
     */
    @Override
    protected void onStop() {
        super.onStop();
        HSSessionMgr.onActivityStop(this, isBackPressed);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            isBackPressed = false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * ATTENTION!!! If you really have to override onBackPressed() in any subclass, and somehow you still want to exit
     * your activity on back key pressed, then remember to call super.onBackPressed() instead of simply calling
     * finish(); Otherwise, the Back key event cannot be recorded and handled by our HSActivity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    /**
     * show dialog safely
     */
    @Override
    public boolean showDialog(AlertDialog alertDialog) {
        this.dismissDialog();

        this.dialog = alertDialog;
        KCCommonUtils.showDialog(dialog);
        return true;
    }

    /**
     * dismiss dialog safely, the purpose of this method is to avoid crashes that happen when the activity is killed but
     * the alert dialog is still showing
     */
    @Override
    public void dismissDialog() {
        if (this.dialog != null) {
            KCCommonUtils.dismissDialog(dialog);
            this.dialog = null;
        }
    }
}
