package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.utils.HSPictureUtils;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class LinkShare {

    private String suffix;

    public static final String RMTCFG_KEY_L2_SERVER = "Server";

    public static final String RMTCFG_KEY_L3_IMAGE_URL_PREFIX = "ImageURLPrefix";

    public LinkShare(String suffix) {
        this.suffix = suffix;
    }

    private static String getImageUrlPrefix() {
        return HSConfig.getString(HSPictureUtils.RMTCFG_KEY_L1_APPLICATION, RMTCFG_KEY_L2_SERVER, RMTCFG_KEY_L3_IMAGE_URL_PREFIX);
    }

    public void share(){
        final String linkPrefix = getImageUrlPrefix();
        HSInputMethod.inputText(linkPrefix + suffix + " ");
    }
}
