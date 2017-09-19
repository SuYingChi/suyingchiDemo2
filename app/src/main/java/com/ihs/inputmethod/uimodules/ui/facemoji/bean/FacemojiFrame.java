package com.ihs.inputmethod.uimodules.ui.facemoji.bean;

import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;

import java.util.List;

/**
 * Created by xu.zhang on 2/27/16.
 */
public class FacemojiFrame implements ISequenceFramesImageItem.IFrame{

    public static final String FACE_PIC = "face_pic";
    private final int interval;
    private final FacePictureParam facePictureParam;// 帧参数
    private final List<String> layerFileNames; // layer : 帧路径


    public FacemojiFrame(int dur, FacePictureParam faceParam, List<String> layers){
        interval = dur;
        facePictureParam = faceParam;
        layerFileNames = layers;
    }

    public int getInterval() {
        return interval;
    }

    public FacePictureParam getFacePictureParam() {
        return facePictureParam;
    }

    public boolean isFaceOnTop(){
        return !FACE_PIC.equals(layerFileNames.get(0));
    }

    public List<String> getLayerFileNames() {
        return layerFileNames;
    }

}


