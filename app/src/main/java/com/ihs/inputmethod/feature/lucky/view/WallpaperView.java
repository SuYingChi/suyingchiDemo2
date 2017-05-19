package com.ihs.inputmethod.feature.lucky.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.crashlytics.android.core.CrashlyticsCore;
import com.honeycomb.launcher.R;
import com.honeycomb.launcher.customize.WallpaperInfo;
import com.honeycomb.launcher.customize.WallpaperMgr;
import com.honeycomb.launcher.customize.util.CustomizeUtils;
import com.honeycomb.launcher.lucky.LuckyActivity;
import com.honeycomb.launcher.lucky.LuckyPreloadManager;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.ConcurrentUtils;
import com.honeycomb.launcher.util.Utils;
import com.honeycomb.launcher.util.ViewUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class WallpaperView extends LinearLayout implements View.OnClickListener {

    public static final String WALLPAPER_PRELOAD_DIRECTORY = "preload" + File.separator + "wallpaper";
    public static final String ICON = "icon";
    public static final String WALLPAPER = "wallpaper";

    private ImageView mWallpaperView;
    private View mAction;
    private WallpaperInfo mInfo;

    public WallpaperView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mWallpaperView = ViewUtils.findViewById(this, R.id.lucky_game_wallpaper_image);
        mAction = ViewUtils.findViewById(this, R.id.lucky_game_wallpaper_action);
        mAction.setClickable(false);
        mAction.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lucky_game_wallpaper_action:
                mAction.setClickable(false);

                final Context context = getContext();
                final File wallpaperFile = new File(Utils.getDirectory(WallpaperView.WALLPAPER_PRELOAD_DIRECTORY), WallpaperView.WALLPAPER);
                final File iconFile = new File(Utils.getDirectory(WallpaperView.WALLPAPER_PRELOAD_DIRECTORY), WallpaperView.ICON);

                String filename = Utils.md5(SystemClock.currentThreadTimeMillis() + "");
                final File wallpaperToSave = new File(Utils.getDirectory(WallpaperMgr.Files.LUCKY_DIRECTORY), filename);

                final File iconToSave = new File(Utils.getDirectory(WallpaperMgr.Files.LUCKY_DIRECTORY),
                        filename + WallpaperMgr.Files.LOCAL_WALLPAPER_THUMB_SUFFIX);

                final WallpaperInfo info = new WallpaperInfo(mInfo.getWallpaperUrl(), mInfo.getThumbnailUrl(), wallpaperToSave.getPath());
                ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            copyFile(wallpaperFile, wallpaperToSave);
                            copyFile(iconFile, iconToSave);
                        } catch (IOException e) {
                            e.printStackTrace();
                            wallpaperToSave.delete();
                            iconToSave.delete();
                        }
                    }
                });

                ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap decodeWallpaper = null;
                        try {
                            decodeWallpaper = BitmapFactory.decodeFile(wallpaperFile.getPath());
                        } catch (OutOfMemoryError error) {
                            CrashlyticsCore.getInstance().logException(error);
                            decodeWallpaper = null;
                        }
                        final Bitmap wallpaper = decodeWallpaper;
                        ConcurrentUtils.postOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                if (wallpaper == null) {
                                    mAction.setClickable(true);
                                }
                                CustomizeUtils.applyWallpaper(context, info, wallpaper, new CustomizeUtils.WallpaperApplyCallback() {
                                    @Override
                                    public void onWallpaperApplied(Bitmap wallpaper, Intent data) {
                                        HSAnalytics.logEvent("Lucky_Award_Wallpaper_Set_Clicked");
                                        CommonUtils.startLauncherAndSelectWallpaper(context);
                                        ((LuckyActivity) context).finish();
                                    }
                                });
                            }
                        });
                    }
                });
                break;
            case R.id.lucky_game_ad_action_cancel:
                ((LuckyActivity) getContext()).hideAwardView("Close");
                break;
            default:
                break;
        }
    }

    public ObjectAnimator getWallpaperAnimation() {
        setAlpha(0.0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(500);
        return fadeIn;
    }

    public boolean fetchWallpaper() {
        WallpaperInfo loaded = LuckyPreloadManager.getInstance().getLuckyWallpaper();
        if (loaded == null) {
            return false;
        }

        mInfo = loaded;
        File icon = new File(Utils.getDirectory(WallpaperView.WALLPAPER_PRELOAD_DIRECTORY), WallpaperView.ICON);
        Bitmap thumb = ImageLoader.getInstance().loadImageSync(Uri.fromFile(icon).toString());
        if (thumb == null) {
            return false;
        }
        mWallpaperView.setImageBitmap(thumb);

        return true;
    }

    static void copyFile(File src, File dst) throws IOException {
        if (!src.exists()) {
            return;
        }
        if (dst.exists()) {
            dst.delete();
        }

        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            outChannel.close();
        }
    }
}
