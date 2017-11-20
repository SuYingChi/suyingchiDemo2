package com.ihs.inputmethod.uimodules.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

/**
 * Created by jixiang on 17/11/20.
 */

public class DialogUtils {

    public static void safeShowDialog(Dialog dialog, Activity activity){
        if (dialog != null && !dialog.isShowing() && !activity.isFinishing()){
            dialog.show();
        }
    }


    public static void safeDismissDialog(Dialog dialog, Context context){
        if (context instanceof Activity){
            safeDismissDialog(dialog,(Activity)context);
        }else {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
    public static void safeDismissDialog(Dialog dialog, Activity activity){
        if (dialog != null && dialog.isShowing() && !activity.isFinishing()){
            dialog.dismiss();
        }
    }

}
