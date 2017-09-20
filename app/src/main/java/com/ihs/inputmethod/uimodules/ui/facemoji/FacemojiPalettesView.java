package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.framework.Constants;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.listeners.DeleteKeyOnTouchListener;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.converts.SyncWorkHandler;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.ProgressListener;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity;
import com.ihs.inputmethod.uimodules.widget.KeyboardProgressView;

public class FacemojiPalettesView extends LinearLayout implements OnTabChangeListener, ViewPager.OnPageChangeListener,
        FacemojiPageGridView.OnFacemojiClickListener, View.OnClickListener {
    private FacemojiManager.FacemojiType facemojiType = FacemojiManager.FacemojiType.CLASSIC;
    private ImageButton mDeleteKey;

    private TabHost mTabHost;
    private FacemojiViewPager mImagePager;

    private FacemojiLayoutParams mStickerLayoutParams;
    private FacemojiPalettesAdapter mStickerPalettesAdapter;

    private int mCurrentPagerPosition = 0;
    private Drawable transparentDrawable;

    private AlphaKeyboardClickListener alphaKeyboardClickListener;

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
        transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
//        TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, R.styleable.KeyboardView, defStyle, R.style.KeyboardView);
//		final StateListDrawable deleteKeyDrawable = new StateListDrawable();
//		deleteKeyDrawable.addState(new int[] { android.R.attr.state_pressed }, HSKeyboardThemeManager.getStyledDrawable(null, "tabbar_gif_delete_pressed.png"));
//		deleteKeyDrawable.addState(new int[] {}, HSKeyboardThemeManager.getStyledDrawable(null, "tabbar_gif_delete.png"));
//		mDeleteKeyBackgroundDrawable = deleteKeyDrawable;

//        keyboardViewAttr.recycle();

        Resources res = context.getResources();
        mStickerLayoutParams = new FacemojiLayoutParams(res);
    }


//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        Resources res = getContext().getResources();
//        int panelWidth = HSResourceUtils.getDefaultKeyboardWidth(res)
//                + getPaddingLeft() + getPaddingRight();
//        int panelHeight = HSResourceUtils.getDefaultKeyboardHeight(res) + getPaddingTop() + getPaddingBottom();
//        setMeasuredDimension(panelWidth, panelHeight);
//    }

    private void addTab(TabHost host, int categoryId) {
        String tabId = FacemojiManager.getInstance().getCategoryName(facemojiType,categoryId);
        TabHost.TabSpec tspec = host.newTabSpec(tabId);
        tspec.setContent(R.id.facemoji_keyboard_dummy);
        ImageView iconView = new ImageView(HSApplication.getContext());
        int height = (int) getResources().getDimension(R.dimen.config_suggestions_strip_height) - 10;
        LayoutParams iconParam = new LayoutParams(height, height);
        iconParam.gravity = Gravity.LEFT;
        iconParam.setMargins(10, 5, 10, 5);
        iconView.setLayoutParams(iconParam);
        iconView.setPadding(10, 10, 10, 10);
        iconView.setScaleType(ImageView.ScaleType.FIT_XY);
        iconView.setImageDrawable(FacemojiManager.getInstance().getClassicCategories().get(categoryId).getCategoryIcon());
        iconView.setBackgroundDrawable(getTabbarCategoryIconBackground());
        tspec.setIndicator(iconView);
        host.addTab(tspec);
    }

    private void clear() {
        if (mShareProgressView != null && mShareProgressView.isShowing()) {
            mShareProgressView.stop();
        }
    }

    public Drawable getTabbarCategoryIconBackground() {
        StateListDrawable tabbarCategoryStatesDrawable = new StateListDrawable();
        Drawable defaultbg = getResources().getDrawable(R.drawable.facemoji_pack_bg);
        tabbarCategoryStatesDrawable.addState(new int[]{android.R.attr.state_focused}, defaultbg);
        tabbarCategoryStatesDrawable.addState(new int[]{android.R.attr.state_pressed}, defaultbg);
        tabbarCategoryStatesDrawable.addState(new int[]{android.R.attr.state_selected}, defaultbg);
        tabbarCategoryStatesDrawable.addState(new int[]{}, transparentDrawable);
        return tabbarCategoryStatesDrawable;
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
            for (int i = 0; i < FacemojiManager.getInstance().getClassicCategories().size(); i++) {
                addTab(mTabHost, i);
            }
            mTabHost.setOnTabChangedListener(this);
            TabWidget tabWidget = mTabHost.getTabWidget();
            tabWidget.setStripEnabled(false);
            tabWidget.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            mStickerPalettesAdapter = new FacemojiPalettesAdapter(this, mStickerLayoutParams);
            mStickerPalettesAdapter.setFacemojiType(facemojiType);
            mImagePager = (FacemojiViewPager) (panelView.findViewById(R.id.facemoji_keyboard_pager));
            mImagePager.setAdapter(mStickerPalettesAdapter);
            mImagePager.addOnPageChangeListener(this);
            mImagePager.setOffscreenPageLimit(0);
            mImagePager.setPersistentDrawingCache(PERSISTENT_NO_CACHE);
            mStickerLayoutParams.setPagerProperties(mImagePager);

//			mStickerCategoryPageIndicatorView = (FacemojiCategoryPageIndicatorView) (panelView.findViewById(R.id.facemoji_category_page_id_view));
//			mStickerCategoryPageIndicatorView.setColors(mCategoryPageIndicatorColor, mCategoryPageIndicatorBackground);
//			mStickerLayoutParams.setCategoryPageIdViewProperties(mStickerCategoryPageIndicatorView);


            // deleteKey depends only on OnTouchListener.
            mDeleteKey = (ImageButton) (panelView.findViewById(R.id.facemoji_keyboard_delete));
            mDeleteKey.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDeleteKey.setTag(Constants.CODE_DELETE);
            mDeleteKey.setOnTouchListener(new DeleteKeyOnTouchListener(getContext()));

            TextView alphabetLeft = (TextView) panelView.findViewById(R.id.keyboard_alphabet_left);
            alphabetLeft.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getFuncKeyTextColor());
            alphabetLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, HSKeyboardThemeManager.getCurrentTheme().getFuncKeyLabelSize());
            alphabetLeft.setText(HSInputMethod.getSwitchToAlphaKeyLabel());
            alphabetLeft.setOnClickListener(this);

            panelView.findViewById(R.id.new_facemoji_btn).setOnClickListener(this);
            panelView.findViewById(R.id.classic_facemoji_btn).setOnClickListener(this);
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
//
//			RelativeLayout.LayoutParams buttonParam = (RelativeLayout.LayoutParams) facemojiEntrance.getLayoutParams();
//			buttonParam.width = (int) (panelWidth * 0.35);
//			buttonParam.height = (int) (panelHeight * 0.16);
//			facemojiEntrance.setLayoutParams(buttonParam);
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
        //uploadStickerCategoryPageIdView();
    }

