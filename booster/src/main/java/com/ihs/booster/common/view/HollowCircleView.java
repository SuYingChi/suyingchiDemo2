package com.ihs.booster.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zhixiangxiao on 15/12/1.
 */
public class HollowCircleView extends View {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Rect rect;
    PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    int viewBackColor = Color.parseColor("#00000000");

    public HollowCircleView(Context context) {
        super(context);
    }

    public HollowCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void setBackColor(int viewBackColor) {
        this.viewBackColor = viewBackColor;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rect = new Rect(0, 0, getWidth() - 0, getHeight() - 0);
        paint.setColor(viewBackColor);
        int sc = canvas.saveLayer(0, 0, getRight() - getLeft(), getBottom() - getTop(), null, Canvas.MATRIX_SAVE_FLAG |
                Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        canvas.drawRect(rect, paint);
        paint.setXfermode(porterDuffXfermode);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 4, paint);
        paint.setXfermode(null);
        canvas.restoreToCount(sc);
    }

}
