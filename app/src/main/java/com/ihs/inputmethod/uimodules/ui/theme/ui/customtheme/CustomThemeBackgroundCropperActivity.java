package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.view.HSMatrixImageView;
import com.keyboard.core.themes.custom.KCCustomThemeData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class CustomThemeBackgroundCropperActivity extends HSActivity {
    public static final String CopperImagePath = "CopperImagePath";
    public static final String KeyboardWidth = "KeyboardWidth";
    public static final String KeyboardHeight = "KeyboardHeight";
    public static final String CopperImageBitmap = "CopperImageBitmap";
    public static final String OldCropperImagePath = "OldCropperImagePath";

    FrameLayout cropperContentFrameLayout;
    private HSMatrixImageView cropperImageView;
    private View cropperImageMaskView;
    private Bitmap cropperImage;
    private String imagePath;
    private int keyboardWidth;
    private int keyboardHeight;
    private String oldCropperImagePath;

    private static final int REQUEST_CODE_START_CUSTOM_THEME = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_theme_image_cropper);

        imagePath = getIntent().getStringExtra(CopperImagePath);
        keyboardWidth = getIntent().getIntExtra(KeyboardWidth, HSResourceUtils.getDefaultKeyboardWidth(getResources()));
        keyboardHeight = getIntent().getIntExtra(KeyboardHeight, HSResourceUtils.getDefaultKeyboardHeight(getResources()));
        oldCropperImagePath = getIntent().getStringExtra(OldCropperImagePath);
        HSLog.d(String.format("page:%s,imagePath:%s", getClass().getSimpleName(), imagePath));
        cropperImage = extractThumbNail(imagePath, keyboardWidth, keyboardHeight);

        cropperImageMaskView = findViewById(R.id.custom_theme_background_cropper_content_mask_view);
        ((LinearLayout.LayoutParams) cropperImageMaskView.getLayoutParams()).height = keyboardHeight;

        cropperContentFrameLayout = (FrameLayout) findViewById(R.id.custom_theme_background_cropper_content_layout);
        ViewTreeObserver vto = cropperContentFrameLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float cropperImageViewYTrans = cropperContentFrameLayout.getHeight() / 2.0f - keyboardHeight / 2.0f;
                cropperImageView.setMaxDeltaYTrans(cropperImageViewYTrans);
                cropperImageView.setMinDeltaYTrans(-cropperImageViewYTrans);

                ViewTreeObserver obs = cropperContentFrameLayout.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });

        cropperImageView = (HSMatrixImageView) findViewById(R.id.custom_theme_background_cropper_content_image_view);
        cropperImageView.setImageBitmap(cropperImage);

        findViewById(R.id.custom_theme_background_cropper_title_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);
                CustomThemeBackgroundCropperActivity.this.finish();

            }
        });

        findViewById(R.id.custom_theme_background_cropper_title_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] imageViewLocation = new int[2];
                cropperImageView.getLocationInWindow(imageViewLocation);

                int[] imageMaskViewLocation = new int[2];
                cropperImageMaskView.getLocationInWindow(imageMaskViewLocation);

                Bitmap cropperBitmap = takeViewShot(cropperImageView, imageMaskViewLocation[0] - imageViewLocation[0], imageMaskViewLocation[1] - imageViewLocation[1], keyboardWidth, keyboardHeight);
                if (cropperBitmap == null) {
                    Toast.makeText(CustomThemeBackgroundCropperActivity.this,R.string.label_use_photo_unsuccessfully,Toast.LENGTH_SHORT).show();
                    return;
                }

                //HSKeyboardThemeManager.getCustomThemeData().setCustomizedBitmap(cropperBitmap);
                String cropperImagePath = KCCustomThemeData.saveCustomizedBackgroundBitmap(cropperBitmap, oldCropperImagePath);

                if (getIntent().getStringExtra("fromWallpaper") != null) {
                    Intent intent = new Intent(CustomThemeBackgroundCropperActivity.this, CustomThemeActivity.class);
                    intent.putExtra("CropperImagePath", cropperImagePath);
                    intent.putExtra("fromCropper", CustomThemeBackgroundCropperActivity.class.getSimpleName());
                    startActivityForResult(intent, REQUEST_CODE_START_CUSTOM_THEME);
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("CropperImagePath", cropperImagePath);
                    setResult(Activity.RESULT_OK, resultIntent);
                    CustomThemeBackgroundCropperActivity.this.finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_START_CUSTOM_THEME) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, ThemeHomeActivity.class);
                intent.putExtra(ThemeHomeActivity.EXTRA_SHOW_TRIAL_KEYBOARD, true);
                intent.putExtra(ThemeHomeActivity.EXTRA_SHOW_AD_ON_TRIAL_KEYBOARD_DISMISS, false);
                startActivity(intent);
                CustomThemeBackgroundCropperActivity.this.finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    static Bitmap takeViewShot(View view, int left, int top, int width, int height) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap activityShot = view.getDrawingCache();

        if (activityShot == null) {
            return null;
        }

//        width = left + width > activityShot.getWidth() ? activityShot.getWidth() - left : width;
//        height = top + height > activityShot.getHeight() ? activityShot.getHeight() - top : height;
        if (left + width > activityShot.getWidth()) {
            width = activityShot.getWidth() - left;
        }
        if (top + height > activityShot.getHeight()) {
            height = activityShot.getHeight() - top;
        }
        Bitmap activityShotRet = Bitmap.createBitmap(activityShot, left, top, width, height);
        view.destroyDrawingCache();
        return activityShotRet;
    }

    private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;

    public static Bitmap extractThumbNail(final String path, final int keyboardWidth, final int keyboardHeight) {
        if (path == null || path.equals("") || keyboardWidth <= 0) {
            return null;
        }

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            boolean isRotateNeeded = orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270;
            Bitmap bitmap = decodeFile(new File(path), keyboardWidth, isRotateNeeded);
            if (bitmap != null) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        bitmap = rotateImage(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        bitmap = rotateImage(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        bitmap = rotateImage(bitmap, 270);
                        break;
                    // etc.
                }
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap decodeFile(File f, int keyboardWidth, boolean isRotateNeeded) {
        Bitmap b = null;


        int IMAGE_MAX_SIZE = 1920;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
            if (isRotateNeeded) {
                //bitmap should be rotate 90 or 270 degree,so bitmap's height should be same as keyboardWidth
                int destHeight = keyboardWidth;
                int destWidth = (int) (destHeight * 1.0 * o.outWidth / o.outHeight);

                o.outHeight = destHeight;
                o.outWidth = destWidth;
            }

            int scale = 1;

            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                        (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            scale++;
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            while (b == null && scale <= 16) {
                try {
                    b = BitmapFactory.decodeStream(fis, null, o2);
                } catch (OutOfMemoryError e) {
                    scale *= 2;
                }
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return b;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

}
