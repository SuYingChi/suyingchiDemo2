package com.ihs.inputmethod.feature.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;

public class TypefacedTextView extends TextView {

    private Drawable mTopDrawable;

    public TypefacedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }

        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TypefacedTextView);
        int fontType = styledAttrs.getResourceId(R.styleable.TypefacedTextView_typeface, R.string.roboto_regular);
        int fontStyle = styledAttrs.getInt(R.styleable.TypefacedTextView_font_style, 0);
        int drawableWidth = styledAttrs.getDimensionPixelSize(R.styleable.TypefacedTextView_drawable_width, -1);
        int drawableHeight = styledAttrs.getDimensionPixelSize(R.styleable.TypefacedTextView_drawable_height, -1);
        styledAttrs.recycle();

        Typeface typeface = FontUtils.getTypeface(FontUtils.Font.ofFontResId(fontType), fontStyle);
        if (typeface != null) {
            setTypeface(typeface);
        }

        if (drawableWidth > 0 && drawableHeight > 0) {
            Drawable[] drawables = getCompoundDrawables();
            for (Drawable drawable : drawables) {
                if (drawable == null) {
                    continue;
                }
                Rect bounds = new Rect(drawable.getBounds());
                bounds.set(bounds.left, bounds.top, bounds.left + drawableWidth, bounds.top + drawableHeight);
                drawable.setBounds(bounds);
            }
            setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        }

        mTopDrawable = getCompoundDrawables()[1];
    }

    public TypefacedTextView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mTopDrawable != null) {
            int height = (int) (mTopDrawable.getBounds().bottom - mTopDrawable.getBounds().top + 1.0f / getLineSpacingMultiplier() * getTextSize() +
                    getPaddingTop() + getPaddingBottom() + getCompoundDrawablePadding());
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