//	private void uploadStickerCategoryPageIdView() {
//		if (mStickerCategoryPageIndicatorView == null) {
//			return;
//		}
//		mStickerCategoryPageIndicatorView.setCategoryPageId(FacemojiManager.getCurrentCategoryPageSize(), FacemojiManager.getCurrentCategoryPageId(), 0.0f);
//	}

    @Override
    public void onPageSelected(int position) {
        final Pair<Integer, Integer> newPos = FacemojiManager.getInstance().getCategoryIdAndPageIdFromPagePosition(facemojiType,position);
        setCurrentCategoryId(newPos.first /* categoryId */, false /* force */);
        FacemojiManager.setCurrentCategoryPageId(newPos.second /* categoryPageId */);
        //uploadStickerCategoryPageIdView();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//		Pair<Integer, Integer> newPos = FacemojiManager.getCategoryIdAndPageIdFromPagePosition(position);
//		int newCategoryId = newPos.first;
//		int newCategorySize = FacemojiManager.getCategoryPageSize(newCategoryId);
//		int currentCategoryId = FacemojiManager.getCurrentCategoryId();
//		int currentCategoryPageId = FacemojiManager.getCurrentCategoryPageId();
//		int currentCategorySize = FacemojiManager.getCurrentCategoryPageSize();
//		if (newCategoryId == currentCategoryId) {
//			mStickerCategoryPageIndicatorView.setCategoryPageId(
//					newCategorySize, newPos.second, positionOffset);
//		} else if (newCategoryId > currentCategoryId) {
//			mStickerCategoryPageIndicatorView.setCategoryPageId(
//					currentCategorySize, currentCategoryPageId, positionOffset);
//		} else if (newCategoryId < currentCategoryId) {
//			mStickerCategoryPageIndicatorView.setCategoryPageId(
//					currentCategorySize, currentCategoryPageId, positionOffset - 1);
//		}

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
        if (force || FacemojiManager.getInstance().getCategoryIdAndPageIdFromPagePosition(facemojiType,mImagePager.getCurrentItem()).first != categoryId) {
            mImagePager.setCurrentItem(newCategoryPageId, false /* smoothScroll */);
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

    public int getCurrentCategoryId() {
        return FacemojiManager.getInstance().getCurrentCategoryId();
    }

    public int getCurrentPagerPosition() {
        return mCurrentPagerPosition;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.keyboard_alphabet_left:
                if (alphaKeyboardClickListener != null){
                    alphaKeyboardClickListener.onAlphaClick();
                }
                break;
            case R.id.new_facemoji_btn:
                updateFacemojiType(FacemojiManager.FacemojiType.NEW);
                break;
            case R.id.classic_facemoji_btn:
                updateFacemojiType(FacemojiManager.FacemojiType.CLASSIC);
                break;
            case R.id.switch_face:
                FacemojiManager.showFaceSwitchView();
                break;

        }
    }

    private void updateFacemojiType(FacemojiManager.FacemojiType facemojiType) {
        this.facemojiType = facemojiType;
        mStickerPalettesAdapter.setFacemojiType(facemojiType);
    }

    public interface AlphaKeyboardClickListener{
        void onAlphaClick();
    }

    public void setAlphaKeyboardClickListener(AlphaKeyboardClickListener alphaKeyboardClickListener) {
        this.alphaKeyboardClickListener = alphaKeyboardClickListener;
    }
}