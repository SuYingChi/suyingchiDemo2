package com.ihs.inputmethod.ads.fullscreen;

import com.acb.adadapter.AcbInterstitialAd;
import com.acb.interstitialads.AcbInterstitialAdLoader;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;

import java.util.List;

/**
 * Created by ihandysoft on 17/4/12.
 */

public abstract class FullScreenAd {

    private String placementName;
    private AcbInterstitialAdLoader acbInterstitialAdLoader;

    FullScreenAd(String placement) {
        this.placementName = placement;
    }

    protected abstract boolean isConditionSatisfied();

    protected abstract void hasFetchedAd();

    void preLoad() {
        // 满足加载条件
        if (isConditionSatisfied()) {
            if (acbInterstitialAdLoader != null) {
                acbInterstitialAdLoader.cancel();
            }
            acbInterstitialAdLoader = new AcbInterstitialAdLoader(HSApplication.getContext(), placementName);
            acbInterstitialAdLoader.load(1, null);
        }
    }

    public void show() {
        if (isConditionSatisfied()) {
            HSGoogleAnalyticsUtils.getInstance().logAppEvent(placementName + "_Load");
            List<AcbInterstitialAd> interstitialAds = AcbInterstitialAdLoader.fetch(HSApplication.getContext(), placementName, 1);
            if (interstitialAds != null && interstitialAds.size() > 0) {
                hasFetchedAd();
                final AcbInterstitialAd interstitialAd = interstitialAds.get(0);
                interstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                    @Override
                    public void onAdDisplayed() {
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(placementName + "_Show");
                    }

                    @Override
                    public void onAdClicked() {
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(placementName + "_Click");
                    }

                    @Override
                    public void onAdClosed() {
                        interstitialAd.release();
                        release();
                    }
                });
                interstitialAd.show();
            }
        }
    }

    protected void release() {
        if (acbInterstitialAdLoader != null) {
            acbInterstitialAdLoader.cancel();
            acbInterstitialAdLoader = null;
        }
    }
}
