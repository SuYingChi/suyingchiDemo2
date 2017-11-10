package com.ihs.inputmethod.feature.common;

import android.content.Context;
import android.text.TextUtils;

import com.acb.adcaffe.common.AdCaffeError;
import com.acb.adcaffe.common.ServerAPIConnection;
import com.acb.adcaffe.common.UserDataUtils;
import com.acb.adcaffe.nativead.AdCaffeNativeAd;
import com.acb.adcaffe.nativead.imp.NativeAd;
import com.acb.adcaffe.nativead.imp.NativeAdLoadCoreConnection;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.HSServerAPIConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.utils.Trie;
import com.ihs.libcommon.utils.HSAdUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by guonan.lv on 17/10/31.
 */

public class AdCaffeHelper {

    private Context context;
    private HSServerAPIConnection connection;
    private static final String SERVER_URL_AD_CAFFE = "http://52.205.105.87/adcaffe/ad/get";
    private static final String KEYWORD_REQUEST_URL = "http://52.205.105.87/adcaffe/ad/keywords/get";
    private static final String SEARCH_AD_UPDATE_TIME = "search_ad_update_time";
    private static final String ASSETS_KEYWORD_FILE_PATH = "Keyword";
    private static final String KEYWORD_FINAL_FILE_NAME = "keyword.txt";
    private static final String KEYWORD_TEMP_FILE_NAME = "keywordTemp";

    private OnNativeAdLoadListener onNativeAdLoadListener;

    public String placementId;
    public String packageName;
    public String adId;
    public String platform;
    public String deviceType;
    public String osVersion;
    public String networkType;
    public String country;
    public String vendor;
    public List<String> keywords;
    public int offset = -1;

    public Trie getTrie() {
        return trie;
    }

    private Trie trie;


    public AdCaffeHelper(Context context, OnNativeAdLoadListener onNativeAdLoadListener) {
        this.context = context;

        placementId = HSConfig.getString("libCommons", "AppID") + "_1000";
        HSLog.e("lv_eee", "placementId " + placementId);
        packageName = UserDataUtils.getPackageName(context);
        HSAdUtils.getAdID(new HSAdUtils.GetAdIdListener() {
            @Override
            public void onGetAdIdSuccess(String s) {
                adId = s;
            }

            @Override
            public void onGetAdIdFailed() {

            }
        });

        platform = "android";
        deviceType = getDeviceType(context);
        osVersion = UserDataUtils.getOsVersion();
        networkType = getNetworkType(context);
        country = UserDataUtils.getCountry();
        this.onNativeAdLoadListener = onNativeAdLoadListener;
    }

    public void requestKeywordListIfConditionSatisfied(String packageName) {
        if (!shouldShowSearchAdForCurrentApp(packageName)) {
            return;
        }
        requestKeywordList();
    }

