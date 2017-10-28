package com.ihs.inputmethod.uimodules.constants;

import com.ihs.commons.utils.HSLog;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Notification {

    // GIF
    public static final String SWITCH_LANGUAGE              = "SWITCH_LANGUAGE";
    public static final String UPLOAD_DATA_CHANGE           = "UPLOAD_DATA_CHANGE";
    public static final String LOCAL_UPLOAD_DATA_CHANGE     = "LOCAL_UPLOAD_DATA_CHANGE";
    public static final String RESET_TAB                    = "RESET_TAB";
    public static final String TRENDING_UPDATED             = "TRENDING_UPDATED";
    public static final String SEARCH_EVENT                 = "SEARCH_EVENT";
    public static final String INSIDE_INPUT_CONNECTION      = "INSIDE_INPUT_CONNECTION";

    // IME
    public static final String SHOW_WINDOW                  = "SHOW_WINDOW";
    public static final String SERVICE_DESTROY              = "SERVICE_DESTROY";
    public static final String SERVICE_START_INPUT_VIEW     = "SERVICE_START_INPUT_VIEW";
    public static final String SHOW_CONTROL_PANEL_VIEW      = "SHOW_CONTROL_PANEL_VIEW";

    // Facemoji
    public static final String FACE_CHANGED                 = "FACE_CHANGED";
    public static final String FACEMOJI_SAVED               = "FACEMOJI_SAVED";


    // Duplicate check
    static HashMap<Object, String> map = new HashMap<>();

    static {
        boolean debugging = HSLog.isDebugging();
        Class clazz = Notification.class;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            try {
                Object value = field.get(null);
                if (map.containsKey(value)) {
                    if (debugging) {
                        throw new RuntimeException(String.format("%s is same as %s", field.getName(), map.get(value)));
                    }
                }
                map.put(value, field.getName());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        map = null;
    }
}
