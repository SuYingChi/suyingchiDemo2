package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xu.zhang on 3/28/16.
 */
public class FacemojiAnimationView extends AppCompatImageView {
    private ScheduledFuture<?> schedule;

    private int mIndex;
    private volatile boolean mIsRunning;
    private Bitmap buffer;

    private final static int INVALIDATE_ANIM = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INVALIDATE_ANIM:
                    if (isShown()) {
                        setImageBitmap(buffer);
                    }
                    break;
            }
        }
    };
    private ObjectAnimator objectAnimator;

    public FacemojiSticker getSticker() {
        return sticker;
    }

    public void setSticker(FacemojiSticker sticker) {
        this.sticker = sticker;
        mIndex = 0;
    }

    private FacemojiSticker sticker;
    private long lastFramePrepareTime;

    public FacemojiAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FacemojiAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mIndex = 0;
        mIsRunning = false;

        mExecutor = FacemojiRenderingExecutor.getInstance();
    }

    /**
     * Starts the animation
     */
    public void start() {
        if (sticker == null || sticker.getName() == null) {
            return;
        }

        if (sticker.getFrames() == null || sticker.getFrames().size() == 0){
            return;
        }

        if (mIsRunning) {
            return;
        }

        synchronized (this) {
            if (mIsRunning) {
                return;
            }
            mIsRunning = true;
        }

        if (mIndex >= sticker.getFrames().size()) {
            mIndex = 0;
        }

        schedule = mExecutor.schedule(new Thread() {
            @Override
            public void run() {

                if (!mIsRunning) {
                    mExecutor.remove(this);
                    return;
                }

                long startTime = System.currentTimeMillis();
                buffer = FacemojiManager.getFrame(sticker, mIndex, false);
                mHandler.sendEmptyMessage(INVALIDATE_ANIM);
                long endTime = System.currentTimeMillis();
                lastFramePrepareTime = endTime - startTime;

                mExecutor.remove(this);
                schedule = mExecutor.schedule(this, Math.max(sticker.getFrames().get(mIndex).getInterval() - lastFramePrepareTime, 0), TimeUnit.MILLISECONDS);

                if (mIndex == sticker.getFrames().size() - 1) {
                    mIndex = 0;
                } else {
                    mIndex++;
                }
            }
        }, Math.max(sticker.getFrames().get(mIndex).getInterval() - lastFramePrepareTime, 0), TimeUnit.MILLISECONDS);
    }

    public void startAnim(){
        if (sticker == null || sticker.getName() == null) {
            startPlaceholderAnim();
        }else {
            start();
        }
    }

    public void stopAnim() {
        if (sticker == null || sticker.getName() == null) {
            stopPlaceholderAnim();
        }else {
            stop();
        }
    }

    private void startPlaceholderAnim() {
        if (objectAnimator == null) {
            objectAnimator = ObjectAnimator.ofFloat(this,"alpha",0,1.0f);
            objectAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            objectAnimator.setDuration(1000);
            objectAnimator.start();
        }else {
            if (Build.VERSION.SDK_INT >= 19) {
                objectAnimator.resume();
            } else {
                objectAnimator.start();
            }
        }
    }

    private void stopPlaceholderAnim() {
        if (objectAnimator != null){
            if (Build.VERSION.SDK_INT >= 19) {
                objectAnimator.pause();
            } else {
                objectAnimator.cancel();
            }
        }
    }

    final ScheduledThreadPoolExecutor mExecutor;

    private void show() {
        if (sticker == null || sticker.getName() == null) {
            return;
        }

        setImageBitmap(FacemojiManager.getFrame(sticker, 0, false));
    }

    /**
     * Stops the animation
     */
    public void stop() {
        if (!mIsRunning) {
            return;
        }

        synchronized (this) {
            if (!mIsRunning) {
                return;
            }
            mIsRunning = false;
        }

        if (schedule != null) {
            schedule.cancel(false);
        }
        mHandler.removeCallbacksAndMessages(null);
    }



    public boolean isRuning() {
        return mIsRunning;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        show();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }
}