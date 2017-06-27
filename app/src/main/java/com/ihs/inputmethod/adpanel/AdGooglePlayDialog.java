package com.ihs.inputmethod.adpanel;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Window;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by yanxia on 2017/6/26.
 */

public class AdGooglePlayDialog extends Dialog {

    public AdGooglePlayDialog(@NonNull Context context) {
        super(context, R.style.Theme_AppCompat_Light_Dialog);
        init();
    }

    public AdGooglePlayDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        init();
    }

    private void init() {
        setCanceledOnTouchOutside(true);
    }

    /**
     * Similar to {@link Activity#onCreate}, you should initialize your dialog
     * in this method, including calling {@link #setContentView}.
     *
     * @param savedInstanceState If this dialog is being reinitialized after a
     *                           the hosting activity was previously shut down, holds the result from
     *                           the most recent call to {@link #onSaveInstanceState}, or null if this
     *                           is the first time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Start the dialog and display it on screen.  The window is placed in the
     * application layer and opaque.  Note that you should not override this
     * method to do initialization when the dialog is shown, instead implement
     * that in {@link #onStart}.
     */
    @Override
    public void show() {
        try {
            Window window = getWindow();
            if (!(getContext() instanceof Activity) && window != null) {
                window.setLayout((int) (DisplayUtils.getDisplay().getWidth() * 0.96), window.getAttributes().height);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(HSApplication.getContext())) {
                    window.setType(WindowManager.LayoutParams.TYPE_TOAST);
                } else {
                    window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
                }
            }
            super.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
