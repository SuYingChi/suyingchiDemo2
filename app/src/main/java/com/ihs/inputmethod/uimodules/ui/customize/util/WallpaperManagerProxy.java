package com.ihs.inputmethod.uimodules.ui.customize.util;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.BuildConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A proxy that delegates calls to {@link android.app.WallpaperManager} after applying appropriate size transformations
 * when setting wallpaper.
 */
public class WallpaperManagerProxy {

    private static volatile WallpaperManagerProxy sInstance;

    private WallpaperManager mWm;

    public static WallpaperManagerProxy getInstance() {
        if (sInstance == null) {
            synchronized (WallpaperManagerProxy.class) {
                if (sInstance == null) {
                    sInstance = new WallpaperManagerProxy();
                }
            }
        }
        return sInstance;
    }

    private WallpaperManagerProxy() {
        mWm = WallpaperManager.getInstance(HSApplication.getContext());
    }

    public void setSystemBitmap(Context context, Bitmap wallpaper) throws IOException {
        setSystemBitmap(context, wallpaper, WallpaperUtils.canScroll(context, this, wallpaper, null));
    }

    public void setSystemBitmap(Context context, Bitmap wallpaper, boolean isScroll) throws IOException {
        if (wallpaper == null) {
            if (BuildConfig.DEBUG) {
                throw new NullPointerException("Wallpaper bitmap can't be null");
            }
            return;
        }
        Point point = WallpaperUtils.getWindowSize(context);
        try {
            if (isScroll) {
                wallpaper = WallpaperUtils.translateToScrollWallpaper(wallpaper, context);
            } else {
                wallpaper = WallpaperUtils.translateToFixedWallpaper(wallpaper, context);
            }
        } catch (OutOfMemoryError error) {
        }

        if (wallpaper != null) {
            try {
                try {
                    ByteArrayInputStream stream = bitmapToStream(wallpaper);
                    mWm.setStream(stream);
                    if (isScroll) {
                        mWm.suggestDesiredDimensions(point.x * 2, point.y);
                    } else {
                        mWm.suggestDesiredDimensions(point.x, point.y);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (OutOfMemoryError error) {
                try {
                    mWm.setBitmap(wallpaper);
                    if (isScroll) {
                        mWm.suggestDesiredDimensions(point.x * 2, point.y);
                    } else {
                        mWm.suggestDesiredDimensions(point.x, point.y);
                    }
                } catch (OutOfMemoryError | Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static ByteArrayInputStream bitmapToStream(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(2048);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return new ByteArrayInputStream(stream.toByteArray());
    }

    public BitmapDrawable getSystemDrawable() {
        BitmapDrawable bitmapDrawable = null;
        try {
            bitmapDrawable = (BitmapDrawable) mWm.getDrawable();
        } catch (Exception e) {
            // Multiple crashes are reported to occur due to internal exceptions:
            // (1) IllegalStateException on Huawei Android 4.4.2 (API 19) devices
            // (2) IOException on Meizu M2 and alps devices
            e.printStackTrace();
        }
        return bitmapDrawable;
    }

    public void restore(IBinder windowToken) {
        if (windowToken != null) {
            mWm.setWallpaperOffsets(windowToken, 0, 0.5f);
        }
    }

    /**
     * @return Whether a live wallpaper is currently applied. Note that this method differs from
     * in that it returns {@code true} when the
     * applied wallpaper is not provided by our {@link android.service.wallpaper.WallpaperService}.
     */
    public boolean isLiveWallpaper() {
        return mWm.getWallpaperInfo() != null;
    }

    /**
     * Delegate System api {@link WallpaperManager}
     */

    public WallpaperInfo getWallpaperInfo() {
        return mWm.getWallpaperInfo();
    }

    public void sendWallpaperCommand(IBinder windowToken, String action,
                                     int x, int y, int z, Bundle extras) {
        mWm.sendWallpaperCommand(windowToken, action, x, y, z, extras);
    }

    public void setWallpaperOffsets(IBinder windowToken, float xOffset, float yOffset) {
        mWm.setWallpaperOffsets(windowToken, xOffset, yOffset);
    }

    public void setWallpaperOffsetSteps(float xStep, float yStep) {
        mWm.setWallpaperOffsetSteps(xStep, yStep);
    }

    public int getDesiredMinimumWidth() {
        return mWm.getDesiredMinimumWidth();
    }

    public int getDesiredMinimumHeight() {
        return mWm.getDesiredMinimumHeight();
    }

    public void suggestDesiredDimensions(int minimumWidth, int minimumHeight) {
        mWm.suggestDesiredDimensions(minimumWidth, minimumHeight);
    }
}
