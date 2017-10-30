package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import android.net.Uri;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.converts.SyncWorkHandler;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.ProgressListener;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.MediaShareUtils;

import java.io.File;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class IntentShare extends FacemojiShare {

    private ISequenceFramesImageItem sequenceFramesImage;
    private ShareChannel shareChannel;
    private Uri shareFileUri;
    private boolean fromKeyboard;

    public IntentShare(ISequenceFramesImageItem sfImage, String format, final ShareChannel channel) {
        this(sfImage, format, false, channel);
    }

    public IntentShare(ISequenceFramesImageItem sfImage, String format, boolean fromKeyboard, final ShareChannel channel) {
        super(format);
        this.sequenceFramesImage = sfImage;
        this.shareChannel = channel;
        this.fromKeyboard = fromKeyboard;
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
        String facemojiName = sequenceFramesImage.getCategoryName() + "_" + sequenceFramesImage.getName() + "_" + faceName;
        File[] tempFiles = HSFileUtils.listFile(shareDir, facemojiName, format);
        // the format file has created
        if (tempFiles != null && tempFiles.length > 0 && !SyncWorkHandler.getInstance().isRunning()) {
            // do share
            this.shareFileUri = Uri.fromFile(new File(tempFiles[0].getAbsolutePath()));
            if (fromKeyboard) {
                MediaShareUtils.share(".gif", shareChannel.getPackageName(), new File(tempFiles[0].getAbsolutePath()), "");
            } else {
                ShareUtils.shareMedia(shareChannel, shareFileUri);
            }
            return;
        }
        // Show progress
        if (progressListener != null) {
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
                        File file = new File(filePath);
                        IntentShare.this.shareFileUri = Uri.fromFile(file);
                        if (fromKeyboard) {
                            MediaShareUtils.share(".gif", shareChannel.getPackageName(), file, "");
                        } else {
                            ShareUtils.shareMedia(shareChannel, shareFileUri);
                        }
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
