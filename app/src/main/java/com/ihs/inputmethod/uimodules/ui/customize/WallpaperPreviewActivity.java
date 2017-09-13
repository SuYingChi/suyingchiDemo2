package com.ihs.inputmethod.uimodules.ui.customize;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.acb.adadapter.AcbNativeAd;
import com.acb.nativeads.AcbNativeAdLoader;
import com.artw.lockscreen.LockerSettings;
import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.feature.common.CommonUtils;
import com.ihs.inputmethod.feature.common.Thunk;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.uimodules.BuildConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.item.WallpaperPackageInfo;
import com.ihs.inputmethod.uimodules.ui.customize.util.WallpaperUtils;
import com.ihs.inputmethod.uimodules.ui.customize.view.CategoryInfo;
import com.ihs.inputmethod.uimodules.ui.customize.view.PreviewViewPage;
import com.ihs.inputmethod.uimodules.ui.customize.view.ProgressDialog;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeBackgroundCropperActivity;
import com.ihs.keyboardutils.utils.ToastUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

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

    public static final String INTENT_KEY_SCENARIO = "scenario";
    public static final String INTENT_KEY_CATEGORY = "category";
    public static final String INTENT_KEY_WALLPAPERS = "wallpapers";
    public static final String INTENT_KEY_INDEX = "index";
    public static final String INTENT_KEY_WALLPAPER_DATA = "wallpaperData";
    public static final String INTENT_KEY_RANDOM_WALLPAPERS_DATA = "randomWallpapers";
    public static final String INTENT_KEY_WALLPAPER_PACKAGE_INFO = "wallpaperPackageInfo";
    @SuppressWarnings("PointlessBooleanExpression")
    private static final String PREF_KEY_PREVIEW_GUIDE_SHOWN = "wallpaper_preview_guide_shown";
    private static final String PREF_KEY_PREVIEW_WALLPAPER_SHOWN_MODE = "pref_key_preview_wallpaper_shown_mode";

    private final static String FULL_SCREEN = "FULL_SCREEN";
    private final static String FULL_IMAGE = "FULL_IMAGE";

    private static final int MODE_GALLERY_WALLPAPER = 0;
    private static final int MODE_LOCAL_WALLPAPER = 1;

    private final static int TOP_MARGIN = CommonUtils.pxFromDp(15);

    private boolean mInitialized;
    private boolean mIsGuide;
    private boolean mIsOnLineWallpaper;
    private boolean mIsFromHub;
    private int mPaperIndex;
    private int mWallpaperMode = MODE_LOCAL_WALLPAPER;
    float sumPositionAndPositionOffset;

    @Thunk
    ViewPager mViewPager;
    private List<Object> mWallpapers = new ArrayList<>();
    private WallpaperInfo mCurrentWallpaper;
    private TextView mSetWallpaperButton;
    private TextView mSetKeyThemeButton;
    private ProgressDialog mDialog;
    private View mEdit;
    private View mReturnArrow;
    private LinearLayout setWallpaperDialog;
    private LinearLayout setHomeScreen;
    private LinearLayout setLockerScreen;
    private LinearLayout setHomeAndLockerScreen;
    private PreviewViewPagerAdapter mAdapter;
    private SparseBooleanArray mLoadMap = new SparseBooleanArray();

    //selectZoomBtn true zoom_out 缩小 mIsCenterCrop state true
    //selectZoomBtn false zoom_in 放大 mIsCenterCrop state false
    private ImageView mZoomBtn;

    private CategoryInfo mCategoryInfo;
    private WallpaperPackageInfo mWallpaperPackageInfo;

    //ad related
    private static final int MAX_CONCURRENT_AD_REQUEST_COUNT = 3;
    private ArrayList<AcbNativeAd> mCandidateAds = new ArrayList<>();
    private boolean mShouldShowAds = false;
    private int mAdStep = 5;
    private int mCurrentRequestCount;
    private int mStartIndex = 0;
    private int mLastAdIndex = -1;
    private int mMaxVisiblePosition;
    private boolean mDestroying = false;
    private AcbNativeAdLoader mAdLoader;
    private ValueAnimator mPackageGuideLeftAnimator;
    private ValueAnimator mPackageGuideRightAnimator;
    private boolean mIsGuideInterrupted = false;

    public static Intent getLaunchIntent(Context context,
                                         CategoryInfo category, int position, Intent data) {
        Intent intent = new Intent(context, WallpaperPreviewActivity.class);
        intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_CATEGORY, category);
        intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_INDEX, position);
        intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_WALLPAPER_DATA, data);
        return intent;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
