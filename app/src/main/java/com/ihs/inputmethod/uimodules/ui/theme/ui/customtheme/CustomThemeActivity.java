package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.activity.ChargingFullScreenAlertDialogActivity;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodSettings;
import com.ihs.inputmethod.api.keyboard.HSKeyboardThemePreview;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSColorUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.framework.AudioAndHapticFeedbackManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.background.BackgroundFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.button.ButtonFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.font.FontFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.sound.SoundFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.view.HSCommonHeaderView;
import com.ihs.inputmethod.uimodules.widget.CustomDesignAlert;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.ihs.inputmethod.uimodules.widget.videoview.HSMediaView;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.keyboard.core.themes.ThemeDirManager;
import com.keyboard.core.themes.custom.KCCustomThemeData;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCBackgroundElement;
import com.keyboard.core.themes.custom.elements.KCBaseElement;
import com.keyboard.core.themes.custom.elements.KCButtonStyleElement;
import com.keyboard.core.themes.custom.elements.KCFontElement;
import com.keyboard.core.themes.custom.elements.KCSoundElement;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static com.ihs.app.framework.HSApplication.getContext;


public class CustomThemeActivity extends HSAppCompatActivity implements INotificationObserver {
    public static final String NOTIFICATION_SHOW_TRIAL_KEYBOARD = "hs.inputmethod.uimodules.ui.theme.ui.SHOW_TRIAL_KEYBOARD"; //显示试用键盘
    public static final String BUNDLE_KEY_BACKGROUND_NAME = "BUNDLE_KEY_BACKGROUND_NAME";
    public static final String BUNDLE_KEY_BACKGROUND_USE_CAMERA = "BUNDLE_KEY_BACKGROUND_USE_CAMERA";
    public static final String BUNDLE_KEY_BACKGROUND_USE_GALLERY = "BUNDLE_KEY_BACKGROUND_USE_GALLERY";
    public static final String BUNDLE_KEY_CUSTOMIZE_ENTRY = "customize_entry";

    public static final int keyboardActivationFromCustom = 15;

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

