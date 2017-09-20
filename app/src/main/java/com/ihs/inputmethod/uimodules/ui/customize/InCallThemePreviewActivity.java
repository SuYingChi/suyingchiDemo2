package com.ihs.inputmethod.uimodules.ui.customize;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.utils.ToastUtils;

import java.util.ArrayList;

public class InCallThemePreviewActivity extends HSAppCompatActivity {

    public static final String TAG = InCallThemePreviewActivity.class.getSimpleName();

    private static final int OVERLAY_REQUEST_CODE = 999;
    private ArrayList<Type> mThemeArray;

    private ThemePreviewWindow mPreviewView;
    private InCallActionView mCallView;
    private Toolbar mToolbar;
    private TextView mSetCallThemeButton;
    private GifDownloadManager mGifDownloader = new GifDownloadManager();
    private int mThemeCurrentSelectedId = Type.NONE;
    private int mThemePreviousSelectedId = Type.NONE;
    private boolean mIsDestroyed;

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
        mSetCallThemeButton = (TextView) findViewById(R.id.set_incoming_call_theme);
        initThemesView();
        requestPermissionsIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPreviewView != null) {
            mPreviewView.startAnimations();
        }
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
        final Type themeType = (Type) getIntent().getSerializableExtra("CallThemeType");
        int themeId = themeType.getValue();

        mThemeCurrentSelectedId = themeId;

        initThemeAnimation(themeType);

        mSetCallThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSAnalytics.logEvent("app_callflash_applied", themeType.getName());
                CPSettings.setScreenFlashModuleEnabled(true);
                HSPreferenceHelper.getDefault().putInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, themeType.getValue());
                ToastUtils.showToast(R.string.incoming_call_theme_success);
            }
        });
    }

    private void initThemeAnimation(final Type themeType) {
        if (themeType.isGif()) {
            prepareAndShowGif(themeType);
        } else {
            findViewById(R.id.theme_progress_bar).setVisibility(View.GONE);
            findViewById(R.id.theme_progress_txt_holder).setVisibility(View.GONE);
            mSetCallThemeButton.setVisibility(View.VISIBLE);
            mPreviewView.playAnimation(themeType);
        }
    }

    private void prepareAndShowGif(final Type type) {
        if (mGifDownloader.isDownloaded(type.getGifFileName())) {
            mPreviewView.playAnimation(type);
            mSetCallThemeButton.setVisibility(View.VISIBLE);
            mCallView.setAutoRun(true);
        } else {
            mCallView.setAutoRun(false);
            mPreviewView.updateThemeLayout(type);
//            ImageLoader.getInstance().displayImage(type.getPreviewImage(), (ImageView) mPreviewView.findViewById(R.id.animation_view));
            RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA);
            Glide.with(this).asBitmap().apply(requestOptions)
                    .load(type.getPreviewImage())
                    .into((ImageView) mPreviewView.findViewById(R.id.animation_view));
            if (!mGifDownloader.isDownloading(type.getGifFileName())) {
                downloadGif(type);
            } else {
                findViewById(R.id.theme_progress_bar).setVisibility(View.VISIBLE);
                findViewById(R.id.theme_progress_txt_holder).setVisibility(View.VISIBLE);
            }
        }
    }

    private void downloadGif(final Type type) {
        final ProgressBar horPro = (ProgressBar) findViewById(R.id.theme_progress_bar);
        final LinearLayout proHolder = (LinearLayout) findViewById(R.id.theme_progress_txt_holder);

        horPro.setVisibility(View.VISIBLE);
        proHolder.setVisibility(View.VISIBLE);
        ((TextView) proHolder.findViewById(R.id.theme_progress_txt)).setText("0 %");
        mGifDownloader.downloadGif(type.getGifUrl(), type.getGifFileName(),
                new GifDownloadManager.SimpleDownloadCallback() {
                    @Override
                    public void onUpdate(final long progress) {

                        horPro.setProgress((int) progress);
                        ((TextView) proHolder.findViewById(R.id.theme_progress_txt)).setText("" + progress + " %");
                    }

                    @Override
                    public void onSuccess(GifDownloadManager.GifDownLoadTask task) {
                        if (mIsDestroyed) {
                            return;
                        }

                        horPro.setVisibility(View.GONE);
                        proHolder.setVisibility(View.GONE);
                        horPro.setProgress(0);
                        ((TextView) proHolder.findViewById(R.id.theme_progress_txt)).setText("0 %");
                        mCallView.doAnimation();
                        mPreviewView.playAnimation(type);
                        mSetCallThemeButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFail(GifDownloadManager.GifDownLoadTask task, String msg) {
                        if (mIsDestroyed) {
                            return;
                        }

                        proHolder.setVisibility(View.GONE);
                        horPro.setVisibility(View.GONE);
                        Toast.makeText(InCallThemePreviewActivity.this, R.string.acb_phone_theme_gif_download_failed_toast, Toast.LENGTH_LONG).show();
                    }
                });
    }
}