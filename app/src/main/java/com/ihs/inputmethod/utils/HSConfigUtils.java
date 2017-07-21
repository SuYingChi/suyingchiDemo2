package com.ihs.inputmethod.utils;

/**
 * Created by yanxia on 2017/6/7.
 */

public class HSConfigUtils {
    public static int toInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException var2) {

                }
            }
            return defaultValue;
        }
    }

    public static boolean toBoolean(Object value, boolean defaultValue) {
        if (value != null && value instanceof Boolean) {
            return  (boolean) value;
        } else {
            return defaultValue;
        }
    }
}
