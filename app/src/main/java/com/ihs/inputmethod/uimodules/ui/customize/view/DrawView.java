package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class DrawView extends View {

    private EditWallpaperHintDrawer mDrawer;
    private boolean mIsClear;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDrawer(EditWallpaperHintDrawer drawer) {
        mDrawer = drawer;
    }
    public void setClear(boolean isClear) {
        mIsClear = isClear;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawer == null) {
            return;
        }
        if (mIsClear) {
            canvas.drawColor(Color.TRANSPARENT);
        } else {
            mDrawer.draw(canvas);
        }
    }
}