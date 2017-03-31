package com.ihs.inputmethod.uimodules.mediacontroller.converts;

import com.ihs.inputmethod.uimodules.mediacontroller.Constants;
import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;
import com.ihs.inputmethod.uimodules.utils.gif.GifEncoder;
import com.ihs.inputmethod.api.utils.HSFileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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

        //创建文件
        File mFile = HSFileUtils.createNewFile(fileName);

        // 2、写入帧图片
        OutputStream os = null;
        GifEncoder gifEncoder = null;
        try {
            os = new FileOutputStream(mFile);
            gifEncoder = new GifEncoder();
            gifEncoder.start(os);
            gifEncoder.setRepeat(0);//无限重复
            int count = sequnceFramesImage.getFrames().size();
            for(int i = 0; i < count; i++){
                gifEncoder.addFrame(getFrame(i));
                gifEncoder.setDelay(getFrameInternal(i));//设置延时
            }
            gifEncoder.finish();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  finally {
            if(os != null)
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return mFile;
    }
}
