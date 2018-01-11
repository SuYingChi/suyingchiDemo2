package com.ihs.inputmethod.uimodules.widget.videoview;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by ihandysoft on 16/12/6.
 */

class VideoSurfaceView extends VideoView {
    private int mVideoWidth;
    private int mVideoHeight;

    private String filePath;

    private OnVedioListener onVedioListener;

    public VideoSurfaceView(Context context) {
        this(context, null);

    }

    private void init() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (!mp.isLooping()) {
                        mp.setLooping(true);
                    }
                    mp.start();
                }
            });
            setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {

                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        if (onVedioListener != null) {
                            onVedioListener.onPrepared();
                        }
                    }

                    return true;
                }
            });
        } else {
            setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (!mp.isLooping()) {
                        mp.setLooping(true);
                    }
                    mp.start();
                    if (onVedioListener != null) {
                        onVedioListener.onPrepared();
                    }
                }
            });
        }

        setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });

        setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (Build.VERSION.SDK_INT == 19) {
                    if (mp != null) {
                        mp.setDisplay(null);
                        mp.reset();
                        mp.setDisplay(getHolder());
                    }
                }
            }
        });
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        setMeasuredDimension(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mVideoWidth, mVideoHeight);
    }


    public void playMedia(final String mp4File, final OnVedioListener onVedioListener) {

        if (!new File(mp4File).exists()) {
            return;
        }
        this.filePath = mp4File;
        this.onVedioListener = onVedioListener;
        post(new Runnable() {
            @Override
            public void run() {
                if(isPlaying()) {
                    stopPlayback();
                }
                setVideoPath(mp4File);
            }
        });
    }

    public String getFilePath() {
        return filePath;
    }

    public interface OnVedioListener {
        void onPrepared();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopPlayback();
        super.onDetachedFromWindow();
    }
}

