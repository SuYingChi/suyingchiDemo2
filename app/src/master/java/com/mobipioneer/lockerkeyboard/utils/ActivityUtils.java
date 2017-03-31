package com.mobipioneer.lockerkeyboard.utils;

import android.content.Intent;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.mobipioneer.inputmethod.panels.settings.EmojiSettingsActivity2;

import static com.ihs.inputmethod.api.utils.HSActivityUtils.isAppActivityOnTop;

/**
 * Created by jixiang on 16/6/2.
 */
public class ActivityUtils {

    public static void showMoreSettingsActivity() {

//        if (getInputService() != null) {
//            HSInputMethod.getInputService().hideWindow();
//        }
        final Intent intent = new Intent();
        intent.setClass(HSApplication.getContext(), EmojiSettingsActivity2.class);
        if (isAppActivityOnTop()) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        HSApplication.getContext().startActivity(intent);
    }

    public static void showAdEngineItemSettingActivity(final int item) {
        final Intent intent = new Intent();
        intent.setClass(HSApplication.getContext(), EmojiSettingsActivity2.class);
        intent.putExtra("ad_item", item);
        if (isAppActivityOnTop()) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        HSApplication.getContext().startActivity(intent);

        //// TODO: 16/12/9 cjx 检查GA
        String action = "";
//        switch (item){
//            case  ADEConstants.ADE_ITEM_CUSTOM_BOOST:
//                action = GAConstants.BOOST_ALERT_SETTING_CLICKED;
//                break;
//            case  ADEConstants.ADE_ITEM_INPUT_SECURITY_CHECK:
//                action = GAConstants.INPUT_SECURITY_CHECK_ALERT_SETTING_CLICKED;
//                break;
//            case  ADEConstants.ADE_ITEM_OPTIMIZE:
//                action = GAConstants.OPTIMIZATION_ALERT_SETTING_CLICKED;
//                break;
//        }
        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(action);
    }
}
