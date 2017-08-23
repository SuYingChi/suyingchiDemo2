package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import android.net.Uri;
import android.util.Pair;

import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.uimodules.mediacontroller.Constants;
import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.ProgressListener;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.api.utils.HSPictureUtils;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class ShareManager {

    private static ShareManager shareManager;

    private ShareManager(){}

    public static ShareManager getInstance(){
        if(shareManager == null){
            synchronized (ShareManager.class){
                if(shareManager == null){
                shareManager = new ShareManager();
            }
            }
        }
        return shareManager;
    }


    /**
     * share facemoji with keyboard
     * @param sfImage
     * @param progressListener
     */
    public void shareFacemojiWithKeyboard(ISequenceFramesImageItem sfImage, ProgressListener progressListener) {
        final String packageName = ShareChannel.CURRENT.getPackageName();
        final Pair<Integer, String> pair = ShareUtils.getSequenceFramesImageShareMode(packageName);
        final int mode = pair.first;
        // HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_facemoji_clicked", sfImage.getCategoryName() + "_" + sfImage.getName());
        if(ShareChannel.MESSAGE.getPackageName().equals(ShareChannel.CURRENT.getPackageName())){
            shareFacemojiByIntent(sfImage, pair.second, ShareChannel.CURRENT, progressListener);
            return;
        }
        switch (mode) {
            case HSPictureUtils.IMAGE_SHARE_MODE_INTENT:
                shareFacemojiByIntent(sfImage, pair.second, ShareChannel.CURRENT, progressListener);
                break;
            default:
                shareFacemojiByExport(sfImage, Constants.MEDIA_FORMAT_GIF);
                break;
        }
    }

    public void shareFacemojiByIntent(ISequenceFramesImageItem sfImage, String format, ShareChannel shareChannel){
        new IntentShare(sfImage, format, shareChannel).shareFacemoji(null);
    }
    public void shareFacemojiByIntent(ISequenceFramesImageItem sfImage, String format, ShareChannel shareChannel, ProgressListener progressListener){
        new IntentShare(sfImage, format, shareChannel).shareFacemoji(progressListener);
    }

    public void shareFacemojiByExport(ISequenceFramesImageItem sfImage, String format){
        new ExportShare(sfImage, format).share();
    }

    public void shareImageByLink(String suffix){
        new LinkShare(suffix).share();
    }

    public void shareImageByIntent(Uri uri, ShareChannel shareChannel){
        //new IntentShare(uri, format, shareChannel).shareMedia();
        ShareUtils.shareMedia(shareChannel, uri);
    }
    public void shareImageByExport(Uri uri, String targetFilePath){
        HSFileUtils.copyFile(uri.getPath(), targetFilePath);
        ShareUtils.updateGallery(targetFilePath);
    }


}
