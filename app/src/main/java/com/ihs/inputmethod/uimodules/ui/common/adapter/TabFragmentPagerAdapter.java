package com.ihs.inputmethod.uimodules.ui.common.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeDownloadActivity;

import java.util.ArrayList;

/**
 * Created by guonan.lv on 17/8/10.
 */

public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;
    private String[] tabTitle = new String[] {"THEME", "STICKER", "FONT"};
    private String[] myTabTitle = new String[] {"MY THEME", "MY STICKER", "MY FONT"};

    public TabFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    public TabFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments, String currentPage) {
        super(fm);
        this.fragments = fragments;
        if(TextUtils.equals(currentPage, ThemeDownloadActivity.class.getSimpleName())) {
            tabTitle = myTabTitle;
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_UNCHANGED;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitle[position];
    }

}
