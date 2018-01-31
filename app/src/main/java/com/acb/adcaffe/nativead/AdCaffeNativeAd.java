package com.acb.adcaffe.nativead;

import com.ihs.app.framework.HSApplication;

import java.util.Date;

/**
 * Created by Arthur on 2018/1/31.
 */

public class AdCaffeNativeAd {
    public static Date Category;
    private byte[] iconUrl;
    private NativeAdOnClickListener onClickListener;
    private int title;
    private String storeRating;

    public Object getPackageName() {
        return HSApplication.getContext().getPackageName();
    }

    public void handleImpression() {

    }

    public String getIconUrl() {
        return "";
    }

    public void setOnClickListener(NativeAdOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void handleClick() {

    }

    public String getVendor() {
        return "a";
    }

    public String getTitle() {
        return "title";
    }

    public String getStoreRating() {
        return "five";
    }

    public interface NativeAdOnClickListener {
        public void onNativeAdClicked(AdCaffeNativeAd adCaffeNativeAd);

        public void onNativeAdHandleClickFinished(AdCaffeNativeAd adCaffeNativeAd);
    }

    public static class PointsType {
        public static final Object CPI = "a";
    }
}
