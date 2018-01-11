package com.ihs.inputmethod.uimodules.ui.customize.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.TabFragmentPagerAdapter;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.FontHomeFragment;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.StickerHomeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeDownloadActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.keyboard.common.KeyboardActivationGuideActivity;

import java.util.ArrayList;

/**
 * Created by guonan.lv on 17/9/9.
 */

public class KeyboardFragment extends Fragment implements View.OnClickListener {

    public static final int TAB_THEME = 0;
    public static final int TAB_STICKER = 1;
    public static final int TAB_FONT = 2;
    private int tabIndex = TAB_THEME;
    public static final String TAB_INDEX = "tabIndex";


    private TabFragmentPagerAdapter tabFragmentPagerAdapter;
    private ArrayList<Class> fragments;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton createThemeLayout;
    private View downloadSwitchButton;

    private String[] homeTabTitles = new String[3];

    private static final int REQUEST_CODE_START_CUSTOM_THEME = 1;
    private static final int REQUEST_CODE_START_KEYBOARD_ACTIVATION = 2;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAB_INDEX, tabIndex);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabIndex = getArguments().getInt(TAB_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrap_home_fragment, container, false);
        tabLayout = view.findViewById(R.id.store_tab);
        viewPager = view.findViewById(R.id.fragment_view_pager);

        createThemeLayout = view.findViewById(R.id.home_create_theme_layout);
        createThemeLayout.setOnClickListener(this);

        downloadSwitchButton = view.findViewById(R.id.download_button_container);
        downloadSwitchButton.setOnClickListener(this);

        fragments = new ArrayList<>();
        fragments.add(ThemeHomeFragment.class);
        fragments.add(StickerHomeFragment.class);
        fragments.add(FontHomeFragment.class);

        homeTabTitles[0] = getActivity().getString(R.string.setting_item_themes);
        homeTabTitles[1] = getActivity().getString(R.string.tab_sticker);
        homeTabTitles[2] = getActivity().getString(R.string.custom_theme_font);

        tabFragmentPagerAdapter = new TabFragmentPagerAdapter(getActivity().getFragmentManager(), fragments);
        tabFragmentPagerAdapter.setTabTitles(homeTabTitles);
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(tabFragmentPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        setTabListener();
        viewPager.setCurrentItem(tabIndex);
        return view;
    }

    private void setTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                tabIndex = tab.getPosition();
                if (tab.getPosition() == 0) {
                    createThemeLayout.setVisibility(View.VISIBLE);
                } else {
                    createThemeLayout.setVisibility(View.GONE);
                }
                HSAnalytics.logEvent("app_tab_top_keyboard_clicked", "tabName", String.valueOf(tab.getText()).toLowerCase());
            }
        });

        tabLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                String title = (String) tabFragmentPagerAdapter.getPageTitle(position);
                HSAnalytics.logEvent("app_tab_top_keyboard_clicked", "tabName", title.toLowerCase());
            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    createThemeLayout.setVisibility(View.VISIBLE);
                } else {
                    createThemeLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            tabIndex = savedInstanceState.getInt(TAB_INDEX);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_create_theme_layout:
                Bundle bundle = new Bundle();
                String customEntry = "store_float_button";
                bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                final Intent intent = new Intent(getActivity(), CustomThemeActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_CODE_START_CUSTOM_THEME);

                HSAnalytics.logEvent("customize_entry_clicked", "store");
                break;
            case R.id.download_button_container:
                switchToDownload();
                break;
            default:
                return;
        }
    }

    public void scrollToTabByIndex(int index) {
        if (viewPager == null) {
            tabIndex = index;
            return;
        }
        viewPager.setCurrentItem(index);
    }

    private void switchToDownload() {
        Intent intent = new Intent(getActivity(), ThemeDownloadActivity.class);
        intent.putExtra(ThemeDownloadActivity.EXTRA_INITIAL_TAB_INDEX, tabIndex);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_START_CUSTOM_THEME) {
            if (resultCode == Activity.RESULT_OK) {
                showTrialKeyboardDialog();
            }
        } else if (requestCode == REQUEST_CODE_START_KEYBOARD_ACTIVATION) {
            if (resultCode == Activity.RESULT_OK) {
                showTrialKeyboardDialog();
            }
        }

    }

    private void showTrialKeyboardDialog() {
        if (HSInputMethodListManager.isMyInputMethodSelected()) {
            TrialKeyboardDialog trialKeyboardDialog = new TrialKeyboardDialog.Builder(getActivity()).create();
            trialKeyboardDialog.show(false);
        } else {
            Intent intent = new Intent(getActivity(), KeyboardActivationGuideActivity.class);
            startActivityForResult(intent, REQUEST_CODE_START_KEYBOARD_ACTIVATION);
        }
    }
}
