package com.ihs.inputmethod.uimodules.softgame;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ihs.app.framework.activity.HSFragmentActivity;
import com.ihs.inputmethod.uimodules.R;

public class SoftGameDisplayActivity extends HSFragmentActivity implements ViewPager.OnPageChangeListener {
    public static final String TOP_50_GAME = "http://api.famobi.com/feed?a=A-KCVWU&n=50&sort=top_games";
    public static final String TOP_NEW_GAME = "http://api.famobi.com/feed?a=A-KCVWU&n=50";
    private ViewPager mViewPager;
    public static final String SOFT_GAME_PLACEMENT_MESSAGE = "soft_game_placement_msg";
    private String placementName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soft_game_display);

        if (getIntent() != null) {
            placementName = getIntent().getStringExtra(SOFT_GAME_PLACEMENT_MESSAGE);
        }

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
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
                    return SoftGameItemFragment.newInstance(TOP_50_GAME, placementName);
                case 1:
                    return SoftGameItemFragment.newInstance(TOP_NEW_GAME, placementName);
                default:
                    return SoftGameItemFragment.newInstance(TOP_50_GAME, placementName);
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