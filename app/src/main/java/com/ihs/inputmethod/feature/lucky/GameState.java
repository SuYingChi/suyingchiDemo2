package com.ihs.inputmethod.feature.lucky;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.Choreographer;

import com.honeycomb.launcher.animation.AnimatorListenerAdapter;
import com.honeycomb.launcher.animation.LauncherAnimUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

public class GameState {

    private static final String TAG = GameState.class.getSimpleName();

    public static final int FULL_CHANCE_COUNT =
            HSConfig.optInteger(9, "Application", "Lucky", "FullChanceCount");

    public static final long CHANCE_INCREMENT_PERIOD =
            HSConfig.optInteger(1800, "Application", "Lucky", "ChanceIncrementPeriodSeconds") * 1000;

    static final int CHANCE_RESTORE_NOT_SCHEDULED = -1;

    private static final long CATCH_ANIM_DURATION_CAUGHT = 800;
    private static final long CATCH_ANIM_DURATION_EMPTY = 400;

    private static final long BORDER_LIGHTS_REVEAL_START_DELAY = 500;
    private static final long BORDER_LIGHTS_REVEAL_DURATION = 800;

    public static final String PREF_KEY_CHANCE_LEFT = "lucky_chance_left";
    public static final String PREF_KEY_RESTORE_COUNTDOWN_START_TIME = "lucky_restore_countdown_start_time";

    interface StateListener {
        /**
         * Invoked at every time slice the game state advances.
         * Listener may want to re-draw the game board at this call.
         */
        void onStateAdvance();

        /**
         * Invoked when number of chances left has changed.
         *
         * @param chanceCount Chances left AFTER change.
         */
        void onChanceCountChanged(int chanceCount);

        /**
         * There's a second level count-down timer to indicate when game chances is restored to
         * {@link #FULL_CHANCE_COUNT}. This is invoked every second when the timer reading is updated.
         *
         * @param restoreTimeLeftSeconds Time (in seconds) left until game chance count is restored.
         */
        void onRestoreTimerUpdate(int restoreTimeLeftSeconds);
    }

    interface GameActivity {
        boolean isInForeground();
    }

    private GameActivity mUserInterface;

    private StateListener mListener;

    private final HSPreferenceHelper mPrefs;

    // Device configurations & constants
    private GameConfig mConfig;

    // Game choreography
    private long mStartTime;
    private long mPauseTime = -1;
    private int mRestoreTimeLeftSeconds = CHANCE_RESTORE_NOT_SCHEDULED;
    private CatchAnimator mCatchAnimator;
    private Choreographer mChoreographer;
    private Handler mHandler;

    // Board status
    private int mChanceCount;
    private float mBeltTranslation;
    private float mArmPosition;
    private float mBorderLightsRevealRatio; // 0f for invisible, 1f for fully revealed

    private List<TargetInfo> mTargets = new ArrayList<>();

    GameState(Context context, HSPreferenceHelper prefs) {
        mConfig = new GameConfig();
        LuckyPreloadManager.getInstance().refreshConfig();
        mUserInterface = (GameActivity) context;
        mPrefs = prefs;

        mChoreographer = Choreographer.getInstance();
        mHandler = new Handler();
    }

    void setStateListener(StateListener listener) {
        mListener = listener;
    }

    int getChanceCount() {
        return mChanceCount;
    }

    @DebugLog
    public void reset() {
        mBeltTranslation = 0f;
        mArmPosition = 0f;
        mChoreographer.removeFrameCallback(mFrameCallback);
        mHandler.removeCallbacksAndMessages(null);
        mChanceCount = mPrefs.getInt(PREF_KEY_CHANCE_LEFT, FULL_CHANCE_COUNT);
        if (mListener != null) {
            long now = System.currentTimeMillis();
            long lastRestored = mPrefs.getLong(PREF_KEY_RESTORE_COUNTDOWN_START_TIME, now);
            if (now - lastRestored >= CHANCE_INCREMENT_PERIOD) {
                int by = (int) ((now - lastRestored) / CHANCE_INCREMENT_PERIOD);
                lastRestored = now - (now - lastRestored) % CHANCE_INCREMENT_PERIOD;
                if (mChanceCount + by >= FULL_CHANCE_COUNT) {
                    by = FULL_CHANCE_COUNT - mChanceCount;
                    lastRestored = now;
                }
                mChanceCount += by;
                mPrefs.putInt(PREF_KEY_CHANCE_LEFT, mChanceCount);
            } else if (now - lastRestored < 0) {
                lastRestored = now;
            }
            mPrefs.putLong(PREF_KEY_RESTORE_COUNTDOWN_START_TIME, lastRestored);
            mListener.onChanceCountChanged(mChanceCount);
            if (mChanceCount >= FULL_CHANCE_COUNT) {
                HSLog.d(TAG, "No need to reset chance restore timer as chance count is already full");
                setRestoreTimeLeft(CHANCE_RESTORE_NOT_SCHEDULED);
            } else {
                long nextRestoreDelay = lastRestored + CHANCE_INCREMENT_PERIOD - now;
                final int currTimerReading = (int) Math.ceil(nextRestoreDelay / 1000f);
                setRestoreTimeLeft(currTimerReading);
            }
        }
    }

