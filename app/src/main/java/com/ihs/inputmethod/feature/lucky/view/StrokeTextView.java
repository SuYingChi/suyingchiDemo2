package com.ihs.inputmethod.feature.lucky.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by zhijieli on 11/8/16.
 */

public class StrokeTextView extends TextView {

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = getPaint();

        paint.setColor(Color.rgb(0x02, 0x70, 0xef));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        canvas.drawText(getText().toString(), getWidth() / 2.0f - paint.measureText(getText().toString()) / 2.0f,
                getHeight() / 2.0f + (paint.getFontMetrics().descent- paint.getFontMetrics().ascent) / 2.0f - paint.getFontMetrics().bottom + 3,
                paint);

        paint.setStyle(Paint.Style.FILL);

        super.onDraw(canvas);
    }
}
