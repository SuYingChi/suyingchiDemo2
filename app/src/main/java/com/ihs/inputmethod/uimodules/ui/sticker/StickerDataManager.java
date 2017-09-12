package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.stickerplus.PlusButton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yanxia on 2017/6/9.
 */

public class StickerDataManager {
    public static final String STICKER_DATA_LOAD_FINISH_NOTIFICATION = "sticker_data_load_finish";
    public static final String STICKER_GROUP_DOWNLOAD_SUCCESS_NOTIFICATION = "sticker_data_change_finish";
    public static final String PREFERENCE_KEY_NEW_STICKER_SET = "sp_key_new_sticker_set";
    private static final String PREFERENCE_SHOW_NEW_TIP_STATE = "sp_show_new_tip_state";
    private static StickerDataManager instance;
    private List<StickerGroup> stickerGroups;
    private boolean isReady = false;
    private boolean isShowNewTip = false;

    private StickerDataManager() {
        stickerGroups = new ArrayList<>();
        loadStickersAsync();
    }

    public static StickerDataManager getInstance() {
        if (instance == null) {
            synchronized (StickerDataManager.class) {
                if (instance == null) {
                    instance = new StickerDataManager();
                }
            }
        }
        return instance;
    }

    public void onConfigChange() {
        HSLog.d("xy, load sticker after config change");
        loadStickersAsync();

    }

    private void loadStickersAsync() {
        new LoadDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            isReady = false;
            loadStickers();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            isReady = true;
            HSGlobalNotificationCenter.sendNotificationOnMainThread(STICKER_DATA_LOAD_FINISH_NOTIFICATION);
        }
    }

    private synchronized void loadStickers() {
        List<StickerGroup> oldStickerGroupList = new ArrayList<>();
        oldStickerGroupList.addAll(stickerGroups);

        Set currentNewStickerSet = getCurrentNewStickerSet(HSApplication.getContext());
        HashSet newStickerSet = new HashSet();

        stickerGroups.clear();
        List<Map<String, Object>> stickerConfigList = (List<Map<String, Object>>) HSConfig.getList("Application", "StickerGroupList");
        for (Map<String, Object> map : stickerConfigList) {
            String stickerGroupName = (String) map.get("name");
            String stickerGroupDownloadDisplayName = (String) map.get("showName");
            StickerGroup stickerGroup = new StickerGroup(stickerGroupName);
            stickerGroup.setDownloadDisplayName(stickerGroupDownloadDisplayName);
            stickerGroups.add(stickerGroup);
            if (oldStickerGroupList.size() > 0) {
                if (!oldStickerGroupList.contains(stickerGroup)) {
                    newStickerSet.add(stickerGroup.getStickerGroupName());
                }
            }
        }

        if (newStickerSet.size() > 0) {
            currentNewStickerSet.addAll(newStickerSet);
            saveCurrentNewStickerSet(HSApplication.getContext(), currentNewStickerSet);
            saveShowNewTipState(true);
        }
    }

    public void saveShowNewTipState(boolean flag) {
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putBoolean(PREFERENCE_SHOW_NEW_TIP_STATE, flag).commit();
    }

    public boolean getShowNewTipState() {
        return PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getBoolean(PREFERENCE_SHOW_NEW_TIP_STATE, false);
    }

    public Set<String> getCurrentNewStickerSet(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(PREFERENCE_KEY_NEW_STICKER_SET, new HashSet());
    }

    public void saveCurrentNewStickerSet(Context context, Set<String> currentNewStickerSet) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(PREFERENCE_KEY_NEW_STICKER_SET, currentNewStickerSet).commit();

    }

    boolean isStickersReady() {
        return isReady;
    }

    void updateStickerGroupList(StickerGroup stickerGroup) {
        if (!isReady) {
            return;
        }
        stickerGroup.reloadStickers();
        HSGlobalNotificationCenter.sendNotificationOnMainThread(STICKER_GROUP_DOWNLOAD_SUCCESS_NOTIFICATION);
    }

    public List<StickerGroup> getStickerGroupList() {
        if (!isReady) {
            return Collections.emptyList();
        } else {
            return stickerGroups;
        }
    }

    public boolean isStickerGroupDownloaded(String name) {
        for (StickerGroup stickerGroup : getStickerGroupList()) {
            if (stickerGroup.getStickerGroupName().equals(name)) {
                return stickerGroup.isStickerGroupDownloaded();
            }
        }
        return false;
    }

}
