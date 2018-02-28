package com.ihs.inputmethod.mydownload.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.themes.adapter.ThemeAdapter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.CommonThemeCardAdapter;
import com.kc.utils.KCAnalytics;
import com.keyboard.common.KeyboardActivationGuideActivity;
import com.keyboard.core.themes.custom.KCCustomThemeManager;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class MyThemeFragment extends Fragment implements CommonThemeCardAdapter.ThemeCardItemClickListener, View.OnClickListener {

    private RecyclerView recyclerView;
    private ThemeAdapter themeAdapter;
    private List<HomeModel> themes = new ArrayList<>();
    private List<HomeModel> downloaded = new ArrayList<>();
    private List<HomeModel> custom = new ArrayList<>();


    private HomeModel customTitle = new HomeModel();

    private boolean showCustomTheme = false;
    private boolean isDeleteEnable = false;

    private static final int KEYBOARD_ACTIVIATION_FROM_CARD = 10;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_theme, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        loadData();
        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED, notificationObserver);
        return view;
    }

    private void loadData() {
        themeAdapter = new ThemeAdapter(getActivity());

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return themeAdapter.getSpanSize(position);
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(themeAdapter);

        customTitle.isTitle = true;
        customTitle.title = getString(R.string.my_theme_customized_theme_title);

        updateCustomThemes();

        HomeModel title = new HomeModel();
        title.isTitle = true;
        title.title = getString(R.string.my_theme_downloaded_theme_title);
        themes.add(title);

        updateDownloadedThemes();

        themeAdapter.setDataList(themes);
        recyclerView.setAdapter(themeAdapter);
    }

    private void updateCustomThemes() {
        if (KCCustomThemeManager.getInstance().getAllCustomThemes().size() > 0) {
            if (!showCustomTheme) {
                themes.add(0, customTitle);
                if (themes.size() > 1) {
                    themeAdapter.notifyItemInserted(0);
                }
            }
            showCustomTheme = true;

            List<HSKeyboardTheme> customThemes = KCCustomThemeManager.getInstance().getAllCustomThemes();
            updateThemes(custom, customThemes, 1, true);
        } else {
            if (showCustomTheme) {
                showCustomTheme = false;
                isDeleteEnable = false;

                themes.remove(customTitle);
                themeAdapter.notifyItemRemoved(0);

                if (custom.size() > 0) {
                    themes.removeAll(custom);
                    themeAdapter.notifyItemRangeRemoved(0, custom.size());
                    custom.clear();
                }
            }

        }
    }

    private void updateDownloadedThemes() {

        ArrayList<HSKeyboardTheme> downloadedKeyboardThemes = new ArrayList<>();
        downloadedKeyboardThemes.addAll(HSKeyboardThemeManager.getBuiltInThemeList());
        downloadedKeyboardThemes.addAll(HSKeyboardThemeManager.getDownloadedThemeList());

        if (custom.size() > 0) {
            updateThemes(downloaded, downloadedKeyboardThemes, custom.size() + 2, false);
        } else {
            updateThemes(downloaded, downloadedKeyboardThemes, custom.size() + 1, false);
        }
    }

    private void updateThemes(List<HomeModel> origin, List<HSKeyboardTheme> current, int location, boolean isCustom/** is custom theme */) {
        int startIndex = -1;
        final int size = origin.size();
        if (size > 0) {
            startIndex = themes.indexOf(origin.get(0));
        }

        themes.removeAll(origin);
        origin.clear();

        HomeModel themeModel;
        for (HSKeyboardTheme theme : current) {
            themeModel = new HomeModel();
            themeModel.item = theme;
            themeModel.isTheme = true;
            if (isCustom) {
                themeModel.deleteEnable = isDeleteEnable;
            }
            origin.add(themeModel);
        }

        themes.addAll(location, origin);

        if (startIndex > 0) {
            int currentSize = current.size();
            if (size > currentSize) {
                themeAdapter.notifyItemRangeRemoved(startIndex + currentSize, size - currentSize);
            } else if (size < currentSize) {
                themeAdapter.notifyItemRangeInserted(startIndex + size, currentSize - size);
            }

            if (Math.min(currentSize, size) > 0) {
                themeAdapter.notifyItemRangeChanged(startIndex, Math.min(currentSize, size));
            }
        }
    }

    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED.equals(s)) {
                updateCustomThemes();
                updateDownloadedThemes();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        setEditEnable(false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setEditEnable(false);
        }
    }


    @Override
    public void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(notificationObserver);
        super.onDestroy();
    }

    @Override
    public void onCardClick(HSKeyboardTheme keyboardTheme) {
    }

    @Override
    public void onMenuShareClick(HSKeyboardTheme keyboardTheme) {
        KCAnalytics.logEvent("mythemes_share_clicked", "themeName", keyboardTheme.mThemeName);
    }

    @Override
    public void onMenuDownloadClick(HSKeyboardTheme keyboardTheme) {

    }

    @Override
    public void onMenuDeleteClick(HSKeyboardTheme keyboardTheme) {

    }

    @Override
    public void onMenuAppliedClick(HSKeyboardTheme keyboardTheme) {

    }

    @Override
    public void onKeyboardActivationStart() {
        Intent intent = new Intent(getActivity(), KeyboardActivationGuideActivity.class);
        intent.putExtra(KeyboardActivationGuideActivity.EXTRA_DISABLE_ACTIVATION_PROMPT, true);
        startActivityForResult(intent, KEYBOARD_ACTIVIATION_FROM_CARD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == KEYBOARD_ACTIVIATION_FROM_CARD) {
            themeAdapter.finishKeyboardActivation(resultCode == RESULT_OK);
        }
    }

    @Override
    public void onClick(View v) {
        setEditEnable(!isDeleteEnable);
    }

    private void setEditEnable(boolean editEnable) {
        boolean editableChanged = editEnable != isDeleteEnable;
        isDeleteEnable = editEnable;
        if (editableChanged && custom.size() > 0) {
            for (HomeModel model : custom) {
                model.deleteEnable = editEnable;
            }
            themeAdapter.notifyItemRangeChanged(0, 1 + custom.size());
        }
    }

}
