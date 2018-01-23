package com.ihs.inputmethod.fonts.stickers;

import android.os.Build;
import android.support.v7.widget.GridLayoutManager;

import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.common.ListActivity;
import com.ihs.inputmethod.fonts.stickers.adapter.FontAdapter;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.FontModel;
import com.ihs.inputmethod.utils.DownloadUtils;
import com.ihs.inputmethod.utils.HSConfigUtils;
import com.kc.utils.KCAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jixiang on 18/1/20.
 */

public class FontListActivity extends ListActivity implements FontAdapter.OnFontCardClickListener {
    private FontAdapter stickerAdapter;

    @Override
    protected void initView() {
        stickerAdapter = new FontAdapter(this, this);
        stickerAdapter.setDataList(getData());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(stickerAdapter);
    }

    private List<FontModel> getData() {
        List<FontModel> fontModelList = new ArrayList<>();
        List<Map<String, Object>> fontList = (List<Map<String, Object>>) HSConfig.getList("Application", "FontList");
        for (Map<String, Object> map : fontList) {
            String fontName = (String) map.get("name");
            String example = (String) map.get("example");
            Object SDKVersion = map.get("sdkVersion");
            int minSDKVersion = 0;
            if (SDKVersion != null) {
                minSDKVersion = HSConfigUtils.toInt(SDKVersion, 0);
            }
            HSSpecialCharacter hsSpecialCharacter = new HSSpecialCharacter();
            hsSpecialCharacter.name = fontName;
            hsSpecialCharacter.example = example;
            FontModel fontModel = new FontModel(hsSpecialCharacter);
            if (!fontModel.isFontDownloaded() && Build.VERSION.SDK_INT >= minSDKVersion) {
                fontModelList.add(fontModel);
            }
        }
        return fontModelList;
    }

    @Override
    public void onFontCardClick(FontModel fontModel) {
        DownloadUtils.getInstance().startForegroundDownloading(this, fontModel.getFontName(), fontModel.getFontDownloadFilePath(fontModel.getFontName()), fontModel.getFontDownloadBaseURL(),
                null, (success, manually) -> {
                    if (success) {
                        HSFontDownloadManager.getInstance().updateFontModel(fontModel);
                        KCAnalytics.logEvent("font_download_succeed", "FontName", fontModel.getFontName());
                    }
                });
    }
}
