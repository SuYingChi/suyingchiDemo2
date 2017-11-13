package com.ihs.inputmethod.uimodules.softgame;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ihs.app.framework.activity.HSFragmentActivity;
import com.ihs.inputmethod.uimodules.R;

public class MainActivity extends HSFragmentActivity implements ViewPager.OnPageChangeListener {
    public static final String TOP_50_GAME = "http://api.famobi.com/feed?a=A-KCVWU&n=50&sort=top_games";
    public static final String TOP_NEW_GAME = "http://api.famobi.com/feed?a=A-KCVWU&n=50";
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soft_game_display);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(this);

        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //设置当前要显示的View
        mViewPager.setCurrentItem(position);
        //选中对应的Tab
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private String[] mTitles = new String[]{"HOT", "NEW"};
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return FirstFragment.newInstance(TOP_50_GAME);
                case 1:
                    return FirstFragment.newInstance(TOP_NEW_GAME);
                default:
                    return FirstFragment.newInstance(TOP_50_GAME);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }
}