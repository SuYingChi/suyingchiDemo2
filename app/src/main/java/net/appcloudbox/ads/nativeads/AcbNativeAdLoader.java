package net.appcloudbox.ads.nativeads;

import android.content.Context;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.common.utils.AcbError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arthur on 2018/1/31.
 */

public class AcbNativeAdLoader {
    public AcbNativeAdLoader(Context context, String string) {

    }

    public void cancel() {

    }

    public void load(int i, AcbNativeAdLoadListener acbNativeAdLoadListener) {

    }

    public static void preload(Context context, int i, String string) {

    }

    public static List<AcbNativeAd> fetch(Context context, String string, int i) {
        return new ArrayList<>();
    }

    public interface AcbNativeAdLoadListener {
        public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) ;

        public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError hsError);
    }
}
