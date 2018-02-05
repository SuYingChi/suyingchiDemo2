package com.ihs.inputmethod.accessbility;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.Window;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by Arthur
 * <p>
 * <p>
 */


public class CustomViewDialog extends Dialog {


    public CustomViewDialog(@NonNull Context context) {
        super(context, R.style.Theme_AppCompat_Light_Dialog);
        init();
    }

    private void init() {
        setCanceledOnTouchOutside(false);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public CustomViewDialog(@NonNull Context context, @StyleRes int themeResId) {
//        super(context, themeResId);
//        init();
//
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public CustomViewDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
//        super(context, cancelable, cancelListener);
//        init();
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

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
