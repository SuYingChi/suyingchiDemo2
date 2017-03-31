package com.ihs.inputmethod.uimodules.mediacontroller.converts;

import android.graphics.Bitmap;

import com.ihs.inputmethod.uimodules.mediacontroller.Constants;
import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;
import com.ihs.inputmethod.api.utils.HSFileUtils;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.scale.BitmapUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class MP4Convert extends BaseConvert {

    public MP4Convert(ISequenceFramesImageItem sfImage, String faceName) {
        super(sfImage, faceName);
    }

    @Override
    public File convert() {
        final String fileName = generateFileName(Constants.MEDIA_FORMAT_MP4);

        mFile = new File(fileName);
        if (mFile.exists()) {
            return mFile;
        }

        //创建文件
        File file = HSFileUtils.createNewFile(fileName);

        //生成MP4
        try {
            SequenceEncoder sequenceEncoder = new SequenceEncoder(file);

            // current total time of mp4
            int totalTime = 0;
            // the index of current frame
            int i = 0;
            // sequenceFramesImage frame count
            int frameCount = sequnceFramesImage.getFrames().size();
            while(!(totalTime >= Constants.MP4_PLAY_TIME_MIN && (i % frameCount == 0))){
                // the current frame internal
                int frameInternal = getFrameInternal(i % frameCount);
                // 帧率为25（每帧的默认时长为40）, cycleCount 为当前帧需要插入次数
                int cycleCount = (int) Math.ceil(frameInternal / 40.0);
                Bitmap bitmap = getFrame(i % frameCount);
                org.jcodec.common.model.Picture pic = BitmapUtil.fromBitmap(bitmap);
                for(int j = 0; j < cycleCount; j++) {
                    sequenceEncoder.encodeNativeFrame(pic);
                }
                totalTime += cycleCount * 40.0;
                i++;
            }
            sequenceEncoder.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }
}
