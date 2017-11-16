package com.ihs.inputmethod.uimodules.ui.facemoji.bean;

import android.graphics.drawable.Drawable;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSMapUtils;
import com.ihs.inputmethod.api.utils.HSYamlUtils;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiDownloadManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xu.zhang on 3/7/16.
 */
public class FacemojiCategory {
    private final static int DEFAULT_SIZE = 6;
    private final int mCategoryId;
    private int width;
    private int height;
    private boolean isBuildIn;
    private String iconFileName;
    private String name;
    private List<FacemojiSticker> stickerList = new ArrayList<>();

    public FacemojiCategory(String name, int categoryId, int width, int height, boolean isBuildIn) {
        this.name = name;
        this.mCategoryId = categoryId;
        this.width = width;
        this.height = height;
        this.isBuildIn = isBuildIn;
    }

    public void setFakeStickerListData(){
        // 设置6个空的sticker占位
        stickerList.clear();
        FacemojiSticker facemojiSticker;
        for (int i = 0 ; i < DEFAULT_SIZE ; i ++){
            facemojiSticker = new FacemojiSticker(name,width,height);
            stickerList.add(facemojiSticker);
        }
    }

    public void parseYaml() {
        try {
            String filePath = HSApplication.getContext().getFilesDir().getAbsolutePath();
            String path = filePath + "/Mojime/" + name + "/package.yaml";
            Map<String, Object> mStyleMap = HSYamlUtils.getYamlConfigMap(path, false);
            iconFileName = HSMapUtils.getString(mStyleMap, "icon");
            List<?> stickers = HSMapUtils.getList(mStyleMap, "stickers");

            if (stickers == null || stickers.size() == 0) {
                FacemojiDownloadManager.setFacemojiCategoryDownloadedFailed(name);
                return;
            }

            List<FacemojiSticker> newFacemojiStickerList = new ArrayList<>();
            for (Object stickerName : stickers) {
                newFacemojiStickerList.add(new FacemojiSticker(name, (String) ((Map<String, ?>) stickerName).get("name")));
            }
            stickerList = newFacemojiStickerList;
        }catch (Exception e){

        }
    }

    public List<FacemojiSticker> getStickerList() {
        return stickerList;
    }

    public String getName() {
        return name;
    }

    public int getCategoryId() {
        return mCategoryId;
    }

    public boolean isBuildIn() {
        return isBuildIn;
    }

    public Drawable getCategoryIcon() {
        String path = HSApplication.getContext().getFilesDir().getAbsolutePath() + "/Mojime/" + name + "/" + iconFileName;
        return Drawable.createFromPath(path);
    }

    public int getPageCount(FacemojiManager.ShowLocation showLocation, int orientation) {
        return (int) Math.ceil((stickerList.size() > 0 ? stickerList.size() : DEFAULT_SIZE) * 1.0 / FacemojiManager.getCurrentPageSize(showLocation, orientation, stickerList.get(0)));
    }

    public List<FacemojiSticker> getStickerList(int page, FacemojiManager.ShowLocation showLocation, int orientation) {
        List<FacemojiSticker> data = new ArrayList<>();
        int start = FacemojiManager.getCurrentPageSize(showLocation, orientation, stickerList.get(0)) * page;
        int end = start + FacemojiManager.getCurrentPageSize(showLocation, orientation, stickerList.get(0));
        end = stickerList.size() > end ? end : stickerList.size();
        for (int i = start; i < end; i++) {
            data.add(stickerList.get(i));
        }
        return data;
    }
}
