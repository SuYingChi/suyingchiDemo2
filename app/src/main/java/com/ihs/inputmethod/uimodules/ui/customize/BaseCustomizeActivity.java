package com.ihs.inputmethod.uimodules.ui.customize;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by guonan.lv on 17/9/1.
 */

public class BaseCustomizeActivity extends HSAppCompatActivity {
    protected boolean mHasPendingTheme = false;
    private boolean mIsDestroying;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind to CustomizeService
    }

    @Override
    protected void onDestroy() {
        mIsDestroying = true;
        super.onDestroy();
    }

    public boolean isDestroying() {
        return mIsDestroying;
    }

    void configToolbar(String title, @ColorInt int titleColor, @ColorInt int backgroundColor) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        if (toolbar == null) {
            return;
        }

        toolbar.setTitle(title);
        toolbar.setTitleTextColor(titleColor);
        toolbar.setBackgroundColor(backgroundColor);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
