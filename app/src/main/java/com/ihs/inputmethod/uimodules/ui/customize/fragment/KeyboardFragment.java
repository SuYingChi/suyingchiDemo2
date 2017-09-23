package com.ihs.inputmethod.uimodules.ui.customize.fragment;

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
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.TabFragmentPagerAdapter;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.FontHomeFragment;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.StickerHomeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeDownloadActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;

import java.util.ArrayList;

/**
 * Created by guonan.lv on 17/9/9.
 */

public class KeyboardFragment extends Fragment implements View.OnClickListener {

    public static final String TAB_INDEX = "tabIndex";
    private int tabIndex = 0;
    private TabFragmentPagerAdapter tabFragmentPagerAdapter;
    private ArrayList<Class> fragments;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton createThemeLayout;
    private View downloadSwitchButton;

    private String[] homeTabTitles = new String[3];

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAB_INDEX, tabIndex);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            tabIndex = savedInstanceState.getInt(TAB_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrap_home_fragment, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.store_tab);
        viewPager = (ViewPager) view.findViewById(R.id.fragment_view_pager);

        createThemeLayout = (FloatingActionButton) view.findViewById(R.id.home_create_theme_layout);
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
        viewPager.setOffscreenPageLimit(fragments.size());
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
                CustomThemeActivity.startCustomThemeActivity(bundle);

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
        intent.putExtra("currentTab", tabIndex);
        startActivity(intent);
    }
}