//        WallpaperMgr.getInstance().initLocalWallpapers(mService, null);
        if (!mInitialized) {
            initData();
            refreshButtonState();
        }
    }

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).build();

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
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDestroying = true;

        if (mPackageGuideLeftAnimator != null) mPackageGuideLeftAnimator.cancel();
        if (mPackageGuideRightAnimator != null) mPackageGuideRightAnimator.cancel();
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
        mSetWallpaperButton = (TextView) findViewById(R.id.set_wallpaper_button);
        mSetWallpaperButton.setOnClickListener(this);

        mSetKeyThemeButton = (TextView) findViewById(R.id.set_key_theme_button);
        mSetKeyThemeButton.setOnClickListener(this);

        setWallpaperDialog = (LinearLayout) findViewById(R.id.select_dialog_wallpaper);
        setWallpaperDialog.setOnClickListener(this);
        setWallpaperDialog.setVisibility(View.GONE);

        mViewPager = (ViewPager) findViewById(R.id.preview_view_pager);
        mAdapter = new PreviewViewPagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setFocusable(true);
        mViewPager.setClickable(true);
        mViewPager.setLongClickable(true);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(mPaperIndex, false);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && mPackageGuideLeftAnimator != null) {
                    mIsGuideInterrupted = true;
                }
                return false;
            }
        });
    }

    private void showSetWallpaperDialog() {
        setWallpaperDialog.setVisibility(View.VISIBLE);
        setHomeScreen = (LinearLayout) findViewById(R.id.set_home_screen);
        setHomeScreen.setOnClickListener(this);
        setLockerScreen = (LinearLayout) findViewById(R.id.set_locker_screen);
        setLockerScreen.setOnClickListener(this);
        setHomeAndLockerScreen = (LinearLayout) findViewById(R.id.set_home_and_locker_screen);
        setHomeAndLockerScreen.setOnClickListener(this);
    }

    private void hideViews() {
        mSetWallpaperButton.setVisibility(View.GONE);
        View draw = ViewUtils.findViewById(this, R.id.preview_guide_draw_view);
        if (draw != null) {
            draw.setVisibility(View.GONE);
        }
    }

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
                boolean isWallpaperReady = mCurrentWallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_BUILT_IN
                        || isSucceed() && !isSettingWallpaper();
                if (!isWallpaperReady) {
                    ToastUtils.showToast(R.string.online_wallpaper_loading);
                    return;
                }
                showSetWallpaperDialog();
                break;
            case R.id.set_key_theme_button:
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File file = Glide.with(WallpaperPreviewActivity.this)
                                    .load(mCurrentWallpaper.getWallpaperUrl())
                                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                    .get();
                            String path = file.getAbsolutePath();
                            final Resources res = getResources();
                            final int keyboardWidth = HSResourceUtils.getDefaultKeyboardWidth(res);
                            final int keyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(res);
                            Intent intent = new Intent(WallpaperPreviewActivity.this, CustomThemeBackgroundCropperActivity.class);
                            intent.putExtra("fromWallpaper", TAG);
                            intent.putExtra(CopperImagePath, path);
                            intent.putExtra(KeyboardWidth, keyboardWidth);
                            intent.putExtra(KeyboardHeight, keyboardHeight);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
                thread.start();
                break;
            case R.id.set_home_screen:
                setHomeScreenWallpaper();
                break;
            case R.id.set_locker_screen:
                setLockerScreenWallpaper();
                break;
            case R.id.set_home_and_locker_screen:
                setHomeScreenWallpaper();
                setLockerScreenWallpaper();
                break;

        }

    }

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
        LockerSettings.setLockerBgUrl(mCurrentWallpaper.getWallpaperUrl());
        setWallpaperDialog.setVisibility(View.GONE);
        mDialog = ProgressDialog.createDialog(this, getString(R.string.wallpaper_setting_progress_dialog_text));
        mDialog.show();
        mDialog.setCancelable(false);
    }

    @Override
    public void onRetryButtonPressed(PreviewViewPage page) {
        int index = (int) (page.getTag());
        displayPage(index, page);
    }

    @Override
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

        BitmapTypeRequest<String> thumbRequest = Glide.with(this).load(thumbUrl).asBitmap();
        thumbRequest.listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String s, Target<Bitmap> target, boolean b) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap bitmap, String s, Target<Bitmap> target, boolean b, boolean b1) {
                imageView.setImageBitmap(bitmap);
                PreviewViewPage page = (PreviewViewPage) imageView.getTag();
                page.width = bitmap.getWidth();
                page.height = bitmap.getHeight();

                imageView.setImageMatrix(WallpaperUtils.centerCrop(bitmap.getWidth(), bitmap.getHeight(),
                        imageView));

                return true;
            }
        }).diskCacheStrategy(DiskCacheStrategy.SOURCE);

        Glide.with(WallpaperPreviewActivity.this).load(uri).asBitmap()
                .thumbnail(thumbRequest).into(new CustomImageLoadingTarget(imageView));
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

    private class CustomImageLoadingTarget extends ImageViewTarget<Bitmap> {

        private boolean mShouldLogEvent;

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
            mShouldLogEvent = true;
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            super.onLoadFailed(e, errorDrawable);
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
            if (mShouldLogEvent) {
                mShouldLogEvent = false;
            }
        }

        @Override
        protected void setResource(Bitmap bitmap) {
            if (view == null) {
                return;
            }
            view.setImageBitmap(bitmap);
            PreviewViewPage page = (PreviewViewPage) view.getTag();
            page.width = bitmap.getWidth();
            page.height = bitmap.getHeight();

            WallpaperInfo info = (WallpaperInfo) getWallpaperInfoByIndex((int) (page.getTag()));
//            info.setTextLight(WallpaperUtils.textColorLightForWallPaper(bitmap));

            ((ImageView) view).setImageMatrix(WallpaperUtils.centerCrop(bitmap.getWidth(), bitmap.getHeight(), (ImageView) view));

            mLoadMap.put((int) (page.getTag()), true);
            refreshButtonState();
            page.loadingView.setVisibility(View.INVISIBLE);
            page.retryLayout.setVisibility(View.INVISIBLE);
            if (mShouldLogEvent) {
                mShouldLogEvent = false;
            }
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
