package com.ihs.inputmethod.uimodules.ui.common.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by guonan.lv on 17/8/10.
 */

public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;
    private String[] tabTitle;

    public TabFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        tabTitle = new String[fragments.size()];
        this.fragments = fragments;
    }

    public void setTabTitles(String[] tabTitles) {
        if (tabTitles.length > getCount()) {
            tabTitle = Arrays.copyOfRange(tabTitles, 0, getCount());
            return;
        }
        tabTitle = tabTitles;
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
