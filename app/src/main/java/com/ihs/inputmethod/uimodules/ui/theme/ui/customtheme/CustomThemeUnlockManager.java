package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme;

import com.ihs.commons.config.HSConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xiayan on 2017/9/17.
 */

public class CustomThemeUnlockManager {
    private static final CustomThemeUnlockManager ourInstance = new CustomThemeUnlockManager();

    private Set<String> needNewVersionToUnlockItemList;
    private List<String> rateToUnlockItemList;

    public static CustomThemeUnlockManager getInstance() {
        return ourInstance;
    }

    private CustomThemeUnlockManager() {
        needNewVersionToUnlockItemList = new HashSet();
        rateToUnlockItemList = new ArrayList<>();
        loadNeedNerVersionToUnlockSet();
    }

    public void onConfigChange() {
        loadNeedNerVersionToUnlockSet();
    }

    private synchronized void loadNeedNerVersionToUnlockSet() {
        List<String> needNewVersionList = (List<String>) HSConfig.getList("Application", "ThemeContents", "custom_theme_needNewVersionToUnlock");
        needNewVersionToUnlockItemList.clear();
        needNewVersionToUnlockItemList.addAll(needNewVersionList);
    }

    public boolean isElementNeedNewAppVersionToUnlock(String elementName) {
        return needNewVersionToUnlockItemList.contains(elementName);
    }

}
