package com.ihs.inputmethod.uimodules.softgame;

import android.app.Activity;
import android.content.Intent;

import com.ihs.app.framework.HSApplication;

/**
 * Created by yanxia on 2017/7/27.
 */

public class SoftGameDisplayHelper {

    public static void DisplaySoftGames(String placement) {
        DisplaySoftGames(null, placement);
    }

    public static void DisplaySoftGames(Activity activity, String placement) {
        Intent intent;
        if (null == activity) {
            intent = new Intent(HSApplication.getContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(SoftGameDisplayActivity.SOFT_GAME_PLACEMENT_MESSAGE, placement);
            HSApplication.getContext().startActivity(intent);
        } else {
            intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//            intent.putExtra(SoftGameDisplayActivity.SOFT_GAME_PLACEMENT_MESSAGE, placement);
            activity.startActivity(intent);
        }
    }

}
