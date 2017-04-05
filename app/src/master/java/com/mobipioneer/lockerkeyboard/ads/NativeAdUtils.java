package com.mobipioneer.lockerkeyboard.ads;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.ihs.app.framework.HSApplication;

/**
 * Created by xu.zhang on 12/23/15.
 */
public class NativeAdUtils {

    public static Drawable getRoundedCornerDrawable(Drawable drawable, int pixels, int width, int height) {

        Bitmap bitmap = null;

        if (drawable==null || !(drawable instanceof BitmapDrawable)) {
            return null;
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        bitmap = bitmapDrawable.getBitmap();

        Bitmap mutableBitmap = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);

        final Paint paint = new Paint();
        final Rect srcRect = new Rect (0, 0, bitmap.getWidth(), bitmap.getHeight());
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, (float) pixels, (float) pixels, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, rect, paint);
        return (new BitmapDrawable(HSApplication.getContext().getResources(), mutableBitmap));
    }


    public static Drawable getRoundedCornerDrawable(Bitmap bitmap, int pixels){
        Bitmap mutableBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, (float) pixels, (float) pixels, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return (new BitmapDrawable(HSApplication.getContext().getResources(), mutableBitmap));
    }


    public static Drawable getRoundedCornerDrawable(Drawable drawable, int pixels) {

        if (drawable==null || !(drawable instanceof BitmapDrawable)) {
            return null;
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap bitmap = bitmapDrawable.getBitmap();

        return getRoundedCornerDrawable(bitmap, pixels);
    }

    public static Drawable getRoundedTopCornerDrawable(Bitmap bitmap, int pixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final Rect bottomRect = new Rect(0, mutableBitmap.getHeight()/2, mutableBitmap.getWidth(), mutableBitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, (float) pixels, (float) pixels, paint);
        canvas.drawRect(bottomRect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return (new BitmapDrawable(HSApplication.getContext().getResources(), mutableBitmap));
    }

}
