package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiDownloadManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiCategory;
import com.ihs.inputmethod.uimodules.utils.DisplayUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MyFacemojiActivity extends HSAppCompatActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener, View.OnClickListener {

    private TabHost mTabHost;
    private ViewPager mImagePager;
    private FacemojiPalettesAdapter mFacemojiPalettesAdapter;
    private List<FacemojiCategory> categories;
    private int mCurrentPagerPosition = 0;
    private Drawable transparentDrawable;
    private int screenWidth;
    private int screenHeight;
    private ImageView face_icon;
    private ImageView triangle;

    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (FacemojiDownloadManager.FACEMOJI_CATEGORY_DOWNLOADED.equals(s)){
                if (hsBundle != null){
                    FacemojiCategory facemojiCategory = (FacemojiCategory) hsBundle.getObject(FacemojiDownloadManager.FACEMOJI_CATEGORY_BUNDLE_KEY);
                    for (int i = 0; i < categories.size(); i++) {
                        FacemojiCategory category = categories.get(i);
                        if (category.getName().equals(facemojiCategory.getName())) {
                            mFacemojiPalettesAdapter.notifyDownloaded(i, mCurrentPagerPosition);
                            break;
                        }
                    }
                }
            }else if (FacemojiManager.FACEMOJI_DATA_CHANGED.equals(s)) {
                if (mFacemojiPalettesAdapter != null) {
                    categories = FacemojiManager.getInstance().getCategories();
                    mFacemojiPalettesAdapter.setData(categories);
                    mFacemojiPalettesAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.face_menu_icon) {
            startFaceListActivity();
        }
    }

    public interface PagerCallback {
        int getCurrentPagerPosition();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.my_facemoji_activity);
        categories = FacemojiManager.getInstance().getCategories();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.my_facemoji_toolbar_title));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        screenWidth = DisplayUtils.getScreenWidthForContent();
        screenHeight = DisplayUtils.getScreenHeightForContent() - DisplayUtils.getStatusBarHeight(getWindow());
        transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        mTabHost = findViewById(R.id.facemoji_category_tabhost);
        mTabHost.setup();

        for (int i = 0; i < FacemojiManager.getInstance().getFacemojiCategories().size(); i++) {
            addTab(mTabHost, i);
        }
        mTabHost.setOnTabChangedListener(this);
        TabWidget tabWidget = mTabHost.getTabWidget();
        tabWidget.setStripEnabled(false);
        mImagePager = findViewById(R.id.facemoji_pager);
        mFacemojiPalettesAdapter = new FacemojiPalettesAdapter(this, categories,getGridViewHeight(), new PagerCallback() {
            @Override
            public int getCurrentPagerPosition() {
                return mCurrentPagerPosition;
            }
        });

        LinearLayout sendFacemojiTipView = findViewById(R.id.tip_layout);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(DisplayUtils.dip2px(HSApplication.getContext(),7));
        shape.setColor(0x55888888);
        sendFacemojiTipView.setBackgroundDrawable(shape);

        mImagePager.setAdapter(mFacemojiPalettesAdapter);
        mImagePager.addOnPageChangeListener(this);
        mImagePager.setOffscreenPageLimit(0);
        mImagePager.setPersistentDrawingCache(ViewGroup.PERSISTENT_NO_CACHE);

        showInitTabByCategoryName();

        LinearLayout.LayoutParams tab_host_param = (LinearLayout.LayoutParams) mTabHost.getLayoutParams();
        tab_host_param.height = getTabBarHeight();
        mTabHost.setLayoutParams(tab_host_param);

        face_icon = findViewById(R.id.face_menu_icon);
        ViewGroup.LayoutParams face_param = face_icon.getLayoutParams();
        face_param.height = (int) (getNavigateBarHeight() * 0.6);
        face_param.width = face_param.height;
        face_icon.setLayoutParams(face_param);
        face_icon.setOnClickListener(this);

        triangle = findViewById(R.id.triangle_button);
        triangle.setOnClickListener(this);
        triangle.setClickable(true);
        Bitmap bitmap = BitmapFactory.decodeResource(HSApplication.getContext().getResources(), R.drawable.facemoji_triangle);
        triangle.setBackgroundDrawable(HSDrawableUtils.getDimmedForegroundDrawable(bitmap));

        face_icon.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Drawable background = face_icon.getBackground();
                if (background != null) {
                    int[] state = background.getState();
                    triangle.getBackground().setState(state);
                }
                return false;
            }
        });

        if (!FacemojiManager.isUsingTempFace() && null == FacemojiManager.getDefaultFacePicUri()) {
            finish();
            return;
        }

        KeyboardFullScreenAd.showSessionOneTimeAd("Facemoji");
        HSGlobalNotificationCenter.addObserver(FacemojiDownloadManager.FACEMOJI_CATEGORY_DOWNLOADED,notificationObserver);
        HSGlobalNotificationCenter.addObserver(FacemojiManager.FACEMOJI_DATA_CHANGED,notificationObserver);
    }

    private void showInitTabByCategoryName() {
        String initTabCategory = getIntent().getStringExtra(FacemojiManager.INIT_SHOW_TAB_CATEGORY);
        int initPosition = 0;
        if (!TextUtils.isEmpty(initTabCategory)) {
            for (int i = 0; i < FacemojiManager.getInstance().getFacemojiCategories().size(); i++) {
                if (initTabCategory.equals(FacemojiManager.getInstance().getFacemojiCategories().get(i).getName())) {
                    initPosition = i;
                    break;
                }
            }
        }
        setCurrentCategoryId(initPosition);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mFacemojiPalettesAdapter != null) {
            mFacemojiPalettesAdapter.startAnim(mCurrentPagerPosition);
        }
        if (null == FacemojiManager.getDefaultFacePicUri()) {
            finish();
            return;
        }

        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(FacemojiManager.getDefaultFacePicUri().toString(), new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
                .postProcessor(new BitmapAddBorderProcessor(Color.WHITE)).build());
        if (bitmap != null) {
            Drawable drawable = HSDrawableUtils.getDimmedForegroundDrawable(bitmap);
            face_icon.setBackgroundDrawable(drawable);
        }

    }

    @Override
    protected void onStop() {
        // NOT use temp face if we quit
        FacemojiManager.setUsingTempFace(false);
        if (mFacemojiPalettesAdapter != null) {
            mFacemojiPalettesAdapter.stopAllAnimations();
            mFacemojiPalettesAdapter.finish();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(notificationObserver);
        super.onDestroy();

        // Destroy temp face
        if (FacemojiManager.hasTempFace()) {
            FacemojiManager.destroyTempFace();
        }
    }

    private void addTab(TabHost host, int categoryId) {
        String tabId = FacemojiManager.getInstance().getCategoryName(categoryId);
        TabHost.TabSpec tspec = host.newTabSpec(tabId);
        tspec.setContent(R.id.facemoji_dummy);
        View v = LayoutInflater.from(this).inflate(R.layout.facemoji_tab_icon_app, null);
        ImageView iconView = v.findViewById(R.id.facemoji_tab_host_icon_app);
        LinearLayout.LayoutParams iconParam = (LinearLayout.LayoutParams) iconView.getLayoutParams();
        iconParam.height = (int) getResources().getDimension(R.dimen.facemoji_category_bar_height);
        Drawable bg = getResources().getDrawable(R.drawable.facemoji_bar_black);
        iconParam.width = iconParam.height * bg.getIntrinsicWidth() / bg.getIntrinsicHeight();
        int margin = (int) getResources().getDimension(R.dimen.facemoji_category_bar_icon_margin);
        iconParam.setMargins(0, margin, 0, margin);
        iconView.setLayoutParams(iconParam);
        FacemojiCategory facemojiCategory = FacemojiManager.getInstance().getFacemojiCategories().get(categoryId);

        int padding = (iconParam.height - DisplayUtils.dip2px(this,24)) / 2;
        iconView.setPadding(padding,padding,padding,padding);
        Drawable drawable = HSApplication.getContext().getResources().getDrawable(R.drawable.ic_sticker_loading_image_grey);
        iconView.setImageDrawable(drawable);
        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA);
        Glide.with(this).asBitmap().apply(requestOptions).load(FacemojiDownloadManager.getInstance().getRemoteTabIconPath(facemojiCategory.getName())).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                iconView.setPadding(0, 0, 0, 0);
                return false;
            }
        }).into(iconView);
        iconView.setBackgroundDrawable(getTabbarCategoryIconBackground());
        tspec.setIndicator(v);
        host.addTab(tspec);
    }

    private void setCurrentCategoryId(int categoryId) {

        int oldCategoryId = FacemojiManager.getInstance().getCurrentCategoryId();
        if (oldCategoryId == categoryId) {
            return;
        }

        mImagePager.setCurrentItem(categoryId, true /* smoothScroll */);
        mTabHost.setCurrentTab(categoryId);
        FacemojiManager.getInstance().setCurrentCategoryId(categoryId);
    }


    @Override
    public void onTabChanged(String tabId) {
        int categoryId = FacemojiManager.getInstance().getCategoryPosition(tabId);
        setCurrentCategoryId(categoryId);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset == 0) {
            mFacemojiPalettesAdapter.stopAllAnimations();
            mFacemojiPalettesAdapter.startAnim(position);
        }
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentCategoryId(position);
        FacemojiManager.getInstance().setCurrentCategoryId(position);

        if (!FacemojiDownloadManager.isFacemojiCategoryDownloadedSuccess(categories.get(position).getName())) {
            FacemojiDownloadManager.getInstance().startDownloadFacemojiResource(categories.get(position),null);
        }
        mCurrentPagerPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private int getGridViewHeight() {
        return (int) (screenHeight * 0.68);
    }

    private int getNavigateBarHeight() {
        return (int) (screenHeight * 0.10);
    }

    private int getTabBarHeight() {
        return getResources().getDimensionPixelSize(R.dimen.facemoji_category_bar_height) + 2 * (int) getResources().getDimension(R.dimen.facemoji_category_bar_icon_margin);
    }

    private int getTopBarHeight() {
        return (int) (screenHeight * 0.06);
    }

    public Drawable getTabbarCategoryIconBackground() {
        StateListDrawable tabbarCategoryStatesDrawable = new StateListDrawable();
        Drawable defaultbg = getResources().getDrawable(R.drawable.facemoji_bar_black);
        tabbarCategoryStatesDrawable.addState(new int[]{android.R.attr.state_focused}, getResources().getDrawable(R.drawable.facemoji_bar_black));
        tabbarCategoryStatesDrawable.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.facemoji_bar_black));
        tabbarCategoryStatesDrawable.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(R.drawable.facemoji_bar_black));
        tabbarCategoryStatesDrawable.addState(new int[]{}, transparentDrawable);
        return tabbarCategoryStatesDrawable;
    }

    /**
     * 生成时间命名的图片名称
     *
     * @return
     */
    private String generateFileName() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
        return format.format(date) + ".png";
    }

    private void startFaceListActivity() {
        Intent i = new Intent(MyFacemojiActivity.this, FaceListActivity.class);
        startActivity(i);
    }
}
