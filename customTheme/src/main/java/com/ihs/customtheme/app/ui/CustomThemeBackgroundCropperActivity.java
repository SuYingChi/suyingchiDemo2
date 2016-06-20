package com.ihs.customtheme.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.customtheme.R;
import com.ihs.inputmethod.theme.HSCustomThemeDataManager;
import com.ihs.inputmethod.theme.HSCustomThemeItemBackground;
import com.ihs.inputmethod.theme.HSCustomThemeItemBase;


public class CustomThemeBackgroundCropperActivity extends HSActivity {
    private static final String TAG = CustomThemeBackgroundCropperActivity.class.getSimpleName();
    public static final String CopperImagePath = "CopperImagePath";
    public static final String KeyboardWidth = "KeyboardWidth";
    public static final String KeyboardHeight = "KeyboardHeight";
    public static final String CopperImageBitmap = "CopperImageBitmap";

    FrameLayout cropperContentFrameLayout;
    private HSMatrixImageView cropperImageView;
    private View cropperImageMaskView;
    private Bitmap cropperImage;
    private String imagePath;
    private int keyboardWidth;
    private int keyboardHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_theme_image_cropper);

        imagePath = getIntent().getStringExtra(CopperImagePath);
        keyboardWidth = getIntent().getIntExtra(KeyboardWidth, 0);
        keyboardHeight = getIntent().getIntExtra(KeyboardHeight, 0);

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

                Bitmap cropperBitmap = takeViewShot(cropperImageView, imageMaskViewLocation[0] - imageViewLocation[0],
                        imageMaskViewLocation[1] - imageViewLocation[1], keyboardWidth, keyboardHeight);
                HSCustomThemeItemBackground background = HSCustomThemeDataManager.getInstance().getCustomThemeData().getBackground();
                if (background != null) {
                    if (background.getItemSource() == HSCustomThemeItemBase.ItemSource.CUSTOMIZED) {
                        HSCustomThemeDataManager.getInstance().getCustomThemeData().getBackground().setCustomizedBitmap(cropperBitmap);
                    }
                }
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                CustomThemeBackgroundCropperActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    static Bitmap takeViewShot(View view, int left, int top, int width, int height) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap activityShot = view.getDrawingCache();
        Bitmap activityShotRet = Bitmap.createBitmap(activityShot, left, top, width, height);
        view.destroyDrawingCache();
        return activityShotRet;
    }

    private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;
    public static Bitmap extractThumbNail(final String path, final int keyboardWidth, final int keyboardHeight) {
        if (path == null || path.equals("") || keyboardWidth <= 0) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap tmp = BitmapFactory.decodeFile(path, options);
        if (tmp != null) {
            tmp.recycle();
        }

        // NOTE: out of memory error
        options.inSampleSize = 1;
        while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
            options.inSampleSize++;
        }

        int height = options.outHeight * keyboardWidth / options.outWidth;
        if (height < keyboardHeight) height = keyboardHeight;
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap == null) {
            Log.e(TAG, "bitmap decode failed");
            return null;
        }

        Log.i(TAG, "bitmap decoded size=" + bitmap.getWidth() + "x" + bitmap.getHeight());
        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, keyboardWidth, height, true);
        if (scaledBitmap != null && scaledBitmap != bitmap) {
            bitmap.recycle();
            return scaledBitmap;
        }

        return bitmap;
    }
}
