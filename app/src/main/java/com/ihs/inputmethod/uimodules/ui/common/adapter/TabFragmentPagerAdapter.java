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

    private ArrayList<String> fragmentNames;
    private String[] tabTitle;

//    private SparseArray // cache the fragments

    public TabFragmentPagerAdapter(FragmentManager fm, ArrayList<String> fragmentNames) {
        super(fm);
        tabTitle = new String[fragmentNames.size()];
        this.fragmentNames = fragmentNames;
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
        try {
            Class fragmentClass = Class.forName(fragmentNames.get(position));
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            return fragment;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getCount() {
        return fragmentNames.size();
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
