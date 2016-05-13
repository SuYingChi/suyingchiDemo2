package com.keyboard.inputmethod.panels.gif.ui.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.facebook.drawee.drawable.ProgressBarDrawable;

/**
 * Created by dsapphire on 16/2/27.
 */
public class CustomProgressDrawable extends ProgressBarDrawable {

	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static int progressColor=Color.parseColor("#1ea0cd");

	@Override
	public void setAlpha(int alpha) {}

	@Override
	public void draw(Canvas canvas) {
		if (getLevel() == 0) {
			return;
		}
		drawBar(canvas, getLevel(), progressColor);
	}

	private void drawBar(Canvas canvas, int level, int color) {
		Rect bounds = getBounds();
		int length = (int)(bounds.width()  * level * 0.01f);
		int xpos = bounds.left ;
		int mBarWidth =bounds.height();
		int ypos = bounds.bottom  - mBarWidth;
		mPaint.setColor(color);
		canvas.drawRect(xpos, ypos, xpos + length, ypos + mBarWidth, mPaint);
	}
}