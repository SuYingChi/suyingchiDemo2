package com.ihs.customtheme.app.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.base.utils.ColorUtils;

/**
 * Created by dsapphire on 16/5/19.
 */
public final class BitmapUtil {

	private BitmapUtil(){

	}

	public static Bitmap makeDarkBitmap(final Bitmap bitmap, final int width, final int height, final float alpha){
		Bitmap pressedArrowNextBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		pressedArrowNextBitmap.eraseColor(Color.TRANSPARENT);

		for (int column = 0; column < width; column++) {
			for (int row = 0; row < height; row++) {
				try {
					int color = bitmap.getPixel(column, row);
					if (Color.alpha(color) != 0) {
						pressedArrowNextBitmap.setPixel(column, row, ColorUtils.adjustAlpha(color, alpha));
					}
				} catch (Exception e) {
					HSLog.d(e.getLocalizedMessage());
				}
			}
		}
		return  pressedArrowNextBitmap;
	}

}
