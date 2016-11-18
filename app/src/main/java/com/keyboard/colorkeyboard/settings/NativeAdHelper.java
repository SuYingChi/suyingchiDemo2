package com.keyboard.colorkeyboard.settings;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.keyboardutils.nativeads.NativeAdConfig;
import com.ihs.keyboardutils.nativeads.NativeAdManager;
import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by jixiang on 16/11/18.
 */

public class NativeAdHelper {
    private static NativeAdHelper instance = new NativeAdHelper();
    public static NativeAdHelper getInstance() {
        return instance;
    }
    private NativeAdHelper(){}

    NativeAdView nativeAdView;

    String adPoolName = "KeyboardSettingAd";

    public void createAd(){
        if(nativeAdView == null){
            HSGlobalNotificationCenter.addObserver(NativeAdView.NOTIFICATION_NATIVE_AD_CLIKED, notificationObserver);
            HSGlobalNotificationCenter.addObserver(NativeAdView.NOTIFICATION_NATIVE_AD_SHOWED, notificationObserver);
            nativeAdView = new NativeAdView(HSApplication.getContext());
            nativeAdView.setConfigParams(adPoolName, com.ihs.inputmethod.uimodules.R.layout.ad_style_3, NativeAdConfig.getNativeAdFrequency());
            ViewItemBuilder.getAdsItem().viewContainer.addView(nativeAdView);
        }
    }

    public void releaseAd(){
        if(nativeAdView!=null){
            nativeAdView.release();
        }
    }

    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if(NativeAdView.NOTIFICATION_NATIVE_AD_CLIKED.equals(s)){
                if(adPoolName!=null && adPoolName.equals(hsBundle.getString(NativeAdManager.NATIVE_AD_POOL_NAME))) {
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("Keyboard_Setting_AD_Click");
                }

            }
            else if(NativeAdView.NOTIFICATION_NATIVE_AD_SHOWED.equals(s)){
                if(adPoolName!=null && adPoolName.equals(hsBundle.getString(NativeAdManager.NATIVE_AD_POOL_NAME))) {
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("Keyboard_Setting_AD_Show");
                }
            }
        }
    };
}
