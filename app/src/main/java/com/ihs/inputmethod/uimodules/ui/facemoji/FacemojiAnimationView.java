package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;

/**
 * Created by xu.zhang on 3/28/16.
 */
public class FacemojiAnimationView extends ImageView {

    private int mIndex;
    private boolean mShouldRun;
    private boolean mIsRunning;
    private Handler mHandler;

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
        mHandler = new Handler();
        mIndex = 0;
        mShouldRun = false;
        mIsRunning = false;
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
        long startTime = System.currentTimeMillis();
        setImageBitmap(FacemojiManager.getFrame(sticker, 0));
        long endTime = System.currentTimeMillis();
        lastFramePrepareTime = endTime - startTime;
        mShouldRun = true;
        if (mIsRunning)
            return;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (!mShouldRun) {
                    mIsRunning = false;
                    mShouldRun = false;
                    return;
                }

                mIsRunning = true;
                mHandler.postDelayed(this, sticker.getFrames().get(mIndex).getInterval() - lastFramePrepareTime);
                if (isShown()) {
                    setImageBitmap(getNextBitmap());
                }
            }
        };

        mHandler.post(runnable);
    }

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

    public boolean isRuning(){
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