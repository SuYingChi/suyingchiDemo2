package com.ihs.booster.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.ihs.booster.utils.DisplayUtils;

/**
 * Created by zhixiangxiao on 15/12/1.
 */
public class RingView extends View {

    Paint paint = new Paint();

    public RingView(Context context) {
        super(context);
    }

    public RingView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Color.parseColor("#dddddddd"));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(DisplayUtils.dip2px(2));
        Log.d("test", getHeight() + "");
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 4, paint);
    }
}
