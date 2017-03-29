package com.ihs.inputmethod.uimodules.mediacontroller.converts;

import com.ihs.inputmethod.uimodules.mediacontroller.Constants;
import com.ihs.inputmethod.uimodules.mediacontroller.IManager;
import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;

import java.io.File;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class ConvertManager implements IManager{

    private static ConvertManager convertManager;
    private ConvertManager(){

    }
    public static ConvertManager getInstance(){
        if(convertManager == null){
            synchronized (ConvertManager.class){
                if(convertManager == null){
                convertManager = new ConvertManager();
                }
            }
        }
        return convertManager;
    }

    public File convertSequenceFramesImage(ISequenceFramesImageItem sfImage, String faceName, String format) throws Exception{
        if(format.equals(Constants.MEDIA_FORMAT_MP4)) {
            return new MP4Convert(sfImage, faceName).convert();
        }
        else if(format.equals(Constants.MEDIA_FORMAT_GIF)) {
            return new GifConvert(sfImage, faceName).convert();
        }
        throw new Exception("media convert failed");
    }
}
