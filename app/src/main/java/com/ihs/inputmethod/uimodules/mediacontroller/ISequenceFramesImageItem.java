package com.ihs.inputmethod.uimodules.mediacontroller;

import java.util.List;
import android.graphics.Bitmap;


/**
 * 模块管理的基于帧序列的图片资源.
 *
 * @author xiqiandong
 */
public interface ISequenceFramesImageItem extends  IImageItem{
    interface IFrame {
        int getInterval();
    }

    List<? extends IFrame> getFrames();
    Bitmap getFrame(int index);
}
