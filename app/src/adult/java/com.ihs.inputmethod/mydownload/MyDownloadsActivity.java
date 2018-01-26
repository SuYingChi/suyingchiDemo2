package com.ihs.inputmethod.mydownload;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.TabFragmentPagerAdapter;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.MyFontFragment;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.MyStickerFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.MyThemeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.ihs.inputmethod.widget.SlidingTabLayout;
import com.ihs.keyboardutils.permission.PermissionUtils;
import com.kc.commons.utils.KCCommonUtils;
import com.kc.utils.KCAnalytics;
import com.keyboard.common.KeyboardActivationGuideActivity;

import java.util.ArrayList;

/**
 * Created by jixiang on 18/1/25.
 */

public class MyDownloadsActivity extends HSAppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_INITIAL_TAB_INDEX = "initial_tab_index";

    private static final int REQUEST_CODE_START_KEYBOARD_ACTIVATION = 1;
    private static final int REQUEST_CODE_START_CUSTOM_THEME = 2;

    private Toolbar toolbar;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private FloatingActionButton createThemeButton;
    private ArrayList<Class> fragments;
    private TabFragmentPagerAdapter tabFragmentPagerAdapter;

    private TrialKeyboardDialog trialKeyboardDialog;
    private boolean isFromUsageAccessActivity;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_downloads);
        getWindow().setBackgroundDrawable(null);

        toolbar = findViewById(R.id.toolbar);
        String downloadTitle = getResources().getString(R.string.store_nav_download);
        toolbar.setTitle(downloadTitle);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.viewpager);
        slidingTabLayout = findViewById(R.id.tab_layout);
        slidingTabLayout.setViewPager(viewPager);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        fragments = new ArrayList<>();
        fragments.add(MyThemeFragment.class);
        fragments.add(MyStickerFragment.class);
        fragments.add(MyFontFragment.class);

        tabFragmentPagerAdapter = new TabFragmentPagerAdapter(getFragmentManager(), fragments);
        String[] tabTitles = new String[3];
        tabTitles[0] = getApplicationContext().getString(R.string.tab_theme_my);
        tabTitles[1] = getApplicationContext().getString(R.string.tab_sticker_my);
        tabTitles[2] = getApplicationContext().getString(R.string.tab_font_my);
        tabFragmentPagerAdapter.setTabTitles(tabTitles);
        viewPager.setOffscreenPageLimit(fragments.size());
        viewPager.setAdapter(tabFragmentPagerAdapter);

        int position = getIntent().getIntExtra(EXTRA_INITIAL_TAB_INDEX, 0);
        createThemeButton = (FloatingActionButton) findViewById(R.id.home_create_theme_layout);
        createThemeButton.setOnClickListener(this);
        if (position == 0) {
            createThemeButton.setVisibility(View.VISIBLE);
        } else if (position == 1) { //my sticker
            createThemeButton.setVisibility(View.GONE);
        } else if (position == 2) { // my font
            createThemeButton.setVisibility(View.GONE);
        }

        setTabListener();
    }

    public static void startThisActivity(Activity activity) {
        activity.startActivity(new Intent(activity, MyDownloadsActivity.class));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        newConfig.orientation = Configuration.ORIENTATION_PORTRAIT;
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HSFloatWindowManager.getInstance().removeAccessibilityCover();
            }
        }, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isFromUsageAccessActivity) {
            isFromUsageAccessActivity = false;
            if (PermissionUtils.isUsageAccessGranted()) {
                KCAnalytics.logEvent("permission_usage_access");
            }
        }

        HSThemeNewTipController.getInstance().removeNewTip(HSThemeNewTipController.ThemeTipType.NEW_TIP_THEME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (trialKeyboardDialog != null) {
            KCCommonUtils.dismissDialog(trialKeyboardDialog);
        }
    }

    private void setTabListener() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    createThemeButton.setVisibility(View.VISIBLE);
                } else {
                    createThemeButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void showTrialKeyboardDialog() {
        if (HSInputMethodListManager.isMyInputMethodSelected()) {
            if (trialKeyboardDialog == null) {
                trialKeyboardDialog = new TrialKeyboardDialog.Builder(MyDownloadsActivity.this).create();
            }
            trialKeyboardDialog.show(false);
        } else {
            Intent intent = new Intent(this, KeyboardActivationGuideActivity.class);
            startActivityForResult(intent, REQUEST_CODE_START_KEYBOARD_ACTIVATION);
        }
    }

    @Override
    protected void onDestroy() {
        if (trialKeyboardDialog != null) {
            KCCommonUtils.dismissDialog(trialKeyboardDialog);
            trialKeyboardDialog = null;
        }

        handler.removeCallbacksAndMessages(null);

        fragments.clear();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_create_theme_layout:
                Bundle bundle = new Bundle();
                String customEntry = "mytheme_float_button";
                bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                final Intent intent = new Intent(this, CustomThemeActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_CODE_START_CUSTOM_THEME);

                KCAnalytics.logEvent("customize_entry_clicked", "mythemes");
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_START_CUSTOM_THEME) {
            if (resultCode == Activity.RESULT_OK) {
                showTrialKeyboardDialog();
            }
        } else if (requestCode == REQUEST_CODE_START_KEYBOARD_ACTIVATION) {
            if (resultCode == Activity.RESULT_OK) {
                showTrialKeyboardDialog();
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
