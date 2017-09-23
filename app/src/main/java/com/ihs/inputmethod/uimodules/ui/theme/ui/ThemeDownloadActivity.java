package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.TabFragmentPagerAdapter;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.MyFontFragment;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.MyStickerFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.ihs.keyboardutils.permission.PermissionUtils;
import com.keyboard.common.KeyboardActivationGuideActivity;

import java.util.ArrayList;

import static android.view.View.GONE;

public class ThemeDownloadActivity extends HSAppCompatActivity implements View.OnClickListener {
    public final static String INTENT_KEY_SHOW_TRIAL_KEYBOARD = "SHOW_TRIAL_KEYBOARD";
    public final static String BUNDLE_AUTO_ENABLE_KEYBOARD = "BUNDLE_AUTO_ENABLE_KEYBOARD";

    private static final int KEYBOARD_ACTIVATION_FROM_ENABLE_TIP = 1;
    public static final int keyboardActivationFromDownloadWithTrial = 114;

    private static int HANDLER_SHOW_ACTIVE_DIALOG = 101;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton createThemeButton;
    private ArrayList<Class> fragments;
    private TabFragmentPagerAdapter tabFragmentPagerAdapter;

    private TrialKeyboardDialog trialKeyboardDialog;
    private boolean isFromUsageAccessActivity;
    private View enableTipTV;

    private Handler handler = new Handler();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_INPUT_METHOD_CHANGED)) {
                boolean isKeyboardSelected = HSInputMethodListManager.isMyInputMethodSelected();
                enableTipTV.setVisibility(isKeyboardSelected ? View.GONE : View.VISIBLE);
            }
        }
    };

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

        enableTipTV = findViewById(R.id.tv_enable_keyboard);
        ((TextView)enableTipTV).setText(getString(R.string.tv_enable_keyboard_tip, getString(R.string.app_name)));
        enableTipTV.setVisibility(GONE);
        enableTipTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemeDownloadActivity.this, KeyboardActivationGuideActivity.class);
                startActivityForResult(intent, KEYBOARD_ACTIVATION_FROM_ENABLE_TIP);
            }
        });

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
        int position = getIntent().getIntExtra("currentTab", 0);
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        tab.select();
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

        onNewIntent(getIntent());

        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_INPUT_METHOD_CHANGED));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean showTrial = intent.getBooleanExtra(INTENT_KEY_SHOW_TRIAL_KEYBOARD, false);
        if (showTrial) {
            handler.removeMessages(HANDLER_SHOW_ACTIVE_DIALOG);
            showTrialKeyboardDialog(keyboardActivationFromDownloadWithTrial);
            getIntent().putExtra(INTENT_KEY_SHOW_TRIAL_KEYBOARD, false);
        } else {
            String from = intent.getStringExtra("From");
            if (trialKeyboardDialog != null && trialKeyboardDialog.isShowing() && from != null && from.equals("Keyboard")) {
                Toast.makeText(this, "Already in " + getResources().getString(R.string.default_themes, getResources().getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
            }
        }
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
                HSAnalytics.logEvent("permission_usage_access");
            }
        }

        HSThemeNewTipController.getInstance().removeNewTip(HSThemeNewTipController.ThemeTipType.NEW_TIP_THEME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.gc();
        if (homeKeyTracker.isHomeKeyPressed() && trialKeyboardDialog != null && trialKeyboardDialog.isShowing()) {
            trialKeyboardDialog.dismiss();
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

    private void showTrialKeyboardDialog(final int activationCode) {
        if (HSInputMethodListManager.isMyInputMethodSelected()) {
            if (trialKeyboardDialog == null) {
                trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeDownloadActivity.this).create();
            }
            trialKeyboardDialog.show(true);
        } else {
            Intent intent = new Intent(this, KeyboardActivationGuideActivity.class);
            startActivityForResult(intent, activationCode);
        }
    }

    @Override
    protected void onDestroy() {
        if (trialKeyboardDialog != null) {
            trialKeyboardDialog.dismiss();
            trialKeyboardDialog = null;
        }

        fragments.clear();
        super.onDestroy();

        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_create_theme_layout:
                Bundle bundle = new Bundle();
                String customEntry = "mytheme_float_button";
                bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                CustomThemeActivity.startCustomThemeActivity(bundle);

                HSAnalytics.logEvent("customize_entry_clicked", "mythemes");
                break;
            default:
        }
    }
}
