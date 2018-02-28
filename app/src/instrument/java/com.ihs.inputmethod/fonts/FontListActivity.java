package com.ihs.inputmethod.fonts;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;

import com.crashlytics.android.Crashlytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.base.BaseListActivity;
import com.ihs.inputmethod.fonts.adapter.FontAdapter;
import com.ihs.inputmethod.mydownload.MyDownloadsActivity;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.FontModel;
import com.ihs.inputmethod.utils.DownloadUtils;
import com.ihs.inputmethod.utils.HSConfigUtils;
import com.kc.utils.KCAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager.FONT_NAME_SAVE_TO_JSON_SUCCESS;

/**
 * Created by jixiang on 18/1/20.
 */

public class FontListActivity extends BaseListActivity implements FontAdapter.OnFontCardClickListener {
    private List<FontModel> fontModelList;
    private FontAdapter fontAdapter;
    private INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (FONT_NAME_SAVE_TO_JSON_SUCCESS.equals(s)) {
                if (fontAdapter != null) {
                    HSSpecialCharacter hsSpecialCharacter = (HSSpecialCharacter) hsBundle.getObject("HSSpecialCharacter");
                    if (hsSpecialCharacter != null) {
                        FontModel fontModel = new FontModel(hsSpecialCharacter);
                        int position = fontModelList.indexOf(fontModel);
                        if (position < 0) {
                            Crashlytics.log("font model index = -1, font model: " + fontModel);
                            return;
                        }
                        fontModelList.remove(position);
                        fontAdapter.notifyItemRemoved(position);
                        fontAdapter.notifyItemRangeChanged(position, fontModelList.size());
                    }
                }
            }
        }
    };

    public static void startThisActivity(Activity activity) {
        activity.startActivity(new Intent(activity, FontListActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSGlobalNotificationCenter.addObserver(FONT_NAME_SAVE_TO_JSON_SUCCESS, observer);
    }

    @Override
    protected void initView() {
        showDownloadIcon(true);

        initFontList();
        fontAdapter = new FontAdapter(this, this);
        fontAdapter.setDataList(fontModelList);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return fontAdapter.getSpanSize(position);
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(fontAdapter);
    }

    @Override
    protected int getTitleTextResId() {
        return R.string.activity_font_title;
    }

    @Override
    protected void onDownloadClick() {
        MyDownloadsActivity.startThisActivity(this, getString(R.string.my_download_tab_font));
    }

    private void initFontList() {
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
        this.fontModelList = fontModelList;
    }

    @Override
    public void onFontCardClick(FontModel fontModel, int position) {
        DownloadUtils.getInstance().startForegroundDownloading(this, fontModel.getFontName(), fontModel.getFontDownloadFilePath(fontModel.getFontName()), fontModel.getFontDownloadBaseURL(),
                null, (success, manually) -> {
                    if (success) {
                        HSFontDownloadManager.getInstance().updateFontModel(fontModel);
                        KCAnalytics.logEvent("font_download_succeed", "FontName", fontModel.getFontName());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(observer);
    }
}
