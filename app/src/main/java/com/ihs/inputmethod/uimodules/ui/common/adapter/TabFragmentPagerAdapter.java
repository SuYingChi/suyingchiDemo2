package com.ihs.inputmethod.uimodules.ui.common.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by guonan.lv on 17/8/10.
 */

public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Class> fragmentClasses;
    private String[] tabTitle;

    private SparseArray fragmentArrays = new SparseArray();

    public TabFragmentPagerAdapter(FragmentManager fm, ArrayList<Class> fragmentNames) {
        super(fm);
        tabTitle = new String[fragmentNames.size()];
        this.fragmentClasses = fragmentNames;
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
            if (fragmentArrays.size() >= position) {
                return (Fragment) fragmentArrays.valueAt(position);
            }
            Fragment fragment = (Fragment) fragmentClasses.get(position).newInstance();
            return fragment;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getCount() {
        return fragmentClasses.size();
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
