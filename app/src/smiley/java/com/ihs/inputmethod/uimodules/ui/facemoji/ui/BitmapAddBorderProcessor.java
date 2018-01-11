package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;

/**
 * Created by zhijieli on 3/17/16.
 */
public class BitmapAddBorderProcessor implements BitmapProcessor {
    private int color;

    public BitmapAddBorderProcessor(int color) {
        this.color = color;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {

        int pixelsToCorp =10;
        if (null == bitmap) {
            return null;
        }

        //add white border
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap result = Bitmap.createBitmap((int) ((bitmap.getWidth()-2*pixelsToCorp)* 1.1), (int) ((bitmap.getHeight()-2*pixelsToCorp) * 1.1), Bitmap.Config.ARGB_8888);
        Matrix matrix = new Matrix();
        matrix.setScale(1.1f, 1.1f);
        Bitmap bg = Bitmap.createBitmap(bitmap, pixelsToCorp, pixelsToCorp, bitmap.getWidth()-2*pixelsToCorp, bitmap.getHeight()-2*pixelsToCorp, matrix, true);

        // color
        paint.setColorFilter(new ColorMatrixColorFilter(new float[]{
                0, 0, 0, 0, Color.red(color),
                0, 0, 0, 0, Color.green(color),
                0, 0, 0, 0, Color.blue(color),
                0, 0, 0, 1, 0,
        }));
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bg, 0, 0, paint);

        canvas.drawBitmap(bitmap, (bg.getWidth() - bitmap.getWidth()) / 2, (bg.getHeight() - bitmap.getHeight()) / 2, null);

        bg.recycle();

        return result;
    }
}

