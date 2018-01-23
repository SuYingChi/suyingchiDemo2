package com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.base.utils.ExecutorUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.downloads.MediaDownload;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.DownloadStatusListener;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download.DownloadTask;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.DirectoryUtils;
import com.ihs.inputmethod.uimodules.ui.theme.ui.view.SectorProgressView;

import java.io.File;


public final class GifView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {

    interface OnGifClickListener {
        void onGifClick(GifItem item, GifView view);

        void onGifLongClick(GifItem item, GifView view);

        void onFavoriteIconClick(GifItem item, GifView view);

        void onDeleteIconClick(GifItem item, GifView view);
    }

    private GifItem gifItem;
    private DownloadTask task;
    private OnGifClickListener listener;

    private View mAnimOne;
    private View favorite;
    private View favorite_alpha;
    private ImageView favorite_iv;
    private ImageView delete_iv;

    private SectorProgressView progressView;

    private boolean isGifEnabled = false;

    private Handler handler = new Handler();

    private long favorite_view_last_touch_time;
    private Runnable fadeOut = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - favorite_view_last_touch_time >= 2900L) {
                if (favorite_alpha != null && favorite_alpha.getVisibility() == VISIBLE) {
                    Animation outAni = createFadeAnimation(0.9f, 0f, 1000);
                    outAni.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            favorite_alpha.setVisibility(INVISIBLE);
                            favorite.setVisibility(INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    favorite_alpha.startAnimation(outAni);
                    favorite.startAnimation(outAni);
                }
            }
        }
    };

    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        favorite = findViewById(R.id.favorite);
        favorite_iv = (ImageView) findViewById(R.id.favorite_iv);
        delete_iv = (ImageView) findViewById(R.id.delete_iv);
        favorite_alpha = findViewById(R.id.alpha_favorite);
        favorite.setVisibility(INVISIBLE);
        favorite_alpha.setVisibility(INVISIBLE);
        mAnimOne = findViewById(R.id.anim_one);
        progressView = (SectorProgressView) findViewById(R.id.gif_item_progress);

        setOnClickListener(this);
        setOnLongClickListener(this);
        if (!HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            setBackgroundColor(getResources().getColor(R.color.gif_panel_search_bar_background_light));
        } else {
            setBackgroundColor(getResources().getColor(R.color.gif_panel_search_bar_background));
        }
    }

    public void setOnImageEventInfo(final GifItem info, OnGifClickListener listener) {
        gifItem = info;
        this.listener = listener;
    }

    public void setDownloadTask(DownloadTask task) {
        this.task = task;
    }

    public DownloadTask getTask() {
        return task;
    }

    public GifItem getGifItem() {
        return gifItem;
    }

    @Override
    public void onClick(View v) {
        if (gifItem != null && listener != null && isGifEnabled) {
            if (favorite.getVisibility() == INVISIBLE) {
                listener.onGifClick(gifItem, this);
                return;
            }
            favorite_view_last_touch_time = System.currentTimeMillis();
            postDelayed(fadeOut, 3000L);
            if (favorite_iv.getVisibility() == VISIBLE) {
                listener.onFavoriteIconClick(gifItem, this);
                return;
            }
            if (delete_iv.getVisibility() == VISIBLE) {
                listener.onDeleteIconClick(gifItem, this);
            }
        }
    }

    public void shareHdGif(final DownloadStatusListener downloadStatusListener) {
        final File downloadedFile = new File(DirectoryUtils.getHDGifDownloadFolder(), gifItem.id);
        MediaController.getDownloadManger().startDownloadInThreadPool(ExecutorUtils.getFixedExecutor("Gif"));
        MediaDownload download = new MediaDownload(gifItem.getHdGifUri(), downloadedFile.getAbsolutePath(), new DownloadStatusListener() {
            @Override
            public void onDownloadProgress(File file, final float percent) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressView.setVisibility(View.VISIBLE);
                        progressView.setPercent((int) (percent * 100));
                        progressView.postInvalidate();
                    }
                });
            }

            @Override
            public void onDownloadSucceeded(final File file) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressView.setVisibility(INVISIBLE);
                        downloadStatusListener.onDownloadSucceeded(file);
                    }
                });
            }

            @Override
            public void onDownloadFailed(File file) {

            }
        });
        MediaController.getDownloadManger().put(download);
    }

    @Override
    public boolean onLongClick(View v) {
        if (gifItem != null && listener != null && isGifEnabled) {
            listener.onGifLongClick(gifItem, this);
            return true;
        }
        return false;
    }

    public void showFavoriteAddView(final boolean isAdded) {
        favorite.setVisibility(VISIBLE);
        if (favorite_alpha != null && favorite_alpha.getVisibility() == INVISIBLE) {
            showFavoriteAddAnimation();
        }
        favorite_iv.setSelected(isAdded);
        favorite_iv.setVisibility(VISIBLE);
        delete_iv.setVisibility(INVISIBLE);
    }

    public void hideFavoriteView() {
        favorite.setVisibility(INVISIBLE);
        favorite_alpha.setVisibility(INVISIBLE);
    }

    public void showFavoriteDeleteView() {
        favorite.setVisibility(VISIBLE);
        if (favorite_alpha != null && favorite_alpha.getVisibility() == INVISIBLE) {
            showFavoriteAddAnimation();
        }
        favorite_iv.setVisibility(INVISIBLE);
        delete_iv.setVisibility(VISIBLE);
    }

    public void setGifEnabled(boolean gifEnabled) {
        isGifEnabled = gifEnabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (favorite_alpha != null && favorite_alpha.getVisibility() == VISIBLE) {
            favorite_view_last_touch_time = System.currentTimeMillis();
            postDelayed(fadeOut, 3000L);
        }
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                final Animation downAni = createScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f);
                this.startAnimation(downAni);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                final Animation upAni = createScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f);
                this.startAnimation(upAni);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void showFavoriteAddAnimation() {
        final ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 8.0f, 0.0f, 8.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(400);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAnimOne.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimOne.setVisibility(INVISIBLE);
                mAnimOne.clearAnimation();
                Animation fadeIn = createFadeAnimation(0.4f, 0.9f, 500);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        favorite_alpha.setVisibility(VISIBLE);
                        favorite_view_last_touch_time = System.currentTimeMillis();
                        GifView.this.postDelayed(fadeOut, 3000L);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                favorite_alpha.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mAnimOne.startAnimation(scaleAnimation);
    }

    private static Animation createScaleAnimation(final float fromX, final float toX, final float fromY, final float toY) {
        final ScaleAnimation animation = new ScaleAnimation(fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(10);
        animation.setFillAfter(true);
        return animation;
    }

    private static Animation createFadeAnimation(final float fromAlpha, final float toAlpha, final int duration) {
        final AlphaAnimation animation = new AlphaAnimation(fromAlpha, toAlpha);
        animation.setDuration(duration);
        return animation;
    }

}