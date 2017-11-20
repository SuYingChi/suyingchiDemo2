package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager;
import com.ihs.inputmethod.uimodules.ui.locker.LockerAppGuideManager;
import com.ihs.inputmethod.utils.DownloadUtils;
import com.ihs.inputmethod.utils.HSConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager.FONT_NAME_SAVE_TO_JSON_SUCCESS;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontHomeFragment extends Fragment implements FontCardAdapter.OnFontCardClickListener, LockerAppGuideManager.ILockerInstallStatusChangeListener {

    private RecyclerView recyclerView;
    private FontCardAdapter fontCardAdapter;
    private List<FontModel> fontModelList = new ArrayList<>();
    public static final String tabTitle = HSApplication.getContext().getString(R.string.custom_theme_font);

    private INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (FONT_NAME_SAVE_TO_JSON_SUCCESS.equals(s)) {
                HSSpecialCharacter hsSpecialCharacter = (HSSpecialCharacter) hsBundle.getObject("HSSpecialCharacter");
                if (hsSpecialCharacter != null) {
                    FontModel fontModel = new FontModel(hsSpecialCharacter);
                    int position = fontModelList.indexOf(fontModel);
                    if (position < 0) {
                        Crashlytics.log("font model index = -1, font model: " + fontModel);
                        return;
                    }
                    fontModelList.remove(position);
                    fontCardAdapter.notifyItemRemoved(position);
                    fontCardAdapter.notifyItemRangeChanged(position, fontModelList.size());
                }
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //随便设置一个，修复按Home键会crash的问题，方法来自https://stackoverflow.com/questions/14516804/nullpointerexception-android-support-v4-app-fragmentmanagerimpl-savefragmentbasi
        outState.putString("xxx",  "xxx");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LockerAppGuideManager.getInstance().addLockerInstallStatusChangeListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_font, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        initView();
        HSGlobalNotificationCenter.addObserver(FONT_NAME_SAVE_TO_JSON_SUCCESS, observer);
        return view;
    }

    private void initView() {
        loadFontModel();
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) {
                if (fontCardAdapter.getItemViewType(position) == FontCardAdapter.ITEM_TYPE.ITEM_TYPE_MORE.ordinal()) {
                    return 2;
                }
                return 1;
            }
        });
        fontCardAdapter = new FontCardAdapter(fontModelList, this);
        fontCardAdapter.setFragmentType(FontHomeFragment.class.getSimpleName());
        recyclerView.setAdapter(fontCardAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void loadFontModel() {
        List<FontModel> fontModelList = new ArrayList<>();
        List<Map<String, Object>> fontList = (List<Map<String, Object>>) HSConfig.getList("Application", "FontList");
        for (Map<String, Object> map : fontList) {
            String fontName = (String) map.get("name");
            String example = (String) map.get("example");
            Object SDKVersion = map.get("sdkVersion");
            int minSDKVersion = 0;
            if (SDKVersion != null) {
                minSDKVersion = HSConfigUtils.toInt(SDKVersion,0);
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(observer);
        LockerAppGuideManager.getInstance().removeLockerInstallStatusChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onFontCardClick(final int position) {
        if (LockerAppGuideManager.getInstance().shouldGuideToDownloadLocker()){
            LockerAppGuideManager.getInstance().showDownloadLockerAlert(getActivity());
        }else {
            final FontModel fontModel = fontModelList.get(position);
            final String fontName = fontModel.getFontName();
            DownloadUtils.getInstance().startForegroundDownloading(HSApplication.getContext(), fontName, fontModel.getFontDownloadFilePath(fontName), fontModel.getFontDownloadBaseURL(),
                    null, success -> {
                        if (success) {
                            HSFontDownloadManager.getInstance().updateFontModel(fontModel);
                            HSAnalytics.logEvent("font_download_succeed", "FontName", fontName);
                        }
                    });
        }
    }

    @Override
    public void onLockerInstallStatusChange() {
        if (fontCardAdapter != null){
            loadFontModel();
            fontCardAdapter.setData(fontModelList);
            fontCardAdapter.notifyDataSetChanged();
        }
    }
}
