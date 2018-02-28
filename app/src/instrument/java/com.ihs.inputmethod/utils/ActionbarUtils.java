package com.ihs.inputmethod.utils;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by jixiang on 18/2/1.
 */

public class ActionbarUtils {

    public static void setCustomTitleWithBackIcon(AppCompatActivity activity, Toolbar toolbar, int titleTextResId) {
        ((TextView) toolbar.findViewById(R.id.title)).setText(titleTextResId);
        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