    public void start() {
        mStartTime = SystemClock.uptimeMillis();
        scheduleRestoreTimer();
        mChoreographer.postFrameCallback(mFrameCallback);
    }

    void pause(boolean pauseDrawing) {
        if (!isPaused()) {
            mPauseTime = SystemClock.uptimeMillis();
        }
        if (pauseDrawing) {
            mChoreographer.removeFrameCallback(mFrameCallback);
        }
    }

    void resume() {
        if (!isPaused()) {
            HSLog.w(TAG, "Game is not paused, skip resume");
            return;
        }

        // Offset start time by pause duration
        mStartTime += (SystemClock.uptimeMillis() - mPauseTime);
        mPauseTime = -1;
        mChoreographer.removeFrameCallback(mFrameCallback);
        mChoreographer.postFrameCallback(mFrameCallback);
    }

    private boolean isPaused() {
        return mPauseTime >= 0;
    }

    @Nullable
    TargetInfo performCatch() {
        decrementChanceCount();
        for (TargetInfo target : mTargets) {
            float activeRowIndex = mConfig.getActiveRowIndex();
            float toleranceVertical = mConfig.getCatchToleranceVertical();
            float toleranceHorizontal = mConfig.getCatchToleranceHorizontal();
            if (target.translation < activeRowIndex - 0.5f * toleranceVertical
                    || target.translation > activeRowIndex + 0.5f * toleranceVertical) {
                continue;
            }
            if (mArmPosition < target.trackIndex - 0.5f * toleranceHorizontal
                    || mArmPosition > target.trackIndex + 0.5f * toleranceHorizontal) {
                continue;
            }
            if (target.hasFlag(TargetInfo.FLAG_CAUGHT)) {
                continue;
            }
            target.setFlags(TargetInfo.FLAG_IN_CATCH_ANIMATION, true);
            startCatchAnimator(CATCH_ANIM_DURATION_CAUGHT);
            return target;
        }
        startCatchAnimator(CATCH_ANIM_DURATION_EMPTY);
        return null;
    }

