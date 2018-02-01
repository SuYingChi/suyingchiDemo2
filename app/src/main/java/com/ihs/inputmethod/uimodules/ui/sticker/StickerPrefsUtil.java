package com.ihs.inputmethod.uimodules.ui.sticker;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Arthur on 17/9/18.
 */

public class StickerPrefsUtil {
    private static final String STICKER_PREFS_FILE = "sticker_prefs_file";
    private static final String STICKER_RECENTLY_USED = "sticker_recent";
    private static final String STICKER_UNSUPPORT_APP = "sticker_unsupport_app";
    private static HSPreferenceHelper hsPreferenceHelper;
    private static StickerPrefsUtil instance;

    public static StickerPrefsUtil getInstance() {
        if (instance == null) {
            instance = new StickerPrefsUtil();
        }
        return instance;
    }

    private StickerPrefsUtil() {
        hsPreferenceHelper = HSPreferenceHelper.create(HSApplication.getContext(), STICKER_PREFS_FILE);
    }

    public void recordStickerSelect(String stickerName) {
        String stickerRecentlyUsed = getStickerUsedTimes();
        String str = stickerName + ";";
        stickerRecentlyUsed = stickerRecentlyUsed.replace(str, "");
        hsPreferenceHelper.putString(STICKER_RECENTLY_USED, str + stickerRecentlyUsed);
    }

    public void recordUnsupportApp(String packageName) {
        String string = hsPreferenceHelper.getString(STICKER_UNSUPPORT_APP, "");
        List<String> strings = Arrays.asList(string.split(";"));
        if (!strings.contains(packageName)) {
            string = string + packageName + ";";
        }
        hsPreferenceHelper.putString(STICKER_UNSUPPORT_APP, string);
    }

    public boolean isAppSupportSticker(String packageName) {
        String string = hsPreferenceHelper.getString(STICKER_UNSUPPORT_APP, "");
        List<String> strings = Arrays.asList(string.split(";"));
        return !strings.contains(packageName);
    }

    public String getStickerUsedTimes() {
        return hsPreferenceHelper.getString(STICKER_RECENTLY_USED, "");
    }

    public List<Sticker> sortStickerListByUsedTimes(List<Sticker> stickerList) {
        List<Sticker> resultList = new ArrayList<>();
        List<String> stickerNames = new ArrayList<>();
        for (Sticker sticker : stickerList) {
            if (sticker != null) {
                stickerNames.add(sticker.getStickerName());
            }
        }
        String[] stickerRecentlyUsed = getStickerUsedTimes().split(";");
        for (String stickerName : stickerRecentlyUsed) {
            int index = stickerNames.indexOf(stickerName);
            if (index > -1) {
                resultList.add(stickerList.get(index));
                stickerList.remove(index);
                stickerNames.remove(index);
            }
        }

        for (Sticker sticker : stickerList) {
            if (sticker != null) {
                resultList.add(sticker);
            }
        }
        return resultList;


//        HashMap<Sticker, Integer> map = new HashMap<>();
//        for (Sticker sticker : stickerList) {
//            map.put(sticker, getStickerUsedTimes(sticker.getStickerName()));
//        }
//        stickerList.clear();
//        for (Map.Entry<Sticker, Integer> stickerIntegerEntry : sortByValueDesc(map)) {
//            stickerList.add(stickerIntegerEntry.getKey());
//        }
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> sortByValueDesc(Map<K, V> map) {
//        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
//        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
//            @Override
//            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
//                return (o2.getValue()).compareTo(o1.getValue());
//            }
//        });
//
//        return list;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

}
