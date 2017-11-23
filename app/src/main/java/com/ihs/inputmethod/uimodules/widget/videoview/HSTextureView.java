package com.ihs.inputmethod.uimodules.widget.videoview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.view.RoundedImageView;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.ui.VideoPlayerView;

/**
 * Created by ihandysoft on 16/12/20.
 * if Builder.VERSION.SDK_INT >= Builder.VERSION_CODES.KITKAT, support mp4
 */

class HSTextureView extends RelativeLayout implements IMediaView {

    private String mp4FilePath;
    private String imgFilePath;

    private VideoPlayerView mp4View;
    private RoundedImageView imageView;

    private Paint imagePaint;
    private Paint roundPaint;

    private float topLeftRadius = 0;
    private float topRightRadius = 0;
    private float bottomLeftRadius = 0;
    private float bottomRightRadius = 0;

    public HSTextureView(Context context, float radius) {
        this(context, null, radius);
    }

    public HSTextureView(Context context, AttributeSet attrs, float radius) {
        this(context, attrs, 0, radius);
    }

    public HSTextureView(Context context, AttributeSet attrs, int defStyleAttr, float radius) {
        super(context, attrs, defStyleAttr);
        topLeftRadius = radius;
        topRightRadius = radius;
        bottomLeftRadius = radius;
        bottomRightRadius = radius;

        roundPaint = new Paint();
        roundPaint.setColor(Color.WHITE);
        roundPaint.setAntiAlias(true);
        roundPaint.setStyle(Paint.Style.FILL);
        roundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        imagePaint = new Paint();
        imagePaint.setXfermode(null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.saveLayer(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), imagePaint, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);

        drawTopLeft(canvas);
        drawTopRight(canvas);
        drawBottomLeft(canvas);
        drawBottomRight(canvas);
        canvas.restore();
    }

//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        if (mp4View != null)
//            mp4View.setVideoSize(w, h);
//
//        if (imageView != null) {
//            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
//            layoutParams.height = h;
//            layoutParams.width = w;
//            imageView.setLayoutParams(layoutParams);
//        }
//    }

    private void setBackgroundMedia(final String filePath) {
        if (mp4View == null) {
            mp4View = new VideoPlayerView(getContext());
            addView(mp4View);

        }
        mp4View.bringToFront();
        HSVideoPlayerManager.playMedia(filePath, mp4View);
    }

    @Override
    public void setHSBackground(Drawable drawable) {
        if (imageView == null) {
            imageView = new RoundedImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            addView(imageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        imageView.bringToFront();
        imageView.setImageDrawable(drawable);
    }

    @Override
    public void setHSBackground(String[] filePath) {
        if(filePath == null || filePath.length == 0) {
            return;
        }
        this.imgFilePath = filePath[0];
        Uri uri;
        if (HSKeyboardThemeManager.getCurrentTheme().getThemeType() == HSKeyboardTheme.ThemeType.BUILD_IN){
            uri = Uri.parse("file:///android_asset/"+imgFilePath);
        }else {
            uri = Uri.parse(filePath[0]);
        }
        setHSBackground(uri);
        if (filePath.length > 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.mp4FilePath = filePath[1];
            setBackgroundMedia(filePath[1]);
        } else {
            this.mp4FilePath = null;
        }
    }

    private void setHSBackground(Uri uri) {
        if (imageView == null) {
            imageView = new RoundedImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            addView(imageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        imageView.bringToFront();
        imageView.setImageURI(uri);
    }

    @Override
    public void setHSBackground(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        if (drawable == null) {
            this.mp4FilePath = "android.resource://" + getContext().getPackageName() + "/" + resId;
            this.imgFilePath = null;
            setBackgroundMedia(mp4FilePath);

        } else {
            this.imgFilePath = "android.resource://" + getContext().getPackageName() + "/" + resId;
            this.mp4FilePath = null;
            setHSBackground(Uri.parse(imgFilePath));
        }
    }

    @Override
    public boolean isMedia() {
        return mp4FilePath != null;
    }

    @Override
    public void stopHSMedia() {
        if (mp4FilePath != null) {
            HSVideoPlayerManager.pausePlayback(mp4FilePath);
        }
    }

    private void drawTopLeft(Canvas canvas) {
        if (topLeftRadius > 0) {
            Path path = new Path();
            path.moveTo(0, topLeftRadius);
            path.lineTo(0, 0);
            path.lineTo(topLeftRadius, 0);
            path.arcTo(new RectF(0, 0, topLeftRadius * 2, topLeftRadius * 2),
                    -90, -90);
            path.close();
            canvas.drawPath(path, roundPaint);
        }
    }

    private void drawTopRight(Canvas canvas) {
        if (topRightRadius > 0) {
            int width = getWidth();
            Path path = new Path();
            path.moveTo(width - topRightRadius, 0);
            path.lineTo(width, 0);
            path.lineTo(width, topRightRadius);
            path.arcTo(new RectF(width - 2 * topRightRadius, 0, width,
                    topRightRadius * 2), 0, -90);
            path.close();
            canvas.drawPath(path, roundPaint);
        }
    }

    private void drawBottomLeft(Canvas canvas) {
        if (bottomLeftRadius > 0) {
            int height = getHeight();
            Path path = new Path();
            path.moveTo(0, height - bottomLeftRadius);
            path.lineTo(0, height);
            path.lineTo(bottomLeftRadius, height);
            path.arcTo(new RectF(0, height - 2 * bottomLeftRadius,
                    bottomLeftRadius * 2, height), 90, 90);
            path.close();
            canvas.drawPath(path, roundPaint);
        }
    }

    private void drawBottomRight(Canvas canvas) {
        if (bottomRightRadius > 0) {
            int height = getHeight();
            int width = getWidth();
            Path path = new Path();
            path.moveTo(width - bottomRightRadius, height);
            path.lineTo(width, height);
            path.lineTo(width, height - bottomRightRadius);
            path.arcTo(new RectF(width - 2 * bottomRightRadius, height - 2
                    * bottomRightRadius, width, height), 0, 90);
            path.close();
            canvas.drawPath(path, roundPaint);
        }
    }

}

