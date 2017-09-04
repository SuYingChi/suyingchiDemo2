package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by guonan.lv on 17/9/4.
 */

public class OnlineWallpaperPage extends RelativeLayout {

    public OnlineWallpaperPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setup(0);
    }

    public void onDestroy() {

    }

    public void setup(int index) {

    }

    private class WallpaperPagerAdapter extends PagerAdapter {

        private Context mContext;

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return false;
        }
    }
}
