package com.keyboard.inputmethod.panels.gif.dao.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.framework.HSInputMethodService;
import com.keyboard.inputmethod.panels.gif.control.DataManager;
import com.keyboard.rainbow.thread.AsyncThreadPools;

import java.util.Locale;

/**
 * Created by dsapphire on 16/1/12.
 */
public final class LanguageDao {

	private final static String PRE_KEY_CURRENT_LANGUAAGE="LanguageDao_PRE_KEY_CURRENT_LANGUAAGE";

	public static String getCurrentLanguageForDB(){
		HSInputMethodService inputMethodService= HSInputMethod.getInputService();
		Locale locale=null;
		if(inputMethodService!=null){
			locale=inputMethodService.getCurrentSubtypeLocale();
		}
		String lang;
		if(locale!=null){
			lang=locale.toString();
		}else {
			lang=Locale.getDefault().toString();
		}
		return lang;
	}

	public static void updateCurrentLanguage() {
		SharedPreferences pre=PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
		if(!getCurrentLanguageForDB().equals(pre.getString(PRE_KEY_CURRENT_LANGUAAGE,"en_US"))){
			pre.edit().putString(PRE_KEY_CURRENT_LANGUAAGE,getCurrentLanguageForDB()).apply();
			AsyncThreadPools.execute(new Runnable() {
				@Override
				public void run() {
					DataManager.getInstance().switchLanguage();
				}
			});
		}
	}
}
