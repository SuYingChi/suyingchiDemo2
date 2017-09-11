package com.ihs.inputmethod.uimodules.ui.common.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by guonan.lv on 17/8/10.
 */

public class TabFragmentPagerAdapter extends FragmentStatePagerAdapter {

    public void setFragmentClasses(ArrayList<Class> fragmentClasses) {
        this.fragmentClasses = fragmentClasses;
    }

    private ArrayList<Class> fragmentClasses;
    private String[] tabTitle;

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
            Fragment fragment = (Fragment) fragmentClasses.get(position).newInstance();
            return fragment;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } finally {

        }
    }

    @Override
    public int getCount() {
        return fragmentClasses.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitle[position];
    }

}
