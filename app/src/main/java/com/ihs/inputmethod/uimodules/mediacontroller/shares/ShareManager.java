package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import android.net.Uri;
import android.util.Pair;

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


// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * share facemoji with keyboard
//     * @param sfImage
//     * @param progressListener
//     */
//    public void shareFacemojiWithKeyboard(ISequenceFramesImageItem sfImage, ProgressListener progressListener) {
//        final String packageName = ShareChannel.CURRENT.getPackageName();
//        final Pair<Integer, String> pair = ShareUtils.getSequenceFramesImageShareMode(packageName);
//        final int mode = pair.first;
//        if(ShareChannel.MESSAGE.getPackageName().equals(ShareChannel.CURRENT.getPackageName())){
//            shareFacemojiByIntent(sfImage, pair.second, ShareChannel.CURRENT, progressListener);
//            return;
//        }
//        switch (mode) {
//            case HSPictureUtils.IMAGE_SHARE_MODE_INTENT:
//                shareFacemojiByIntent(sfImage, pair.second, ShareChannel.CURRENT, progressListener);
//                break;
//            default:
//                shareFacemojiByExport(sfImage, Constants.MEDIA_FORMAT_GIF);
//                break;
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public void shareFacemojiByIntent(ISequenceFramesImageItem sfImage, String format, ShareChannel shareChannel, ProgressListener progressListener){
        new IntentShare(sfImage, format, shareChannel).shareFacemoji(progressListener);
    }

    public void shareFacemojiFromKeyboard(ISequenceFramesImageItem sfImage, String format, ShareChannel shareChannel, ProgressListener progressListener){
        new IntentShare(sfImage, format, true ,shareChannel).shareFacemoji(progressListener);
    }

    public void shareFacemojiByExport(ISequenceFramesImageItem sfImage, String format){
        new ExportShare(sfImage, format).share();
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void shareImageByIntent(Uri uri, ShareChannel shareChannel){
//        //new IntentShare(uri, format, shareChannel).shareMedia();
//        ShareUtils.shareMedia(shareChannel, uri);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void shareImageByExport(Uri uri, String targetFilePath){
//        HSFileUtils.copyFile(uri.getPath(), targetFilePath);
//        ShareUtils.updateGallery(targetFilePath);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)


}
