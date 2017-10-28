package com.ihs.inputmethod.uimodules.ui.facemoji.bean;

import android.graphics.drawable.Drawable;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSMapUtils;
import com.ihs.inputmethod.api.utils.HSYamlUtils;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xu.zhang on 3/7/16.
 */
public class FacemojiCategory {
    private final int mCategoryId;
    private String iconFileName;
    private List<FacemojiSticker> stickerList;

    public String getName() {
        return name;
    }

    private String name;

    public FacemojiCategory(String name, int categoryId){
        this.name = name;
        this.mCategoryId = categoryId;
        stickerList = new ArrayList<>();
        parseCategoryMetadata();
    }

    public int getCategoryId() {
        return mCategoryId;
    }
    private void parseCategoryMetadata(){
        String filePath = HSApplication.getContext().getFilesDir().getAbsolutePath();
        String path = filePath + "/Mojime/" + name + "/package.yaml";
        Map<String, Object> mStyleMap = HSYamlUtils.getYamlConfigMap(path, false);
        iconFileName = HSMapUtils.getString(mStyleMap, "icon");
        List<?> stickers = HSMapUtils.getList(mStyleMap, "stickers");
        for (Object stickerName : stickers) {
            stickerList.add(new FacemojiSticker(name, (String)((Map<String,?>) stickerName).get("name")));
        }
    }

    public List<FacemojiSticker> getStickerList(){
        return stickerList;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public Drawable getCategoryIcon(){
        String path = HSApplication.getContext().getFilesDir().getAbsolutePath() + "/Mojime/" + name + "/"+iconFileName;
        return Drawable.createFromPath(path);
    }

    public int getPageCount(FacemojiManager.ShowLocation showLocation, int orientation){
        return (int) Math.ceil((stickerList.size()*1.0)/ FacemojiManager.getCurrentPageSize(showLocation,orientation, stickerList.get(0)));
    }

    public List<FacemojiSticker> getStickerList(int page,FacemojiManager.ShowLocation showLocation, int orientation){
        List<FacemojiSticker> data=new ArrayList<>();
        int start = FacemojiManager.getCurrentPageSize(showLocation, orientation, stickerList.get(0)) * page;
        int end = start + FacemojiManager.getCurrentPageSize(showLocation, orientation, stickerList.get(0));
        end = stickerList.size() > end ? end : stickerList.size();
        for(int i=start;i<end;i++){
            data.add(stickerList.get(i));
        }
        return data;
    }
}
