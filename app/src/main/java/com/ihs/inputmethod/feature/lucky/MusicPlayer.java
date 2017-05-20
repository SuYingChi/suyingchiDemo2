package com.ihs.inputmethod.feature.lucky;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.RawRes;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.feature.common.ConcurrentUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Handles sound effects of game.
 */
public class MusicPlayer {

    private static final String TAG = MusicPlayer.class.getSimpleName();

    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, MediaPlayer> mMedias = new HashMap<>();
    private final ArrayList<MediaPlayer> mMutedMedias = new ArrayList<>();
    private MediaPlayer mBackground;
    private boolean mMute;


    MusicPlayer(boolean mute) {
        mMute = mute;
    }

    void preload(final Context context, final @RawRes int[] resIds) {
        final List<Integer> mediasToLoad = new ArrayList<>(resIds.length);
        for (int resId : resIds) {
            synchronized (mMedias) {
                if (mMedias.get(resId) == null) {
                    mediasToLoad.add(resId);
                }
            }
        }
        if (mediasToLoad.isEmpty()) {
            return;
        }
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                for (int resId : mediasToLoad) {
                    MediaPlayer media = MediaPlayer.create(context, resId);
                    if (media != null) {
                        synchronized (mMedias) {
                            mMedias.put(resId, media);
                        }
                    } else {
                        HSLog.w(TAG, "Failed to create media for res " + Integer.toHexString(resId));
                    }
                }
            }
        });
    }

    public void play(Context context, @RawRes int resId) {
        doPlay(context, resId, false);
    }

    void playBackground(Context context, @RawRes int resId) {
        ObjectAnimator fadeInAnim = ObjectAnimator.ofFloat(this, "volume", 0f, 1f);
        fadeInAnim.setDuration(4000);
        fadeInAnim.start();

        doPlay(context, resId, true);
    }

    public void pauseBackground() {
        if (mBackground != null) {
            mBackground.pause();
        }
    }

    public void resumeBackground() {
        if (mBackground != null && !mMute) {
            mBackground.start();
        }
    }

    public void setVolume(float volume) {
        synchronized (mMedias) {
            for (MediaPlayer media : mMedias.values()) {
                media.setVolume(volume, volume);
            }
        }
    }

    private void doPlay(final Context context, @RawRes final int resId, final boolean background) {
        MediaPlayer media;
        synchronized (mMedias) {
            media = mMedias.get(resId);
        }
        if (media == null) {
            ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
                @Override
                public void run() {
                    MediaPlayer asyncLoadedMedia = MediaPlayer.create(context, resId);
                    if (asyncLoadedMedia == null) {
                        HSLog.w(TAG, "Failed to create media for res " + Integer.toHexString(resId));
                        return;
                    }
                    asyncLoadedMedia.setLooping(background);
                    synchronized (mMedias) {
                        mMedias.put(resId, asyncLoadedMedia);
                    }
                    doPlay(context, resId, background);
                }
            });
            return;
        }
        if (background) {
            setVolume(0f); // Fade in background
            mBackground = media;
        }
        if (mMute) {
            if (shouldResume(media)) {
                synchronized (mMutedMedias) {
                    mMutedMedias.add(media);
                }
            }
        } else {
            try {
                media.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return Whether mute AFTER toggle.
     */
    boolean toggleMute() {
        boolean mute = mMute = !mMute;
        ArrayList<MediaPlayer> playingMedias;
        synchronized (mMedias) {
            Collection<MediaPlayer> medias;
            medias = mMedias.values();
            playingMedias = new ArrayList<>(medias.size() / 2);
            for (MediaPlayer media : medias) {
                if (media.isPlaying()) {
                    playingMedias.add(media);
                }
            }
            if (mute) {
                for (MediaPlayer media : playingMedias) {
                    if (shouldResume(media)) {
                        try {
                            media.pause();
                        } catch (IllegalStateException e) {
                            //media state : dle, Initialized, Prepared, Stopped, Error
                            e.printStackTrace();
                            continue;
                        }
                        synchronized (mMutedMedias) {
                            mMutedMedias.add(media);
                        }
                    } else {
                        media.stop();
                    }
                }
            } else {
                synchronized (mMutedMedias) {
                    for (MediaPlayer media : mMutedMedias) {
                        try {
                            media.start();
                        } catch (IllegalStateException e) {
                            //media state : dle, Initialized, Stopped, Error
                            e.printStackTrace();
                            continue;
                        }
                    }
                    mMutedMedias.clear(); // Consumed
                }
            }
        }
        return mute;
    }

    private boolean shouldResume(MediaPlayer media) {
        return media.isLooping();
    }


    void release() {
        Collection<MediaPlayer> medias;
        synchronized (mMedias) {
            medias = mMedias.values();
            for (MediaPlayer media : medias) {
                if (media.isPlaying()) {
                    media.stop();
                }
                media.release();
            }
            mMedias.clear();
        }
        mBackground = null;
    }
}
