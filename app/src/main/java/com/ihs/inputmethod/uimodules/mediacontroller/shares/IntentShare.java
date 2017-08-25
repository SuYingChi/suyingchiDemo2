package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import android.net.Uri;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.converts.SyncWorkHandler;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.ProgressListener;
import com.ihs.inputmethod.api.utils.HSFileUtils;

import java.io.File;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class IntentShare extends FacemojiShare {

    private ISequenceFramesImageItem sequenceFramesImage;
    private ShareChannel shareChannel;
    private Uri shareFileUri;

    public IntentShare(Uri uri, String format, ShareChannel channel){
        super(format);
        this.shareFileUri = uri;
        this.shareChannel = channel;
    }

    public IntentShare(ISequenceFramesImageItem sfImage, String format, final ShareChannel channel) {
        super(format);
        this.sequenceFramesImage = sfImage;
        this.shareChannel = channel;
    }

    public void shareFacemoji(final ProgressListener progressListener) {

        HSLog.d("shareFacemojiByIntent " + sequenceFramesImage.getCategoryName() + "-" + sequenceFramesImage.getName() + " via " + shareChannel.getPackageName());

        // share cache dir
        String shareDir = null;
        try {
            shareDir = getShareDir();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // check current face picture
        String faceName = MediaController.getFaceNameProvider().faceName();
        if (faceName == null) return;
        // check the format file
        File[] tempFiles = HSFileUtils.listFile(shareDir, sequenceFramesImage.getCategoryName() + "_" + sequenceFramesImage.getName() + "_" + faceName, format);
        // the format file has created
        if (tempFiles != null && tempFiles.length > 0 && !SyncWorkHandler.getInstance().isRunning()) {
            // do share
            this.shareFileUri = Uri.fromFile(new File(tempFiles[0].getAbsolutePath()));
            ShareUtils.shareMedia(shareChannel, shareFileUri);
            return;
        }
        // Show progress
        if(progressListener != null) {
            progressListener.startProgress();
        }
        // generate mp4 or gif
        SyncWorkHandler.getInstance().post(new SyncWorkHandler.MediaConvertRunnable(sequenceFramesImage, faceName, format) {

            @Override
            public void onExecuteFinished(final String filePath) {
                // Show progress
                MediaController.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (progressListener != null) {
                            progressListener.stopProgress();
                        }
                        // do share
                        IntentShare.this.shareFileUri = Uri.fromFile(new File(filePath));
                        ShareUtils.shareMedia(shareChannel, shareFileUri);
                    }
                });
            }

            @Override
            public void onInterrupted() {
                HSLog.d("");
                MediaController.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        progressListener.stopProgress();
                    }
                });
            }
        });
    }

    /**
     * 分享
     */
//    public void shareMedia() {
//        // 可选的支持的应用包名，如果默认的包名对应的应用不能打开，则尝试可选的包名对应的应用打开
//        String[] optionPackageName = MediaShareUtils.getAvailablePackages(shareChannel);
//        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setPackage(shareChannel.getPackageName());
//        shareIntent.putExtra(Intent.EXTRA_STREAM, shareFileUri);
//        try {
//            shareIntent.setType(getMimeType());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        if(MediaShareUtils.isIntentAvailable(shareIntent)) {
//            HSApplication.getContext().startActivity(shareIntent);
//            return;
//        }else if(optionPackageName!=null && optionPackageName.length >0){
//            for(String pkg : optionPackageName){
//                shareIntent.setPackage(pkg);
//                if(MediaShareUtils.isIntentAvailable(shareIntent)) {
//                    HSApplication.getContext().startActivity(shareIntent);
//                    return;
//                }
//            }
//        }
//        Toast.makeText(HSApplication.getContext(), "sorry,can not find appropriate share tools", Toast.LENGTH_SHORT).show();
//    }


}
