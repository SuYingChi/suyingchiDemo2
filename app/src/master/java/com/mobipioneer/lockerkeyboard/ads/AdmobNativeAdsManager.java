package com.mobipioneer.lockerkeyboard.ads;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.mobipioneer.lockerkeyboard.app.MyInputMethodApplication;
import com.mobipioneer.lockerkeyboard.utils.MasterConstants;

/**
 * Created by xu.zhang on 12/10/15.
 */
public class AdmobNativeAdsManager {

    public static final String NOTIFICATION_APP_INSTALL_AD_LOADED = "notification_app_install_ad_loaded";
    public static final String NOTIFICATION_CONTENT_AD_LOADED = "notification_content_ad_loaded";
    public static final String NOTIFICATION_APP_INSTALL_AD_CLICKED = "notification_app_install_ad_clicked";
    private NativeAd mNativeAd;
    private NativeAd mNativeAdNext;

    private boolean isCachedAdConsumed = true;
    private long startShowingTime;

    private int adsOwner;

    private AdLoader adLoader;

    /**
     * Constructor
     * @param adsOwner Ads observer
     * @param adsId ads unit id
     */
    public AdmobNativeAdsManager(final int adsOwner, final String adsId) {
        this.adsOwner = adsOwner;

        adLoader = new AdLoader.Builder(HSApplication.getContext(), adsId)
                .forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
                    @Override
                    public void onAppInstallAdLoaded(NativeAppInstallAd appInstallAd) {
                        // Show the app install ad.
                        HSLog.d("admob onAppInstallAdLoaded .....");
                        logAdLoad();
                        if (mNativeAd == null) {
                            mNativeAd = appInstallAd;
                        } else if (mNativeAdNext == null) {
                            mNativeAdNext = appInstallAd;
                        }
                        isCachedAdConsumed = false;
                        sendNotification(NOTIFICATION_APP_INSTALL_AD_LOADED);
                    }
                })
                .forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                    @Override
                    public void onContentAdLoaded(NativeContentAd contentAd) {
                        HSLog.d("admob onContentAdLoaded .....");
                        logAdLoad();
                        if (mNativeAd == null) {
                            mNativeAd = contentAd;
                        } else if (mNativeAdNext == null) {
                            mNativeAdNext = contentAd;
                        }
                        isCachedAdConsumed = false;
                        sendNotification(NOTIFICATION_APP_INSTALL_AD_LOADED);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        HSLog.d("admob onAdFailedToLoad ....." + "  error code is " + errorCode);
                    }

                    @Override
                    public void onAdOpened() {
                        super.onAdOpened();
                        logAdAlertClick();
                        HSLog.d("admob onAdOpened .....");
                        isCachedAdConsumed = true;
                        sendNotification(NOTIFICATION_APP_INSTALL_AD_CLICKED);
                        //fetchAds();
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        HSLog.d("admob onAdClosed .....");
                    }

                    @Override
                    public void onAdLeftApplication() {
                        super.onAdLeftApplication();
                        HSLog.d("admob onAdLeftApplication .....");
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().setReturnUrlsForImageAssets(true).setRequestMultipleImages(false).setImageOrientation(NativeAdOptions.ORIENTATION_LANDSCAPE).build())
                .build();
    }

    public void fetchAds() {
        if (adLoader.isLoading()) {
            return;
        }

        if (!MyInputMethodApplication.isGooglePlayInstalled()) {
            mNativeAd = null;
            mNativeAdNext = null;
            HSLog.d("native ad google play not installed");
            return;
        }

        int adLoadMode = HSConfig.optInteger(2, "Application", AdsConstants.CONFIG_NODE_NATIVE_ADS, AdsConstants.CONFIG_NODE_ADS_LOAD_MODEL);
        if (adLoadMode == 0) {
            mNativeAd = null;
            mNativeAdNext = null;
            HSLog.d("native ad configured to show facebook only");
            return;
        }

        logAdRequest();
        AdRequest.Builder builder = new AdRequest.Builder();
        if (HSLog.isDebugging()) {
            builder.addTestDevice("F89A2030078AF5025BE4DABF3785C143");
        }
        adLoader.loadAd(builder.build());
    }

    public boolean isCachedAdConsumed() {
        return isCachedAdConsumed;
    }

    public NativeAd getNativeAppInstallAd() {
        return mNativeAd;
    }

    public void setStartShowingTime(long time) {
        startShowingTime = time;
    }


    public void logAdShowingTime() {
        int showingTime = (int) ((System.currentTimeMillis() - startShowingTime) / 1000);
        if (showingTime > 100) {
            showingTime = 100;
        }

        logAdShow(AdsConstants.ADS_TYPE_ADMOB + "    time:" + String.valueOf(showingTime));
    }

    private void logAdLoad() {
        switch (adsOwner) {
            case AdsConstants.ADS_OWNER_SETTINGS:
                logEvent(MasterConstants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_LOAD);
                break;

            default:
                break;
        }
    }

    private void logAdAlertClick() {
        switch (adsOwner) {
            case AdsConstants.ADS_OWNER_SETTINGS:
                logEvent(MasterConstants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_ALERT_CLICKED);
                break;

            default:
                break;
        }
    }

    public void logAdClick() {
        switch (adsOwner) {
            case AdsConstants.ADS_OWNER_SETTINGS:
                logEvent(MasterConstants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_CLICKED);
                break;

            default:
                break;
        }
    }

    private void logAdRequest() {
        switch (adsOwner) {
            case AdsConstants.ADS_OWNER_SETTINGS:
                logEvent(MasterConstants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_REQUEST);
                break;

            default:
                break;
        }
    }

    private void logAdShow(final String label) {
        switch (adsOwner) {
            case AdsConstants.ADS_OWNER_SETTINGS:
                logEvent(MasterConstants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_SHOW, label);
                break;

            default:
                break;
        }
    }

    private void logEvent(final String event) {
        logEvent(event, AdsConstants.ADS_TYPE_ADMOB);
    }

    private void logEvent(final String event, final String label) {
        HSGoogleAnalyticsUtils.getInstance().logAppEvent(event, label);
    }

    public void next() {
        if (mNativeAdNext != null) {
            mNativeAd = mNativeAdNext;
            mNativeAdNext = null;
        }
    }

    private void sendNotification(final String notification) {
        HSBundle bundle = new HSBundle();
        bundle.putInt(AdsConstants.KEY_ADS_OWNER, adsOwner);
        HSGlobalNotificationCenter.sendNotification(notification, bundle);
    }

}