    public static void startCustomThemeActivity(final Bundle bundle) {
        if (!ThemeDirManager.moveCustomAssetsToFileIfNecessary()) {
            Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getResources().getString(R.string.theme_create_custom_theme_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        HSInputMethod.hideWindow();
        String currentAppName = HSInputMethod.getCurrentHostAppPackageName();
        String myPkName = HSApplication.getContext().getPackageName();
        int delay = 0;
        if (myPkName != null && myPkName.equals(currentAppName)) { //延迟100ms，让试用键盘可以有足够时间消失掉
            delay = 100;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent intent = new Intent();
                intent.setClass(HSApplication.getContext(), com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity.class);
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                HSApplication.getContext().startActivity(intent);
            }
        }, delay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(0x00ffffff));
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
        ImageLoader.getInstance().clearMemoryCache();
        AudioAndHapticFeedbackManager.getInstance().releaseSoundResource();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    private boolean initThemeResource() {
        /**
         * Prepare custom theme resource
         */
//        if (!HSKeyboardThemeManager.initCustomThemeResource()) {
//            return false;
//        }
//        KCCustomThemeManager.getInstance();

        /**
         * Init custom theme preview
         */
        HSKeyboardThemeManager.setPreviewCustomTheme(true);
        if (!HSKeyboardThemeManager.resetPreviewTheme()) {
            return false;
        }


//        HSKeyboardThemeManager.resetCustomThemeData();//设置默认选项
//        HSKeyboardThemeManager.clearCustomThemePath();//去掉上次可能存在的缓存
        defaultBackgroundElement = customThemeData.getBackgroundElement();
        Intent intent = getIntent();
        String backgroundName = intent.getStringExtra(BUNDLE_KEY_BACKGROUND_NAME);
        if (backgroundName != null) {
            KCBackgroundElement background = new KCBackgroundElement(backgroundName);
            customThemeData.setElement(background);
//            HSKeyboardThemeManager.getCustomThemeData().setBackground(background);
        }

        shouldUseCamera = intent.getBooleanExtra(BUNDLE_KEY_BACKGROUND_USE_CAMERA, false);
        shouldUseGallery = intent.getBooleanExtra(BUNDLE_KEY_BACKGROUND_USE_GALLERY, false);


        String customEntry = intent.getStringExtra(BUNDLE_KEY_CUSTOMIZE_ENTRY);
        if ("keyboard_create".equals(customEntry)) {
//            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
            HSAnalytics.logEvent(BUNDLE_KEY_CUSTOMIZE_ENTRY, "bundle_key", customEntry);
        } else {
//            HSGoogleAnalyticsUtils.getInstance().logAppEvent(BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
            HSAnalytics.logEvent(BUNDLE_KEY_CUSTOMIZE_ENTRY, "bundle_key", customEntry);
        }

        return true;
    }

    private void initView() {
        rootView = (ViewGroup) findViewById(R.id.root_view);
        headerView = (HSCommonHeaderView) findViewById(R.id.custom_theme_head_common);
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
                mp4HSBackgroundView.setHSBackground(new BitmapDrawable(BitmapFactory.decodeFile(customThemeData.getCustomizedBackgroundImagePath())));
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

    private Drawable getPromptPurchaseViewBackground(KCBaseElement item) {
        if (item instanceof KCButtonStyleElement) {
            return KCElementResourseHelper.getButtonStyleBackgroundDrawable(customThemeData.getBackgroundMainColor());
        } else if (item instanceof KCSoundElement) {
            return KCElementResourseHelper.getSoundBackgroundDrawable(((KCSoundElement) item).getBackgroundColor());
        } else if (item instanceof KCFontElement) {
            return KCElementResourseHelper.getFontBackgroundDrawable();
        }
        return null;
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
            if (pageIndex == FRAGMENT_INDEX_LOAD_INTERSTITIAL_AD) {
                if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                    KCInterstitialAd.load(getString(R.string.placement_full_screen_open_keyboard));
                }
            }

            try {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                currentFragment = getFragmentClasses().get(pageIndex).newInstance();
                currentFragment.setCustomThemeData(customThemeData);
                transaction.replace(R.id.custom_theme_items_container, currentFragment).commit();

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
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("app_customize_background_cancel_clicked", customEntry);
                    HSAnalytics.logEvent("app_customize_background_cancel_clicked", "bundle_key", customEntry);
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
//        HSCustomThemeData customThemeData = HSKeyboardThemeManager.getCustomThemeData();
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
                // HSGoogleAnalyticsUtils.getInstance().logAppEvent(action, customEntry);
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
//        HSGoogleAnalyticsUtils.getInstance().logAppEvent(action, label);
        HSAnalytics.logEvent(action, "currentPage", label);
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

            if (!savingDialog.isShowing() && !isFinishing()) {
                savingDialog.show();
            }

            Resources res = getContext().getResources();
            Bitmap bitmap = Bitmap.createBitmap(HSResourceUtils.getDefaultKeyboardWidth(res), HSResourceUtils.getDefaultKeyboardHeight(res), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Drawable drawable = new BitmapDrawable(BitmapFactory.decodeFile(customThemeData.getBackgroundElement().getKeyboardImageContentPath()));
            drawable.setBounds(0, 0, HSResourceUtils.getDefaultKeyboardWidth(res), HSResourceUtils.getDefaultKeyboardHeight(res));
            drawable.draw(canvas);
            getKeyboardView().draw(canvas);
            new SaveThemeChangesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bitmap);

//            HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_customize_save", null);
            HSAnalytics.logEvent("app_customize_save", "save_state", "Save_Success");
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (KCCustomThemeManager.NOTIFICATION_CUSTOM_THEME_CONTENT_DOWNLOAD_FINISHED.equals(s)) {
            refreshKeyboardView();
        }
    }

    private void onNewThemeCreated() {
        if (!showInterstitialAdsAfterSaveTheme()) {
            showTrialKeyboard();
        }
    }

    private boolean showInterstitialAdsAfterSaveTheme() {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return false;
        }
        return KCInterstitialAd.show(getString(R.string.placement_full_screen_open_keyboard),
                getString(R.string.interstitial_ad_title_after_save_theme),
                getString(R.string.interstitial_ad_subtitle_after_save_theme),
                new KCInterstitialAd.OnAdCloseListener() {
                    @Override
                    public void onAdClose() {
                        showTrialKeyboard();
                    }
                }
        );
    }

    private void showTrialKeyboard() {
        setResult(RESULT_OK);
        HSBundle bundle = new HSBundle();
        bundle.putString(TrialKeyboardDialog.BUNDLE_KEY_SHOW_TRIAL_KEYBOARD_ACTIVITY, ThemeHomeActivity.class.getSimpleName());
        bundle.putInt(KeyboardActivationProcessor.BUNDLE_ACTIVATION_CODE, keyboardActivationFromCustom);
        HSGlobalNotificationCenter.sendNotification(CustomThemeActivity.NOTIFICATION_SHOW_TRIAL_KEYBOARD, bundle);
        if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("CUSTOM_THEME_SAVE", false)) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("CUSTOM_THEME_SAVE", true).apply();
        }
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
            mp4HSBackgroundView = (HSMediaView) keyboardFrameLayout.findViewById(R.id.keyboard_bg);
            mp4HSBackgroundView.init();

            keyboardView = (HSKeyboardThemePreview) keyboardFrameLayout.findViewById(R.id.keyboard_view);
            keyboardView.setCustomThemeData(customThemeData);
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            Drawable backgroundDrawable = new BitmapDrawable(BitmapFactory.decodeFile(defaultBackgroundElement.getKeyboardImageContentPath()));
            keyboardView.loadKeyboard();
            return backgroundDrawable;
        }

        @Override
        protected void onPostExecute(Drawable backgroundDrawable) {
            mp4HSBackgroundView.setHSBackground(backgroundDrawable);
            rootView.addView(keyboardFrameLayout);
            ObjectAnimator animator = ObjectAnimator.ofFloat(keyboardFrameLayout, "translationY", height, 0);
            animator.setDuration(500);
            animator.start();
            refreshKeyboardView();
        }
    }

    protected class SaveThemeChangesTask extends AsyncTask<Bitmap, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            if (params != null && params.length > 0) {
                String themeName = KCCustomThemeManager.getInstance().generateCustomTheme(customThemeData);
                if (themeName != null) {
                    return themeName;// HSKeyboardThemeManager.saveCustomTheme(params[0], HSKeyboardThemeManager.getCustomThemeData());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String name) {
            if (!TextUtils.isEmpty(name)) {
                KCCustomThemeManager.getInstance().addCustomTheme(name);
                HSKeyboardThemeManager.setPreviewCustomTheme(false);
                HSLog.e("custome ximu +" + name);
                HSKeyboardThemeManager.setKeyboardTheme(name);
                onNewThemeCreated();

            } else {
                setResult(RESULT_CANCELED);
                HSLog.e("generate custom theme failed.");
            }
            if (savingDialog.isShowing() && !isFinishing()) {
                savingDialog.dismiss();
            }
            finish();
        }
    }
}
