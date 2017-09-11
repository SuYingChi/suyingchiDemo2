package com.ihs.inputmethod.uimodules.ui.customize.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.TabFragmentPagerAdapter;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.FontHomeFragment;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.MyFontFragment;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.MyStickerFragment;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.StickerHomeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.MyThemeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;

import java.util.ArrayList;

/**
 * Created by guonan.lv on 17/9/9.
 */

public class WrapFragment extends Fragment implements View.OnClickListener {

    private int tabIndex = 0;
    private TabFragmentPagerAdapter tabFragmentPagerAdapter;
    private ArrayList<Class> fragments;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LinearLayout createThemeLayout;
    private View downloadSwitchButton;
    private CURRENT_TYPE type;

    private String[] homeTabTitles = new String[3];
    private String[] myTabTitles = new String[3];

    private enum CURRENT_TYPE {
        HOME,
        MY
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tabIndex", tabIndex);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            tabIndex = savedInstanceState.getInt("tabIndex");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrap_home_fragment, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.store_tab);
        viewPager = (ViewPager) view.findViewById(R.id.fragment_view_pager);

        createThemeLayout = (LinearLayout) view.findViewById(R.id.home_create_theme_layout);
        createThemeLayout.setOnClickListener(this);

        downloadSwitchButton = view.findViewById(R.id.download_button_container);
        downloadSwitchButton.setOnClickListener(this);

        fragments = new ArrayList<>();
        fragments.add(ThemeHomeFragment.class);
        fragments.add(StickerHomeFragment.class);
        fragments.add(FontHomeFragment.class);

        homeTabTitles[0] = getActivity().getString(R.string.tab_theme);
        homeTabTitles[1] = getActivity().getString(R.string.tab_sticker);
        homeTabTitles[2] = getActivity().getString(R.string.tab_font);

        myTabTitles[0] = getActivity().getString(R.string.tab_theme_my);
        myTabTitles[1] = getActivity().getString(R.string.tab_sticker_my);
        myTabTitles[2] = getActivity().getString(R.string.tab_font_my);

        tabFragmentPagerAdapter = new TabFragmentPagerAdapter(getActivity().getFragmentManager(), fragments);
        tabFragmentPagerAdapter.setTabTitles(homeTabTitles);
        viewPager.setOffscreenPageLimit(fragments.size());
        viewPager.setAdapter(tabFragmentPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        type = CURRENT_TYPE.HOME;
        setTabListener();
        return view;
    }

    private void setTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                if (tab.getPosition() == 0) {
                    createThemeLayout.setVisibility(View.VISIBLE);
                } else {
                    createThemeLayout.setVisibility(View.GONE);
                }
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
            tabIndex = savedInstanceState.getInt("tabIndex");
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

    private void switchToDownload() {
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        Fragment myWrapFragment = new MyWrapFragment();
//        fragmentTransaction.replace(R.id.content_layout, myWrapFragment, MY_WRAP_TAB_FRAGMENT).addToBackStack(null).commit();
        fragments.clear();
        fragments.add(MyThemeFragment.class);
        fragments.add(MyStickerFragment.class);
        fragments.add(MyFontFragment.class);
        tabFragmentPagerAdapter.setFragmentClasses(fragments);
        tabFragmentPagerAdapter.setTabTitles(myTabTitles);
        tabFragmentPagerAdapter.notifyDataSetChanged();
        downloadSwitchButton.setVisibility(View.GONE);
    }

    private void swithToHome() {
        if (type == CURRENT_TYPE.HOME) {
            return;
        }
        fragments.clear();
        fragments.add(ThemeHomeFragment.class);
        fragments.add(StickerHomeFragment.class);
        fragments.add(FontHomeFragment.class);
        tabFragmentPagerAdapter.setFragmentClasses(fragments);
        tabFragmentPagerAdapter.setTabTitles(homeTabTitles);
        tabFragmentPagerAdapter.notifyDataSetChanged();
        downloadSwitchButton.setVisibility(View.VISIBLE);
    }
}
