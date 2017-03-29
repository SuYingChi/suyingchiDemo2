package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.converts.SyncWorkHandler;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.api.utils.HSToastUtils;

import java.io.File;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class ExportShare extends FacemojiShare {

    private ISequenceFramesImageItem mSequenceFramesImage;

    public ExportShare(ISequenceFramesImageItem sfImage, String format) {
        super(format);
        this.mSequenceFramesImage = sfImage;
    }

    public void share() {
        HSToastUtils.toastCenterLong("The facemoji was saved to your gallery.");
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
        File[] tempFiles = HSFileUtils.listFile(shareDir, mSequenceFramesImage.getCategoryName() + "_" + mSequenceFramesImage.getName() + "_" + faceName, format);
        // the format file has created
        if (tempFiles != null && tempFiles.length > 0) {
            // do share
            ShareUtils.updateGallery(tempFiles[0].getAbsolutePath());
            return;
        }

        // generate mp4 or gif
        SyncWorkHandler.getInstance().post(new SyncWorkHandler.MediaConvertRunnable(mSequenceFramesImage, faceName, format) {

            @Override
            public void onExecuteFinished(final String filePath) {
                // Show progress
                MediaController.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        // do share
                        ShareUtils.updateGallery(filePath);
                    }
                });
            }

            @Override
            public void onInterrupted() {}
        });
    }
}
