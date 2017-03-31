package com.ihs.booster.boost.memory;


import com.ihs.booster.boost.common.BaseBlackWhiteListPrefs;
import com.ihs.booster.common.event.MBObserverEvent;
import com.ihs.booster.manager.MBBoostManager;

import java.util.List;

/**
 * Created by sharp on 16/4/5.
 */
public class MemoryPrefsManager extends BaseBlackWhiteListPrefs {
    private static MemoryPrefsManager instance;
    // 白名单 优先级小于 黑名单
    private static final String PREFS_FILE_NAME = "MemoryPrefs";
    private static final String CONFIG_NODE = "MemoryModule";

    public static synchronized MemoryPrefsManager getInstance() {
        if (instance == null) {
            instance = new MemoryPrefsManager(PREFS_FILE_NAME, CONFIG_NODE);
        }
        return instance;
    }

    private MemoryPrefsManager(String prefsFileName, String configNode) {
        super(prefsFileName, configNode);
    }

    // default - noDisplay - hate + love
    public List<String> getWhiteList() {
        List<String> defaultIgnoreList = getDefaultWhiteList();
        List<String> blackListByUser = getBlackListByUser();
        List<String> whiteListByUser = getWhiteListByUser();
        defaultIgnoreList.removeAll(blackListByUser);
        defaultIgnoreList.addAll(whiteListByUser);
        return removeSameItem(defaultIgnoreList);
    }

    public void addBlackListByUser(String pkgName) {
        super.addBlackListByUser(pkgName);
        MBBoostManager.globalObserverCenter.notifyOnUIThread(MBObserverEvent.IGNORE_APP_LIST_REMOVE, pkgName);
    }

    public void addWhiteListByUser(String pkgName) {
        super.addWhiteListByUser(pkgName);
        MBBoostManager.globalObserverCenter.notifyOnUIThread(MBObserverEvent.IGNORE_APP_LIST_ADD, pkgName);
    }
}
