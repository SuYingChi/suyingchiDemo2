package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity.PREFERENCE_KEY_SHOW_FONT_DOWNLOAD_NEW_MARK;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontHomeFragment extends Fragment implements FontCardAdapter.OnFontCardClickListener {
    
    private RecyclerView recyclerView;
    private FontCardAdapter fontCardAdapter;
    private List<FontModel> fontModelList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_font, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        initView();
        return view;
    }

    private void initView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        loadFontModel();
        fontCardAdapter = new FontCardAdapter(fontModelList, this);
        recyclerView.setAdapter(fontCardAdapter);
    }

    private void loadFontModel() {
        List<Map<String, Object>> fontList = (List<Map<String, Object>>) HSConfig.getList("Application", "FontList");
        for (Map<String, Object> map : fontList) {
            String fontName = (String) map.get("name");
            String example = (String) map.get("example");
            HSSpecialCharacter hsSpecialCharacter = new HSSpecialCharacter();
            hsSpecialCharacter.name = fontName;
            hsSpecialCharacter.example = example;
            FontModel fontModel = new FontModel(hsSpecialCharacter);
            if (!fontModel.isFontDownloaded()) {
                fontModelList.add(fontModel);
            }
        }
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
        super.onDestroy();
    }

    @Override
    public void onFontCardClick(final FontModel fontModel) {
        FontDownloadManager.getInstance().startForegroundDownloading(HSApplication.getContext(), fontModel, null, new AdLoadingView.OnAdBufferingListener() {
            @Override
            public void onDismiss(boolean success) {
                if (success) {
                    int position = fontModelList.indexOf(fontModel);
                    HSPreferenceHelper.getDefault().putBoolean(PREFERENCE_KEY_SHOW_FONT_DOWNLOAD_NEW_MARK, true);
                    fontModelList.remove(position);
                    fontCardAdapter.notifyItemRemoved(position);
                    fontCardAdapter.notifyItemRangeChanged(position, fontModelList.size());
                }
            }
        });
    }
}
