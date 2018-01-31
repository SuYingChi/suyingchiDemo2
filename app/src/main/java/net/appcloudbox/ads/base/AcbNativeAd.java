package net.appcloudbox.ads.base;

/**
 * Created by Arthur on 2018/1/31.
 */

public class AcbNativeAd {
    public static Object LOAD_RESOURCE_TYPE_ICON;
    private AcbNativeClickListener nativeClickListener;
    private Object iconUrl;

    public void release() {

    }

    public void setNativeClickListener(AcbNativeClickListener nativeClickListener) {
        this.nativeClickListener = nativeClickListener;
    }

    public String getResourceFilePath(Object loadResourceTypeIcon) {
        return "";
    }

    public String getIconUrl() {
        return "";
    }

    public CharSequence getSubtitle() {
        return "aa";
    }


    public interface AcbNativeClickListener {
        public void onAdClick(AcbAd acbAd);
    }

    public interface AcbAdListener {
        public void onAdExpired(AcbAd acbAd);

        public void onAdWillExpired(AcbAd acbAd);
    }
}
