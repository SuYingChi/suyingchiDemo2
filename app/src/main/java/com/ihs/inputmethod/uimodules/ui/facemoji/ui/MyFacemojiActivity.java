package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.managers.HSPictureManager;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.utils.DisplayUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MyFacemojiActivity extends HSAppCompatActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener, View.OnClickListener {

    private TabHost mTabHost;
    private ViewPager mImagePager;
    private FacemojiPalettesAdapter mFacemojiPalettesAdapter;
    private int mCurrentPagerPosition = 0;
    private Drawable transparentDrawable;
    private int screenWidth;
    private int screenHeight;
    private ImageView top_bar_bg;
    private RelativeLayout navigation_bar;
    private ImageView back_button;
    private ImageView face_icon;
    private ImageView triangle;
    private View mSaveButtonHolder;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if  (id == R.id.face_menu_icon) {
                startFaceListActivity();
        } else if (id == R.id.back_button_holder) {
                onBackPressed();
        } else if (id == R.id.triangle_button) {
        } else if (id ==  R.id.face_save_btn_holder) {
            onClickSaveButton();
        }

    }

    private void onClickSaveButton() {
        try {
            // Save face
            saveFaceToSDCard();

            // Destroy temp face
            FacemojiManager.destroyTempFace();

            // Finish Camera activity
            HSGlobalNotificationCenter.sendNotificationOnMainThread(CameraActivity.FACEMOJI_SAVED);

            // Finish self
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface PagerCallback {
        public int getCurrentPagerPosition();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.my_facemoji_activity);

        screenWidth = DisplayUtils.getScreenWidthForContent();
        screenHeight = DisplayUtils.getScreenHeightForContent() - DisplayUtils.getStatusBarHeight(getWindow());
        transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        mTabHost = (TabHost) findViewById(R.id.facemoji_category_tabhost);
        mTabHost.setup();
        for (int i = 0; i < FacemojiManager.getCategories().size(); i++) {
            addTab(mTabHost, i);
        }
        mTabHost.setOnTabChangedListener(this);
        TabWidget tabWidget = mTabHost.getTabWidget();
        tabWidget.setStripEnabled(false);
        mImagePager = (ViewPager) findViewById(R.id.facemoji_pager);
        mFacemojiPalettesAdapter = new FacemojiPalettesAdapter(getGridViewHeight(), new PagerCallback() {
            @Override
            public int getCurrentPagerPosition() {
                return mCurrentPagerPosition;
            }
        });


        mImagePager.setAdapter(mFacemojiPalettesAdapter);
        mImagePager.addOnPageChangeListener(this);
        mImagePager.setOffscreenPageLimit(0);
        mImagePager.setPersistentDrawingCache(ViewGroup.PERSISTENT_NO_CACHE);

        setCurrentCategoryId(FacemojiManager.getCurrentCategoryId());

        navigation_bar = (RelativeLayout) findViewById(R.id.navigation_bar);
        LinearLayout.LayoutParams navigation_bar_param = (LinearLayout.LayoutParams) navigation_bar.getLayoutParams();
        navigation_bar_param.height = getNavigateBarHeight();
        navigation_bar.setLayoutParams(navigation_bar_param);

        LinearLayout.LayoutParams tab_host_param = (LinearLayout.LayoutParams) mTabHost.getLayoutParams();
        tab_host_param.height = getTabBarHeight();
        mTabHost.setLayoutParams(tab_host_param);

        back_button = (ImageView) findViewById(R.id.back_button);
        LinearLayout.LayoutParams back_param = (LinearLayout.LayoutParams) back_button.getLayoutParams();
        back_param.height = (int) (getResources().getDrawable(R.drawable.back_button).getIntrinsicHeight() * 0.8);
        back_param.width = (int) (getResources().getDrawable(R.drawable.back_button).getIntrinsicWidth() * 0.8);
        back_button.setLayoutParams(back_param);

        View backButtonHolder = findViewById(R.id.back_button_holder);
        RelativeLayout.LayoutParams hoderPara = (RelativeLayout.LayoutParams) backButtonHolder.getLayoutParams();
        hoderPara.width = (int) (back_param.width * 2.5f);
        hoderPara.height = getTabBarHeight();
        backButtonHolder.setLayoutParams(hoderPara);
        backButtonHolder.setOnClickListener(this);

        face_icon = (ImageView) findViewById(R.id.face_menu_icon);
        RelativeLayout.LayoutParams face_param = (RelativeLayout.LayoutParams) face_icon.getLayoutParams();
        face_param.height = (int) (getNavigateBarHeight() * 0.7);
        face_param.width = face_param.height;
        face_icon.setLayoutParams(face_param);
        face_icon.setOnClickListener(this);

        triangle = (ImageView) findViewById(R.id.triangle_button);
        triangle.setOnClickListener(this);
        triangle.setClickable(true);
        Bitmap bitmap = BitmapFactory.decodeResource(HSApplication.getContext().getResources(),  R.drawable.facemoji_triangle);
        triangle.setBackgroundDrawable(HSDrawableUtils.getDimmedForegroundDrawable(bitmap));

        face_icon.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int[] state = face_icon.getBackground().getState();
                triangle.getBackground().setState(state);
                return false;
            }
        });

        mSaveButtonHolder = findViewById(R.id.face_save_btn_holder);
        RelativeLayout.LayoutParams saveHolderPara = (RelativeLayout.LayoutParams) mSaveButtonHolder.getLayoutParams();
        saveHolderPara.height = getNavigateBarHeight();
        mSaveButtonHolder.setLayoutParams(saveHolderPara);
        mSaveButtonHolder.setOnClickListener(this);

        if (FacemojiManager.isUsingTempFace()) {
            switchToNewState();
        } else {
            if (null == FacemojiManager.getDefaultFacePicUri()) {
                finish();
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (FacemojiManager.hasTempFace()) {
            // Enable using temp face flag
            FacemojiManager.setUsingTempFace(true);
            switchToNewState();
        } else {
            if (null == FacemojiManager.getDefaultFacePicUri()) {
                finish();
                return;
            }

            Drawable drawable = HSDrawableUtils.getDimmedForegroundDrawable(ImageLoader.getInstance().loadImageSync(FacemojiManager.getDefaultFacePicUri().toString(), new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
                    .postProcessor(new BitmapAddBorderProcessor(Color.WHITE)).build()));
            face_icon.setBackgroundDrawable(drawable);
        }
    }

    @Override
    protected void onStop() {
        // NOT use temp face if we quit
        FacemojiManager.setUsingTempFace(false);
        if (mFacemojiPalettesAdapter != null) {
            mFacemojiPalettesAdapter.finish();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Destroy temp face
        if (FacemojiManager.hasTempFace()) {
            FacemojiManager.destroyTempFace();
        }
    }

    @Override
    public void onBackPressed() {
//        mFacemojiPalettesAdapter.finish();
//        mFacemojiPalettesAdapter = null;
//        mImagePager.removeAllViews();
//        mImagePager.setAdapter(null);
//        mImagePager = null;
//        transparentDrawable = null;
        super.onBackPressed();
    }

    private void addTab(TabHost host, int categoryId) {
        String tabId = FacemojiManager.getCategoryName(categoryId);
        TabHost.TabSpec tspec = host.newTabSpec(tabId);
        tspec.setContent(R.id.facemoji_dummy);
        View v = LayoutInflater.from(this).inflate(R.layout.facemoji_tab_icon_app, null);
        ImageView iconView = (ImageView) v.findViewById(R.id.facemoji_tab_host_icon_app);
        LinearLayout.LayoutParams iconParam = (LinearLayout.LayoutParams) iconView.getLayoutParams();
        iconParam.height = (int) getResources().getDimension(R.dimen.facemoji_category_bar_height);
        Drawable bg = getResources().getDrawable(R.drawable.facemoji_bar_black);
        iconParam.width = iconParam.height * bg.getIntrinsicWidth() / bg.getIntrinsicHeight();
        int margin = (int) getResources().getDimension(R.dimen.facemoji_category_bar_icon_margin);
        iconParam.setMargins(0, margin, 0, margin);
        iconView.setLayoutParams(iconParam);
        iconView.setImageDrawable(FacemojiManager.getCategories().get(categoryId).getCategoryIcon());
        iconView.setBackgroundDrawable(getTabbarCategoryIconBackground());
        tspec.setIndicator(v);
        host.addTab(tspec);
    }

    private void setCurrentCategoryId(int categoryId) {

        int oldCategoryId = FacemojiManager.getCurrentCategoryId();
        if (oldCategoryId == categoryId) {
            return;
        }

        mImagePager.setCurrentItem(categoryId, true /* smoothScroll */);
        mTabHost.setCurrentTab(categoryId);
        FacemojiManager.setCurrentCategoryId(categoryId);
    }


    @Override
    public void onTabChanged(String tabId) {
        int categoryId = FacemojiManager.getCategoryPosition(tabId);
        setCurrentCategoryId(categoryId);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setCurrentCategoryId(position);
        FacemojiManager.setCurrentCategoryId(position);
        mCurrentPagerPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return 0;
    }

    private int getTabbarIconWidth() {
        return screenWidth - 20 * 2 - 30 * 4;
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

    private void saveFaceToSDCard() {
        HSLog.d("");

        // Disable save button to avoid multi-click
        mSaveButtonHolder.setEnabled(false);

        // Save to sd card
        String tempFaceFilePath = CameraActivity.getTempFaceFilePath();
        String faceFilePath = HSPictureManager.getFaceDirectory() + generateFileName();
        File srcFile = new File(tempFaceFilePath);
        File dstFile = new File(faceFilePath);
        HSFileUtils.copyFile(srcFile, dstFile);

        // Reload faces collection
        FacemojiManager.loadFaceList();
        FacemojiManager.setCurrentFacePicUri(Uri.fromFile(dstFile));
    }

    private void switchToNewState() {
        // Switch UI
        face_icon.setVisibility(View.GONE);
        triangle.setVisibility(View.GONE);
        mSaveButtonHolder.setVisibility(View.VISIBLE);
    }
}
