package com.ihs.inputmethod.uimodules.widget.videoview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

/**
 * Created by ihandysoft on 16/12/6.
 */
class HSVideoView extends RelativeLayout implements IMediaView {
    private VideoSurfaceView mp4View;
    private ImageView imageView;
    private String[] filePath;

    public HSVideoView(Context context) {
        this(context, null);
    }

    public HSVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setHSBackgroundMedia() {
        if(filePath.length < 2) {
            return;
        }
        if (mp4View == null) {
            mp4View = new VideoSurfaceView(getContext());
            addView(mp4View, 0);
        }
        if (imageView == null) {
            imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            addView(imageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        imageView.setVisibility(VISIBLE);
        imageView.setImageDrawable(loadDrawableFromAbsolutePath(filePath[0]));
        mp4View.setVisibility(VISIBLE);
        mp4View.playMedia(filePath[1], new VideoSurfaceView.OnVedioListener() {
            @Override
            public void onPrepared() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (imageView != null) {
                        imageView.setVisibility(INVISIBLE);
                    }
                } else {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (imageView != null) {
                                imageView.setVisibility(INVISIBLE);
                            }
                        }
                    }, 300);
                }
            }
        });
    }

    @Override
    public void stopHSMedia() {
        if(filePath == null) {
            return;
        }
        if(filePath.length < 1) {
            return;
        }
        if (imageView == null) {
            imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            addView(imageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
            imageView.bringToFront();
            imageView.setVisibility(VISIBLE);
        }
        imageView.setImageDrawable(loadDrawableFromAbsolutePath(filePath[0]));
    }

    @Override
    public void setHSBackground(Drawable drawable) {
        if (imageView == null) {
            imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            addView(imageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        filePath = null;
        imageView.bringToFront();
        imageView.setVisibility(VISIBLE);
        imageView.setImageDrawable(drawable);
    }

    @Override
    public void setHSBackground(final String[] filePath) {
        if(filePath == null || filePath.length == 0) {
            return;
        }
        File file = new File(filePath[0]);
        if (!file.exists() || !file.isFile()) {
            return;
        }
        if (shouldChangeBackground(filePath)) {
            this.filePath = filePath;
            if (filePath.length > 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setHSBackgroundMedia();
            } else {
                setHSBackground(loadDrawableFromAbsolutePath(filePath[0]));
            }
        } else if (this.filePath.length > 1 && mp4View != null && !mp4View.isPlaying()) {
            setHSBackgroundMedia();
        }
    }

    @Override
    public void setHSBackground(int resId) {
//        Drawable drawable = getResources().getDrawable(resId);
//        this.filePath = new String[1];
//        if (drawable == null) {
//            this.filePath[0] = "android.resource://" + getContext().getPackageName() + "/" + resId;
//            setHSBackgroundMedia(filePath[0]);
//        } else {
//            this.filePath[0] = "android.resource://" + getContext().getPackageName() + "/" + resId;
//            setHSBackground(drawable);
//        }
    }

    @Override
    public boolean isMedia() {
        return filePath != null && filePath.length > 1;
    }

    private boolean shouldChangeBackground(String[] filePath) {
        boolean shouldChange = false;
        if (this.filePath == null || this.filePath.length != filePath.length) {
            shouldChange = true;
        } else {
            for (int i = 0; i < filePath.length; i++) {
                if (!this.filePath[i].equals(filePath[i])) {
                    shouldChange = true;
                    break;
                }
            }
        }
        return shouldChange;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mp4View != null) {
            mp4View.setVideoSize(right - left, bottom - top);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    public Drawable loadDrawableFromAbsolutePath(String path){
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).build();
        Bitmap itemBitmap = ImageLoader.getInstance().loadImageSync("file://"+path, options);
        return new BitmapDrawable(itemBitmap);
    }
}
