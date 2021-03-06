package com.ihs.inputmethod.uimodules.ui.customize;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artw.lockscreen.LockerSettings;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.kc.utils.KCAnalytics;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.feature.common.Thunk;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.uimodules.BuildConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.util.WallpaperUtils;
import com.ihs.inputmethod.uimodules.ui.customize.view.CategoryInfo;
import com.ihs.inputmethod.uimodules.ui.customize.view.PreviewViewPage;
import com.ihs.inputmethod.uimodules.ui.customize.view.ProgressDialog;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeBackgroundCropperActivity;
import com.ihs.keyboardutils.utils.ToastUtils;
import com.kc.commons.utils.KCCommonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

import static com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeBackgroundCropperActivity.CopperImagePath;
import static com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeBackgroundCropperActivity.KeyboardHeight;
import static com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeBackgroundCropperActivity.KeyboardWidth;

/**
 * Created by guonan.lv on 17/9/6.
 */

public class WallpaperPreviewActivity extends WallpaperBaseActivity
        implements OnPageChangeListener, PreviewViewPage.PreviewPageListener, View.OnClickListener {

    private static final String TAG = WallpaperPreviewActivity.class.getSimpleName();
    private static final boolean DEBUG = true && BuildConfig.DEBUG;

    // --Commented out by Inspection (18/1/11 下午2:41):public static final String INTENT_KEY_SCENARIO = "scenario";
    public static final String INTENT_KEY_CATEGORY = "category";
    public static final String INTENT_KEY_WALLPAPERS = "wallpapers";
    public static final String INTENT_KEY_INDEX = "index";
    // --Commented out by Inspection (18/1/11 下午2:41):public static final String INTENT_KEY_WALLPAPER_DATA = "wallpaperData";
    // --Commented out by Inspection (18/1/11 下午2:41):public static final String INTENT_KEY_RANDOM_WALLPAPERS_DATA = "randomWallpapers";
    // --Commented out by Inspection (18/1/11 下午2:41):public static final String INTENT_KEY_WALLPAPER_PACKAGE_INFO = "wallpaperPackageInfo";
// --Commented out by Inspection START (18/1/11 下午2:41):
//    @SuppressWarnings("PointlessBooleanExpression")
//    private static final String PREF_KEY_PREVIEW_GUIDE_SHOWN = "wallpaper_preview_guide_shown";
// --Commented out by Inspection STOP (18/1/11 下午2:41)
    // --Commented out by Inspection (18/1/11 下午2:41):private static final String PREF_KEY_PREVIEW_WALLPAPER_SHOWN_MODE = "pref_key_preview_wallpaper_shown_mode";

    // --Commented out by Inspection (18/1/11 下午2:41):private final static String FULL_SCREEN = "FULL_SCREEN";
    // --Commented out by Inspection (18/1/11 下午2:41):private final static String FULL_IMAGE = "FULL_IMAGE";

    // --Commented out by Inspection (18/1/11 下午2:41):private static final int MODE_GALLERY_WALLPAPER = 0;
    private static final int MODE_LOCAL_WALLPAPER = 1;

    // --Commented out by Inspection (18/1/11 下午2:41):private final static int TOP_MARGIN = CommonUtils.pxFromDp(15);

    private boolean mInitialized;
    // --Commented out by Inspection (18/1/11 下午2:41):private boolean mIsGuide;
    private boolean mIsOnLineWallpaper;
    // --Commented out by Inspection (18/1/11 下午2:41):private boolean mIsFromHub;
    private int mPaperIndex;
    // --Commented out by Inspection (18/1/11 下午2:41):private int mWallpaperMode = MODE_LOCAL_WALLPAPER;
    // --Commented out by Inspection (18/1/11 下午2:41):float sumPositionAndPositionOffset;

    @Thunk
    ViewPager mViewPager;
    private List<Object> mWallpapers = new ArrayList<>();
    private WallpaperInfo mCurrentWallpaper;
    private TextView mSetWallpaperButton;
    private TextView mSetKeyThemeButton;
    private ProgressDialog mDialog;
    // --Commented out by Inspection (18/1/11 下午2:41):private View mEdit;
    private View mReturnArrow;
    private boolean mIsSettingKeyTheme = false;
    private LinearLayout setWallpaperDialog;
    private LinearLayout setHomeScreen;
    private LinearLayout setLockerScreen;
    private LinearLayout setHomeAndLockerScreen;
    private PreviewViewPagerAdapter mAdapter;
    private SparseBooleanArray mLoadMap = new SparseBooleanArray();

// --Commented out by Inspection START (18/1/11 下午2:41):
//    //selectZoomBtn true zoom_out 缩小 mIsCenterCrop state true
//    //selectZoomBtn false zoom_in 放大 mIsCenterCrop state false
//    private ImageView mZoomBtn;
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    private CategoryInfo mCategoryInfo;
    // --Commented out by Inspection (18/1/11 下午2:41):private WallpaperPackageInfo mWallpaperPackageInfo;

// --Commented out by Inspection START (18/1/11 下午2:41):
//    //ad related
//    private static final int MAX_CONCURRENT_AD_REQUEST_COUNT = 3;
// --Commented out by Inspection STOP (18/1/11 下午2:41)
    // --Commented out by Inspection (18/1/11 下午2:41):private ArrayList<AcbNativeAd> mCandidateAds = new ArrayList<>();
    // --Commented out by Inspection (18/1/11 下午2:41):private boolean mShouldShowAds = false;
    // --Commented out by Inspection (18/1/11 下午2:41):private int mAdStep = 5;
    // --Commented out by Inspection (18/1/11 下午2:41):private int mCurrentRequestCount;
    // --Commented out by Inspection (18/1/11 下午2:41):private int mStartIndex = 0;
    // --Commented out by Inspection (18/1/11 下午2:41):private int mLastAdIndex = -1;
    private int mMaxVisiblePosition;
    // --Commented out by Inspection (18/1/11 下午2:41):private boolean mDestroying = false;
    // --Commented out by Inspection (18/1/11 下午2:41):private AcbNativeAdLoader mAdLoader;
    // --Commented out by Inspection (18/1/11 下午2:41):private ValueAnimator mPackageGuideLeftAnimator;
    // --Commented out by Inspection (18/1/11 下午2:41):private ValueAnimator mPackageGuideRightAnimator;
    // --Commented out by Inspection (18/1/11 下午2:41):private boolean mIsGuideInterrupted = false;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
//        WallpaperMgr.getInstance().initLocalWallpapers(mService, null);
        if (!mInitialized) {
            initData();
            refreshButtonState();
        }
    }

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_preview);
        initView();
        if (mService != null) {
            initData();
            refreshButtonState();
        }

    }

    private void initData() {
        mCategoryInfo = getIntent().getParcelableExtra(INTENT_KEY_CATEGORY);
        mPaperIndex = getIntent().getIntExtra(INTENT_KEY_INDEX, 0);
        mWallpapers.clear();
        mWallpapers.addAll(getIntent().getParcelableArrayListExtra(INTENT_KEY_WALLPAPERS));
        mCurrentWallpaper = (WallpaperInfo) getWallpaperInfoByIndex(mPaperIndex);
        mAdapter.notifyDataSetChanged();
        mIsOnLineWallpaper = true;
        mInitialized = true;
        mViewPager.setCurrentItem(mPaperIndex, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIsOnLineWallpaper) {
        }
        if (mDialog != null) {
            KCCommonUtils.dismissDialog(mDialog);
            mDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (setWallpaperDialog.getVisibility() == View.VISIBLE) {
            hideSetWallpaperSelectDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getIntent().putExtra(INTENT_KEY_INDEX, mPaperIndex);
    }

    @SuppressWarnings("RestrictedApi")
    private void initView() {
        mReturnArrow = findViewById(R.id.wallpaper_view_return);
        mReturnArrow.setOnClickListener(this);
//        mReturnArrow.setBackgroundResource(R.drawable.moment_round_material_compat_dark);
        mSetWallpaperButton = findViewById(R.id.set_wallpaper_button);
        mSetWallpaperButton.setOnClickListener(this);

        mSetKeyThemeButton = findViewById(R.id.set_key_theme_button);
        mSetKeyThemeButton.setOnClickListener(this);

        setWallpaperDialog = findViewById(R.id.select_dialog_wallpaper);
        setWallpaperDialog.setOnClickListener(this);
        setWallpaperDialog.setVisibility(View.INVISIBLE);

        mViewPager = findViewById(R.id.preview_view_pager);
        mAdapter = new PreviewViewPagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setFocusable(true);
        mViewPager.setClickable(true);
        mViewPager.setLongClickable(true);
        mViewPager.setOnClickListener(this);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(mPaperIndex, false);
    }

    private void showSetWallpaperSelectDialog() {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
        translateAnimation.setDuration(200);
        translateAnimation.setFillAfter(true);
        setWallpaperDialog.startAnimation(translateAnimation);
        setWallpaperDialog.setVisibility(View.VISIBLE);
    }

    private void hideSetWallpaperSelectDialog() {
        final TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
        translateAnimation.setDuration(200);
        translateAnimation.setFillAfter(true);
        setWallpaperDialog.startAnimation(translateAnimation);
        setWallpaperDialog.setVisibility(View.INVISIBLE);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setWallpaperDialog.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showSetWallpaperDialog() {
        showSetWallpaperSelectDialog();
        setHomeScreen = findViewById(R.id.set_home_screen);
        setHomeScreen.setOnClickListener(this);
        setLockerScreen = findViewById(R.id.set_locker_screen);
        setLockerScreen.setOnClickListener(this);
        setHomeAndLockerScreen = findViewById(R.id.set_home_and_locker_screen);
        setHomeAndLockerScreen.setOnClickListener(this);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    private void hideViews() {
//        mSetWallpaperButton.setVisibility(View.GONE);
//        View draw = ViewUtils.findViewById(this, R.id.preview_guide_draw_view);
//        if (draw != null) {
//            draw.setVisibility(View.GONE);
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    private void resetViewVisibility() {
        mSetWallpaperButton.setVisibility(View.VISIBLE);
//        mZoomBtn.setVisibility(View.VISIBLE);
//        mEdit.setVisibility(View.VISIBLE);
        View draw = ViewUtils.findViewById(this, R.id.preview_guide_draw_view);
        if (draw != null) {
            draw.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        int index = position;
        resetViewVisibility();
        mPaperIndex = index;
        mCurrentWallpaper = (WallpaperInfo) getWallpaperInfoByIndex(mPaperIndex);
        refreshButtonState();
        refreshButtonTint();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wallpaper_view_return:
                this.finish();
                break;
            case R.id.set_wallpaper_button:
                if (mCurrentWallpaper == null) {
                    return;
                }
                KCAnalytics.logEvent("app_wallpaper_setwallpaper_clicked", "wallpaperName", mCurrentWallpaper.getName());
                boolean isWallpaperReady = isSucceed() && !isSettingWallpaper();
                if (!isWallpaperReady) {
                    ToastUtils.showToast(R.string.online_wallpaper_loading);
                    return;
                }

                if (LockerSettings.isLockerMuted()) {
                    KCAnalytics.logEvent("app_wallpaper_setwallpaper_homescreen_clicked", "wallpaperName", mCurrentWallpaper.getName());
                    setHomeScreenWallpaper();
                } else {
                    showSetWallpaperDialog();
                }
//                mSetSelectPopWin.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM, 0, 0);
                break;
            case R.id.set_key_theme_button:
                if (mCurrentWallpaper == null) {
                    return;
                }
                KCAnalytics.logEvent("app_wallpaper_setkeytheme_clicked", "wallpaperName", mCurrentWallpaper.getName());
                if (!isSucceed() || isSettingWallpaper()) {
                    ToastUtils.showToast(R.string.online_wallpaper_loading);
                    return;
                }
                mDialog = ProgressDialog.createDialog(WallpaperPreviewActivity.this, getString(R.string.key_theme_loading_progress_dialog_text));
                mDialog.setCancelable(true);
                KCCommonUtils.showDialog(mDialog);
                GetFilePathTask getFilePathTask = new GetFilePathTask();
                getFilePathTask.execute(mCurrentWallpaper.getWallpaperUrl());
                break;
            case R.id.set_home_screen:
                KCAnalytics.logEvent("app_wallpaper_setwallpaper_homescreen_clicked", "wallpaperName", mCurrentWallpaper.getName());
                setHomeScreenWallpaper();
                break;
            case R.id.set_locker_screen:
                KCAnalytics.logEvent("app_wallpaper_setwallpaper_lockscreen_clicked", "wallpaperName", mCurrentWallpaper.getName());
                hideSetWallpaperSelectDialog();
                setLockerScreenWallpaper();
                ToastUtils.showToast(R.string.interstitial_ad_title_after_try_keyboard);
                finish();
                break;
            case R.id.set_home_and_locker_screen:
                KCAnalytics.logEvent("app_wallpaper_setwallpaper_bothscreen_clicked", "wallpaperName", mCurrentWallpaper.getName());
                setLockerScreenWallpaper();
                setHomeScreenWallpaper();
                break;
            default:
                if (setWallpaperDialog.getVisibility() == View.VISIBLE) {
                    hideSetWallpaperSelectDialog();
                }
        }

    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    protected boolean isSettingKeyTheme() {
//        return mIsSettingKeyTheme;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    private void setHomeScreenWallpaper() {
        mSetWallpaperButton.setTextColor(0x80ffffff);
        mSetWallpaperButton.setClickable(false);
        mCurrentWallpaper.setEdit("");
        mCurrentWallpaper.setApplied(true);
        if (mCurrentWallpaper.getCategory() == null) {
            mCurrentWallpaper.setCategory(mCategoryInfo);
        }
        applyWallpaper(mCurrentWallpaper.getType() != WallpaperInfo.WALLPAPER_TYPE_GALLERY, false);
    }

    private void setLockerScreenWallpaper() {
        LockerSettings.setLockerEnabled(true);
        LockerSettings.setLockerBgUrl(mCurrentWallpaper.getWallpaperUrl());
    }

    @Override
    public void onRetryButtonPressed(PreviewViewPage page) {
        int index = (int) (page.getTag());
        displayPage(index, page);
    }

    protected void refreshButtonState() {

    }

    @Override
    protected Bitmap tryGetWallpaperToSet() {
        if (mCurrentWallpaper == null) {
            return null;
        }
        Bitmap wallpaper = null;
        switch (mCurrentWallpaper.getType()) {
            case WallpaperInfo.WALLPAPER_TYPE_BUILT_IN:
                wallpaper = BitmapFactory.decodeResource(getResources(), mCurrentWallpaper.getBuiltInDrawableId());
                break;
            case WallpaperInfo.WALLPAPER_TYPE_ONLINE:
            case WallpaperInfo.WALLPAPER_TYPE_LUCKY:
                for (int i = 0; i < mViewPager.getChildCount(); i++) {
                    if (!(mViewPager.getChildAt(i) instanceof PreviewViewPage)) {
                        continue;
                    }
                    PreviewViewPage page = (PreviewViewPage) mViewPager.getChildAt(i);
                    if ((int) page.getTag() == mPaperIndex) {
                        BitmapDrawable drawable = ((BitmapDrawable) page.largeWallpaperImageView.getDrawable());
                        if (drawable != null) {
                            wallpaper = drawable.getBitmap();
                        }
                        break;
                    }
                }
                break;
            case WallpaperInfo.WALLPAPER_TYPE_GALLERY:
                for (int i = 0; i < mViewPager.getChildCount(); i++) {
                    PreviewViewPage page = (PreviewViewPage) mViewPager.getChildAt(i);
                    if ((int) page.getTag() == mPaperIndex) {
                        wallpaper = ((BitmapDrawable) page.largeWallpaperImageView.getDrawable()).getBitmap();
                        wallpaper = WallpaperUtils.centerInside(wallpaper);
                        break;
                    }
                }
                break;
        }
        return wallpaper;
    }

    @Override
    protected WallpaperInfo getCurrentWallpaper() {
        return mCurrentWallpaper;
    }

    private void refreshButtonTint() {
        boolean textLight = mCurrentWallpaper.isTextLight();
        onTintChanged(textLight);
    }

    private Object getWallpaperInfoByIndex(int index) {
        if (mWallpapers.size() == 0) {
            return null;
        } else if (index < 0) {
            return mWallpapers.get(0);
        } else if (index >= mWallpapers.size()) {
            return mWallpapers.get(mWallpapers.size() - 1);
        } else {
            return mWallpapers.get(index);
        }
    }

    private void displayPage(int index, PreviewViewPage page) {
        final ImageView imageView = page.largeWallpaperImageView;
        page.retryLayout.setVisibility(View.INVISIBLE);
        final WallpaperInfo info = (WallpaperInfo) getWallpaperInfoByIndex(index);
        Uri uri = null;
        String thumbUrl = null;
        switch (info.getType()) {
            case WallpaperInfo.WALLPAPER_TYPE_BUILT_IN:
                if (DEBUG) HSLog.d(TAG, "Display built-in wallpaper: " + index);
                page.loadingView.setVisibility(View.INVISIBLE);
                uri = Uri.parse("android.resource://com.honeycomb.launcher/" + info.getBuiltInDrawableId());
                break;
            case WallpaperInfo.WALLPAPER_TYPE_ONLINE:
                uri = Uri.parse(info.getWallpaperUrl());
                thumbUrl = info.getThumbnailUrl();
                break;
            case WallpaperInfo.WALLPAPER_TYPE_GALLERY:
                File file = new File(info.getPath());
                uri = Uri.fromFile(file);
                break;
            case WallpaperInfo.WALLPAPER_TYPE_LUCKY:
                File luckyFile = new File(info.getPath());
                if (luckyFile.exists()) {
                    uri = Uri.fromFile(luckyFile);
                } else {
                    uri = Uri.parse(info.getWallpaperUrl());
                }
                break;
        }
        if (uri == null) {
            return;
        }

        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA);

        RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                imageView.setImageDrawable(resource);
                PreviewViewPage page = (PreviewViewPage) imageView.getTag();
                page.width = resource.getIntrinsicWidth();
                page.height = resource.getIntrinsicHeight();
                imageView.setImageMatrix(WallpaperUtils.centerCrop(page.width, page.height,
                        imageView));

                return true;
            }
        };

        RequestBuilder<Drawable> requestBuilder = Glide.with(this).load(uri).apply(requestOptions)
                .thumbnail(Glide.with(this).load(thumbUrl).listener(requestListener));
        requestBuilder.into(new CustomImageLoadingTarget(imageView));
    }

    private boolean isSucceed() {
        return mLoadMap.get(mPaperIndex);
    }

    private void onTintChanged(boolean textLight) {
//        if (!textLight) {
//            mReturnArrow.setBackgroundResource(R.drawable.moment_round_material_compat_dark);
//            mEdit.setBackgroundResource(R.drawable.moment_round_material_compat_dark);
//            mZoomBtn.setBackgroundResource(R.drawable.moment_round_material_compat_dark);
//        } else {
//            mReturnArrow.setBackgroundResource(R.drawable.moment_round_material_compat);
//            mEdit.setBackgroundResource(R.drawable.moment_round_material_compat);
//            mZoomBtn.setBackgroundResource(R.drawable.moment_round_material_compat);
//        }
    }

    private PreviewViewPage getPreviewPage(ViewGroup view, int paperIndex) {
        PreviewViewPage page = (PreviewViewPage) this.getLayoutInflater().inflate(R.layout.item_wallpaper_page, view, false);
        page.setOnClickListener(this);
        page.setListener(this);
        page.setTag(paperIndex);
        page.largeWallpaperImageView.setTag(page);
        return page;
    }


    public class PreviewViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            if (null == mWallpapers) {
                return 0;
            }
            return mWallpapers.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            int index = position;

            mMaxVisiblePosition = Math.max(mMaxVisiblePosition, index);

            PreviewViewPage page = getPreviewPage(view, index);
            displayPage(index, page);
            view.addView(page, 0);
            return page;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }
    }

    //TODO Change to static class
    private class GetFilePathTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                File file = Glide.with(WallpaperPreviewActivity.this)
                        .download(params[0]).submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                return file.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mDialog != null) {
                KCCommonUtils.dismissDialog(mDialog);
            }
            if (result == null) {
                ToastUtils.showToast(getString(R.string.access_set_up_failed));
            }
            final Resources res = getResources();
            final int keyboardWidth = HSResourceUtils.getDefaultKeyboardWidth(res);
            final int keyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(res);
            Intent intent = new Intent(WallpaperPreviewActivity.this, CustomThemeBackgroundCropperActivity.class);
            intent.putExtra("fromWallpaper", TAG);
            intent.putExtra(CopperImagePath, result);
            intent.putExtra(KeyboardWidth, keyboardWidth);
            intent.putExtra(KeyboardHeight, keyboardHeight);
            startActivity(intent);
        }
    }

    private class CustomImageLoadingTarget extends ImageViewTarget<Drawable> {

        public CustomImageLoadingTarget(ImageView view) {
            super(view);
        }

        @Override
        public void onLoadStarted(Drawable placeholder) {
            super.onLoadStarted(placeholder);
            if (view == null) {
                return;
            }
            PreviewViewPage page = (PreviewViewPage) view.getTag();
            page.loadingView.setVisibility(View.VISIBLE);
            page.retryLayout.setVisibility(View.INVISIBLE);
            mLoadMap.put((int) (page.getTag()), false);
        }

        @Override
        public void onLoadFailed(Drawable errorDrawable) {
            super.onLoadFailed(errorDrawable);
            if (view == null) {
                return;
            }
            final PreviewViewPage page = (PreviewViewPage) view.getTag();
            mLoadMap.put((int) (page.getTag()), false);
            page.postDelayed(new Runnable() {
                @Override
                public void run() {
                    page.loadingView.setVisibility(View.INVISIBLE);
                    page.retryLayout.setVisibility(View.VISIBLE);
                    page.largeWallpaperImageView.setImageResource(android.R.color.transparent);
                    mSetWallpaperButton.setVisibility(View.INVISIBLE);
                }
            }, 600);
        }

        @Override
        public void onResourceReady(Drawable resource, @Nullable Transition<? super Drawable> transition) {
            super.onResourceReady(resource, transition);
            if (view == null) {
                return;
            }
            view.setImageDrawable(resource);
            PreviewViewPage page = (PreviewViewPage) view.getTag();
            page.width = resource.getIntrinsicWidth();
            page.height = resource.getIntrinsicHeight();

            WallpaperInfo info = (WallpaperInfo) getWallpaperInfoByIndex((int) (page.getTag()));
//            info.setTextLight(WallpaperUtils.textColorLightForWallPaper(bitmap));

            view.setImageMatrix(WallpaperUtils.centerCrop(page.width, page.height, view));

            mLoadMap.put((int) (page.getTag()), true);
            refreshButtonState();
            page.loadingView.setVisibility(View.INVISIBLE);
            page.retryLayout.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void setResource(@Nullable Drawable resource) {

        }

        @Override
        public void setRequest(Request request) {
            view.setTag(R.id.glide_tag_id, request);
        }

        @Override
        public Request getRequest() {
            return (Request) view.getTag(R.id.glide_tag_id);
        }
    }
}
