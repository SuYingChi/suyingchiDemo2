package com.ihs.inputmethod.uimodules.ui.facemoji.faceswitcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.feature.common.VectorCompat;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;

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

        boolean isCurrentThemeDarkBg = HSKeyboardThemeManager.getCurrentTheme().isDarkBg();
        int elementColor = Color.WHITE;
        if (!isCurrentThemeDarkBg){
            elementColor = HSApplication.getContext().getResources().getColor(R.color.emoji_panel_tab_selected_color);
        }

        // view pager
        mViewPager = findViewById(R.id.face_viewpager);
        mFaceLayoutParams = new FaceLayoutParams(HSApplication.getContext().getResources());
        mViewPager.setAdapter(new FacePalettesViewAdapter(mFaceLayoutParams,this));//设置监听为this，因为外部传入该类的onFaceSwitchListener时机比该方法慢
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setPersistentDrawingCache(PERSISTENT_NO_CACHE);
        mFaceLayoutParams.setPagerProperties(mViewPager);

        // page id view
        mFacePageIndicatorView = findViewById(R.id.face_switch_page_id_view);
        mFacePageIndicatorView.setColors(0xff1ea0cd, 0x00000000);

        TextView title = findViewById(R.id.face_switch_title);
        title.setTextColor(elementColor);

        TextView manageText = findViewById(R.id.face_switch_edit_btn);
        manageText.setTextColor(elementColor);

        ImageView backImage = findViewById(R.id.face_switch_back_btn);
        VectorDrawableCompat closeDrawable = VectorCompat.createVectorDrawable(HSApplication.getContext(),R.drawable.ic_close_black_24dp);
        DrawableCompat.setTint(closeDrawable,elementColor);
        backImage.setImageDrawable(closeDrawable);
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


// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void prepare() {
//        mViewPager.getAdapter().notifyDataSetChanged();
//        mViewPager.setCurrentItem(0);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    @Override
    public void onFaceSwitch() {
        if (onFaceSwitchListener != null){
            onFaceSwitchListener.onFaceSwitch();
        }
    }
}
