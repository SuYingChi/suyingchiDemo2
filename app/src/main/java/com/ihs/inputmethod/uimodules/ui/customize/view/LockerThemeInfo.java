package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.crashlytics.android.core.CrashlyticsCore;
import com.ihs.commons.utils.HSLog;

import java.util.Map;

/**
 * Created by guonan.lv on 17/9/6.
 */

public class LockerThemeInfo {

    public String name;
    public boolean installed;

    public static @Nullable
    LockerThemeInfo ofConfig(Map<String, ?> config) {
        LockerThemeInfo info = new LockerThemeInfo();

        try {
            String name = (String) config.get("name");
//            if (TextUtils.isEmpty(packageName)) {
//                return null;
//            }
            info.name = name;

            if (TextUtils.isEmpty(name)) {
                return null;
            }
        } catch (Exception e) {
            HSLog.w("Theme.Locker", "Error loading locker theme config, please check config format");
            CrashlyticsCore.getInstance().logException(e);
            return null;
        }

        return info;
    }
}
