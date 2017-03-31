package com.ihs.booster.utils;

import android.text.TextUtils;

import com.ihs.commons.utils.HSLog;

public class L {
    private static final String LOG_TAG = "MBLog";
    private static final int timeCosumingBlankStringMaxLength = 130;
    private static boolean LOG_ENABLE = HSLog.isDebugging();
    private static long startTime = System.nanoTime();
    private static long lastStopTime = System.nanoTime();
    private static String lastFileName = "";

    public static void line(String info) {
        if (LOG_ENABLE) {
            try {
                final StackTraceElement[] stack = new Throwable().getStackTrace();
                final StackTraceElement ste = stack[1];
                String fileName = ste.getFileName();
                if (!TextUtils.isEmpty(fileName) && fileName.equalsIgnoreCase(lastFileName)) {
                    fileName = getBlankString(lastFileName.length());
                } else {
                    lastFileName = fileName;
                }
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(
                        LOG_TAG,
                        String.format("-----------------------------===========[%s][%s]%s[%s]==========----------------------------------", fileName, ste.getMethodName(),
                                ste.getLineNumber(), info));
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, " ");
            } catch (Exception e) {
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, "-----------------------------=====================----------------------------------");
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, " ");
                android.util.Log.d(LOG_TAG, " ");
            }
        }
    }

    private static String getBlankString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static void l(String info) {
        if (LOG_ENABLE) {
            try {
                final StackTraceElement[] stack = new Throwable().getStackTrace();
                final StackTraceElement ste = stack[1];
                String fileName = ste.getFileName();
                if (!TextUtils.isEmpty(fileName) && fileName.equalsIgnoreCase(lastFileName)) {
                    fileName = getBlankString(lastFileName.length());
                } else {
                    lastFileName = fileName;
                }
                android.util.Log.d(LOG_TAG, String.format("--%s[%s]%s[%s]", fileName, ste.getMethodName(), ste.getLineNumber(), info));
            } catch (Exception e) {
                android.util.Log.d(LOG_TAG, String.format("--[%s]", info));
            }
        }
    }

    public static void l_stack(String info) {
        if (LOG_ENABLE) {
            try {
                final StackTraceElement[] stack = new Throwable().getStackTrace();
                StackTraceElement ste = stack[1];
                String fileName = ste.getFileName();
                if (!TextUtils.isEmpty(fileName) && fileName.equalsIgnoreCase(lastFileName)) {
                    fileName = getBlankString(lastFileName.length());
                } else {
                    lastFileName = fileName;
                }
                android.util.Log.d(LOG_TAG, String.format("--%s[%s]%s[%s]", fileName, ste.getMethodName(), ste.getLineNumber(), info));
                for (int i = 2; i <= (stack.length > 2 ? 2 : 1); i++) {
                    ste = stack[i];
                    fileName = getBlankString((lastFileName + "--").length()) + ste.getFileName();
                    android.util.Log.d(LOG_TAG, String.format("%s[%s]%s", fileName, ste.getMethodName(), ste.getLineNumber()));
                }
            } catch (Exception e) {
                android.util.Log.d(LOG_TAG, String.format("--[%s]", info));
            }
        }
    }

    public static void l_stack_all(String info) {
        if (LOG_ENABLE) {
            try {
                final StackTraceElement[] stack = new Throwable().getStackTrace();
                StackTraceElement ste = stack[1];
                String fileName = ste.getFileName();
                if (!TextUtils.isEmpty(fileName) && fileName.equalsIgnoreCase(lastFileName)) {
                    fileName = getBlankString(lastFileName.length());
                } else {
                    lastFileName = fileName;
                }
                android.util.Log.d(LOG_TAG, info + " " + String.format("--%s[%s]%s[%s]", fileName, ste.getMethodName(), ste.getLineNumber(), info));
                for (int i = 2; i < stack.length; i++) {
                    ste = stack[i];
                    fileName = getBlankString((lastFileName + "-- ").length()) + ste.getFileName();
                    android.util.Log.d(LOG_TAG, info + " " + String.format("%s[%s]%s", fileName, ste.getMethodName(), ste.getLineNumber()));
                }
                android.util.Log.d(LOG_TAG, info + " " + String.format("-- stack end!!--length:[%s]", stack.length - 1));
            } catch (Exception e) {
                android.util.Log.d(LOG_TAG, String.format("--[%s]", info));
            }
        }
    }

    public static void startTiming(String info) {
        startTime = System.nanoTime();
        lastStopTime = startTime;
        if (LOG_ENABLE) {
            try {
                StackTraceElement[] stack = new Throwable().getStackTrace();
                StackTraceElement ste = stack[1];
                String fileName = ste.getFileName();
                if (!TextUtils.isEmpty(fileName) && fileName.equalsIgnoreCase(lastFileName)) {
                    fileName = getBlankString(lastFileName.length());
                } else {
                    lastFileName = fileName;
                }
                android.util.Log.d(LOG_TAG, String.format("--%s[%s]%s[%s]", fileName, ste.getMethodName(), ste.getLineNumber(), "StartTiming =====================" + info));
            } catch (Exception e) {
                android.util.Log.d(LOG_TAG, String.format("--StartTiming..."));
            }
        }
    }

    public static float lapTiming(String info) {
        float intervalConsuming = (int) ((System.nanoTime() - lastStopTime) / 1000f / 1000f * 100) / 100f;
        lastStopTime = System.nanoTime();
        float totalConsuming = (int) ((lastStopTime - startTime) / 1000f / 1000f * 100) / 100f;
        if (LOG_ENABLE) {
            try {
                StackTraceElement[] stack = new Throwable().getStackTrace();
                StackTraceElement ste = stack[1];
                String fileName = ste.getFileName();
                if (!TextUtils.isEmpty(fileName) && fileName.equalsIgnoreCase(lastFileName)) {
                    fileName = getBlankString(lastFileName.length());
                } else {
                    lastFileName = fileName;
                }
                String logPrefix = String.format("      %s[%s] %s [%s] --> ", fileName, ste.getMethodName(), ste.getLineNumber(), info);
                android.util.Log.d(LOG_TAG,
                        String.format("%-" + timeCosumingBlankStringMaxLength + "s in %-10s ms,  total: %-10s ms", logPrefix, intervalConsuming, totalConsuming));
            } catch (Exception e) {
                android.util.Log.d(LOG_TAG, String.format("-- in %-10s ms,  total: %-10s ms", intervalConsuming, totalConsuming));
            }
        }
        return totalConsuming;
    }

    public static void e(String info) {
        if (LOG_ENABLE) {
            android.util.Log.e(LOG_TAG, info);
        }
    }
}
