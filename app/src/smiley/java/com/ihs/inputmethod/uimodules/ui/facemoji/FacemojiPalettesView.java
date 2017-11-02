package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.feature.common.VectorCompat;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.Constants;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.converts.SyncWorkHandler;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.ProgressListener;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareChannel;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiCategory;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity;
import com.ihs.inputmethod.uimodules.widget.KeyboardProgressView;

import java.util.List;

public class FacemojiPalettesView extends LinearLayout implements OnTabChangeListener, ViewPager.OnPageChangeListener,
        FacemojiPageGridView.OnFacemojiClickListener, View.OnClickListener {
    private TabHost mTabHost;
    private FacemojiViewPager mViewPager;
    private FacemojiIndicatorView pageIndicatorView;

    private ImageView currentFaceImage;
    private FacemojiLayoutParams mStickerLayoutParams;
    private FacemojiPalettesAdapter mStickerPalettesAdapter;
    private OnItemClickListener onItemClickListener;

    private int mCurrentPagerPosition = 0;
    private int mCurrentCategoryId = 0;

    boolean isCurrentThemeDarkBg;

    // Share progress
    private KeyboardProgressView mShareProgressView;
    private ProgressListener mProgressListener = new ProgressListener() {
        @Override
        public void startProgress() {
            mShareProgressView.start();
            mStickerPalettesAdapter.pauseAnimation();
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
        isCurrentThemeDarkBg = HSKeyboardThemeManager.getCurrentTheme().isDarkBg();

        HSGlobalNotificationCenter.addObserver(FacemojiManager.FACE_CHANGED, notificationObserver);
        HSGlobalNotificationCenter.addObserver(FacemojiManager.FACEMOJI_CATEGORY_DOWNLOADED, notificationObserver);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (FacemojiManager.FACE_CHANGED.equals(s)) {
                if (currentFaceImage != null) {
                    currentFaceImage.setImageURI(FacemojiManager.getCurrentFacePicUri());
                }
            }else if (FacemojiManager.FACEMOJI_CATEGORY_DOWNLOADED.equals(s)){
                if (hsBundle != null){
                    FacemojiCategory facemojiCategory = (FacemojiCategory) hsBundle.getObject(FacemojiManager.FACEMOJI_CATEGORY_BUNDLE_KEY);
                    List<FacemojiCategory> categories = FacemojiManager.getInstance().getCategories();
                    for (int i = 0 ; i < categories.size() ; i++ ){
                        FacemojiCategory category = categories.get(i);
                        if (category.getName().equals(facemojiCategory.getName())){
                            mStickerPalettesAdapter.notifyDownloaded(i);
                            mStickerPalettesAdapter.notifyDataSetChanged();
                            break;
                        }
                    }

                }
            }
        }
    };


    private void addTab(TabHost host, int categoryId) {
        String tabId = FacemojiManager.getInstance().getCategoryName(categoryId);
        TabHost.TabSpec tspec = host.newTabSpec(tabId);
        tspec.setContent(R.id.facemoji_keyboard_dummy);
        ImageView iconView = new ImageView(HSApplication.getContext());
        int height = (int) (getResources().getDimension(R.dimen.config_suggestions_strip_height) - getResources().getDimension(R.dimen.facemoji_panel_tab_margin_top) * 2);
        LayoutParams iconParam = new LayoutParams(height, height);
        iconParam.gravity = Gravity.LEFT;
        iconParam.setMargins(10, 5, 10, 5);
        iconView.setLayoutParams(iconParam);
        iconView.setPadding(10, 10, 10, 10);
        FacemojiCategory facemojiCategory = FacemojiManager.getInstance().getFacemojiCategories().get(categoryId);
        if (facemojiCategory.isBuildIn() || facemojiCategory.isDownloadedSuccess()){
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iconView.setImageDrawable(facemojiCategory.getCategoryIcon());
        }else {
            iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Drawable drawable;
            if (Build.VERSION.SDK_INT < 21){
                drawable = VectorCompat.createVectorDrawable(HSApplication.getContext(),R.drawable.ic_sticker_panel_tab);
            }else {
                drawable = HSApplication.getContext().getResources().getDrawable(R.drawable.ic_sticker_loading_image);
            }

            int tintColor = isCurrentThemeDarkBg ? Color.WHITE : HSApplication.getContext().getResources().getColor(R.color.emoji_panel_tab_normal_color);
            DrawableCompat.setTint(drawable,tintColor);
            RequestOptions requestOptions = new RequestOptions().placeholder(drawable).diskCacheStrategy(DiskCacheStrategy.DATA);
            Glide.with(this).asBitmap().apply(requestOptions).load(FacemojiDownloadManager.getInstance().getRemoteTabIconPath(facemojiCategory.getName())).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    return false;
                }
            }).into(iconView);
        }

        StateListDrawable stateListDrawable = new StateListDrawable();
        GradientDrawable shapeDrawable = new GradientDrawable();
        shapeDrawable.setShape(GradientDrawable.OVAL);
        shapeDrawable.setColor(isCurrentThemeDarkBg ? Color.parseColor("#aaf5f4f4"):Color.parseColor("#aac3c3c3"));
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, shapeDrawable);
        stateListDrawable.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        iconView.setBackgroundDrawable(stateListDrawable);

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
            showCreateFacemojiView();
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
            for (int i = 0; i < FacemojiManager.getInstance().getCategories().size(); i++) {
                addTab(mTabHost, i);
            }
            mTabHost.setOnTabChangedListener(this);
            TabWidget tabWidget = mTabHost.getTabWidget();
            tabWidget.setStripEnabled(false);
            tabWidget.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            currentFaceImage = (ImageView) panelView.findViewById(R.id.current_face_image);
            currentFaceImage.setImageURI(FacemojiManager.getCurrentFacePicUri());

            mStickerPalettesAdapter = new FacemojiPalettesAdapter(this, mStickerLayoutParams);
            mViewPager = (FacemojiViewPager) (panelView.findViewById(R.id.facemoji_keyboard_pager));
            mViewPager.setAdapter(mStickerPalettesAdapter);
            mViewPager.addOnPageChangeListener(this);
            mViewPager.setOffscreenPageLimit(0);
            mViewPager.setPersistentDrawingCache(PERSISTENT_NO_CACHE);
            mStickerLayoutParams.setPagerProperties(mViewPager);

            pageIndicatorView = (FacemojiIndicatorView) panelView.findViewById(R.id.page_indicator);

            panelView.findViewById(R.id.switch_face).setOnClickListener(this);

            // Find share progress view
            mShareProgressView = (KeyboardProgressView) panelView.findViewById(R.id.share_progress_view);
            mShareProgressView.setOnClickListener(null); // Intercept touch event

            addView(panelView);

            setCurrentCategoryId(mCurrentCategoryId, true /* force */);
        }
    }

    private void showCreateFacemojiView() {
        Resources res = HSApplication.getContext().getResources();
        int panelWidth = HSResourceUtils.getDefaultKeyboardWidth(res)
                + getPaddingLeft() + getPaddingRight();
        int panelHeight = HSResourceUtils.getDefaultKeyboardHeight(res)
                + res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height)
                - res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        RelativeLayout createFacemojiView = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.facemoji_default_palette, null);
        LayoutParams param = new LayoutParams(panelWidth, panelHeight);
        createFacemojiView.setLayoutParams(param);
        createFacemojiView.setGravity(Gravity.CENTER);
        Button facemojiEntrance = (Button) createFacemojiView.findViewById(R.id.facemoji_button_keyboard);

        ImageView image = (ImageView) createFacemojiView.findViewById(R.id.face_arrow);
        RelativeLayout.LayoutParams imageParam = (RelativeLayout.LayoutParams) image.getLayoutParams();
        imageParam.width = (int) (panelWidth * 0.5);
        imageParam.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        image.setLayoutParams(imageParam);

        LinearLayout rightArea = (LinearLayout) createFacemojiView.findViewById(R.id.right_area_layout);
        RelativeLayout.LayoutParams rightAreaLayoutParams = (RelativeLayout.LayoutParams) rightArea.getLayoutParams();
        rightAreaLayoutParams.width = (int) (panelWidth * 0.5);
        image.setLayoutParams(imageParam);

        boolean isCurrentThemeDarkBg = HSKeyboardThemeManager.getCurrentTheme().isDarkBg();
        int textColor = isCurrentThemeDarkBg ? Color.WHITE : HSApplication.getContext().getResources().getColor(R.color.emoji_panel_tab_selected_color);

        ((TextView)createFacemojiView.findViewById(R.id.facemoji_title)).setTextColor(textColor);
        ((TextView)createFacemojiView.findViewById(R.id.facemoji_text)).setTextColor(textColor);

        facemojiEntrance.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickFacemojiCreateButton();
            }
        });
        addView(createFacemojiView);
    }

    @Override
    public void onTabChanged(String tabId) {
        clear();

        int categoryId = FacemojiManager.getInstance().getCategoryIdByName(tabId);
        setCurrentCategoryId(categoryId, false);
    }

    @Override
    public void onPageSelected(int position) {
        final Pair<Integer, Integer> newPos = FacemojiManager.getInstance().getCategoryIdAndPageIdFromPagePosition(position,FacemojiManager.ShowLocation.Keyboard,getContext().getResources().getConfiguration().orientation);
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
        HSAnalytics.logEvent("keyboard_facemoji_create_clicked");
        HSInputMethod.hideWindow();
        startCameraActivity();
    }


    private void startCameraActivity() {
        Intent i = new Intent(HSApplication.getContext(), CameraActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        HSApplication.getContext().startActivity(i);
    }

    private void setCurrentCategoryId(int categoryId, boolean force) {
        Pair<Integer, Integer> categoryIdAndPageIdFromPagePosition = FacemojiManager.getInstance().getCategoryIdAndPageIdFromPagePosition(mViewPager.getCurrentItem(),FacemojiManager.ShowLocation.Keyboard,getContext().getResources().getConfiguration().orientation);
        int totalPageOfCurrentCategory = FacemojiManager.getInstance().getCategoryPageSize(categoryIdAndPageIdFromPagePosition.first,FacemojiManager.ShowLocation.Keyboard,getContext().getResources().getConfiguration().orientation);
        if (totalPageOfCurrentCategory > 1) {
            pageIndicatorView.setVisibility(VISIBLE);
            pageIndicatorView.updateIndicator(categoryIdAndPageIdFromPagePosition.second, FacemojiManager.getInstance().getCategoryPageSize(categoryIdAndPageIdFromPagePosition.first,FacemojiManager.ShowLocation.Keyboard,getContext().getResources().getConfiguration().orientation));
        }else {
            pageIndicatorView.setVisibility(INVISIBLE);
        }

        if (mCurrentCategoryId == categoryId && !force) {
            return;
        }

        mCurrentCategoryId = categoryId;

        int newTabId = categoryId;
        final int newCategoryPageId = FacemojiManager.getInstance().getPageIdFromCategoryId(categoryId,FacemojiManager.ShowLocation.Keyboard,getContext().getResources().getConfiguration().orientation);


        if (force || categoryIdAndPageIdFromPagePosition.first != categoryId) {
            mViewPager.setCurrentItem(newCategoryPageId, false /* smoothScroll */);
        }
        if (force || mTabHost.getCurrentTab() != newTabId) {
            mTabHost.setCurrentTab(newTabId);
        }

    }

    @Override
    public void onFacemojiClicked(FacemojiSticker sticker) {
        if (sticker.getName() == null){
            return;
        }
        HSAnalytics.logEvent("keyboard_facemoji_sent","categoryAndName",sticker.getCategoryName()+"-"+sticker.getName());
        MediaController.getShareManager().shareFacemojiFromKeyboard(sticker, Constants.MEDIA_FORMAT_GIF,
                ShareChannel.CURRENT,
                mProgressListener);
    }

    public int getCurrentPagerPosition() {
        return mCurrentPagerPosition;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_face:
                if (onItemClickListener != null){
                    onItemClickListener.onSwitchFaceClick();
                }
                break;

        }
    }

    public void onDestory() {
        HSGlobalNotificationCenter.removeObserver(notificationObserver);
    }

    public void restartAnim(){
        if (mStickerPalettesAdapter != null) {
            mStickerPalettesAdapter.startAnimation(mCurrentPagerPosition);
        }
    }

    public void stopAllAnim() {
        if (mStickerPalettesAdapter != null) {
            mStickerPalettesAdapter.stopAllAnimations();
        }
    }

    public interface OnItemClickListener{
        void onSwitchFaceClick();
    }
}