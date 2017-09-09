package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.fragment.LockerThemeFragment;
import com.ihs.inputmethod.uimodules.ui.customize.fragment.OnlineWallpaperFragment;
import com.ihs.inputmethod.uimodules.ui.customize.fragment.WrapFragment;
import com.ihs.inputmethod.uimodules.ui.customize.service.ICustomizeService;
import com.ihs.inputmethod.uimodules.ui.customize.service.ServiceListener;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;

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
        };

        private String[] FRAGMENT_TAG = new String[] {
                "wrap_fragment_home",
                "online_wallpaper_page",
                "locker_themes_page"
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

        private Fragment createFragmentByType(@LayoutRes int layoutId) {
            switch (layoutId) {
                case R.layout.wrap_home_fragment:
                    return new WrapFragment();
                case R.layout.online_wallpaper_page:
                    return new OnlineWallpaperFragment();
                case R.layout.locker_themes_page:
                    return new LockerThemeFragment();
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
            if (!(mContext instanceof HSAppCompatActivity)) {
                return;
            }
            String tag = FRAGMENT_TAG[position];
            FragmentManager fragmentManager = ((HSAppCompatActivity)mContext).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment currentFragment = fragmentManager.findFragmentByTag(tag);

            if (currentFragment == null) {
                currentFragment = createFragmentByType(layoutId);
                fragmentTransaction.add(R.id.content_layout, currentFragment, tag);
            }
            hideOtherFragment(position, fragmentTransaction, fragmentManager);
            fragmentTransaction.show(currentFragment).commit();
        }

        @Override
        public void onServiceConnected(ICustomizeService service) {

        }
    }
}
