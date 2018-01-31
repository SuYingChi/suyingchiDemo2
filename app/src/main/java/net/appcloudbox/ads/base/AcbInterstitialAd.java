package net.appcloudbox.ads.base;

/**
 * Created by Arthur on 2018/1/31.
 */

public class AcbInterstitialAd {
    private IAcbInterstitialAdListener interstitialAdListener;

    public void setInterstitialAdListener(IAcbInterstitialAdListener interstitialAdListener) {
        this.interstitialAdListener = interstitialAdListener;
    }

    public void show() {

    }

    public interface IAcbInterstitialAdListener {
        public void onAdDisplayed();

        public void onAdClicked();

        public void onAdClosed();
    }
}