    private void requestKeywordList() {
        File tempFile = HSFileUtils.createNewFile(getKeywordFilePathBase() + KEYWORD_TEMP_FILE_NAME + System.currentTimeMillis());
        File destFile = HSFileUtils.createNewFile(getKeywordFilePathBase() + KEYWORD_FINAL_FILE_NAME);
        if (System.currentTimeMillis() - HSPreferenceHelper.getDefault().getLong(SEARCH_AD_UPDATE_TIME, 0)
                < TimeUnit.MINUTES.toMillis(HSConfig.getInteger("Application", "SearchAd", "updateTimeInMin"))) {
            if (trie == null) {
                new Thread(() -> readKeywordListFromFile(destFile)).start();
            }
            return;
        }

        HSHttpConnection connection = new HSHttpConnection(KEYWORD_REQUEST_URL);
        connection.setDownloadFile(tempFile);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                readKeywordListFromFile(tempFile);
                HSPreferenceHelper.getDefault().putLong(SEARCH_AD_UPDATE_TIME, System.currentTimeMillis());
                if (destFile.exists()) {
                    destFile.delete();
                }
                tempFile.renameTo(destFile);
                tempFile.delete();
                HSLog.e("lv_eee", "success");
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                tempFile.delete();
                HSLog.e("lv_eee", "fail");
            }
        });
        connection.startAsync();
    }

    private void readKeywordListFromFile(File file) {
        List<String> tempKeywordList = new ArrayList<>();
        try {
            InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file), "UTF-8");// 考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;

            while ((lineTxt = bufferedReader.readLine()) != null) {
                tempKeywordList.add(lineTxt);
            }
            trie = new Trie();
            for (String keyword : tempKeywordList) {
                trie.insert(keyword);
            }
            bufferedReader.close();
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean shouldShowSearchAdForCurrentApp(String packageName) {
        return HSConfig.getList("Application", "SearchAd", "PackageNameList").contains(packageName);
    }

    private String getKeywordFilePathBase() {
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_KEYWORD_FILE_PATH + File.separator;
    }

    public void checkKeywordAndLoad(List<String> words, OnKeywordCheckListener onKeywordCheckListener) {
        if (words.size() > 0) {
            String currentWord = words.get(words.size()-1);
            HSLog.e("lv_eee", currentWord);
            if (trie != null) {
                if (trie.startsWith(currentWord)) {
                    loadAdWithKeywords(words);
                    onKeywordCheckListener.onCheck(true);
                    return;
                }
            }
        }
        onKeywordCheckListener.onCheck(false);
    }

    private void loadAdWithKeywords(List<String> keywords) {
        updateKeywords(keywords);
        startConnection();
    }

    private void updateKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    private void startConnection() {
        JSONObject jsonObject = new JSONObject();
        fillJson(jsonObject, "ad_id", adId);

        fillJson(jsonObject, "placement_id", placementId.toLowerCase());
        fillJson(jsonObject, "package_name", packageName.toLowerCase());
        fillJson(jsonObject, "platform", platform.toLowerCase());
        fillJson(jsonObject, "device", deviceType.toLowerCase());
        fillJson(jsonObject, "country", country.toLowerCase());

        fillJson(jsonObject, "os_version", osVersion);
        fillJson(jsonObject, "network", networkType);
        fillJsonArray(jsonObject, "keyword", keywords);

        connection = new ServerAPIConnection("/ad/get", HttpRequest.Method.GET, jsonObject);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                HSError error = new HSError(AdCaffeError.ServerException, AdCaffeError.Domain);
                if (hsHttpConnection.isSucceeded()) {
                    JSONObject responseJson = hsHttpConnection.getBodyJSON();
                    if (ServerAPIConnection.isServerCode200(responseJson)) {
                        try {
                            JSONObject dataJson = responseJson.getJSONObject("data");
                            JSONArray adsJson = dataJson.getJSONArray("ads");
                            List<AdCaffeNativeAd> nativeAds = parseJsonResponse(adsJson);
                            boolean hasMore = dataJson.optBoolean("has_more", false);
                            int nextOffset = dataJson.optInt("offset", -1);
                            if (nativeAds != null) {
                                onNativeAdLoadListener.onNativeAdLoadSuccess(nativeAds, hasMore, nextOffset);
                                if (hasMore) {
                                    offset = nextOffset;
                                }
                                return;
                            } else {
                                error = new HSError(AdCaffeError.NoAvaibleAd, AdCaffeError.Domain);
                            }
                        } catch (JSONException ignored) {
                        }
                    }
                }
                onNativeAdLoadListener.onNativeAdLoadFail(error);
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                onNativeAdLoadListener.onNativeAdLoadFail(hsError);
            }
        });
        connection.startAsync();
    }

    private void fillJson(JSONObject jsonObject, String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            try {
                jsonObject.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillJson(JSONObject jsonObject, String key, int value) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fillJsonArray(JSONObject jsonObject, String key, List<String> stringList) {
        JSONArray jsonArray = new JSONArray();
        for (String string : stringList) {
            jsonArray.put(string);
        }
        try {
            jsonObject.put(key, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private AdCaffeNativeAd parseAdJson(JSONObject adJson) {
        if (adJson == null) {
            return null;
        }

        JSONObject offerDataJson = adJson.optJSONObject("offer_data");
        if (offerDataJson == null) {
            return null;
        }
        JSONObject trackingJson = adJson.optJSONObject("tracking");
        if (trackingJson == null) {
            return null;
        }

        NativeAd nativeAd = newNativeAd();
        this.setNativeAd(nativeAd, "PointsType", AdCaffeNativeAd.PointsType.CPI);
        try {
            this.setNativeAd(nativeAd, "Vendor", offerDataJson.getString("vendor"));
        } catch (JSONException ignored) {
        }
        try {
            this.setNativeAd(nativeAd, "PackageName", offerDataJson.getString("package_name"));
        } catch (JSONException ignored) {
        }
        try {
            // TODO test has default implete?
            this.setNativeAd(nativeAd, "Category", AdCaffeNativeAd.Category.from(offerDataJson.getString("category")));
        } catch (JSONException ignored) {
        }
        try {
            this.setNativeAd(nativeAd, "Body", offerDataJson.getString("description"));
        } catch (JSONException ignored) {
        }
        try {
            this.setNativeAd(nativeAd, "Points", (float) offerDataJson.getDouble("points"));
        } catch (JSONException ignored) {
        }
        try {
            this.setNativeAd(nativeAd, "PackageSize", offerDataJson.getString("size"));
        } catch (JSONException ignored) {
        }
        try {
            this.setNativeAd(nativeAd, "StoreRating", offerDataJson.getString("store_rating"));
        } catch (JSONException ignored) {
        }
        try {
            this.setNativeAd(nativeAd, "Downloads", offerDataJson.getString("downloads"));
        } catch (JSONException ignored) {
        }
        try {
            this.setNativeAd(nativeAd, "Title", offerDataJson.getString("title"));
        } catch (JSONException ignored) {
        }
        try {
            this.setNativeAd(nativeAd, "ClickUrl", offerDataJson.getString("click_url"));
        } catch (JSONException ignored) {
        }
        try {
            this.setNativeAd(nativeAd, "ImageUrl", offerDataJson.getString("banner_url"));
        } catch (JSONException ignored) {
        }
        try {
            this.setNativeAd(nativeAd, "IconUrl", offerDataJson.getString("icon_url"));
        } catch (JSONException ignored) {
        }

        try {
            this.setNativeAd(nativeAd, "ClickTrackingUrls", this.trackingUrlsFromJsonArray(trackingJson.getJSONArray("click")));
        } catch (JSONException ignored) {
        }
        List<String> impressTrackingUrls = new ArrayList<>();
        try {
            impressTrackingUrls.addAll(this.trackingUrlsFromJsonArray(trackingJson.getJSONArray("impress")));
        } catch (JSONException ignored) {
        }
        try {
            impressTrackingUrls.add(offerDataJson.getString("impress_url"));
        } catch (JSONException ignored) {
        }

        if (impressTrackingUrls.size() > 0) {
            this.setNativeAd(nativeAd, "ImpressTrackingUrls", impressTrackingUrls);
        }

        if (HSLog.isDebugging()) {
            try {
                HSLog.d("NativeAd aid=" + adJson.getString("aid") + ", packageName=" + nativeAd.getPackageName());
            } catch (JSONException ignored) {
            }
            try {
                HSLog.d("NativeAd Countries : " + offerDataJson.getString("country"));
                String[] countries = offerDataJson.getString("country").split(" ");
                boolean found = false;
                for (String country : countries) {
                    if (TextUtils.equals(UserDataUtils.getCountry(), country)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    HSLog.w("NativeAd Country mismatch");
                }
            } catch (JSONException ignored) {
            }

            try {
                HSLog.d("NativeAd minOSVersion : " + offerDataJson.getString("min_os_version"));
                long minOSVersion = versionToLong(offerDataJson.getString("min_os_version"));
                long myVersion = versionToLong(UserDataUtils.getOsVersion());
                if (myVersion < minOSVersion) {
                    HSLog.w("NativeAd minOSVersion mismatch");
                }
            } catch (JSONException ignored) {
            }
        }
        return nativeAd;
    }

    private long versionToLong(String version) {
        String[] versions = version.split("\\.");
        long ret = 0;
        for (int i = 0, j = 10000; i < versions.length && i < 3; i++, j /= 100) {
            ret += Integer.valueOf(versions[i]) * j;
        }
        return ret;
    }

    private List<String> trackingUrlsFromJsonArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                urls.add(jsonArray.getString(i));
            } catch (JSONException ignored) {
            }
        }
        return urls;
    }

    public String getDeviceType(Context context) {
        return (String) callMethod(context, "getDeviceType");
    }

    public NativeAd newNativeAd() {
        try {
            Constructor constructor = NativeAd.class.getDeclaredConstructor(Context.class);
            constructor.setAccessible(true);
            return (NativeAd) constructor.newInstance(context);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void setNativeAd(NativeAd nativeAd, String key, Object value) {
        try {
            Class myClass = value.getClass();
            if (myClass == Float.class) {
                myClass = float.class;
            }
            if (myClass == ArrayList.class) {
                myClass = List.class;
            }
            Method method = NativeAd.class.getDeclaredMethod("set" + key, myClass);
            method.setAccessible(true);
            try {
                method.invoke(nativeAd, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private Object callMethod(Context context, String methodName) {
        Constructor<NativeAdLoadCoreConnection> coreConnectionConstructor = null;
        try {
            coreConnectionConstructor = NativeAdLoadCoreConnection.class.getDeclaredConstructor(
                    Context.class, String.class, int.class);
            coreConnectionConstructor.setAccessible(true);
            NativeAdLoadCoreConnection coreConnection;
            coreConnection = coreConnectionConstructor.newInstance(context, "haha", 1);
            Method method = NativeAdLoadCoreConnection.class.getDeclaredMethod(methodName, Context.class);
            method.setAccessible(true);
            return method.invoke(coreConnection, context);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private String getNetworkType(Context context) {
        return (String) callMethod(context, "getNetworkType");
    }

    private List<AdCaffeNativeAd> parseJsonResponse(JSONArray adsJsonArray) {
        List<AdCaffeNativeAd> adCaffeNativeAds = new ArrayList<>();
        for (int i = 0; i < adsJsonArray.length(); i++) {
            try {
                AdCaffeNativeAd nativeAd = parseAdJson(adsJsonArray.getJSONObject(i));
                if (nativeAd != null && !UserDataUtils.isPackageInstalled(nativeAd.getPackageName())) {
                    adCaffeNativeAds.add(nativeAd);
                }
            } catch (JSONException ignored) {
            }
        }
        return adCaffeNativeAds;
    }

    public interface OnNativeAdLoadListener {
        void onNativeAdLoadFail(HSError hsError);

        void onNativeAdLoadSuccess(List<AdCaffeNativeAd> nativeAds, boolean hasMore, int nextOffset);
    }

    public interface OnKeywordCheckListener {
        void onCheck(boolean success);
    }
}
