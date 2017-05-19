package com.ihs.inputmethod.feature.boost.plus;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BoostBgImageView extends ImageView {

    public BoostBgImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoostBgImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BoostBgImageView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(45, canvas.getWidth()/2 , canvas.getHeight()/2);
        super.onDraw(canvas);
    }
}
