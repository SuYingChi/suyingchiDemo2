package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.fragment.LockerThemeFragment;
import com.ihs.inputmethod.uimodules.ui.customize.fragment.OnlineWallpaperFragment;
import com.ihs.inputmethod.uimodules.ui.customize.service.ICustomizeService;
import com.ihs.inputmethod.uimodules.ui.customize.service.ServiceListener;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeFragment;

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
        removeAllViews();
        addView(mAdapter.getView(position));
    }

    @Override
    public void onServiceConnected(ICustomizeService service) {
        mAdapter.onServiceConnected(service);
    }

    private static class CustomizeContentAdapter implements ServiceListener {
        private CustomizeContentView mView;
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        private SparseArray<View> mViewMap = new SparseArray<>(3);

        private int[] CONTENT_VIEW_IDS = new int[]{
                R.layout.fragment_theme_home,
                R.layout.online_wallpaper_page,
                R.layout.locker_themes_page,
        };

        private int[] FRAGMENT_TAG = new int[] {
                0, 1, 2
        };

        private Fragment themeHomeFragment;
        private Fragment onlineWallpaperFragment;
        private Fragment lockerThemeFragment;

        CustomizeContentAdapter(CustomizeContentView view) {
            mView = view;
            mContext = view.getContext();
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        public int getCount() {
            return CONTENT_VIEW_IDS.length;
        }

        View getView(int position) {
            int layoutId = CONTENT_VIEW_IDS[position];
            View child = mViewMap.get(layoutId);
            if (child == null) {
                child = mLayoutInflater.inflate(layoutId, mView, false);
                setupWithInitialTabIndex(layoutId, child);
                mViewMap.put(layoutId, child);
            } else {
                setupWithInitialTabIndex(layoutId, child);
            }

            return child;
        }

        private void setupWithInitialTabIndex(@LayoutRes int layoutId, View child) {
            if (!(mContext instanceof Activity)) {
                return;
            }
            FragmentManager fragmentManager = ((Activity) mContext).getFragmentManager();
            switch (layoutId) {
                case R.layout.fragment_theme_home:
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    themeHomeFragment = fragmentManager.findFragmentById(FRAGMENT_TAG[0]);
                    if (themeHomeFragment == null) {
                        themeHomeFragment = new ThemeHomeFragment();
                        fragmentTransaction.add(R.id.content_layout, themeHomeFragment, themeHomeFragment.getTag());
                    }
                    if(onlineWallpaperFragment != null) {
                        fragmentTransaction.hide(onlineWallpaperFragment);
                    }
                    if(lockerThemeFragment != null) {
                        fragmentTransaction.hide(lockerThemeFragment);
                    }
                    fragmentTransaction.show(themeHomeFragment).commit();
                    break;
                case R.layout.online_wallpaper_page:
                   fragmentTransaction = fragmentManager.beginTransaction();
                    onlineWallpaperFragment = fragmentManager.findFragmentById(FRAGMENT_TAG[1]);
                    if (onlineWallpaperFragment == null) {
                        onlineWallpaperFragment = new OnlineWallpaperFragment();
                        fragmentTransaction.add(R.id.content_layout, onlineWallpaperFragment, onlineWallpaperFragment.getTag());
                    }
//                    ((OnlineWallpaperPage) child).setup(0);
                    fragmentTransaction.show(onlineWallpaperFragment).commit();
                    break;
                case R.layout.locker_themes_page:
                    fragmentTransaction = fragmentManager.beginTransaction();
                    lockerThemeFragment = fragmentManager.findFragmentById(FRAGMENT_TAG[2]);
                    if (lockerThemeFragment == null) {
                        lockerThemeFragment = new LockerThemeFragment();
                        fragmentTransaction.add(R.id.content_layout, lockerThemeFragment, lockerThemeFragment.getTag());
                    }
                    fragmentTransaction.show(lockerThemeFragment).commit();
                    break;
                default:

            }
        }

        @Override
        public void onServiceConnected(ICustomizeService service) {
            for (int i = 0, size = mViewMap.size(); i < size; i++) {
                View child = mViewMap.valueAt(i);
                if (child instanceof ServiceListener) {
                    ((ServiceListener) child).onServiceConnected(service);
                }
            }
        }
    }
}
