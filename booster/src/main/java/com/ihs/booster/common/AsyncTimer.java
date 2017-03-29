package com.ihs.booster.common;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

@Deprecated
public class AsyncTimer {
    private Timer mTimer;
    private Handler mHandler;
    private Runnable mRunnable;
    private long mPeriod;
    private long mDelay;
    private boolean mRepeat;
    private boolean mCanceled;

    private AsyncTimer() {
    }

    /**
     * 此方法会在下个周期立即执行 调用这个方法来生成并开始一个Timer,
     * 需要在一个有Looper的Thread上运行
     * Timer触发时,目标代码会Post到调用的Thread上
     *
     * @param runnable Timer 触发时运行的目标代码
     * @return Timer 对象
     */
    public static AsyncTimer runAsync(Runnable runnable) {
        return schedule(0, 0, false, new Handler(), runnable);
    }

    private static AsyncTimer schedule(long delay, long period, boolean repeat, Handler handler, Runnable runnable) {
        AsyncTimer timer = new AsyncTimer();
        timer.scheduleTimer(delay, period, repeat, handler, runnable);
        return timer;
    }

    private void scheduleTimer(long delay, long period, boolean repeat, Handler handler, Runnable runnable) {
        mPeriod = period;
        mDelay = delay;
        mRunnable = runnable;
        mRepeat = repeat;
        mHandler = handler;
        mCanceled = false;

        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mCanceled && mRunnable != null) {
                            mRunnable.run();
                            if (!mRepeat) {
                                AsyncTimer.this.cancel();
                            }
                        }
                    }
                });
            }
        };
        if (mRepeat) {
            mTimer.schedule(task, delay, period);
        } else {
            mTimer.schedule(task, delay);
        }
    }

    /**
     * 调用这个方法来对Timer进行Cancel,需要和schedule的调用在同一个线程上
     */
    public void cancel() {
        mCanceled = true;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        mRunnable = null;
    }

    /**
     * 此方法会在下个周期 延迟 delay ms 后执行 调用这个方法来生成并开始一个Timer
     * 需要在一个有 Looper 的 Thread 上运行
     * Timer触发时,目标代码会Post到调用的Thread上
     *
     * @param delay    延迟时间
     * @param runnable Timer触发时运行的目标代码
     * @return Timer对象
     */
    public static AsyncTimer runAsync(Runnable runnable, long delay) {
        return schedule(delay, 0, false, new Handler(), runnable);
    }

    /**
     * 此方法会在下个周期 延迟 delay ms 后,循环开始一个周期为 period 的任务
     * 调用这个方法来生成并开始一个Timer,需要在一个有Looper的Thread上运行
     * Timer触发时，目标代码会Post到调用的Thread上
     *
     * @param delay    延迟时间
     * @param period   Timer触发的间隔时间
     * @param runnable Timer触发时运行的目标代码
     * @return Timer对象
     */
    public static AsyncTimer schedule(long period, Runnable runnable, long delay) {
        return schedule(delay, period, true, new Handler(), runnable);
    }

    public long getPeriod() {
        return mPeriod;
    }

    public long getDelay() {
        return mDelay;
    }

    public boolean isRepeat() {
        return mRepeat;
    }
}
