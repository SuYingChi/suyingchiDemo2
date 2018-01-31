package com.ihs.commons.utils;

import android.util.Log;

/**
 * Created by Arthur on 18/1/17.
 */

public class HSLog {

    public static void d(String s) {
        Log.d("log", s);
    }

    public static void i(String s) {
        Log.i("log", s);
    }

    public static void e(String s) {
        Log.e("log", s);
    }

    public static void w(String s) {
        Log.w("log", s);
    }

    public static boolean isDebugging() {
        return true;
    }

    public static void d(String jxg, String format) {
    }

    public static void e(String panelCOntainer, String s) {
    }

    public static void v(String tag, String s) {

    }

    public static void i(String tag, String s) {

    }

    public static void w(String tag, String s) {

    }
}
