package com.ihs.inputmethod.uimodules.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSFileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by hdd on 16/3/25.
 */
public class BitmapUtils {

    public static final float TABLET_WIDTH_SCALE_FACTOR = 0.75f;

    /**
     * 缩放图片（屏幕大小）
     *
     * @param filePath
     * @return
     */
    public static Bitmap compressBitmap(String filePath, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        final int heightRatio = Math.round((float) options.outHeight / (float) height);
        final int widthRatio = Math.round((float) options.outWidth / (float) width);

        int inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        if (inSampleSize < 1) {
            inSampleSize = 1;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        Bitmap source = BitmapFactory.decodeFile(filePath, options);

        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int degree = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(degree);

        Bitmap bmRotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        if (degree != 0) {
            source.recycle();
        }

        matrix.reset();
        float scale = 1.0f * width / bmRotated.getWidth();
        if (scale < 1.0f * height / bmRotated.getHeight()) {
            scale = 1.0f * height / bmRotated.getHeight();
        }
        matrix.setScale(scale, scale);
        Bitmap scaledBm = Bitmap.createBitmap(bmRotated, 0, 0, bmRotated.getWidth(), bmRotated.getHeight(), matrix, true);

        if (scaledBm.getWidth() != bmRotated.getWidth() || scaledBm.getHeight() != bmRotated.getHeight()) {
            bmRotated.recycle();
        }

        return scaledBm;
    }

    public static Bitmap compressBitmap(byte[] imageData, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

        final int heightRatio = Math.round((float) options.outHeight / (float) height);
        final int widthRatio = Math.round((float) options.outWidth / (float) width);

        int inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        if (inSampleSize < 1) {
            inSampleSize = 1;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
    }


    public static Bitmap addBorder(Bitmap crop, int color) {
        if (null == crop) {
            return null;
        }

        //add white border
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap result = Bitmap.createBitmap((int) (crop.getWidth() * 1.1), (int) (crop.getHeight() * 1.1), Bitmap.Config.ARGB_8888);
        Matrix matrix = new Matrix();
        matrix.setScale(1.1f, 1.1f);
        Bitmap bg = Bitmap.createBitmap(crop, 0, 0, crop.getWidth(), crop.getHeight(), matrix, true);

        // color
        paint.setColorFilter(new ColorMatrixColorFilter(new float[]{
                0, 0, 0, 0, Color.red(color),
                0, 0, 0, 0, Color.green(color),
                0, 0, 0, 0, Color.blue(color),
                0, 0, 0, 1, 0,
        }));
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bg, 0, 0, paint);
        canvas.drawBitmap(crop, (bg.getWidth() - crop.getWidth()) / 2, (bg.getHeight() - crop.getHeight()) / 2, null);

        bg.recycle();
        if (!crop.isRecycled()) {
            crop.recycle();
        }
        return result;
    }

    public static Bitmap drawMaskView(Bitmap crop, int width, int height) {
        if (null == crop) {
            return null;
        }

        Matrix matrix = new Matrix();
        matrix.setScale(width * TABLET_WIDTH_SCALE_FACTOR / crop.getWidth(), width * TABLET_WIDTH_SCALE_FACTOR / crop.getWidth());
        Bitmap pic = Bitmap.createBitmap(crop, 0, 0, crop.getWidth(), crop.getHeight(), matrix, true);

        if (!crop.isRecycled()) {
            crop.recycle();
        }

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setAlpha(217);
        Bitmap bg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bg.setHasAlpha(true);

        Canvas canvas = new Canvas(bg);
        canvas.drawRect(new Rect(0, 0, (width - pic.getWidth()) / 2, height), paint);
        canvas.drawRect(new Rect((width + pic.getWidth()) / 2, 0, width, height), paint);
        canvas.drawRect(new Rect((width - pic.getWidth()) / 2, pic.getHeight(), (width + pic.getWidth()) / 2, height), paint);
        canvas.save();
        canvas.drawBitmap(pic, (bg.getWidth() - pic.getWidth()) / 2, 0, null);


        return bg;
    }

    public static Bitmap addBorder(Uri uri, int color) {
        Bitmap picture = null;
        try {
            picture = MediaStore.Images.Media.getBitmap(HSApplication.getContext().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addBorder(picture, color);
    }

    public static Bitmap corpAndAddBorder(Uri uri, int color, int pixelsToCorp) {
        Bitmap picture = null;
        try {
            picture = MediaStore.Images.Media.getBitmap(HSApplication.getContext().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap dstBmp = Bitmap.createBitmap(
                picture,
                pixelsToCorp,
                pixelsToCorp,
                picture.getHeight() - 2 * pixelsToCorp,
                picture.getHeight() - 2 * pixelsToCorp
        );
        picture.recycle();

        return addBorder(dstBmp, color);
    }

    public static File ifNeedCompressPhoto(String filePath, int width, int height){
        File file = new File(filePath);
        String fileType = HSFileUtils.FileTypeDetector.getInstance().getFileType(file);
        if((fileType.equals("jpeg") || fileType.equals("png")) && file.length() > 5 * 1024 * 1024){
            Bitmap bitmap = compressBitmap(filePath, width, height);
            File mFile = HSFileUtils.createTempFile(fileType);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mFile);
                if(fileType.equals("jpeg")) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }
                else{
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mFile.deleteOnExit();
            } finally {
                if(fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(mFile.exists()) {
                    return mFile;
                }
            }
        }

        return file;
    }

}
