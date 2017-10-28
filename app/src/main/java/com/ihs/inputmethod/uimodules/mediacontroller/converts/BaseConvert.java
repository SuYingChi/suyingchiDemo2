package com.ihs.inputmethod.uimodules.mediacontroller.converts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.Constants;

import java.io.File;

/**
 * Created by ihandysoft on 16/6/1.
 */
public abstract class BaseConvert {

    protected File                     mFile; //生成的结果文件
    protected ISequenceFramesImageItem sequnceFramesImage;
    protected String faceName;

    public BaseConvert(ISequenceFramesImageItem sfImage, String faceName){
        this.sequnceFramesImage = sfImage;
        this.faceName = faceName;
    }

    public abstract File convert();

    /**
     * 生成新文件名称
     */
    protected String generateFileName(final String suffixFormat){
        StringBuilder filePath = new StringBuilder();//生成的文件名称
        if(suffixFormat.equals(Constants.MEDIA_FORMAT_MP4)){
            filePath.append(MediaController.getConfig().getMp4SharePath());
        }
        else {
            filePath.append(MediaController.getConfig().getGifSharePath());
        }
        filePath.append(sequnceFramesImage.getCategoryName());
        filePath.append("_");
        filePath.append(sequnceFramesImage.getName());
        filePath.append("_");
        filePath.append(faceName);
        filePath.append("_");
        filePath.append(System.currentTimeMillis());
        filePath.append(".");
        filePath.append(suffixFormat);
        return filePath.toString();
    }

    /**
     * 从 sequnceFramesImage 中提取 bitmap
     * @return
     */
    protected Bitmap getFrame(int index){

        Bitmap temp = sequnceFramesImage.getFrame(index,true);
        if(temp != null){
            return setBackgroudColor(temp, Color.WHITE);
        }

        return null;
    }

    /**
     * 设置背景颜色
     * @param bitmap
     * @param color
     * @return
     */
    protected Bitmap setBackgroudColor(Bitmap bitmap, int color){
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(color);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return newBitmap;

    }

    /**
     * 获取当前帧的延迟时间
     * @return
     */
    protected Integer getFrameInternal(int index){
        return sequnceFramesImage.getFrames().get(index).getInterval();
    }

}
