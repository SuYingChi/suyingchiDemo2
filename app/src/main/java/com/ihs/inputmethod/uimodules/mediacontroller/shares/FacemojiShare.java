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

//    protected String getMimeType() throws Exception {
//        String mimeType = null;
//        if (MasterConstants.MEDIA_FORMAT_GIF.equals(format)) {
//            mimeType = MasterConstants.MIME_IMAGE;
//            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(GAConstants.KEYBOARD_FACEMOJI_SEND_TYPE, format);
//
//        } else if(MasterConstants.MEDIA_FORMAT_MP4.equals(format)){
//            mimeType = MasterConstants.MIME_MP4;
//            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(GAConstants.KEYBOARD_FACEMOJI_SEND_TYPE, format);
//        }
//        if(mimeType != null) {
//            return mimeType;
//        }
//        else{
//            throw new Exception("unsupport the mime type");
//        }
//
//    }
}
