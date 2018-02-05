package com.ihs.inputmethod.uimodules.ui.facemoji.bean;

import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;

import java.util.List;

/**
 * Created by xu.zhang on 2/27/16.
 */
public class FacemojiFrame implements ISequenceFramesImageItem.IFrame{

    // --Commented out by Inspection (18/1/11 下午2:41):public static final String FACE_PIC = "face_pic";
    private final int interval;
    private final FacePictureParam facePictureParam;// 帧参数
    private final List<FacemojiLayer> facemojiLayers; // layer : 帧路径


    public FacemojiFrame(int dur, FacePictureParam faceParam, List<FacemojiLayer> facemojiLayers){
        interval = dur;
        facePictureParam = faceParam;
        this.facemojiLayers = facemojiLayers;
    }

    public int getInterval() {
        return interval;
    }

    public FacePictureParam getFacePictureParam() {
        return facePictureParam;
    }

    public List<FacemojiLayer> getLayerList() {
        return facemojiLayers;
    }

    public static class FacemojiLayer {
        int type;
        public String srcName;
        public boolean isFace(){
            return type == 0;
        }

        public FacemojiLayer(int type, String srcName) {
            this.type = type;
            this.srcName = srcName;
        }
    }
}


