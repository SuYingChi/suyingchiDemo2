package com.mobipioneer.lockerkeyboard.ads;


import android.graphics.Bitmap;
import android.view.View;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAd;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.mobipioneer.lockerkeyboard.app.MyInputMethodApplication;
import com.mobipioneer.lockerkeyboard.utils.Constants;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by xu.zhang on 12/10/15.
 */
public class FacebookNativeAdsManager {
    public static final String NOTIFICATION_FACEBOOK_AD_LOADED = "notification_facebook_ad_loaded";
    public static final String NOTIFICATION_FACEBOOK_AD_CLICKED = "notification_facebook_ad_clicked";
    private NativeAd nativeAd;
    private NativeAd nativeAdNext;
    private boolean isCachedAdConsumed = true;
    private long startShowingTime;

    private String adsId;
    private int adsOwner;

    private static DisplayImageOptions defaultOptions =
            new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();

    private static ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    };

    /**
     * Constructor
     * @param adsOwner Ads observer
     * @param adsId ads unit id
     */
    public FacebookNativeAdsManager(final int adsOwner, final String adsId) {
        this.adsOwner = adsOwner;
        this.adsId = adsId;
    }

    public void fetchAds() {

        if (!MyInputMethodApplication.isFacebookAppInstalled()) {
            HSLog.d("native ad facebook not installed");
            return;
        }
        int adLoadMode = HSConfig.optInteger(2, "Application", AdsConstants.CONFIG_NODE_NATIVE_ADS, AdsConstants.CONFIG_NODE_ADS_LOAD_MODEL);
        if (adLoadMode == 1) {
            HSLog.d("native ad configured to show only admob ads");
            return;
        }

        nativeAd = new NativeAd(HSApplication.getContext(), adsId);
        if (HSLog.isDebugging()) {
            AdSettings.addTestDevice("5d8550ba4c1830d734df636c3161282a");
        }
        nativeAd.setAdListener(new AdListener() {

            @Override
            public void onError(Ad ad, AdError error) {
                HSLog.d("show facebook native ads onError " + error.getErrorMessage() + " ,  code is: " + error.getErrorCode());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                HSLog.d("show facebook native ads onAdLoaded");

                if (null != nativeAd.getAdIcon()) {
                    ImageLoader.getInstance().loadImage(nativeAd.getAdIcon().getUrl(), defaultOptions, imageLoadingListener);
                }

                if (null != nativeAd.getAdCoverImage()) {
                    ImageLoader.getInstance().loadImage(nativeAd.getAdCoverImage().getUrl(),defaultOptions, imageLoadingListener);
                }

                sendNotification(NOTIFICATION_FACEBOOK_AD_LOADED);
                logAdLoad();
                isCachedAdConsumed = false;
            }

            @Override
            public void onAdClicked(Ad ad) {
                HSLog.d("show facebook native ads onAdClicked");
                boolean shouldShowFacebookAlert = HSConfig.optBoolean(true, "Application", AdsConstants.CONFIG_NODE_NATIVE_ADS, AdsConstants.CONFIG_NODE_SHOW_FACEBOOK_ALERT);
                if (shouldShowFacebookAlert) {
                    logAdAlertClick();
                } else {
                    logAdClick();
//                    logAdShowingTime();
                }
                sendNotification(NOTIFICATION_FACEBOOK_AD_CLICKED);
                isCachedAdConsumed = true;
            }
        });
        logAdRequest();
        nativeAd.loadAd();
    }


    public void fetchNextAds() {

        if (!MyInputMethodApplication.isFacebookAppInstalled()) {
            HSLog.d("native ad facebook not installed");
            nativeAd = null;
            nativeAdNext = null;
            return;
        }
        int adLoadMode = HSConfig.optInteger(2, "Application", AdsConstants.CONFIG_NODE_NATIVE_ADS, AdsConstants.CONFIG_NODE_ADS_LOAD_MODEL);
        if (adLoadMode == 1) {
            nativeAd = null;
            nativeAdNext = null;
            HSLog.d("native ad configured to show only admob ads");
            return;
        }

        nativeAdNext = new NativeAd(HSApplication.getContext(), adsId);
        if (HSLog.isDebugging()) {
            AdSettings.addTestDevice("5d8550ba4c1830d734df636c3161282a");
        }
        nativeAdNext.setAdListener(new AdListener() {

            @Override
            public void onError(Ad ad, AdError error) {
                HSLog.d("show facebook native ads onError " + error.getErrorMessage() + " ,  code is: " + error.getErrorCode());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                HSLog.d("show facebook native ads onAdLoaded");

                if (null != nativeAdNext.getAdIcon()) {
                    ImageLoader.getInstance().loadImage(nativeAdNext.getAdIcon().getUrl(),defaultOptions, imageLoadingListener);
                }
                if (null != nativeAdNext.getAdCoverImage()) {
                    ImageLoader.getInstance().loadImage(nativeAdNext.getAdCoverImage().getUrl(),defaultOptions, imageLoadingListener);
                }

                sendNotification(NOTIFICATION_FACEBOOK_AD_LOADED);
                logAdLoad();
                isCachedAdConsumed = false;
            }

            @Override
            public void onAdClicked(Ad ad) {
                HSLog.d("show facebook native ads onAdClicked");
                boolean shouldShowFacebookAlert = HSConfig.optBoolean(true, "Application", AdsConstants.CONFIG_NODE_NATIVE_ADS, AdsConstants.CONFIG_NODE_SHOW_FACEBOOK_ALERT);
                if (shouldShowFacebookAlert) {
                    logAdAlertClick();
                } else {
                    logAdClick();
//                    logAdShowingTime();
                }
                sendNotification(NOTIFICATION_FACEBOOK_AD_CLICKED);
                isCachedAdConsumed = true;
                // fetchAds();
            }
        });
        logAdRequest();
        nativeAdNext.loadAd();
    }


    public void next() {
        if (nativeAdNext != null && nativeAdNext.isAdLoaded()) {
            nativeAd = nativeAdNext;
            nativeAdNext = null;
        }
    }


    public NativeAd getFacebookNativeAd() {
        return nativeAd;
    }

    public boolean isCachedAdConsumed() {
        return isCachedAdConsumed;
    }

    public void setStartShowingTime(long time) {
        startShowingTime = time;
    }


    public void logAdShowingTime() {
        int showingTime = (int) ((System.currentTimeMillis() - startShowingTime) / 1000);
        if (showingTime > 100) {
            showingTime = 100;
        }
        logAdShow(AdsConstants.ADS_TYPE_FACEBOOK + "    " + "time:" + String.valueOf(showingTime));
    }

    private void logAdLoad() {
        switch (adsOwner) {
            case AdsConstants.ADS_OWNER_SETTINGS:
                logEvent(Constants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_LOAD);
                break;

            default:
                break;
        }
    }

    private void logAdAlertClick() {
        switch (adsOwner) {
            case AdsConstants.ADS_OWNER_SETTINGS:
                logEvent(Constants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_ALERT_CLICKED);
                break;

            default:
                break;
        }
    }

    public void logAdClick() {
        switch (adsOwner) {
            case AdsConstants.ADS_OWNER_SETTINGS:
                logEvent(Constants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_CLICKED);
                break;

            default:
                break;
        }
    }

    private void logAdRequest() {
        switch (adsOwner) {
            case AdsConstants.ADS_OWNER_SETTINGS:
                logEvent(Constants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_REQUEST);
                break;

            default:
                break;
        }
    }

    private void logAdShow(final String label) {
        switch (adsOwner) {
            case AdsConstants.ADS_OWNER_SETTINGS:
                logEvent(Constants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_SHOW, label);
                break;

            default:
                break;
        }
    }

    private void logEvent(final String event) {
        logEvent(event, AdsConstants.ADS_TYPE_FACEBOOK);
    }

    private void logEvent(final String event, final String label) {
        HSGoogleAnalyticsUtils.getInstance().logAppEvent(event, label);
    }

    private void sendNotification(final String notification) {
        HSBundle bundle = new HSBundle();
        bundle.putInt(AdsConstants.KEY_ADS_OWNER, adsOwner);
        HSGlobalNotificationCenter.sendNotification(notification, bundle);
    }
}
