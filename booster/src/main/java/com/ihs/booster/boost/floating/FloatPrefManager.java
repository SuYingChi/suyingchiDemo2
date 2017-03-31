package com.ihs.booster.boost.floating;

import android.content.Context;
import android.content.SharedPreferences;

import com.ihs.app.framework.HSApplication;
import com.ihs.booster.utils.DisplayUtils;

/**
 * Created by weiqianwang on 16/4/22.
 */
public class FloatPrefManager {
    private static final String PREFS_MEMORY_PROTECTION_SCENE = "MEMORY_PROTECTION_SCENE";
    private static final String PREFS_MEMORY_IS_FIRST_INSTALL = "IS_FIRST_TIME_INSTALL";
    private static final String PREFS_MEMORY_PERCENT_BEFORE_CLEAN = "MEM_PERCENT_BEFORE_CLEAN";
    private static final String PREFS_MEMORY_FLOAT_BALL_Y = "MEMORY_FLOAT_BALL_Y";
    private static final String PREFS_MEMORY_USAGE_BEFORE_DISMISS = "MEMORY_USAGE_BEFORE_DISMISS";
    private static final String PREFS_MEMORY_FLOAT_BALL_X = "MEMORY_FLOAT_BALL_X";
    private static final String PREFS_IS_FIRST_INSTALL_RECORDED = "IS_FIRST_INSTALL_RECORDED";


    public static SharedPreferences getSharedPreferences() {
        return HSApplication.getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
    }

    public static boolean showFloatOnlyLuncher() {
        return getSharedPreferences().getBoolean(PREFS_MEMORY_PROTECTION_SCENE, true);
    }

    public static boolean setFloatOnlyLuncher(boolean flag) {
        return getSharedPreferences().edit().putBoolean(PREFS_MEMORY_PROTECTION_SCENE, flag).commit();
    }

    public static boolean isFirstTimeInstall() {
        return getSharedPreferences().getBoolean(PREFS_MEMORY_IS_FIRST_INSTALL, true);
    }

    public static boolean setFirstTimeInstall(boolean flag) {
        return getSharedPreferences().edit().putBoolean(PREFS_MEMORY_IS_FIRST_INSTALL, flag).commit();
    }

    public static boolean setMemPercentBeforeClean(float percent) {
        return getSharedPreferences().edit().putFloat(PREFS_MEMORY_PERCENT_BEFORE_CLEAN, percent).commit();
    }

    public static float getMemPercentBeforeClean() {
        return getSharedPreferences().getFloat(PREFS_MEMORY_PERCENT_BEFORE_CLEAN, 0);
    }

    public static boolean setBallYCoordinate(float ballYCoordinate) {
        return getSharedPreferences().edit().putFloat(PREFS_MEMORY_FLOAT_BALL_Y, ballYCoordinate).commit();
    }

    public static float getFloatBallYCoord() {
        return getSharedPreferences().getFloat(PREFS_MEMORY_FLOAT_BALL_Y, DisplayUtils.getDisplayMetrics().heightPixels * 0.48913043478261f);
    }

    public static boolean setBallXCoordinate(float ballXCoordinate) {
        return getSharedPreferences().edit().putFloat(PREFS_MEMORY_FLOAT_BALL_X, ballXCoordinate).commit();
    }

    public static float getFloatBallXCoord() {
        return getSharedPreferences().getFloat(PREFS_MEMORY_FLOAT_BALL_X, DisplayUtils.getDisplayMetrics().widthPixels);
    }

    public static boolean setLastBoostedMemUsage(int percent) {
        if(percent>0){
            return getSharedPreferences().edit().putInt(PREFS_MEMORY_USAGE_BEFORE_DISMISS, percent).commit();
        }else{
            return false;
        }
    }

    public static int getLastBoostedMemUsage() {
        return getSharedPreferences().getInt(PREFS_MEMORY_USAGE_BEFORE_DISMISS, 0);
    }

    public static boolean isFirstInstallRecorded(){
        if(!getSharedPreferences().getBoolean(PREFS_IS_FIRST_INSTALL_RECORDED,false)){
            getSharedPreferences().edit().putBoolean(PREFS_IS_FIRST_INSTALL_RECORDED,true).commit();
            return false;
        }else{
            return true;
        }
    }
}
