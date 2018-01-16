package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.kc.utils.KCAnalytics;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.TabFragmentPagerAdapter;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.MyFontFragment;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.MyStickerFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.ihs.keyboardutils.permission.PermissionUtils;
import com.kc.commons.utils.KCCommonUtils;
import com.keyboard.common.KeyboardActivationGuideActivity;

import java.util.ArrayList;

public class ThemeDownloadActivity extends HSAppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_INITIAL_TAB_INDEX = "initial_tab_index";

    private static final int REQUEST_CODE_START_KEYBOARD_ACTIVATION = 1;
    private static final int REQUEST_CODE_START_CUSTOM_THEME = 2;

    private Toolbar toolbar;
    private TabLayout tabLayout;
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

        setContentView(R.layout.activity_theme_download);
        getWindow().setBackgroundDrawable(null);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        String downloadTitle = getResources().getString(R.string.store_nav_download);
        toolbar.setTitle(downloadTitle);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.store_tab);

        viewPager = (ViewPager) findViewById(R.id.fragment_view_pager);

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

        tabLayout.setupWithViewPager(viewPager);
        int position = getIntent().getIntExtra(EXTRA_INITIAL_TAB_INDEX, 0);
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab != null) {
            tab.select();
        }
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
        if (trialKeyboardDialog != null ) {
            KCCommonUtils.dismissDialog(trialKeyboardDialog);
        }
    }

    private void setTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                if (tab.getPosition() == 0) {
                    createThemeButton.setVisibility(View.VISIBLE);
                } else {
                    createThemeButton.setVisibility(View.GONE);
                }
            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    createThemeButton.setVisibility(View.VISIBLE);
                } else {
                    createThemeButton.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showTrialKeyboardDialog() {
        if (HSInputMethodListManager.isMyInputMethodSelected()) {
            if (trialKeyboardDialog == null) {
                trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeDownloadActivity.this).create();
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
}
