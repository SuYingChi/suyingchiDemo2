package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodSettings;
import com.ihs.inputmethod.api.keyboard.HSKeyboardThemePreview;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeBitmapUtils;
import com.ihs.inputmethod.api.utils.HSColorUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.constants.AdPlacements;
import com.ihs.inputmethod.framework.AudioAndHapticFeedbackManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.background.BackgroundFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.button.ButtonFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.font.FontFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.sound.SoundFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.view.HSCommonHeaderView;
import com.ihs.inputmethod.uimodules.widget.videoview.HSMediaView;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.kc.commons.utils.KCCommonUtils;
import com.kc.utils.KCAnalytics;
import com.keyboard.common.SplashActivity;
import com.keyboard.core.themes.custom.KCCustomThemeData;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.keyboard.core.themes.custom.elements.KCBackgroundElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static com.ihs.app.framework.HSApplication.getContext;


public class CustomThemeActivity extends HSAppCompatActivity implements INotificationObserver {
    public static final String BUNDLE_KEY_BACKGROUND_NAME = "BUNDLE_KEY_BACKGROUND_NAME";
    public static final String BUNDLE_KEY_BACKGROUND_USE_CAMERA = "BUNDLE_KEY_BACKGROUND_USE_CAMERA";
    public static final String BUNDLE_KEY_BACKGROUND_USE_GALLERY = "BUNDLE_KEY_BACKGROUND_USE_GALLERY";
    public static final String BUNDLE_KEY_CUSTOMIZE_ENTRY = "customize_entry";

    private static final int FRAGMENT_INDEX_LOAD_INTERSTITIAL_AD = 1;
    private static List<Class<? extends BaseThemeFragment>> fragmentClasses = new ArrayList<>();

    static {
        fragmentClasses.add(BackgroundFragment.class);
        fragmentClasses.add(ButtonFragment.class);
        fragmentClasses.add(FontFragment.class);
        fragmentClasses.add(SoundFragment.class);
    }

