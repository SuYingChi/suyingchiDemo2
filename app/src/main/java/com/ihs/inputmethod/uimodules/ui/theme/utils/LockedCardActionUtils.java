package com.ihs.inputmethod.uimodules.ui.theme.utils;

import android.content.Context;
import android.view.View;

import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ICondition;

/**
 * Created by jixiang on 17/11/21.
 */

public class LockedCardActionUtils {

    public final static String LOCKED_CARD_FROM_THEME = "theme";
    public final static String LOCKED_CARD_FROM_STICKER = "sticker";
    public final static String LOCKED_CARD_FROM_KEYBOARD_STICKER = "keyboardSticker";
    public final static String LOCKED_CARD_FROM_FONT = "font";
    public final static String LOCKED_CARD_FROM_CUSTOMIZE = "customize";
    public final static String LOCKED_CARD_FROM_HOME_BACKGROUND = "homeBackground";
    public final static String LOCKED_CARD_FROM_THEME_DETAIL = "homeDetail";


    public final static String UNLOCK_RATE_ALERT_SHOW = "UNLOCK_RATE_ALERT_SHOW";
    public final static String UNLOCK_SHARE_ALERT_SHOW = "UNLOCK_SHARE_ALERT_SHOW";

    public static boolean shouldLock(ICondition condition) {
        return     (LockerAppGuideManager.getInstance().shouldGuideToDownloadLocker() && condition.isDownloadLockerToUnlock())
                || (condition.isNeedNewVersionToUnlock() && ApkUtils.isNewVersionAvailable())
                || (condition.isRateToUnlock() && ApkUtils.shouldShowRateAlert())
                || (condition.isShareToUnlock() && ApkUtils.isInstagramInstalled() && !ApkUtils.isSharedKeyboardOnInstagramBefore());
    }

    /**
     *
     * @param context
     * @param action
     * @param nextAction
     */
    public static void handleLockAction(Context context,String from, ICondition action , Runnable nextAction) {
        if (LockerAppGuideManager.getInstance().shouldGuideToDownloadLocker() && action.isDownloadLockerToUnlock()) {
            LockerAppGuideManager.getInstance().showDownloadLockerAlert(context,LockerAppGuideManager.FLURRY_ALERT_UNLOCK);
            return;
        } else if (action.isNeedNewVersionToUnlock() && ApkUtils.isNewVersionAvailable()) {
            ApkUtils.showCustomUpdateAlert(from);
            return;
        } else if (action.isRateToUnlock() && ApkUtils.shouldShowRateAlert()) {
            if (ApkUtils.showCustomRateAlert(from,new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIController.getInstance().getUIHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (action.isDownloadInApp()) {
                                if (nextAction != null) {
                                    nextAction.run();
                                }
                            }
                            HSGlobalNotificationCenter.sendNotification(UNLOCK_RATE_ALERT_SHOW);
                        }
                    },3000);

                }
            })) {
                return;
            }
        } else if (action.isShareToUnlock() && ApkUtils.isInstagramInstalled() && !ApkUtils.isSharedKeyboardOnInstagramBefore()) {
            ApkUtils.showCustomShareAlert(from,context, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIController.getInstance().getUIHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (action.isDownloadInApp()) {
                                if (nextAction != null) {
                                    nextAction.run();
                                }
                            }
                            HSGlobalNotificationCenter.sendNotification(UNLOCK_SHARE_ALERT_SHOW);
                        }
                    },3000);
                }
            });
            return;
        } else {
            if (nextAction != null) {
                nextAction.run();
            }
        }
    }

}
