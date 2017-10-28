package com.ihs.inputmethod.uimodules.ui.facemoji.faceswitcher;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FaceItem;

public final class FacePalettesView extends LinearLayout implements ViewPager.OnPageChangeListener, FacePageGridView.OnFaceClickListener, FacePageGridViewAdapter.OnFaceSwitchListener {

    private ViewPager mViewPager;
    private FaceLayoutParams mFaceLayoutParams;
    private FacePageIndicatorView mFacePageIndicatorView;
    private FacePageGridViewAdapter.OnFaceSwitchListener onFaceSwitchListener;

    public FacePalettesView(Context context) {
        super(context);
    }

    public FacePalettesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnFaceSwitchListener(FacePageGridViewAdapter.OnFaceSwitchListener onFaceSwitchListener) {
        this.onFaceSwitchListener = onFaceSwitchListener;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Resources res = getContext().getResources();
        int width = HSResourceUtils.getDefaultKeyboardWidth(res)
                + getPaddingLeft() + getPaddingRight();
        int height = HSResourceUtils.getDefaultKeyboardHeight(res)
                +res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height)
                + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // view pager
        mViewPager = (ViewPager) findViewById(R.id.face_viewpager);
        mFaceLayoutParams = new FaceLayoutParams(HSApplication.getContext().getResources());
        mViewPager.setAdapter(new FacePalettesViewAdapter(mFaceLayoutParams,this));//设置监听为this，因为外部传入该类的onFaceSwitchListener时机比该方法慢
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setPersistentDrawingCache(PERSISTENT_NO_CACHE);
        mFaceLayoutParams.setPagerProperties(mViewPager);

        // page id view
        mFacePageIndicatorView = (FacePageIndicatorView) findViewById(R.id.face_switch_page_id_view);
        mFacePageIndicatorView.setColors(0xff1ea0cd, 0x00000000);
    }

    @Override
    public void onPageSelected(int position) {
        mFacePageIndicatorView.setPageId(FacemojiManager.getFacePageCount(), position, 0.0f);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mFacePageIndicatorView.setPageId(FacemojiManager.getFacePageCount(), position, positionOffset);
    }

    @Override
    public void onFaceClick(FaceItem faceItem) {

    }

    public void prepare() {
        mViewPager.getAdapter().notifyDataSetChanged();
        mViewPager.setCurrentItem(0);
    }

    @Override
    public void onFaceSwitch() {
        if (onFaceSwitchListener != null){
            onFaceSwitchListener.onFaceSwitch();
        }
    }
}