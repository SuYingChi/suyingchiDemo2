package com.ihs.inputmethod.feature.common;

import android.content.Context;
import android.text.TextUtils;

import com.acb.adcaffe.common.AdCaffeError;
import com.acb.adcaffe.common.ServerAPIConnection;
import com.acb.adcaffe.common.UserDataUtils;
import com.acb.adcaffe.nativead.AdCaffeNativeAd;
import com.acb.adcaffe.nativead.imp.NativeAd;
import com.acb.adcaffe.nativead.imp.NativeAdLoadCoreConnection;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.HSServerAPIConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.libcommon.utils.HSAdUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guonan.lv on 17/10/31.
 */

public class AdCaffeHelper {

    private Context context;
    private HSServerAPIConnection connection;
    private static final String SERVER_URL_AD_CAFFE = "http://52.205.105.87/adcaffe/ad/get";

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
    public String[] keywords;
    public int offset = -1;


    public AdCaffeHelper(Context context, String placementId, OnNativeAdLoadListener onNativeAdLoadListener) {
        this.context = context;

        this.placementId = "999999_24581";
        this.packageName = UserDataUtils.getPackageName(context);
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

    public void loadAdWithKeywords(String[] keywords) {
        updateKeywords(keywords);
        startConnection();
    }

    public void updateKeywords(String[] keywords) {
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

        JSONArray jsonArray = new JSONArray();
        jsonArray.put("tools");
        jsonArray.put("games");
        try {
            jsonObject.put("category", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        fillJson(jsonObject, "os_version", osVersion);
        fillJson(jsonObject, "network", networkType);
        fillJsonArray(jsonObject, "keyword", keywords);

        connection = new HSServerAPIConnection(SERVER_URL_AD_CAFFE, HttpRequest.Method.GET, jsonObject);
        connection.setSigKey("x5UJ~fb}3_Dma>l B]YB/?'1As[\"E<I!", "1");
        connection.setEncryptKey("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD", "1");
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

    private void fillJsonArray(JSONObject jsonObject, String key, String[] stringList) {
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
            NativeAdLoadCoreConnection coreConnection = null;
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
}
