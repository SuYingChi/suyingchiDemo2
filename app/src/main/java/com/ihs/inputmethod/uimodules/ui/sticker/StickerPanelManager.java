package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Pair;

import com.ihs.inputmethod.api.utils.HSJsonUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.utils.GlideURIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanxia on 2017/6/6.
 */

public class StickerPanelManager {

    public static final String STICKER_RECENT = "sticker_recent";
    private final String PRE_LAST_SHOWN_TAB_KEY = "sticker_last_show_tab";
    private final String PREF_STICKER_RECENT_KEYS = "sticker_recent_keys";
    private final List<Sticker> recentTemp = new ArrayList<>();
    private final SharedPreferences prefs;
    private final int maxRecentCount;
    private final int rowCount;
    private List<StickerPanelItemGroup> stickerPanelItemGroups = new ArrayList<>();
    private StickerPanelItemGroup recentStickerPanelItems;
    private String currentTabName = STICKER_RECENT;
    private boolean hasPendingRecent;
    private StickerPanelView stickerPanelView;

    StickerPanelManager(final SharedPreferences prefs, final Resources res, StickerPanelView stickerPanelView) {
        this.prefs = prefs;
        this.stickerPanelView = stickerPanelView;

        this.maxRecentCount = res.getInteger(R.integer.config_sticker_recent_max_count);
        this.rowCount = res.getInteger(R.integer.config_sticker_row_count);
        currentTabName = this.prefs.getString(PRE_LAST_SHOWN_TAB_KEY, getDefaultTab());
        recentStickerPanelItems = new StickerPanelItemGroup(STICKER_RECENT);
    }

    void loadData() {
        loadRecent();
        while (recentStickerPanelItems.size() > maxRecentCount) {
            recentStickerPanelItems.removeLastStickerPanelItem();
        }
        stickerPanelItemGroups.clear();
        stickerPanelItemGroups.add(recentStickerPanelItems);
        for (StickerGroup stickerGroup : getSortedStickerGroup()) {
            StickerPanelItemGroup stickerPanelItemGroup = new StickerPanelItemGroup(stickerGroup.getStickerGroupName());
            for (Sticker sticker : stickerGroup.getStickerList()) {
                stickerPanelItemGroup.addStickerPanelItem(new StickerPanelItem(sticker));
            }
            stickerPanelItemGroups.add(stickerPanelItemGroup);
        }
        for (StickerPanelItemGroup stickerPanelItemGroup : stickerPanelItemGroups) {
            addPlaceHolderStickerPanelItem(stickerPanelItemGroup);
        }
        stickerPanelView.onDataLoaded();
    }

    private List<StickerGroup> getSortedStickerGroup() {
        List<StickerGroup> stickerSortedGroupList = new ArrayList<>();
        List<StickerGroup> stickerBuildInList = new ArrayList<>();
        List<StickerGroup> stickerDownloadedList = new ArrayList<>();
        List<StickerGroup> stickerNeedDownloadList = new ArrayList<>();
        for (StickerGroup stickerGroup : StickerDataManager.getInstance().getStickerGroupList()) {
            if (stickerGroup.isInternalStickerGroup()) {
                stickerBuildInList.add(stickerGroup);
            } else if (stickerGroup.isStickerGroupDownloaded()) {
                stickerDownloadedList.add(stickerGroup);
                if (stickerGroup.getStickerList().isEmpty()) {
                    stickerGroup.reloadStickers();
                }
            } else {
                stickerNeedDownloadList.add(stickerGroup);
            }
        }
        stickerSortedGroupList.addAll(stickerBuildInList);
        stickerSortedGroupList.addAll(stickerDownloadedList);
        stickerSortedGroupList.addAll(stickerNeedDownloadList);
        return stickerSortedGroupList;
    }

    /**
     * 获取除了未下载的所有stickerGroup集合
     * @return
     */
    private List<StickerGroup> getSortedExceptNeedDownloadStickerGroup() {
        List<StickerGroup> stickerSortedGroupList = new ArrayList<>();
        List<StickerGroup> stickerBuildInList = new ArrayList<>();
        List<StickerGroup> stickerDownloadedList = new ArrayList<>();
        for (StickerGroup stickerGroup : StickerDataManager.getInstance().getStickerGroupList()) {
            if (stickerGroup.isInternalStickerGroup()) {
                stickerBuildInList.add(stickerGroup);

            } else if (stickerGroup.isStickerGroupDownloaded()) {
                if (stickerGroup.getStickerList().isEmpty()) {
                    stickerGroup.reloadStickers();
                }
                stickerDownloadedList.add(stickerGroup);
            }
        }
        stickerSortedGroupList.addAll(stickerBuildInList);
        stickerSortedGroupList.addAll(stickerDownloadedList);
        return stickerSortedGroupList;
    }