    private void startCatchAnimator(long duration) {
        mCatchAnimator = new CatchAnimator(duration);
        mCatchAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCatchAnimator = null;
            }
        });
        mCatchAnimator.start();
    }

    public GameConfig getConfig() {
        return mConfig;
    }

    public List<TargetInfo> getTargets() {
        return mTargets;
    }

    public float getArmPosition() {
        return mArmPosition;
    }

    public float getBorderLightsRevealRatio() {
        return mBorderLightsRevealRatio;
    }

    /**
     * @return {@code null} if no catch animation is running.
     */
    public @Nullable
    CatchAnimator getCatchAnimator() {
        return mCatchAnimator;
    }

    void decrementChanceCount() {
        int beforeDecrement = mChanceCount;
        boolean resignFullCount = (beforeDecrement == FULL_CHANCE_COUNT);
        mChanceCount--;
        if (resignFullCount) {
            startRestoreTimer();
        }
        if (mChanceCount < 0) {
            mChanceCount = 0;
        }
        mPrefs.putInt(PREF_KEY_CHANCE_LEFT, mChanceCount);
        if (mListener != null && mChanceCount != beforeDecrement) {
            mListener.onChanceCountChanged(mChanceCount);
        }
    }

    void incrementChanceCount(int by) {
        boolean reachFullCount = (mChanceCount < FULL_CHANCE_COUNT && mChanceCount + by >= FULL_CHANCE_COUNT);
        mChanceCount += by;
        if (reachFullCount) {
            scheduleRestoreTimer();
        }
        mPrefs.putInt(PREF_KEY_CHANCE_LEFT, mChanceCount);
        if (mListener != null) {
            mListener.onChanceCountChanged(mChanceCount);
        }
    }

    // Private

    private Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (!mUserInterface.isInForeground()) {
                return;
            }
            long equivalentNow = isPaused() ? mPauseTime : (frameTimeNanos / 1000000);
            long timeSinceStart = equivalentNow - mStartTime;

            // Belt moves
            float beltSpeed = mConfig.getBeltSpeed();
            mBeltTranslation = beltSpeed * timeSinceStart / 1000f;
            updateTargets();

            // Arm moves
            float armSpeed = mConfig.getArmSpeed();
            int trackCount = mConfig.getTrackCount();
            float armTranslation = armSpeed * timeSinceStart / 1000f;
            float singleTripSpan = trackCount - 1;
            float singleTrips = armTranslation / singleTripSpan;
            int singleTripsInt = (int) singleTrips;
            float singleTripsDecimal = singleTrips - singleTripsInt;
            float smoothStep = mSmoothStepInterpolator.getInterpolation(singleTripsDecimal);
            if (singleTripsInt % 2 == 0) {
                mArmPosition = smoothStep * singleTripSpan;
            } else {
                mArmPosition = (1f - smoothStep) * singleTripSpan;
            }

            // Border lights reveal ratio update
            if (timeSinceStart < BORDER_LIGHTS_REVEAL_START_DELAY) {
                mBorderLightsRevealRatio = 0f;
            } else {
                mBorderLightsRevealRatio = Math.min(1f,
                        (float) (timeSinceStart - BORDER_LIGHTS_REVEAL_START_DELAY)
                                / BORDER_LIGHTS_REVEAL_DURATION);
            }

            if (mListener != null) {
                mListener.onStateAdvance();
            }
            mChoreographer.postFrameCallback(this);
        }
    };

    private TimeInterpolator mSmoothStepInterpolator = new TimeInterpolator() {
        @Override
        public float getInterpolation(float input) {
            return input * input * (3 - 2 * input);
        }
    };

    private void updateTargets() {
        // Add new targets if necessary
        int youngestBirthTime = -1;
        if (!mTargets.isEmpty()) {
            TargetInfo youngest = mTargets.get(mTargets.size() - 1);
            youngestBirthTime = youngest.birthTime;
        }
        int birthUntil = (int) mBeltTranslation;
        for (int birthTime = youngestBirthTime + 1; birthTime <= birthUntil; birthTime++) {
            TargetInfo.newTargets(mConfig, birthTime, mTargets);
        }

        // Remove targets that have moved out of game board
        int rowCount = mConfig.getRowCount();
        List<TargetInfo> removed = new ArrayList<>();
        for (TargetInfo target : mTargets) {
            if (target.birthTime < mBeltTranslation - rowCount - 1) {
                removed.add(target);
            } else {
                break;
            }
        }
        if (!removed.isEmpty()) {
            mTargets.removeAll(removed);
        }
        for (TargetInfo target : mTargets) {
            target.translation = mBeltTranslation - target.birthTime;
        }
    }

    private void startRestoreTimer() {
        mPrefs.putLong(PREF_KEY_RESTORE_COUNTDOWN_START_TIME, System.currentTimeMillis());
        setRestoreTimeLeft((int) (CHANCE_INCREMENT_PERIOD / 1000));
        scheduleRestoreTimer();
    }

    private void scheduleRestoreTimer() {
        if (mChanceCount >= FULL_CHANCE_COUNT) {
            HSLog.d(TAG, "No need to start chance restore timer as chance count is already full");
            setRestoreTimeLeft(CHANCE_RESTORE_NOT_SCHEDULED);
            return;
        }
        long now = System.currentTimeMillis();
        long lastRestored = mPrefs.getLong(PREF_KEY_RESTORE_COUNTDOWN_START_TIME, 0);
        if (now - lastRestored > CHANCE_INCREMENT_PERIOD) {
            incrementChanceCount(1);
            startRestoreTimer();
        } else {
            long nextRestoreDelay = lastRestored + CHANCE_INCREMENT_PERIOD - now;
            final int nextTimerReading = (int) (nextRestoreDelay / 1000);
            long updateDelay = nextRestoreDelay % 1000;
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRestoreTimeLeft(nextTimerReading);
                    scheduleRestoreTimer();
                }
            }, updateDelay);
            HSLog.d(TAG, "Post restore timer update to " + nextTimerReading + " to " + updateDelay + " ms in the future");
        }
    }

    private void setRestoreTimeLeft(int timeLeftSeconds) {
        if (mRestoreTimeLeftSeconds != timeLeftSeconds) {
            mRestoreTimeLeftSeconds = timeLeftSeconds;
            if (mListener != null) {
                mListener.onRestoreTimerUpdate(mRestoreTimeLeftSeconds);
            }
        }
    }

    public static class CatchAnimator extends ValueAnimator {
        /**
         * 0f     Catch                       Retrieve           1f (end)
         * |___________________|____________________________________|
         *
         *    Arm Down                         Arm Up
         * |______________|    |____________________________________|     ---------> Time
         *         Arm Close
         *     |_______________|
         *                     Light Offs
         *        |__1__|        |__2__|        |__3__|
         */
        private static final float ARM_DOWN_END_PROGRESS = 0.23f;
        private static final float ARM_CLOSE_START_PROGRESS = 0.1f;
        private static final float ARM_CLOSE_END_PROGRESS = 0.33f;
        private static final float ARM_UP_START_PROGRESS = 0.33f;
        private static final float LIGHT_OFF_1_START_PROGRESS = 0.2f;
        private static final float LIGHT_OFF_1_END_PROGRESS = 0.3f;
        private static final float LIGHT_OFF_2_START_PROGRESS = 0.4f;
        private static final float LIGHT_OFF_2_END_PROGRESS = 0.5f;
        private static final float LIGHT_OFF_3_START_PROGRESS = 0.6f;
        private static final float LIGHT_OFF_3_END_PROGRESS = 0.7f;

        static final int SECTION_FLAG_ARM_DOWN = 0x00000001;
        static final int SECTION_FLAG_ARM_CLOSE = 0x00000002;
        public static final int SECTION_FLAG_ARM_UP = 0x00000004;

        private ValueAnimator mAnimator;

        CatchAnimator(long duration) {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(duration);
            animator.setInterpolator(LauncherAnimUtils.LINEAR);
            mAnimator = animator;
        }

        @Override
        public void addListener(AnimatorListener listener) {
            mAnimator.addListener(listener);
        }

        @Override
        public void start() {
            mAnimator.start();
        }

        public int getRunningSectionFlags() {
            int flags = 0;
            float progress = (float) mAnimator.getAnimatedValue();
            if (progress < ARM_DOWN_END_PROGRESS) {
                flags |= SECTION_FLAG_ARM_DOWN;
            }
            if (progress >= ARM_CLOSE_START_PROGRESS && progress < ARM_CLOSE_END_PROGRESS) {
                flags |= SECTION_FLAG_ARM_CLOSE;
            }
            if (progress >= ARM_UP_START_PROGRESS) {
                flags |= SECTION_FLAG_ARM_UP;
            }
            return flags;
        }

        public float getArmDownProgress() {
            float progress = (float) mAnimator.getAnimatedValue();
            return Math.max(0f, Math.min(progress / ARM_DOWN_END_PROGRESS, 1f));
        }

        public float getArmCloseProgress() {
            float progress = (float) mAnimator.getAnimatedValue();
            return Math.max(0f, Math.min(
                    (progress - ARM_CLOSE_START_PROGRESS) / (ARM_CLOSE_END_PROGRESS - ARM_CLOSE_START_PROGRESS), 1f));
        }

        public float getArmUpProgress() {
            float progress = (float) mAnimator.getAnimatedValue();
            return Math.max(0f, Math.min((progress - ARM_UP_START_PROGRESS) / (1f - ARM_UP_START_PROGRESS), 1f));
        }

        public float getBorderLightAlpha() {
            float progress = (float) mAnimator.getAnimatedValue();
            if ((progress >= LIGHT_OFF_1_START_PROGRESS && progress < LIGHT_OFF_1_END_PROGRESS)
                    || (progress >= LIGHT_OFF_2_START_PROGRESS && progress < LIGHT_OFF_2_END_PROGRESS)
                    || (progress >= LIGHT_OFF_3_START_PROGRESS && progress < LIGHT_OFF_3_END_PROGRESS)) {
                return 0f;
            }
            return 1f;
        }
    }
}
