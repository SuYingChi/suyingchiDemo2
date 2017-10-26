package com.ihs.inputmethod.uimodules.mediacontroller.converts;

import android.graphics.Bitmap;

import com.ihs.inputmethod.uimodules.mediacontroller.Constants;
import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class GifConvert extends BaseConvert {

    public GifConvert(ISequenceFramesImageItem sfImage, String faceName) {
        super(sfImage, faceName);
    }

    @Override
    public File convert() {
        final String fileName = generateFileName(Constants.MEDIA_FORMAT_GIF);

        mFile = new File(fileName);
        if (mFile.exists()) {
            return mFile;
        }

        GifEncoder gifEncoder = new GifEncoder();
        int count = sequnceFramesImage.getFrames().size();
        Bitmap frameBitmap = sequnceFramesImage.getFrame(0,true);
        try {
            gifEncoder.init(frameBitmap.getWidth(), frameBitmap.getHeight(), fileName, GifEncoder.EncodingType.ENCODING_TYPE_SIMPLE_FAST);
            gifEncoder.encodeFrame(frameBitmap, getFrameInternal(0));
            for(int i = 1; i < count; i++){
                gifEncoder.encodeFrame(getFrame(i), getFrameInternal(i));
            }
            gifEncoder.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return mFile;
    }
}