    List<String> getSortedStickerGroupNameList() {
        List<String> stickerSortedNameList = new ArrayList<>();
        List<String> stickerBuildInNameList = new ArrayList<>();
        List<String> stickerDownloadedNameList = new ArrayList<>();
        List<String> stickerNeedDownloadNameList = new ArrayList<>();
        for (StickerGroup stickerGroup : StickerDataManager.getInstance().getStickerGroupList()) {
            if (stickerGroup.isInternalStickerGroup()) {
                stickerBuildInNameList.add(stickerGroup.getStickerGroupName());
            } else if (stickerGroup.isStickerGroupDownloaded()) {
                if (stickerGroup.getStickerList().isEmpty()) {
                    stickerGroup.reloadStickers();
                }
                stickerDownloadedNameList.add(stickerGroup.getStickerGroupName());
            } else {
                stickerNeedDownloadNameList.add(stickerGroup.getStickerGroupName());
            }
        }
        stickerSortedNameList.add(STICKER_RECENT);
        stickerSortedNameList.addAll(stickerBuildInNameList);
        stickerSortedNameList.addAll(stickerDownloadedNameList);
        stickerSortedNameList.addAll(stickerNeedDownloadNameList);
        return stickerSortedNameList;
    }

    List<String> getSortedStickerGroupNameInKeyboardList() {
        List<String> stickerSortedNameList = new ArrayList<>();
        List<String> stickerBuildInNameList = new ArrayList<>();
        List<String> stickerDownloadedNameList = new ArrayList<>();
        List<String> stickerNeedDownloadNameList = new ArrayList<>();

        List<StickerGroup> stickerGroupInKeyboardList = getNeedDownloadStickerGroupInKeyboardList();

        for (StickerGroup stickerGroup : StickerDataManager.getInstance().getStickerGroupList()) {
            if (stickerGroup.isInternalStickerGroup()) {
                stickerBuildInNameList.add(stickerGroup.getStickerGroupName());
            } else if (stickerGroup.isStickerGroupDownloaded()) {
                if (stickerGroup.getStickerList().isEmpty()) {
                    stickerGroup.reloadStickers();
                }
                stickerDownloadedNameList.add(stickerGroup.getStickerGroupName());
            } else if (stickerGroupInKeyboardList.contains(stickerGroup)) {
                stickerNeedDownloadNameList.add(stickerGroup.getStickerGroupName());
            }
        }
        stickerSortedNameList.add(STICKER_RECENT);
        stickerSortedNameList.addAll(stickerBuildInNameList);
        stickerSortedNameList.addAll(stickerDownloadedNameList);
        stickerSortedNameList.addAll(stickerNeedDownloadNameList);
        return stickerSortedNameList;
    }

    /**
     * 获取除了未下载的所有的StickerGroup名字
     * @return
     */
    List<String> getSortedExceptNeedDownloadStickerGroupNameList() {
        List<String> stickerSortedExceptNeedDownloadNameList = new ArrayList<>();
        List<String> stickerBuildInNameList = new ArrayList<>();
        List<String> stickerDownloadedNameList = new ArrayList<>();
        for (StickerGroup stickerGroup :StickerDataManager.getInstance().getStickerGroupList()) {
            if (stickerGroup.isInternalStickerGroup()) {
                stickerBuildInNameList.add(stickerGroup.getStickerGroupName());
            } else if (stickerGroup.isStickerGroupDownloaded()){
                stickerDownloadedNameList.add(stickerGroup.getStickerGroupName());
                if (stickerGroup.getStickerList().isEmpty()) {
                    stickerGroup.reloadStickers();
                }
            }
        }
        stickerSortedExceptNeedDownloadNameList.add(STICKER_RECENT);
        stickerSortedExceptNeedDownloadNameList.addAll(stickerBuildInNameList);
        stickerSortedExceptNeedDownloadNameList.addAll(stickerDownloadedNameList);

        return stickerSortedExceptNeedDownloadNameList;
    }

    private List<String> getStickerNeedDownloadList() {
        List<String> stickerNeedDownloadList = new ArrayList<>();
        for (StickerGroup stickerGroup : StickerDataManager.getInstance().getStickerGroupList()) {
            if (!stickerGroup.isInternalStickerGroup() && !stickerGroup.isStickerGroupDownloaded()) {
                stickerNeedDownloadList.add(stickerGroup.getStickerGroupName());
            }
        }
        return stickerNeedDownloadList;
    }

    List<StickerGroup> getNeedDownloadStickerGroupList() {
        List<StickerGroup> stickerGroups = new ArrayList<>();
        for (StickerGroup stickerGroup : StickerDataManager.getInstance().getStickerGroupList()) {
            if (!stickerGroup.isInternalStickerGroup() && !stickerGroup.isStickerGroupDownloaded()) {
                stickerGroups.add(stickerGroup);
            }
        }
        return stickerGroups;
    }

    List<StickerGroup> getNeedDownloadStickerGroupInKeyboardList() {
        List<StickerGroup> stickerGroups = new ArrayList<>();
        for (StickerGroup stickerGroup : StickerDataManager.getInstance().getStickerGroupsInKeyboardList()) {
            if (!stickerGroup.isInternalStickerGroup() && !stickerGroup.isStickerGroupDownloaded()) {
                stickerGroups.add(stickerGroup);
            }
        }
        return stickerGroups;
    }

