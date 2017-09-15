package com.ihs.inputmethod.uimodules.ui.customize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.CPSettings;
import com.acb.call.GifDownloadManager;
import com.acb.call.activity.HSAppCompatActivity;
import com.acb.call.constant.CPConst;
import com.acb.call.customize.AcbCallManager;
import com.acb.call.receiver.IncomingCallReceiver;
import com.acb.call.themes.Type;
import com.acb.call.utils.Utils;
import com.acb.call.views.DownloadProgressBar;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.airbnb.lottie.LottieAnimationView;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;

public class InCallThemePreviewActivity extends HSAppCompatActivity {

    public static final String TAG = InCallThemePreviewActivity.class.getSimpleName();

    private static final int OVERLAY_REQUEST_CODE = 999;
    private ArrayList<Type> mThemeArray;

    private HorizontalScrollView mThemesScrollView;
    private LinearLayout mThemesParent;
    private ThemePreviewWindow mPreviewView;
    private InCallActionView mCallView;
    private Toolbar mToolbar;
    private GifDownloadManager mGifDownloader = new GifDownloadManager();
    private int mThemeCurrentSelectedId = Type.NONE;
    private int mThemePreviousSelectedId = Type.NONE;
    private boolean mIsDestroyed;

    private static int sPhoneWidth;
    private int mThemeChildWidth = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_activity_theme_preview);

        mThemeArray = Type.values();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        TextView titleView = new TextView(this);
        titleView.setText(R.string.acb_screen_flash_name);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(20);
        Toolbar.LayoutParams toolbarTitleParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT, Gravity.LEFT | Gravity.START);
        boolean showToolBarBack = HSConfig.optBoolean(true, "Application", "LibColorPhone", "ShowToolBarBack");
        if (showToolBarBack) {
            toolbarTitleParams.setMargins(Utils.pxFromDp(20), 0, 0, 0);
        } else {
            toolbarTitleParams.leftMargin = Utils.pxFromDp(30);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                toolbarTitleParams.setMarginStart(Utils.pxFromDp(30));
            }
        }

        titleView.setLayoutParams(toolbarTitleParams);
        mToolbar.addView(titleView);
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mToolbar.setBackgroundResource(R.drawable.acb_phone_theme_tool_bar_bg);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(showToolBarBack);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mPreviewView = (ThemePreviewWindow) findViewById(R.id.flash_view);
        mPreviewView.setPreviewType(ThemePreviewWindow.PreviewType.PREVIEW);

        mCallView = (InCallActionView) findViewById(R.id.in_call_view);
        mCallView.enableFullScreen(false);
        initThemesView();
        requestPermissionsIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean screenFlashSetting;
    private boolean assistantSetting;

    @Override
    protected void onStart() {
        super.onStart();
        if (mPreviewView != null) {
            mPreviewView.startAnimations();
        }
        if (mCallView != null) {
            mCallView.doAnimation();
        }
        screenFlashSetting = CPSettings.isScreenFlashModuleEnabled();
        assistantSetting = CPSettings.isCallAssistantModuleEnabled();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPreviewView != null) {
            mPreviewView.stopAnimations();
        }
        if (mCallView != null) {
            mCallView.stopAnimations();
        }

        Type curType = mThemeArray.get(getIndexOfTheme(mThemeCurrentSelectedId));
        boolean curFlashEnabled = CPSettings.isScreenFlashModuleEnabled();
        boolean curAssistantEnabled = CPSettings.isCallAssistantModuleEnabled();
        if (screenFlashSetting != curFlashEnabled) {
            if (curFlashEnabled) {
                HSAnalytics.logEvent("Flashlight_ScreenFlashEnabled_FromSettings", "ThemeName",
                        curType.getName());
            } else {
                HSAnalytics.logEvent("Flashlight_ScreenFlashDisabled_FromSettings");
            }
        }
        if (curFlashEnabled) {
            if (mThemePreviousSelectedId != 0 && mThemeCurrentSelectedId != mThemePreviousSelectedId) {
                HSAnalytics.logEvent("Flashlight_ScreenFlashChangeTheme_FromSettings", "ThemeName",
                        curType.getName());
            }
        }
        if (assistantSetting != curAssistantEnabled) {
            if (curAssistantEnabled) {
                HSAnalytics.logEvent("Flashlight_CallAssistantEnabled_FromSettings");
            } else {
                HSAnalytics.logEvent("Flashlight_CallAssistantDisabled_FromSettings");
            }
        }
    }

    @Override
    protected void onDestroy() {
        mIsDestroyed = true;
        mGifDownloader.cancelAllDownload();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        AcbCallManager.getInstance().getAcbCallFactory().getViewConfig().onCreateOptionsMenu(getMenuInflater(), menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (AcbCallManager.getInstance().getAcbCallFactory().getViewConfig().onPrepareOptionsMenu(menu)) {
            return true;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (AcbCallManager.getInstance().getAcbCallFactory().getViewConfig().onOptionsItemSelected(item)) {
            return true;
        } else if (i == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void requestPermissionsIfNeeded() {
        if (!IncomingCallReceiver.IncomingCallListener.getInstance().getPermission().canDrawOverlays()) {
            IncomingCallReceiver.IncomingCallListener.getInstance().getPermission().requestDrawOverlays(this, OVERLAY_REQUEST_CODE);
        }
    }

    private void initThemesView() {
        mThemesScrollView = (HorizontalScrollView) findViewById(R.id.flash_settings_scroll_view);
        mThemesParent = (LinearLayout) findViewById(R.id.flash_settings_scroll_container);
        addThemeChildren();
        if (mThemesParent.getChildCount() <= 0) {
            return;
        }

        final View tempChild = mThemesParent.getChildAt(0);
        tempChild.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (tempChild.getWidth() <= 0) {
                    return;
                }
                tempChild.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                mThemeChildWidth = tempChild.getWidth();
                if (sPhoneWidth <= 0) {
                    sPhoneWidth = Utils.getPhoneWidth(HSApplication.getContext());
                }

                if (4.5f * mThemeChildWidth > sPhoneWidth) {
                    mThemeChildWidth = (int) (sPhoneWidth / 3.5f);
                } else {
                    mThemeChildWidth = (int) (sPhoneWidth / 4.5f);
                }

                final int childCount = mThemesParent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    final View child = mThemesParent.getChildAt(i);
                    final int childIndex = i;
                    final int themeId = mThemeArray.get(childIndex).getValue();
                    setThemesChild(child, i);
                    child.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!mThemeArray.get(childIndex).isGif()) {
                                if (themeId == mThemeCurrentSelectedId) {
                                    return;
                                }
                                HSPreferenceHelper.getDefault().putInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, themeId);
                            }

                            resetThemeSelected(mThemesParent.getChildAt(getIndexOfTheme(mThemeCurrentSelectedId)));
                            setThemeSelected(child, childIndex);
                            scrollToRightPosition(childIndex);
                            if (mThemeCurrentSelectedId == Type.NONE && themeId != Type.NONE) {
                                CPSettings.setScreenFlashModuleEnabled(true);
                            } else if (themeId == Type.NONE) {
                                CPSettings.setScreenFlashModuleEnabled(false);
                            }

                            mThemeCurrentSelectedId = themeId;
                        }
                    });
                }

                mThemePreviousSelectedId = HSPreferenceHelper.getDefault().getInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID,
                        CPSettings.isScreenFlashModuleEnabled() ? AcbCallManager.getInstance().getAcbCallFactory().getDefaultThemeId() : Type.NONE);
                mThemeCurrentSelectedId = mThemePreviousSelectedId;
                final int index = getIndexOfTheme(mThemeCurrentSelectedId);
                setThemeSelected(mThemesParent.getChildAt(index), index);
                mThemesParent.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mThemeCurrentSelectedId == Type.NONE) {
                            mThemesScrollView.smoothScrollTo(Utils.isRtl() ? mThemeArray.size() * mThemeChildWidth : 0, 0);
                        } else {
                            scrollToRightPosition(index);
                        }
                    }
                });

            }


        });
    }

    private void addThemeChildren() {
        for (int i = 0; i < mThemeArray.size(); i ++) {
            View.inflate(this, R.layout.acb_phone_theme_scroll_item, mThemesParent);
        }
    }

    private int getIndexOfTheme(int themeId) {
        for (int i = 0; i < mThemeArray.size(); i++) {
            if (mThemeArray.get(i).getValue() == themeId) {
                return i;
            }
        }
        // return index of NONE
        return mThemeArray.size() - 1;
    }

    private void setThemesChild(View view, int index) {
        if (view == null) {
            return;
        }
        Type type = mThemeArray.get(index);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.width = mThemeChildWidth;
        view.setLayoutParams(lp);
        view.requestLayout();

        ImageView img = (ImageView) view.findViewById(R.id.flash_settings_scroll_item_img);
        TextView txt = (TextView) view.findViewById(R.id.flash_settings_scroll_item_txt);

        AcbCallManager.getInstance().getImageLoader().load(type, type.getItemIcon(), type.getItemPlaceHolder(), img);
        txt.setText(type.getName());

        if (type.isGif()) {
            AcbCallManager.getInstance().getImageLoader().load(type, type.getPreviewImage());
            if (!mGifDownloader.isDownloaded(type.getGifFileName())) {
                View download = view.findViewById(R.id.acb_phone_download);
                download.setVisibility(View.VISIBLE);
            }
        }
    }


    private void setThemeSelected(final View view, final int index) {
        if (view == null) {
            return;
        }
        initThemeAnimation(view, index);

        ImageView img = (ImageView) view.findViewById(R.id.flash_settings_scroll_item_img);
        img.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.acb_phone_theme_item_selected_mark));
    }

    private void scrollToRightPosition(int index) {
        if (sPhoneWidth <= 0) {
            sPhoneWidth = Utils.getPhoneWidth(HSApplication.getContext());
        }

        int childCount = mThemeArray.size();

        if (sPhoneWidth <= 0) {
            sPhoneWidth = Utils.getPhoneWidth(HSApplication.getContext());
        }
        int scrollViewX = mThemesScrollView.getScrollX();
        int viewRightPosition = (index + 1) * mThemeChildWidth;
        int viewLeftPosition = index * mThemeChildWidth;
        if (Utils.isRtl()) {
            viewRightPosition = (childCount - index) * mThemeChildWidth;
            viewLeftPosition = (childCount - 1 - index) * mThemeChildWidth;
        }

        if (viewLeftPosition - scrollViewX < sPhoneWidth / 3) {
            mThemesScrollView.smoothScrollTo(viewLeftPosition - sPhoneWidth / 2, 0);
        } else if (viewLeftPosition - scrollViewX >= sPhoneWidth) {
            mThemesScrollView.scrollTo(viewLeftPosition - sPhoneWidth / 2, 0);
        } else if (viewRightPosition - scrollViewX > sPhoneWidth * 2 / 3) {
            mThemesScrollView.smoothScrollTo(viewRightPosition - sPhoneWidth / 2, 0);
        }
    }

    private void resetThemeSelected(View view) {
        if (view == null) {
            return;
        }
        ImageView img = (ImageView) view.findViewById(R.id.flash_settings_scroll_item_img);
        img.setBackgroundDrawable(null);
    }

    private void initThemeAnimation(final View view, final int index) {
        Type type = mThemeArray.get(index);
        if (type.isGif()) {
            prepareAndShowGif(view, type);
        } else {
            findViewById(R.id.theme_progress_bar).setVisibility(View.GONE);
            findViewById(R.id.theme_progress_txt_holder).setVisibility(View.GONE);
            mPreviewView.playAnimation(type);
        }

        mCallView.setAutoRun(type.getValue() != Type.NONE);
    }

    private void hideProgressViews(View root) {
        final FrameLayout progressContainer = (FrameLayout) root.findViewById(R.id.acb_theme_gif_preview_loading_view);
        progressContainer.findViewById(R.id.acb_theme_gif_loading_progressbar).setVisibility(View.GONE);
        progressContainer.findViewById(R.id.acb_theme_gif_loading_progress_label).setVisibility(View.GONE);
        progressContainer.findViewById(R.id.acb_theme_gif_download_finished_anim).setVisibility(View.GONE);
        progressContainer.setVisibility(View.GONE);
        findViewById(R.id.theme_progress_bar).setVisibility(View.GONE);
        findViewById(R.id.theme_progress_txt_holder).setVisibility(View.GONE);
    }

    private void prepareAndShowGif(final View view, final Type type) {
        if (mGifDownloader.isDownloaded(type.getGifFileName())) {
            hideProgressViews(view);
            HSPreferenceHelper.getDefault().putInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, type.getValue());
            mPreviewView.playAnimation(type);
        } else {
            mPreviewView.updateThemeLayout(type);
            AcbCallManager.getInstance().getImageLoader().load(type, type.getPreviewImage(), type.getPreviewPlaceHolder(), (ImageView) mPreviewView.findViewById(R.id.animation_view));
            if (!mGifDownloader.isDownloading(type.getGifFileName())) {
                downloadGif(view, type);
            } else {
                findViewById(R.id.theme_progress_bar).setVisibility(View.VISIBLE);
                findViewById(R.id.theme_progress_txt_holder).setVisibility(View.VISIBLE);
            }
        }
    }

    private void downloadGif(final View view, final Type type) {
        final FrameLayout progressContainer = (FrameLayout) view.findViewById(R.id.acb_theme_gif_preview_loading_view);
        final DownloadProgressBar progressBar = (DownloadProgressBar) progressContainer.findViewById(R.id.acb_theme_gif_loading_progressbar);
        final TextView progressLabel = (TextView) progressContainer.findViewById(R.id.acb_theme_gif_loading_progress_label);
        final ProgressBar horPro = (ProgressBar) findViewById(R.id.theme_progress_bar);
        final LinearLayout proHolder = (LinearLayout) findViewById(R.id.theme_progress_txt_holder);
        final View downloadIcon = view.findViewById(R.id.acb_phone_download);
        final LottieAnimationView checkAnimation = (LottieAnimationView) progressContainer.findViewById(R.id.acb_theme_gif_download_finished_anim);

        horPro.setVisibility(View.VISIBLE);
        proHolder.setVisibility(View.VISIBLE);
        progressContainer.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        progressLabel.setVisibility(View.VISIBLE);
        ((TextView) proHolder.findViewById(R.id.theme_progress_txt)).setText("0 %");
        progressBar.reset();
        progressBar.setProgress(1);
        downloadIcon.setVisibility(View.GONE);
        checkAnimation.setVisibility(View.GONE);
        checkAnimation.setProgress(0f);
        mGifDownloader.downloadGif(type.getGifUrl(), type.getGifFileName(),
                new GifDownloadManager.SimpleDownloadCallback() {
                    @Override
                    public void onUpdate(final long progress) {
                        progressBar.setProgress((int) progress);
                        progressLabel.setText("" + progress + "%");

                        if (type.getValue() == mThemeCurrentSelectedId) {
                            horPro.setProgress((int) progress);
                            ((TextView) proHolder.findViewById(R.id.theme_progress_txt)).setText("" + progress + " %");
                        }
                    }

                    @Override
                    public void onSuccess(GifDownloadManager.GifDownLoadTask task) {
                        if (mIsDestroyed) {
                            return;
                        }
                        checkAnimation.setVisibility(View.VISIBLE);
                        progressLabel.setVisibility(View.GONE);
                        progressLabel.setText("0%");
                        progressBar.setProgress(100);

                        if (type.getValue() == mThemeCurrentSelectedId) {
                            ((TextView) proHolder.findViewById(R.id.theme_progress_txt)).setText("100 %");
                            horPro.setProgress(100);
                        }

                        checkAnimation.addAnimatorListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                checkAnimation.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                                if (type.getValue() == mThemeCurrentSelectedId) {
                                    horPro.setVisibility(View.GONE);
                                    proHolder.setVisibility(View.GONE);
                                    horPro.setProgress(0);
                                    ((TextView) proHolder.findViewById(R.id.theme_progress_txt)).setText("0 %");
                                }
                                progressContainer.setVisibility(View.GONE);
                                progressContainer.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (type.getValue() == mThemeCurrentSelectedId) {
                                            HSPreferenceHelper.getDefault().putInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, type.getValue());
                                            mPreviewView.playAnimation(type);
                                        }
                                    }
                                }, 100);
                            }
                        });
                        checkAnimation.playAnimation();
                    }

                    @Override
                    public void onFail(GifDownloadManager.GifDownLoadTask task, String msg) {
                        if (mIsDestroyed) {
                            return;
                        }

                        checkAnimation.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        progressContainer.setVisibility(View.GONE);
                        proHolder.setVisibility(View.GONE);
                        horPro.setVisibility(View.GONE);
                        downloadIcon.setVisibility(View.VISIBLE);
                        Toast.makeText(InCallThemePreviewActivity.this, R.string.acb_phone_theme_gif_download_failed_toast, Toast.LENGTH_LONG).show();
                    }
                });
    }
}