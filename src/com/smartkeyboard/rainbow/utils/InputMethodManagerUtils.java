package com.smartkeyboard.rainbow.utils;

import android.content.Context;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

public class InputMethodManagerUtils {

    public static boolean isCurrentIMEEnabled(Context context) {
        InputMethodManager mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        for (final InputMethodInfo imi : mImm.getEnabledInputMethodList()) {
            if (imi.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCurrentIMESelected(Context context) {

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        for (final InputMethodInfo imi : imm.getInputMethodList()) {
            if (imi.getId().equals(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {
                return imi.getPackageName().equals(context.getPackageName());
            }
        }
        return false;
    }

    public static void openInputMethodPicker(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showInputMethodPicker();
    }

}