    String getDefaultTab() {
        final List<StickerGroup> stickerGroups = getSortedStickerGroup();
        if (stickerGroups.size() >= 1) {
            return stickerGroups.get(0).getStickerGroupName();
        }
        return STICKER_RECENT;
    }

    String getCurrentTabName() {
        return currentTabName;
    }

    String getLastDownloadedTabName() {
        List<String> sortedStickerGroupNameList = new ArrayList<>();
        sortedStickerGroupNameList.addAll(getSortedStickerGroupNameList());
        List<String> needDownloadStickerGroupNameList = getStickerNeedDownloadList();
        sortedStickerGroupNameList.removeAll(needDownloadStickerGroupNameList);
        if (sortedStickerGroupNameList.size() > 0) {
            return sortedStickerGroupNameList.get(sortedStickerGroupNameList.size() - 1);
        }
        return getDefaultTab();
    }

    void setCurrentTabName(final String tabName) {
        currentTabName = tabName;
        prefs.edit().putString(PRE_LAST_SHOWN_TAB_KEY, tabName).apply();
    }

    String getTabNameForPosition(final int position) {
        int sum = 0;
        for (int i = 0; i < stickerPanelItemGroups.size(); ++i) {
            final StickerPanelItemGroup group = stickerPanelItemGroups.get(i);
            if (position >= sum && position < sum + group.size()) {
                return group.getStickerPanelItemGroupName();
            }
            sum += group.size();
        }
        return STICKER_RECENT;
    }

    boolean isRecentTab(final String tabName) {
        return STICKER_RECENT.equals(tabName);
    }

    void saveRecent() {
        final List<Object> keys = new ArrayList<>();
        for (final Sticker sticker : recentTemp) {
            if (sticker.toString().trim().length() > 0) {
                keys.add(sticker.toString());
            }
        }
        final String jsonStr = HSJsonUtils.listToJsonStr(keys);
        prefs.edit().putString(PREF_STICKER_RECENT_KEYS, jsonStr).apply();
    }

    private void loadRecent() {
        final String str = prefs.getString(PREF_STICKER_RECENT_KEYS, "");
        final List<Object> keys = HSJsonUtils.jsonStrToList(str);
        if (keys.size() > 0) {
            recentStickerPanelItems.clear();
        }
        recentTemp.clear();
        for (final Object key : keys) {
            String imageLoaderUri = key.toString();
            if (imageLoaderUri.startsWith("assets:")){
                imageLoaderUri = imageLoaderUri.replace("assets://", GlideURIUtils.Type.Assets.getStr());
            }
            final Sticker sticker = new Sticker(imageLoaderUri);
            recentStickerPanelItems.addStickerPanelItem(new StickerPanelItem(sticker));
            recentTemp.add(sticker);
        }
    }

    void pendingRecentSticker(final Sticker sticker) {
        hasPendingRecent = true;
        final Sticker temp = new Sticker(sticker.getStickerUri());
        recentTemp.remove(temp);
        recentTemp.add(0, temp);
        if (recentTemp.size() > maxRecentCount) {
            recentTemp.remove(maxRecentCount);
        }

        saveRecent();
    }

    boolean hasPendingRecent() {
        return hasPendingRecent;
    }

    void flushPendingRecentSticker() {
        hasPendingRecent = false;
        recentStickerPanelItems.clear();
        for (Sticker sticker : recentTemp) {
            recentStickerPanelItems.addStickerPanelItem(new StickerPanelItem(sticker));
        }
        addPlaceHolderStickerPanelItem(recentStickerPanelItems);
    }

    Pair<Integer, Integer> getLastShownItemPositionForTab(String tab) {
        int sum = 0;
        for (int i = 0; i < stickerPanelItemGroups.size(); ++i) {
            final StickerPanelItemGroup group = stickerPanelItemGroups.get(i);
            if (tab.equals(group.getStickerPanelItemGroupName())) {
                return new Pair<>(sum, 0);
            }
            sum += group.size();
        }
        return new Pair<>(0, 0);
    }

    List<StickerPanelItem> getStickerPanelItemList() {
        List<StickerPanelItem> stickerPanelItems = new ArrayList<>();
        for (StickerPanelItemGroup group : stickerPanelItemGroups) {
            stickerPanelItems.addAll(group.getStickerPanelItemList());
        }
        return stickerPanelItems;
    }

    boolean isRecentEmpty() {
        return recentTemp.isEmpty();
    }

    private void addPlaceHolderStickerPanelItem(StickerPanelItemGroup stickerPanelItemGroup) {
        List<StickerPanelItem> stickerPanelItemList = stickerPanelItemGroup.getStickerPanelItemList();
        while (stickerPanelItemList.size() % rowCount != 0) {
            stickerPanelItemList.add(StickerPanelItem.PLACEHOLDER);
        }
        if (stickerPanelItemList.size() > 0 && !stickerPanelItemList.get(stickerPanelItemList.size() - 1).isDivider()) {
            for (int i = 0; i < rowCount; i++) {
                stickerPanelItemList.add(StickerPanelItem.DIVIDER);
            }
        }
    }
}
