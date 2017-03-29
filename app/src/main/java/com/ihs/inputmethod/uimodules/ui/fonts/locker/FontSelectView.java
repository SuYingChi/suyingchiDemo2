package com.ihs.inputmethod.uimodules.ui.fonts.locker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.api.utils.HSResourceUtils;

public class FontSelectView extends ListView {
    
    private Drawable mBackground;
    private Drawable mItemSelectedBackground;
    private final int fontItemTextColor;
    private final int fontItemDividerColor;
    
    public FontSelectView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontSelectView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, com.ihs.inputmethod.R.styleable.FontSelectPannel, defStyle, com.ihs.inputmethod.R.style.KeyboardView);
        fontItemTextColor = keyboardViewAttr.getColor(com.ihs.inputmethod.R.styleable.FontSelectPannel_fontTextColor, 0);
        fontItemDividerColor = keyboardViewAttr.getColor(com.ihs.inputmethod.R.styleable.FontSelectPannel_fontDividerColor, 0);

//        this.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
//        mItemSelectedBackground = mBackground.getConstantState().newDrawable();
//        mItemSelectedBackground.mutate().setAlpha(204);
        //set background and mItemSelectedBackground only for item view press status use, item view background is fontItemDividerColor(set on Adapter).
        this.setBackgroundColor(getResources().getColor(R.color.font_select_listview_bg));
        mBackground = getBackground();
        mItemSelectedBackground = new ColorDrawable(getResources().getColor(R.color.font_select_listview_item_selected_bg));
        setDivider(new ColorDrawable(fontItemDividerColor));
        setDividerHeight(1);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = HSResourceUtils.getDefaultKeyboardWidth(getResources());
        final int height = HSResourceUtils.getDefaultKeyboardHeight(getResources())
               - getContext().getResources().getDimensionPixelSize(R.dimen.config_suggestions_strip_height);
        setMeasuredDimension(width, height);
    }
    
    public Drawable getItemDefaultBackground() {
      return mBackground;
    }

    public int getItemDividerColor() {
        return fontItemDividerColor;
    }

    public int getItemTextColor() {
        return fontItemTextColor;
    }

    public Drawable getItemSelectedBackground() {
      return mItemSelectedBackground;
    }

    public void onDismiss() {
        ((FontSelectViewAdapter) this.getAdapter()).cancelAnimation();
    }
}

