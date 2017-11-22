package com.ihs.inputmethod.uimodules.ui.theme.utils;

import android.content.Context;
import android.view.View;

import com.artw.lockscreen.LockerAppGuideManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ICondition;

/**
 * Created by jixiang on 17/11/21.
 */

public class LockedCardActionUtils {

    public static boolean shouldLock(ICondition condition) {
        return     (LockerAppGuideManager.getInstance().shouldGuideToDownloadLocker() && condition.isDownloadLockerToUnlock())
                || (condition.isNeedNewVersionToUnlock() && ApkUtils.isNewVersionAvailable())
                || (condition.isRateToUnlock() && ApkUtils.shouldShowRateAlert())
                || (condition.isShareToUnlock() && ApkUtils.isInstagramInstalled() && !ApkUtils.isSharedKeyboardOnInstagramBefore());
    }

    public static void handleLockAction(Context context, ICondition action , Runnable nextAction) {
        if (LockerAppGuideManager.getInstance().shouldGuideToDownloadLocker() && action.isDownloadLockerToUnlock()) {
            LockerAppGuideManager.getInstance().showDownloadLockerAlert(context, HSApplication.getContext().getResources().getString(R.string.locker_guide_unlock_for_free_dialog_title), LockerAppGuideManager.FLURRY_ALERT_UNLOCK);
            return;
        } else if (action.isNeedNewVersionToUnlock() && ApkUtils.isNewVersionAvailable()) {
            ApkUtils.showCustomUpdateAlert();
            return;
        } else if (action.isRateToUnlock() && ApkUtils.shouldShowRateAlert()) {
            if (ApkUtils.showCustomRateAlert(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIController.getInstance().getUIHandler().postDelayed(nextAction, 2000);
                }
            })) {
                return;
            }
        } else if (action.isShareToUnlock() && ApkUtils.isInstagramInstalled() && !ApkUtils.isSharedKeyboardOnInstagramBefore()) {
            ApkUtils.showCustomShareAlert(context, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIController.getInstance().getUIHandler().postDelayed(nextAction, 2000);
                }
            });
            return;
        } else {
            nextAction.run();
        }
    }

}
