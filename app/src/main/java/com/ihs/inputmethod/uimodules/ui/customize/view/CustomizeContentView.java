package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.fragment.KeyboardFragment;
import com.ihs.inputmethod.uimodules.ui.customize.fragment.LockerThemeFragment;
import com.ihs.inputmethod.uimodules.ui.customize.fragment.OnlineWallpaperFragment;
import com.ihs.inputmethod.uimodules.ui.customize.fragment.SettingsFragment;
import com.ihs.inputmethod.uimodules.ui.customize.service.ICustomizeService;
import com.ihs.inputmethod.uimodules.ui.customize.service.ServiceListener;

/**
 * Created by guonan.lv on 17/9/2.
 */

public class CustomizeContentView extends FrameLayout implements ServiceListener {

    private CustomizeContentAdapter mAdapter;

    public CustomizeContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAdapter = new CustomizeContentAdapter(this);
    }

    public void setChildSelected(int position) {
        mAdapter.setUpView(position);
    }

    public void setWithChildTabSelected(int position, int tabPosition) {
        mAdapter.setupWithTabSelected(position, tabPosition);
    }

    @Override
    public void onServiceConnected(ICustomizeService service) {
        mAdapter.onServiceConnected(service);
    }

    private static class CustomizeContentAdapter implements ServiceListener {
        private CustomizeContentView mView;
        private Context mContext;

        private int[] CONTENT_VIEW_IDS = new int[]{
                R.layout.wrap_home_fragment,
                R.layout.online_wallpaper_page,
                R.layout.locker_themes_page,
                R.layout.fragment_theme_home
        };

        private String[] FRAGMENT_TAG = new String[] {
                "wrap_fragment_home",
                "online_wallpaper_page",
                "locker_themes_page",
                "pref_theme"
        };

        CustomizeContentAdapter(CustomizeContentView view) {
            mView = view;
            mContext = view.getContext();
        }

        public int getCount() {
            return CONTENT_VIEW_IDS.length;
        }

        void setUpView(int position) {
            int layoutId = CONTENT_VIEW_IDS[position];
            setupWithInitialTabIndex(layoutId, position);
        }

        private Fragment createFragmentByType(@LayoutRes int layoutId,Bundle bundle) {
            switch (layoutId) {
                case R.layout.wrap_home_fragment:
                    return new KeyboardFragment();
                case R.layout.online_wallpaper_page:
                    return new OnlineWallpaperFragment();
                case R.layout.locker_themes_page:
                    return new LockerThemeFragment();
                case R.layout.fragment_theme_home:
                    return new SettingsFragment();
                default:
                    return null;
            }
        }

        private void hideOtherFragment(int position, FragmentTransaction fragmentTransaction, FragmentManager fragmentManager) {
            for (int i = 0; i < CONTENT_VIEW_IDS.length; i++) {
                if (i == position) {
                    continue;
                }
                Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG[i]);
                if (fragment != null) {
                    fragmentTransaction.hide(fragment);
                }
            }
        }

        private void setupWithInitialTabIndex(@LayoutRes int layoutId, int position) {
            if (!(mContext instanceof Activity)) {
                return;
            }
            String tag = FRAGMENT_TAG[position];
            FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment currentFragment = fragmentManager.findFragmentByTag(tag);

            if (currentFragment == null) {
                currentFragment = createFragmentByType(layoutId,null);
                fragmentTransaction.add(R.id.content_layout, currentFragment, tag);
            }
            hideOtherFragment(position, fragmentTransaction, fragmentManager);
            fragmentTransaction.show(currentFragment).commitAllowingStateLoss();
        }

        private void setupWithTabSelected(int position, int tabPosition) {
            if (!(mContext instanceof Activity)) {
                return;
            }
            String tag = FRAGMENT_TAG[position];
            FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment currentFragment = fragmentManager.findFragmentByTag(tag);

            if (currentFragment == null) {
                Bundle bundle = new Bundle();
                bundle.putInt(KeyboardFragment.TAB_INDEX, tabPosition);
                currentFragment = createFragmentByType(CONTENT_VIEW_IDS[position],bundle);
                currentFragment.setArguments(bundle);
                fragmentTransaction.add(R.id.content_layout, currentFragment, tag);
            } else {
                if (currentFragment instanceof KeyboardFragment) {
                    ((KeyboardFragment) currentFragment).scrollToTabByIndex(tabPosition);
                }
            }
            hideOtherFragment(position, fragmentTransaction, fragmentManager);
            fragmentTransaction.show(currentFragment).commit();
        }

        @Override
        public void onServiceConnected(ICustomizeService service) {

        }
    }
}
