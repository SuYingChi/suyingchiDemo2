package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import android.net.Uri;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.converts.SyncWorkHandler;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.ProgressListener;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;

import java.io.File;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class IntentShare extends FacemojiShare {

    private ISequenceFramesImageItem sequenceFramesImage;
    private ShareChannel shareChannel;
    private Uri shareFileUri;

    public IntentShare(ISequenceFramesImageItem sfImage, String format, final ShareChannel channel) {
        super(format);
        this.sequenceFramesImage = sfImage;
        this.shareChannel = channel;
    }

    public void shareFacemoji(final ProgressListener progressListener) {

        // share cache dir
        String shareDir = null;
        try {
            shareDir = getShareDir();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check current face picture
        String faceName = HSFileUtils.getFileName(FacemojiManager.getCurrentFacePicUri());
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

}
