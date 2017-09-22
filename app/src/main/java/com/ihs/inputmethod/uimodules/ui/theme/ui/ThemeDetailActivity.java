package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artw.lockscreen.LockerEnableDialog;
import com.artw.lockscreen.LockerSettings;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSInstallationUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.theme.ThemeLockerBgUtil;
import com.ihs.inputmethod.theme.download.ApkUtils;
import com.ihs.inputmethod.theme.download.ThemeDownloadManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.CommonThemeCardAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;
import com.ihs.inputmethod.uimodules.ui.theme.utils.ThemeMenuUtils;
import com.ihs.inputmethod.uimodules.utils.ViewConvertor;
import com.ihs.inputmethod.uimodules.widget.MdProgressBar;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;
import com.keyboard.common.KeyboardActivationGuideActivity;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ThemeDetailActivity extends HSAppCompatActivity implements View.OnClickListener, CommonThemeCardAdapter.ThemeCardItemClickListener {
    public final static String INTENT_KEY_THEME_NAME = "themeName";
    private NestedScrollView rootView;
    private View screenshotContainer;
    private ImageView keyboardThemeScreenShotImageView;
    private MdProgressBar screenshotLoading;
    private TextView leftBtn;
    private TextView rightBtn;
    private RecyclerView recommendRecyclerView;
    private CommonThemeCardAdapter themeCardAdapter;
    private TrialKeyboardDialog trialKeyboardDialog;
    private String themeName;
    private HSKeyboardTheme.ThemeType themeType;
    private HSKeyboardTheme keyboardTheme;
    private KCNativeAdView nativeAdView;
    private String themeLockerBgUrl;

    private static final int KEYBOARD_ACTIVIATION_FROM_THEME_CARD = 1;
    private static final int KEYBOARD_ACTIVIATION_FROM_APPLY_BUTTON = 2;

    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED.equals(s)) {
                updateCurrentThemeStatus();
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getStringExtra(INTENT_KEY_THEME_NAME) != null) {
            themeName = intent.getStringExtra(INTENT_KEY_THEME_NAME);
        }
        if (themeName != null) {
            List<HSKeyboardTheme> allKeyboardThemeList = HSKeyboardThemeManager.getAllKeyboardThemeList();
            for (HSKeyboardTheme keyboardTheme : allKeyboardThemeList) {
                if (themeName.equals(keyboardTheme.mThemeName) || themeName.equals(keyboardTheme.getThemePkName())) {
                    this.keyboardTheme = keyboardTheme;
                    this.themeType = keyboardTheme.getThemeType();
                    break;
                }
            }
        }

        if (keyboardTheme != null) {

            if (keyboardTheme.getThemeType() == HSKeyboardTheme.ThemeType.CUSTOM) {
                screenshotContainer.getLayoutParams().height = (int) (getResources().getDisplayMetrics().widthPixels * (HSResourceUtils.getDefaultKeyboardHeight(getResources()) * 1.0f / HSResourceUtils.getDefaultKeyboardWidth(getResources())));
                getSupportActionBar().setTitle(R.string.theme_detail_custom_theme_title_name);
                keyboardThemeScreenShotImageView.setImageURI(Uri.fromFile(new File(HSKeyboardThemeManager.getKeyboardThemeScreenshotFile(keyboardTheme.mThemeName))));
            } else {
                screenshotContainer.getLayoutParams().height = (int) (getResources().getDisplayMetrics().widthPixels * 850 * 1.0f / 1080);
                String themeNameTitle = keyboardTheme.getThemeShowName();
                getSupportActionBar().setTitle(getString(R.string.theme_detail_common_title_name, getString(R.string.app_name)));

                if (keyboardTheme.getLargePreivewImgUrl() != null) {
                    keyboardThemeScreenShotImageView.setImageDrawable(null);
                    ImageLoader.getInstance().displayImage(keyboardTheme.getLargePreivewImgUrl(), keyboardThemeScreenShotImageView, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build()
                            , new ImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String imageUri, View view) {
                                    screenshotLoading.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    if (isCurrentImageUri(imageUri)) {
                                        screenshotLoading.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    if (isCurrentImageUri(imageUri)) {
                                        screenshotLoading.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onLoadingCancelled(String imageUri, View view) {
                                    if (isCurrentImageUri(imageUri)) {
                                        screenshotLoading.setVisibility(View.GONE);
                                    }
                                }

                                public boolean isCurrentImageUri(String imageUri) {
                                    if (keyboardTheme != null && keyboardTheme.getLargePreivewImgUrl() != null && keyboardTheme.getLargePreivewImgUrl().equals(imageUri)) {
                                        return true;
                                    }
                                    return false;
                                }
                            }
                    );

                }
            }

            themeLockerBgUrl = ThemeLockerBgUtil.getInstance().getThemeBgUrl(themeName);

            setButtonText();

        }

        String text = rightBtn.getText().toString();
        boolean applied = text.equalsIgnoreCase(getString(R.string.theme_card_menu_applied));
        if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled() && !applied && themeName != null) {
            ThemeAnalyticsReporter.getInstance().recordThemeShownInDetailActivity(themeName);
        }
        //show all themes except custom themes and current theme
        themeCardAdapter.setItems(getKeyboardThemesExceptMe());
        themeCardAdapter.notifyDataSetChanged();

        rootView.smoothScrollTo(0, 0);
    }

    @NonNull
    private List<ThemeHomeModel> getKeyboardThemesExceptMe() {
        List<HSKeyboardTheme> keyboardThemeList = new ArrayList<>();
        keyboardThemeList.addAll(HSKeyboardThemeManager.getAllKeyboardThemeList());
        keyboardThemeList.removeAll(KCCustomThemeManager.getInstance().getAllCustomThemes());
        keyboardThemeList.removeAll(HSKeyboardThemeManager.getDownloadedThemeList());
        if (keyboardTheme != null) {
            keyboardThemeList.remove(keyboardTheme);
        }
        List<ThemeHomeModel> models = new ArrayList<>();
        for (HSKeyboardTheme keyboardTheme : keyboardThemeList) {
            ThemeHomeModel model = new ThemeHomeModel();
            model.keyboardTheme = keyboardTheme;
            models.add(model);
        }
        return models;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_detail);
        initView();
        onNewIntent(getIntent());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED, notificationObserver);
    }

    private void initView() {
        rootView = (NestedScrollView) findViewById(R.id.root_view);
        screenshotContainer = findViewById(R.id.keyboard_theme_screenshot_container);
        keyboardThemeScreenShotImageView = (ImageView) findViewById(R.id.keyboard_theme_screenshot);
//        themeNameText = (TextView) findViewById(R.id.theme_name);
//        themeDescText = (TextView) findViewById(R.id.theme_desc);
        screenshotLoading = (MdProgressBar) findViewById(R.id.screenshot_loading);
        leftBtn = (TextView) findViewById(R.id.theme_detail_left_btn);
        rightBtn = (TextView) findViewById(R.id.theme_detail_right_btn);
        leftBtn.setOnClickListener(this);
        rightBtn.setOnClickListener(this);

        recommendRecyclerView = (RecyclerView) findViewById(R.id.theme_detail_recommend_recycler_view);
        recommendRecyclerView.setNestedScrollingEnabled(false);
        recommendRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        themeCardAdapter = new CommonThemeCardAdapter(this, this, false);
//        themeCardAdapter.setThemeCardItemClickListener(this);
        recommendRecyclerView.setAdapter(themeCardAdapter);

        addNativeAdView();
    }

    private void addNativeAdView() {
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            // 添加广告
            if (nativeAdView == null) {
                final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ad_container);
                int width = HSDisplayUtils.getScreenWidthForContent() - HSDisplayUtils.dip2px(16);
                View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_1, null);
                LinearLayout loadingView = (LinearLayout) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_loading_3, null);
                LinearLayout.LayoutParams loadingLP = new LinearLayout.LayoutParams(width, (int) (width / 1.9f));
                loadingView.setLayoutParams(loadingLP);
                loadingView.setGravity(Gravity.CENTER);
                nativeAdView = new KCNativeAdView(HSApplication.getContext());
                nativeAdView.setAdLayoutView(view);
                nativeAdView.setLoadingView(loadingView);
                nativeAdView.setPrimaryViewSize(width, (int) (width / 1.9f));
                nativeAdView.load(getString(R.string.ad_placement_themetryad));
                CardView cardView = ViewConvertor.toCardView(nativeAdView);
                linearLayout.addView(cardView);
                linearLayout.setVisibility(View.GONE);
                nativeAdView.setOnAdLoadedListener(new KCNativeAdView.OnAdLoadedListener() {
                    @Override
                    public void onAdLoaded(KCNativeAdView nativeAdView) {
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (homeKeyTracker.isHomeKeyPressed() && trialKeyboardDialog != null && trialKeyboardDialog.isShowing()) {
            trialKeyboardDialog.dismiss();
        }
    }

    private void setButtonText() {
        switch (themeType) {
            case NEED_DOWNLOAD:
                if (TextUtils.isEmpty(themeLockerBgUrl)) {
                    leftBtn.setText(R.string.theme_card_menu_share);
                } else {
                    leftBtn.setText(R.string.theme_card_set_locker_bg);
                }
                if (ThemeDownloadManager.getInstance().isDownloading(keyboardTheme.mThemeName)) {
                    rightBtn.setText(HSApplication.getContext().getString(R.string.theme_card_menu_downloading));
                    rightBtn.setEnabled(false);
                } else {
                    rightBtn.setText(HSApplication.getContext().getString(R.string.theme_card_menu_download));
                    rightBtn.setEnabled(true);
                }
                break;
            case CUSTOM:
            case DOWNLOADED:
            case BUILD_IN:
                updateApplyButton();
                if (TextUtils.isEmpty(themeLockerBgUrl)) {
                    leftBtn.setText(R.string.theme_card_menu_share);
                } else {
                    leftBtn.setText(R.string.theme_card_set_locker_bg);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.theme_detail_left_btn) {
            handleButtonClick(v);
        } else if (id == R.id.theme_detail_right_btn) {
            handleButtonClick(v);
        }
    }

    private void handleButtonClick(View v) {
        if (keyboardTheme == null) {
            return;
        }

        String text = ((TextView) v).getText().toString();
        if (HSApplication.getContext().getString(R.string.theme_card_menu_download).equalsIgnoreCase(text)) {
            ((TextView) v).setText(R.string.theme_card_menu_downloading);
            v.setEnabled(false);
            ThemeDownloadManager.getInstance().downloadTheme(keyboardTheme);
            HSAnalytics.logEvent("themedetails_download_clicked", "themeName", themeName);
            if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
                ThemeAnalyticsReporter.getInstance().recordThemeDownloadInDetailActivity(themeName);
            }
        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_delete).equalsIgnoreCase(text)) {
            KCCustomThemeManager.getInstance().removeCustomTheme(keyboardTheme.getThemeId());

        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_share).equalsIgnoreCase(text)) {
            ThemeMenuUtils.shareTheme(this, keyboardTheme);
            HSAnalytics.logEvent("themedetails_share_clicked", "themeName", themeName);
        } else if (HSApplication.getContext().getString(R.string.theme_card_set_locker_bg).equalsIgnoreCase(text)) {
            HSAnalytics.logEvent("keyboard_setaslockscreen_button_clicked", "occasion", "app_theme_detail");
            LockerEnableDialog.showLockerEnableDialog(this, themeLockerBgUrl, getString(R.string.locker_enable_title_no_desc), "app_theme_detail", new LockerEnableDialog.OnLockerBgLoadingListener() {
                @Override
                public void onFinish() {
                }
            });

        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_apply).equalsIgnoreCase(text)) {
            if (keyboardTheme.getThemeType() == HSKeyboardTheme.ThemeType.DOWNLOADED && !HSInstallationUtils.isAppInstalled(keyboardTheme.getThemePkName())) {
                ApkUtils.startInstall(HSApplication.getContext(), Uri.fromFile(new File(ThemeDownloadManager.getThemeDownloadLocalFile(keyboardTheme.mThemeName))));
            } else {
                if (HSKeyboardThemeManager.setKeyboardTheme(themeName)) {
                    Intent intent = new Intent(this, KeyboardActivationGuideActivity.class);
                    startActivityForResult(intent, KEYBOARD_ACTIVIATION_FROM_APPLY_BUTTON);
                } else {
                    String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                    HSToastUtils.toastCenterLong(String.format(failedString, keyboardTheme.getThemeShowName()));
                }
            }
        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_applied).equalsIgnoreCase(text)) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == KEYBOARD_ACTIVIATION_FROM_THEME_CARD) {
            themeCardAdapter.finishKeyboardActivation(resultCode == RESULT_OK);
        } else if (requestCode == KEYBOARD_ACTIVIATION_FROM_APPLY_BUTTON) {
            if (resultCode == RESULT_OK) {
                if (LockerSettings.isLockerEnableShowSatisfied()) {
                    LockerEnableDialog.showLockerEnableDialog(ThemeDetailActivity.this,
                            ThemeLockerBgUtil.getInstance().getThemeBgUrl(HSKeyboardThemeManager.getCurrentThemeName()),
                            getString(R.string.locker_enable_title_has_text),
                            "app_theme_detail",
                            this::showTryKeyboardDialog);
                } else {
                    showTryKeyboardDialog();
                }
            }
        }
    }

    private void showTryKeyboardDialog() {
        if (trialKeyboardDialog == null) {
            trialKeyboardDialog = new TrialKeyboardDialog.Builder(this).create();
            trialKeyboardDialog.setOnDismissListener(dialog -> updateApplyButton());
        }
        trialKeyboardDialog.show(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 更改当前主题状态,有可能当前主题对应的主题包APK被安装或者删除了
     */
    private void updateCurrentThemeStatus() {
        if (themeName != null) {
            if (themeType == HSKeyboardTheme.ThemeType.DOWNLOADED) {
                //查找是否当前显示的主题是否被删了,需要重新下载
                boolean isNeedDownload = false;
                List<HSKeyboardTheme> needDownloadThemes = HSKeyboardThemeManager.getNeedDownloadThemeList();
                for (HSKeyboardTheme keyboardTheme : needDownloadThemes) {
                    if (themeName.equals(keyboardTheme.mThemeName)) {
                        isNeedDownload = true;
                        break;
                    }
                }

                //如果需要重新需要下载,修改类型并修改按钮文字
                if (isNeedDownload) {
                    themeType = HSKeyboardTheme.ThemeType.NEED_DOWNLOAD;
                    setButtonText();
                }
            } else if (themeType == HSKeyboardTheme.ThemeType.NEED_DOWNLOAD) {
                //查找是否当前显示的主题是否已下载
                boolean isDownloaded = false;
                List<HSKeyboardTheme> downloadedThemes = HSKeyboardThemeManager.getDownloadedThemeList();
                for (HSKeyboardTheme keyboardTheme : downloadedThemes) {
                    if (themeName.equals(keyboardTheme.mThemeName)) {
                        isDownloaded = true;
                        break;
                    }
                }
                //如果已下载,修改类型并修改按钮文字
                if (isDownloaded) {
                    themeType = HSKeyboardTheme.ThemeType.DOWNLOADED;
                    setButtonText();
                }
            }
        }
        if (themeCardAdapter != null) {
            themeCardAdapter.setItems(getKeyboardThemesExceptMe());
            themeCardAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        if (nativeAdView != null) {
            nativeAdView.release();
            nativeAdView = null;
        }
        HSGlobalNotificationCenter.removeObserver(notificationObserver);
        if (trialKeyboardDialog != null) {
            trialKeyboardDialog.dismiss();
            trialKeyboardDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCardClick(HSKeyboardTheme keyboardTheme) {
        HSAnalytics.logEvent("themedetails_themes_preview_clicked", "keyboardTheme", keyboardTheme.mThemeName);
    }

    @Override
    public void onKeyboardActivationStart() {
        Intent intent = new Intent(this, KeyboardActivationGuideActivity.class);
        startActivityForResult(intent, KEYBOARD_ACTIVIATION_FROM_THEME_CARD);
    }

    @Override
    public void onMenuShareClick(HSKeyboardTheme keyboardTheme) {
    }

    @Override
    public void onMenuDownloadClick(HSKeyboardTheme keyboardTheme) {
        HSAnalytics.logEvent("themedetails_themes_download_clicked", "keyboardTheme", keyboardTheme.mThemeName);
        if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
            ThemeAnalyticsReporter.getInstance().recordThemeDownloadInDetailActivity(keyboardTheme.mThemeName);
        }
    }

    @Override
    public void onMenuDeleteClick(HSKeyboardTheme keyboardTheme) {

    }

    @Override
    public void onMenuAppliedClick(HSKeyboardTheme keyboardTheme) {

    }

    private void updateApplyButton() {
        if (TextUtils.equals(themeName, HSKeyboardThemeManager.getCurrentThemeName())) {
            rightBtn.setText(R.string.theme_card_menu_applied);
            rightBtn.setEnabled(false);
        } else {
            rightBtn.setText(R.string.theme_card_menu_apply);
            rightBtn.setEnabled(true);
        }
    }

}
