package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.CustomizeActivity;

/**
 * Created by guonan.lv on 17/9/2.
 */

public class CustomizeContentView extends FrameLayout {

    private CustomizeContentAdapter mAdapter;

    public CustomizeContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAdapter = new CustomizeContentAdapter(this);
    }

    public void setChildSelected(int position) {
        removeAllViews();
        addView(mAdapter.getView(position));
    }

    private static class CustomizeContentAdapter {
        private CustomizeContentView mView;
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        private SparseArray<View> mViewMap = new SparseArray<>(3);

        private int[] CONTENT_VIEW_IDS = new int[]{
//                R.layout.online_theme_page,
                R.layout.online_wallpaper_page,
                R.layout.locker_themes_page,
        };

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
            if (layoutId == R.layout.online_wallpaper_page) {
                int index = ((CustomizeActivity) mContext).mWallpaperTabIndex;
                if (index > 0) {
                    ((OnlineWallpaperPage) child).setup(index);
                }
            }
        }
    }
}
