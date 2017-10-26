package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xu.zhang on 3/28/16.
 */
public class FacemojiAnimationView extends AppCompatImageView {

    private int mIndex;
    private boolean mShouldRun;
    private boolean mIsRunning;
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
        mShouldRun = false;
        mIsRunning = false;

        mExecutor = FacemojiRenderingExecutor.getInstance();
    }

    private Bitmap getNextBitmap() {
        if (mIndex == sticker.getFrames().size() - 1) {
            mIndex = 0;
        } else {
            mIndex++;
        }
        long startTime = System.currentTimeMillis();
        Bitmap bitmap = FacemojiManager.getFrame(sticker, mIndex);
        long endTime = System.currentTimeMillis();
        lastFramePrepareTime = endTime - startTime;
        return bitmap;
    }

    /**
     * Starts the animation
     */
    public synchronized void start() {
        if (sticker == null) {
            return;
        }
        if (mIsRunning){
            return;
        }

        mShouldRun = true;
        mIsRunning = true;

        mExecutor.schedule( new Thread() {
            @Override
            public void run() {

                if (!mShouldRun) {
                    mIsRunning = false;
                    mExecutor.remove(this);
                    return;
                }

                long startTime = System.currentTimeMillis();
                buffer = FacemojiManager.getFrame(sticker, mIndex);
                mHandler.sendEmptyMessage(INVALIDATE_ANIM);
                long endTime = System.currentTimeMillis();
                lastFramePrepareTime = endTime - startTime;

                mExecutor.remove(this);
                mExecutor.schedule(this, Math.max(sticker.getFrames().get(mIndex).getInterval() - lastFramePrepareTime , 0), TimeUnit.MILLISECONDS);

                if (mIndex == sticker.getFrames().size() - 1) {
                    mIndex = 0;
                } else {
                    mIndex++;
                }
            }
        }, Math.max(sticker.getFrames().get(mIndex).getInterval() - lastFramePrepareTime , 0), TimeUnit.MILLISECONDS);
    }

    final ScheduledThreadPoolExecutor mExecutor;

    private void show() {
        if (sticker == null) {
            return;
        }

        setImageBitmap(FacemojiManager.getFrame(sticker, 0));
    }

    /**
     * Stops the animation
     */
    public synchronized void stop() {
        mShouldRun = false;
        mIsRunning = false;
        mHandler.removeCallbacksAndMessages(null);
    }

    public boolean isRuning() {
        return mShouldRun;
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