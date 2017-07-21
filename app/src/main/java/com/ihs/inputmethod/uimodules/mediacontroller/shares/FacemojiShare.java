package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import com.ihs.inputmethod.uimodules.mediacontroller.Constants;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;

/**
 * Created by ihandysoft on 16/6/1.
 */
public abstract class FacemojiShare {

    protected String format;

    protected FacemojiShare(String format){
        this.format = format;
    }

    protected String getShareDir() throws Exception {
        if (format.equals(Constants.MEDIA_FORMAT_MP4)) {
            return MediaController.getConfig().getMp4SharePath();
        } else if (format.equals(Constants.MEDIA_FORMAT_GIF)) {
            return MediaController.getConfig().getGifSharePath();
        }
        throw new Exception("unsupport the format");
    }
}
