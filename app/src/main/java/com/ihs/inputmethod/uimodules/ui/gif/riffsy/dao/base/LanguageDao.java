package com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.utils.HSThreadUtils;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.DataManager;

import java.util.Locale;

/**
 * Created by dsapphire on 16/1/12.
 */
public final class LanguageDao {

    private final static String PRE_KEY_CURRENT_LANGUAAGE = "LanguageDao_PRE_KEY_CURRENT_LANGUAAGE";

    public static String getCurrentLanguageForDB() {
        Locale locale = HSInputMethod.getCurrentSubtypeLocale();
        String lang;
        if (locale != null) {
            lang = locale.toString();
        } else {
            lang = Locale.getDefault().toString();
        }
        return lang;
    }

    public static void updateCurrentLanguage() {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        if (!getCurrentLanguageForDB().equals(pre.getString(PRE_KEY_CURRENT_LANGUAAGE, "en_US"))) {
            pre.edit().putString(PRE_KEY_CURRENT_LANGUAAGE, getCurrentLanguageForDB()).apply();
            HSThreadUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataManager.getInstance().switchLanguage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
