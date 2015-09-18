package com.smartkeyboard.rainbow.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;

import com.ihs.inputmethod.latin.utils.ResourceUtils;
import com.smartkeyboard.rainbow.R;

public class HSFontSelectView extends ListView {
    
    private Drawable mBackground;
    private Drawable mItemSelectedBackground;
    
    public HSFontSelectView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.fontSelectionViewStyle);
    }

    public HSFontSelectView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        
        mBackground = getBackground();
        mItemSelectedBackground = mBackground.getConstantState().newDrawable();
        mItemSelectedBackground.mutate().setAlpha(204);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final Resources res = getContext().getResources();
        final int width = ResourceUtils.getDefaultKeyboardWidth(res);
        final int height = ResourceUtils.getDefaultKeyboardHeight(res);
        setMeasuredDimension(width, height);
    }
    
    public Drawable getItemDefaultBackground() {
      return mBackground;
    }

    public Drawable getItemSelectedBackground() {
      return mItemSelectedBackground;
    }

    public void onDismiss() {
        ((HSFontSelectViewAdapter) this.getAdapter()).cancelAnimation();
    }
}