    private HSKeyboardThemePreview keyboardView;
    private HSMediaView mp4HSBackgroundView;
    private HSCommonHeaderView headerView;
    private ViewGroup rootView;
    private MaterialDialog savingDialog;
    private BaseThemeFragment currentFragment;
    private KCCustomThemeData customThemeData;
    private KCBackgroundElement defaultBackgroundElement;
    private boolean shouldUseCamera;
    private boolean shouldUseGallery;
    private Handler handler = new Handler();
    private int currentPageIndex;
    private boolean isThemeSaving;
    private boolean isActive;
    private View tip;
    private ContentObserver volumeOb = new ContentObserver(handler) {
        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updateTipViewVisibility();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            updateTipViewVisibility();
        }
    };

    public static void startCustomThemeActivity(Context context, final Bundle bundle) {
        HSInputMethod.hideWindow();
        String currentAppName = HSInputMethod.getCurrentHostAppPackageName();
        String myPkName = context.getPackageName();
        int delay = 0;
        if (myPkName != null && myPkName.equals(currentAppName)) { //延迟100ms，让试用键盘可以有足够时间消失掉
            delay = 100;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent intent = new Intent();
                intent.setClass(context, SplashActivity.class);
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(SplashActivity.JUMP_TAG, SplashActivity.JUMP_TO_CUSTOM_THEME);
                context.startActivity(intent);
            }
        }, delay);
    }

    public static void startCustomThemeActivity(final Bundle bundle) {
        startCustomThemeActivity(HSApplication.getContext(), bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_theme);
        customThemeData = KCCustomThemeManager.getInstance().newCustomThemeData();

        if (savedInstanceState != null) {
            loadAsync();
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadAsync();
                }
            }, 0);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        newConfig.orientation = Configuration.ORIENTATION_PORTRAIT;
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HSKeyboardThemeManager.setPreviewCustomTheme(true);
        updateTipViewVisibility();
        refreshKeyboardView();
        getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, volumeOb);
        HSGlobalNotificationCenter.addObserver(KCCustomThemeManager.NOTIFICATION_CUSTOM_THEME_CONTENT_DOWNLOAD_FINISHED, this);
        isActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        HSKeyboardThemeManager.setPreviewCustomTheme(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        HSKeyboardThemeManager.setPreviewCustomTheme(false);
        getContentResolver().unregisterContentObserver(volumeOb);
        HSGlobalNotificationCenter.removeObserver(KCCustomThemeManager.NOTIFICATION_CUSTOM_THEME_CONTENT_DOWNLOAD_FINISHED, this);
        isActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioAndHapticFeedbackManager.getInstance().releaseSoundResource();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    private boolean initThemeResource() {
        /**
         * Init custom theme preview
         */
        HSKeyboardThemeManager.setPreviewCustomTheme(true);
        if (!HSKeyboardThemeManager.resetPreviewTheme()) {
            return false;
        }
        defaultBackgroundElement = customThemeData.getBackgroundElement();
        Intent intent = getIntent();
        String backgroundName = intent.getStringExtra(BUNDLE_KEY_BACKGROUND_NAME);
        if (backgroundName != null) {
            KCBackgroundElement background = new KCBackgroundElement(backgroundName);
            customThemeData.setElement(background);
        }

        shouldUseCamera = intent.getBooleanExtra(BUNDLE_KEY_BACKGROUND_USE_CAMERA, false);
        shouldUseGallery = intent.getBooleanExtra(BUNDLE_KEY_BACKGROUND_USE_GALLERY, false);

        String customEntry = intent.getStringExtra(BUNDLE_KEY_CUSTOMIZE_ENTRY);
        if ("keyboard_create".equals(customEntry)) {
            KCAnalytics.logEvent(BUNDLE_KEY_CUSTOMIZE_ENTRY, "bundle_key", customEntry);
        } else {
            KCAnalytics.logEvent(BUNDLE_KEY_CUSTOMIZE_ENTRY, "bundle_key", customEntry);
        }

        return true;
    }

    private void initView() {
        rootView = findViewById(R.id.root_view);
        headerView = findViewById(R.id.custom_theme_head_common);
    }

    private HSKeyboardThemePreview getKeyboardView() {
        return keyboardView;
    }

    public void refreshKeyboardView() {
        if (keyboardView != null) {
            keyboardView.refreshPreview();
        }

        if (mp4HSBackgroundView != null) {
            if (customThemeData.getBackgroundImageSource() == KCCustomThemeData.ImageSource.Official) {
                KCBackgroundElement backgroundElement = customThemeData.getBackgroundElement();
                boolean hasLocalImageContent = backgroundElement.hasLocalContent();
                boolean hasLocalMP4Content = backgroundElement.hasLocalMP4Content();
                if (hasLocalImageContent && hasLocalMP4Content) {
                    mp4HSBackgroundView.setHSBackground(new String[]{backgroundElement.getKeyboardImageContentPath(), backgroundElement.getKeyboardMP4ContentPath()});
                } else if (hasLocalImageContent) {
                    mp4HSBackgroundView.setHSBackground(new String[]{backgroundElement.getKeyboardImageContentPath()});
                }
            } else {
                try {
                    Resources res = HSApplication.getContext().getResources();
                    Bitmap bitmap = HSThemeBitmapUtils.decodeImage(customThemeData.getCustomizedBackgroundImagePath(), HSResourceUtils.getDefaultKeyboardWidth(res),
                            HSResourceUtils.getDefaultKeyboardHeight(res));
                    if (bitmap != null) {
                        mp4HSBackgroundView.setHSBackground(new BitmapDrawable(bitmap));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showKeyboard() {
        if (!isActive) {
            return;
        }

        if (rootView == null || keyboardView != null) {
            return;
        }


        new ShowKeyboardTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private boolean isLastPage() {
        return currentPageIndex == fragmentClasses.size() - 1;
    }

    private boolean isFirstPage() {
        return currentPageIndex == 0;
    }

    public List<Class<? extends BaseThemeFragment>> getFragmentClasses() {
        return fragmentClasses;
    }

    public void showFragment(int pageIndex) {
        if (pageIndex >= 0 && pageIndex < getFragmentClasses().size()) {

            if (pageIndex == FRAGMENT_INDEX_LOAD_INTERSTITIAL_AD && KeyboardFullScreenAd.canShowSessionAd) {
                if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                    KCInterstitialAd.load(AdPlacements.INTERSTITIAL_SPRING);
                }
            }

            try {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                currentFragment = getFragmentClasses().get(pageIndex).newInstance();
                currentFragment.setCustomThemeData(customThemeData);
                transaction.replace(R.id.custom_theme_items_container, currentFragment).commitAllowingStateLoss();

                updateHeaderView();

                if (currentFragment instanceof BackgroundFragment) {
                    if (shouldUseCamera) {
                        shouldUseCamera = false;
                        ((BackgroundFragment) currentFragment).setEntryMode(BackgroundFragment.EntryMode.Camera);
                    } else if (shouldUseGallery) {
                        shouldUseGallery = false;
                        ((BackgroundFragment) currentFragment).setEntryMode(BackgroundFragment.EntryMode.Gallery);
                    }
                }

                updateTipViewVisibility();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showKeyboard();
                    }
                }, 1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateHeaderView() {
        String middleText = getResources().getString(R.string.custom_theme_title);
        String leftText = isFirstPage() ? getResources().getString(R.string.cancel) : getResources().getString(R.string.back);
        String rightText = isLastPage() ? getResources().getString(R.string.save) : getResources().getString(R.string.next);
        headerView.setText(leftText, middleText, rightText);
        headerView.setButtonVisibility(!isFirstPage(), !isLastPage());
        headerView.setOnNavigationClickListener(new HSCommonHeaderView.OnNavigationClickListener() {
            @Override
            public void onLeftClick(View view) {
                if (isFirstPage()) {
                    //首页返回
                    Intent intent = getIntent();
                    String customEntry = intent.getStringExtra(BUNDLE_KEY_CUSTOMIZE_ENTRY);
                    KCAnalytics.logEvent("app_customize_background_cancel_clicked", "bundle_key", customEntry);
                    finish();
                } else {
                    //回上一页
                    currentPageIndex--;
                    showFragment(currentPageIndex);
                }
            }

            @Override
            public void onRightClick(View view) {
                logGAEventForNextAction(currentPageIndex);//最后一个页面需要同时记录next和save
                if (isLastPage()) {
                    //最后一页，保存
                    saveTheme();
                } else {
                    currentPageIndex++;
                    showFragment(currentPageIndex);
                }
            }
        });
    }

    private void logGAEventForNextAction(int currentPage) {
        String action = "";
        String label = "";
        switch (currentPage) {
            case 0:
                //background
                action = "app_customize_background_next_clicked";
                if (customThemeData.getBackgroundImageSource() == KCCustomThemeData.ImageSource.Official) {
                    label = customThemeData.getBackgroundElement().getName();
                } else if (customThemeData.getBackgroundImageSource() == KCCustomThemeData.ImageSource.Album) {
                    label = "album";
                } else if (customThemeData.getBackgroundImageSource() == KCCustomThemeData.ImageSource.Camera) {
                    label = "camera";
                }
                Intent intent = getIntent();
                String customEntry = intent.getStringExtra(BUNDLE_KEY_CUSTOMIZE_ENTRY);
                break;
            case 1:
                //button
                action = "app_customize_button_next_clicked";
                String buttonShape = customThemeData.getButtonShapeElement().getName();
                String buttonStyle = customThemeData.getButtonStyleElement().getName();
                label = String.format("%s;%s", buttonShape, buttonStyle);
                break;
            case 2:
                //font
                action = "app_customize_font_next_clicked";
                String font = customThemeData.getFontElement().getName();
                String font_color = HSColorUtils.getHexColor(customThemeData.getTextColorElement().getColor());
                label = String.format("%s;%s", font, font_color);
                break;
            case 3:
                //font
                action = "app_customize_sound_next_clicked";
                label = customThemeData.getSoundElement().getName();
                break;
        }
        KCAnalytics.logEvent(action, "currentPage", label);
    }

    private void updateTipViewVisibility() {
        if (currentFragment instanceof SoundFragment) {
            final AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            final int level = am.getStreamVolume(AudioManager.STREAM_SYSTEM);

            if (!HSInputMethodSettings.getKeySoundEnabled() || level <= 0) {
                if (tip == null) {
                    tip = LayoutInflater.from(this).inflate(R.layout.custom_theme_sound_tip, null);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(ALIGN_PARENT_BOTTOM);
                    params.bottomMargin = HSResourceUtils.getDefaultKeyboardHeight(getResources());
                    rootView.addView(tip, params);
                }
                tip.setVisibility(View.VISIBLE);
            } else {
                if (tip != null) {
                    tip.setVisibility(View.INVISIBLE);
                }
            }

        } else {
            if (tip != null) {
                tip.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void saveTheme() {
        if (!isThemeSaving) {
            isThemeSaving = true;

            if (savingDialog == null) {
                savingDialog = new MaterialDialog.Builder(CustomThemeActivity.this)
                        .content(getString(R.string.saving))
                        .widgetColorRes(R.color.light_button_normal)
                        .backgroundColor(Color.WHITE)
                        .contentColor(Color.BLACK)
                        .progress(true, 15)
                        .build();
            }

            KCCommonUtils.showDialog(savingDialog);

            try {
                new SaveThemeChangesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                KCAnalytics.logEvent("app_customize_save", "save_state", "Save_Success");
            } catch (Exception e) {
                exitWhenSaveFailed();
                return;
            } catch (OutOfMemoryError error) {
                exitWhenSaveFailed();
                return;
            }
        }
    }

    private void exitWhenSaveFailed() {
        dismissDialog(savingDialog);
        Toast.makeText(this, R.string.save_theme_failed, Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (KCCustomThemeManager.NOTIFICATION_CUSTOM_THEME_CONTENT_DOWNLOAD_FINISHED.equals(s)) {
            refreshKeyboardView();
        }
    }

    private void onNewThemeCreated() {
        if (!showInterstitialAdsAfterSaveTheme()) {
            finishSuccess();
        }
    }

    private boolean showInterstitialAdsAfterSaveTheme() {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return false;
        }
        return KCInterstitialAd.show(AdPlacements.INTERSTITIAL_SPRING,
                getString(R.string.interstitial_ad_title_after_save_theme),
                getString(R.string.interstitial_ad_subtitle_after_save_theme),
                new KCInterstitialAd.OnAdCloseListener() {
                    @Override
                    public void onAdClose() {
                        finishSuccess();
                    }
                }
        );
    }

    private void finishSuccess() {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("CUSTOM_THEME_SAVE", false)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("CUSTOM_THEME_SAVE", true).apply();
        }
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showKeyboard();
                }
            }, 1000);
        }
    }

    public void setHeaderNextEnable(boolean enable) {
        headerView.setHeaderNextEnable(enable);
    }

    /**
     * Load res and display custom elements
     */
    private void loadAsync() {
        if (initThemeResource()) {
            initView();
            showFragment(currentPageIndex);
            if (getIntent().getStringExtra("fromCropper") != null) {
                if (currentFragment instanceof BackgroundFragment) {
                    ((BackgroundFragment) currentFragment).setKeyboardTheme(getIntent());
                }
            }
        } else {
            HSToastUtils.toastCenterLong(getResources().getString(R.string.theme_create_custom_theme_failed));
            finish();
        }
    }

    public class ShowKeyboardTask extends AsyncTask<Void, Void, Drawable> {

        private RelativeLayout keyboardFrameLayout;
        private float height;

        @Override
        protected void onPreExecute() {
            height = HSResourceUtils.getDefaultKeyboardHeight(getResources());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(ALIGN_PARENT_BOTTOM);
            params.height = (int) height;

            keyboardFrameLayout = (RelativeLayout) View.inflate(getContext(), R.layout.layout_theme_preview, null);
            keyboardFrameLayout.setLayoutParams(params);
            mp4HSBackgroundView = keyboardFrameLayout.findViewById(R.id.keyboard_bg);
            mp4HSBackgroundView.init();

            keyboardView = keyboardFrameLayout.findViewById(R.id.keyboard_view);
            keyboardView.setCustomThemeData(customThemeData);
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            try {
                // 拷贝customThemeCommon到本地
                if (KCCustomThemeManager.getInstance().saveDefaultBackgroundToLocalReady() && KCCustomThemeManager.getInstance().saveCustomThemeCommonToLocalReady()) {
                    keyboardView.loadKeyboard();
                    Resources res = HSApplication.getContext().getResources();
                    Bitmap bitmap = HSThemeBitmapUtils.decodeImage(defaultBackgroundElement.getKeyboardImageContentPath(), HSResourceUtils.getDefaultKeyboardWidth(res),
                            HSResourceUtils.getDefaultKeyboardHeight(res));
                    return new BitmapDrawable(res, bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError outOfMemoryError) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable backgroundDrawable) {
            if (backgroundDrawable == null) { //内存不足，导致背景图片没创建成功
                Toast.makeText(CustomThemeActivity.this, R.string.low_memory_tip, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            mp4HSBackgroundView.setHSBackground(backgroundDrawable);
            rootView.addView(keyboardFrameLayout);
            refreshKeyboardView();
            ObjectAnimator animator = ObjectAnimator.ofFloat(keyboardFrameLayout, "translationY", height, 0);
            animator.setDuration(500);
            animator.start();
            if (currentFragment != null) {
                currentFragment.refreshHeaderNextButtonState();
            }
        }
    }

    protected class SaveThemeChangesTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return KCCustomThemeManager.getInstance().generateCustomTheme(customThemeData);
            }catch (Exception e){
                e.printStackTrace();
            }catch (OutOfMemoryError e1){
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String name) {
            if (!TextUtils.isEmpty(name)) {
                KCCustomThemeManager.getInstance().addCustomTheme(name);
                HSKeyboardThemeManager.setPreviewCustomTheme(false);
                HSKeyboardThemeManager.setKeyboardTheme(name);
                setResult(RESULT_OK);
                onNewThemeCreated();
            } else {
                exitWhenSaveFailed();
            }

            dismissDialog(savingDialog);
        }
    }

    private void dismissDialog(Dialog dialog) {
        if (dialog != null) {
            KCCommonUtils.dismissDialog(dialog);
        }
    }
}
