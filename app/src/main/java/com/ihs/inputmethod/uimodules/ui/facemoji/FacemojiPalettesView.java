package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.converts.SyncWorkHandler;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.ProgressListener;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity;
import com.ihs.inputmethod.uimodules.widget.KeyboardProgressView;

public class FacemojiPalettesView extends LinearLayout implements OnTabChangeListener, ViewPager.OnPageChangeListener,
        FacemojiPageGridView.OnFacemojiClickListener, View.OnClickListener {
    private FacemojiManager.FacemojiType facemojiType = FacemojiManager.FacemojiType.CLASSIC;
    private TabHost mTabHost;
    private FacemojiViewPager mViewPager;
    private ImageView currentFaceImage;

    private FacemojiLayoutParams mStickerLayoutParams;
    private FacemojiPalettesAdapter mStickerPalettesAdapter;

    private int mCurrentPagerPosition = 0;

    // Share progress
    private KeyboardProgressView mShareProgressView;
    private ProgressListener mProgressListener = new ProgressListener() {
        @Override
        public void startProgress() {
            mShareProgressView.start();
        }

        @Override
        public void stopProgress() {
            mShareProgressView.stop();
            mStickerPalettesAdapter.resumeAnimation();
        }
    };

    public FacemojiPalettesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FacemojiPalettesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Resources res = context.getResources();
        mStickerLayoutParams = new FacemojiLayoutParams(res);

        HSGlobalNotificationCenter.addObserver(CameraActivity.FACE_CHANGED,notificationObserver);
    }

    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (CameraActivity.FACE_CHANGED.equals(s)){
                if (currentFaceImage != null) {
                    currentFaceImage.setImageURI(FacemojiManager.getCurrentFacePicUri());
                }
            }
        }
    };


    private void addTab(TabHost host, int categoryId) {
        String tabId = FacemojiManager.getInstance().getCategoryName(facemojiType,categoryId);
        TabHost.TabSpec tspec = host.newTabSpec(tabId);
        tspec.setContent(R.id.facemoji_keyboard_dummy);
        ImageView iconView = new ImageView(HSApplication.getContext());
        int height = (int) (getResources().getDimension(R.dimen.config_suggestions_strip_height) - getResources().getDimension(R.dimen.facemoji_panel_tab_margin_top) * 2);
        LayoutParams iconParam = new LayoutParams(height, height);
        iconParam.gravity = Gravity.LEFT;
        iconParam.setMargins(10, 5, 10, 5);
        iconView.setLayoutParams(iconParam);
        iconView.setPadding(10, 10, 10, 10);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iconView.setImageDrawable(FacemojiManager.getInstance().getCategories(facemojiType).get(categoryId).getCategoryIcon());
        iconView.setBackgroundResource(R.drawable.facemoji_tab_bg);
        tspec.setIndicator(iconView);
        host.addTab(tspec);
    }

    private void clear() {
        if (mShareProgressView != null && mShareProgressView.isShowing()) {
            mShareProgressView.stop();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


    public void onPanelShow() {
        if (FacemojiManager.getDefaultFacePicUri() == null) {
            removeAllViews();
            initDefualtFacemojiPanel();
            mStickerPalettesAdapter = null;
            return;
        } else {
            if (!SyncWorkHandler.getInstance().isRunning()) {
                clear();
            }
            if (mStickerPalettesAdapter != null) {
                return;
            }
            removeAllViews();
            LinearLayout panelView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.facemoji_palette_layout, null);

            mTabHost = (TabHost) (panelView.findViewById(R.id.facemoji_keyboard_category_tabhost));
            mTabHost.setup();
            for (int i = 0; i < FacemojiManager.getInstance().getCategories(facemojiType).size(); i++) {
                addTab(mTabHost, i);
            }
            mTabHost.setOnTabChangedListener(this);
            TabWidget tabWidget = mTabHost.getTabWidget();
            tabWidget.setStripEnabled(false);
            tabWidget.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            currentFaceImage = (ImageView) panelView.findViewById(R.id.current_face_image);
            currentFaceImage.setImageURI(FacemojiManager.getCurrentFacePicUri());

            mStickerPalettesAdapter = new FacemojiPalettesAdapter(this, mStickerLayoutParams);
            mStickerPalettesAdapter.setFacemojiType(facemojiType);
            mViewPager = (FacemojiViewPager) (panelView.findViewById(R.id.facemoji_keyboard_pager));
            mViewPager.setAdapter(mStickerPalettesAdapter);
            mViewPager.addOnPageChangeListener(this);
            mViewPager.setOffscreenPageLimit(0);
            mViewPager.setPersistentDrawingCache(PERSISTENT_NO_CACHE);
            mStickerLayoutParams.setPagerProperties(mViewPager);

            panelView.findViewById(R.id.switch_face).setOnClickListener(this);

            // Find share progress view
            mShareProgressView = (KeyboardProgressView) panelView.findViewById(R.id.share_progress_view);
            mShareProgressView.setOnClickListener(null); // Intercept touch event

            addView(panelView);

            setCurrentCategoryId(FacemojiManager.getInstance().getCurrentCategoryId(), true /* force */);
        }
    }

    private void initDefualtFacemojiPanel() {
        int panelWidth = HSResourceUtils.getDefaultKeyboardWidth(HSApplication.getContext().getResources())
                + getPaddingLeft() + getPaddingRight();
        int panelHeight = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources()) + getPaddingTop() + getPaddingBottom();
        RelativeLayout defaultView = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.facemoji_default_palette, null);
        LayoutParams param = new LayoutParams(panelWidth, panelHeight);
        defaultView.setLayoutParams(param);
        defaultView.setGravity(Gravity.CENTER);
        int orientation = HSApplication.getContext().getResources().getConfiguration().orientation;
        Button facemojiEntrance = (Button) defaultView.findViewById(R.id.facemoji_button_keyboard);
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ImageView image = (ImageView) defaultView.findViewById(R.id.face_arrow_keyboard);
            RelativeLayout.LayoutParams imageParam = (RelativeLayout.LayoutParams) image.getLayoutParams();
            imageParam.width = (int) (panelWidth * 0.5);
            imageParam.height = (int) (panelHeight * 0.6);
            image.setLayoutParams(imageParam);

            RelativeLayout.LayoutParams buttonParam = (RelativeLayout.LayoutParams) facemojiEntrance.getLayoutParams();
            buttonParam.width = (int) (panelWidth * 0.35);
            buttonParam.height = (int) (panelHeight * 0.18);
            facemojiEntrance.setLayoutParams(buttonParam);
        } else {
            ImageView image = (ImageView) defaultView.findViewById(R.id.face_arrow_keyboard);
            RelativeLayout.LayoutParams imageParam = (RelativeLayout.LayoutParams) image.getLayoutParams();
            imageParam.height = (int) (panelHeight * 0.8);
            image.setLayoutParams(imageParam);
        }

        facemojiEntrance.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickFacemojiCreateButton();
            }
        });
        addView(defaultView);
    }

    @Override
    public void onTabChanged(String tabId) {
        clear();

        int categoryId = FacemojiManager.getInstance().getCategoryIdByName(facemojiType,tabId);
        setCurrentCategoryId(categoryId, false);
    }

    @Override
    public void onPageSelected(int position) {
        final Pair<Integer, Integer> newPos = FacemojiManager.getInstance().getCategoryIdAndPageIdFromPagePosition(facemojiType,position);
        setCurrentCategoryId(newPos.first /* categoryId */, false /* force */);
        FacemojiManager.setCurrentCategoryPageId(newPos.second /* categoryPageId */);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset == 0) {
            mStickerPalettesAdapter.stopAllAnimations();
            mCurrentPagerPosition = position;
            mStickerPalettesAdapter.startAnimation(mCurrentPagerPosition);
        }
    }

    private void onClickFacemojiCreateButton() {

        startCameraActivity();
    }


    private void startCameraActivity() {
        Intent i = new Intent(HSApplication.getContext(), CameraActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        HSApplication.getContext().startActivity(i);
    }

    private void setCurrentCategoryId(int categoryId, boolean force) {

        int oldCategoryId = FacemojiManager.getInstance().getCurrentCategoryId();
        if (oldCategoryId == categoryId && !force) {
            return;
        }
        FacemojiManager.getInstance().setCurrentCategoryId(categoryId);

        int newTabId = categoryId;
        final int newCategoryPageId = FacemojiManager.getInstance().getPageIdFromCategoryId(facemojiType,categoryId);
        if (force || FacemojiManager.getInstance().getCategoryIdAndPageIdFromPagePosition(facemojiType, mViewPager.getCurrentItem()).first != categoryId) {
            mViewPager.setCurrentItem(newCategoryPageId, false /* smoothScroll */);
        }
        if (force || mTabHost.getCurrentTab() != newTabId) {
            mTabHost.setCurrentTab(newTabId);
        }
    }

    @Override
    public void onFacemojiClicked(FacemojiSticker item) {
        mStickerPalettesAdapter.pauseAnimation();
        MediaController.getShareManager().shareFacemojiWithKeyboard(item, mProgressListener);
    }

    public int getCurrentPagerPosition() {
        return mCurrentPagerPosition;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_face:
                FacemojiManager.showFaceSwitchView();
                break;

        }
    }

    public void onDestory() {
        HSGlobalNotificationCenter.removeObserver(notificationObserver);
    }
}