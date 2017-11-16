package com.ihs.inputmethod.uimodules.ui.facemoji.bean;

import android.graphics.Bitmap;

import com.crashlytics.android.Crashlytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;
import com.ihs.inputmethod.api.utils.HSYamlUtils;
import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xu.zhang on 2/27/16.
 */
public class FacemojiSticker implements ISequenceFramesImageItem {
    private String categoryName;//sticker的category
    private String name;//sticker的名字
    private int version;
    private int width;
    private int height;
    private List<FacemojiFrame> animFrames;

    public String getCategoryName() {
        return categoryName;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacemojiSticker that = (FacemojiSticker) o;

        if (version != that.version) return false;
        if (categoryName != null ? !categoryName.equals(that.categoryName) : that.categoryName != null)
            return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        int result = categoryName != null ? categoryName.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + version;
        return result;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public List<? extends IFrame> getFrames() {
        return animFrames;
    }

    @Override
    public Bitmap getFrame(int index,boolean encodeGif) {
        return FacemojiManager.getFrame(this, index ,encodeGif);
    }

    public List<FacemojiFrame> getFacemojiFrames() {
        return animFrames;
    }

    public FacemojiSticker(String categoryName, String name){
        this.categoryName = categoryName;
        this.name = name;
        parseMojimeSticker();
    }

    public FacemojiSticker(String categoryName, int width,int height){
        this.categoryName = categoryName;
        this.width = width;
        this.height = height;
    }

    private void parseMojimeSticker() {
        try {
            Map<String, Object> mStyleMap = HSYamlUtils.getYamlConfigMap(getMojimeConfigFilePath(), false);
            if (mStyleMap == null){
                Crashlytics.setString("category_name", this.categoryName != null?this.categoryName:"null");
                Crashlytics.setString("sticker_name", this.name != null?this.name:"null");
                Crashlytics.logException(new IllegalStateException("exception_parse_facemoji_sticker_failure"));
                return;
            }

            String stickername = (String) mStyleMap.get("name");
            version = (Integer) mStyleMap.get("version");
            width = HSMapUtils.getInteger(mStyleMap, "size", "width");
            height = HSMapUtils.getInteger(mStyleMap, "size", "height");
            HSLog.d("stickername is " + stickername + " version " + version + " width " + width + " height " + height);
            List<?> frames = HSMapUtils.getList(mStyleMap, "frames");
            animFrames = new ArrayList<>(frames.size());
            for (Object f : frames) {
                int w = 0;
                int h = 0;
                float transX = 0;
                float transY = 0;
                float sclX = 0;
                float sclY = 0;
                float skewX = 0;
                float skewY = 0;
                int interval = HSMapUtils.getInteger((Map<String, ?>) f, "interval");
                HSLog.d("frame interval is " + interval);
                List<?> layers = HSMapUtils.getList((Map<String, ?>) f, "layers");
                List<FacemojiFrame.FacemojiLayer> layerFileNames = new ArrayList<>(layers.size());
                for (Object l : layers) {
                    int type = HSMapUtils.getInteger((Map<String, ?>) l, "type");
                    if (type == 1) {
                        layerFileNames.add(new FacemojiFrame.FacemojiLayer(type,HSMapUtils.getString((Map<String, ?>) l, "src")));
                    } else {
                        layerFileNames.add(new FacemojiFrame.FacemojiLayer(type,null));
                        w = HSMapUtils.getInteger((Map<String, ?>) l, "size", "width");
                        h = HSMapUtils.getInteger((Map<String, ?>) l, "size", "height");
                        transX = HSMapUtils.optFloat((Map<String, ?>) l, 0, "transform", "translateX");
                        transY = HSMapUtils.optFloat((Map<String, ?>) l, 0, "transform", "translateY");
                        sclX = HSMapUtils.optFloat((Map<String, ?>) l, 1, "transform", "scaleX");
                        sclY = HSMapUtils.optFloat((Map<String, ?>) l, 1, "transform", "scaleY");
                        skewX = HSMapUtils.optFloat((Map<String, ?>) l, 0, "transform", "skewX");
                        skewY = HSMapUtils.optFloat((Map<String, ?>) l, 0, "transform", "skewY");
                    }
                }
                FacePictureParam param = new FacePictureParam(w, h, transX, transY, sclX, sclY, skewX, skewY);
                FacemojiFrame frame = new FacemojiFrame(interval, param, layerFileNames);
                animFrames.add(frame);
            }
        }catch (Exception e){

        }
    }

    private String getMojimeConfigFilePath() {
        String filePath = HSApplication.getContext().getFilesDir().getAbsolutePath();
        String path = filePath + "/Mojime/" + categoryName + "/" + name + "/manifest.yaml";
        return path;
    }



}
