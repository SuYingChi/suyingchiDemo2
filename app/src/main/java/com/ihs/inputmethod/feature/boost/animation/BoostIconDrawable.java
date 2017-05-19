package com.ihs.inputmethod.feature.boost.animation;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.R;
import com.honeycomb.launcher.animation.DeviceEvaluator;
import com.honeycomb.launcher.animation.LauncherAnimUtils;
import com.honeycomb.launcher.boost.BoostConditionManager;
import com.honeycomb.launcher.boost.BoostIcon;
import com.honeycomb.launcher.boost.BoostSource;
import com.honeycomb.launcher.boost.BoostTipUtils;
import com.honeycomb.launcher.boost.BoostType;
import com.honeycomb.launcher.boost.RamUsageDisplayUpdater;
import com.honeycomb.launcher.dialog.LauncherTipManager;
import com.honeycomb.launcher.util.BitmapUtils;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.ConcurrentUtils;
import com.honeycomb.launcher.util.Utils;
import com.honeycomb.launcher.util.ViewUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import hugo.weaving.DebugLog;

/**
 * Drawable for animated boost icon.
 */
public class BoostIconDrawable extends AnimatedIconDrawable implements RamUsageDisplayUpdater.RamUsageChangeListener {

    private static final String TAG = BoostIconDrawable.class.getSimpleName();

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_VERBOSE = false && BuildConfig.DEBUG;

    /**
     * Relative to icon radius
     */
    private static final float PROGRESS_TRACK_STROKE_WIDTH_RATIO = 0.214f;
    private static final float PROGRESS_TRACK_MID_RADIUS_RATIO = 0.822f;

    private static final float VORTEX_RADIUS_RATIO = 1.55f;
    private static final float VORTEX_ROTATE_TOTAL_DEGREE = 1000f;
    private static final float CIRCLE_IN_RADIUS_RATIO = 1.35f;
    private static final float CIRCLE_MIDDLE_RADIUS_RATIO = 1.45f;
    private static final float CIRCLE_OUT_RADIUS_RATIO = 1.55f;
    private static final float CIRCLE_ROTATE_TOTAL_DEGREE = 3600f;
    private static final float DOT_TOTAL_SIZE = CommonUtils.pxFromDp(15);
    private static final float ICON_TOTAL_SIZE = CommonUtils.pxFromDp(28);
    private static final float ICON_DELAY_RATIO = 1.7f;
    private static final float ICON_MOVE_RATIO = 2f;

    private static final float FIGURE_TEXT_SIZE_RATIO = 0.357f;
    private static final float PERCENTAGE_MARKER_TEXT_SIZE_RATIO = 0.214f;

    /**
     * All durations are measured in frame indices
     */
    private static final int ANIM_PROGRESS_PAD_FIRST_EXPAND_START = 0;
    private static final int ANIM_PROGRESS_PAD_FIRST_EXPAND_END = 43;

    private static final int ANIM_PROGRESS_PAD_FIRST_SHRINK_START = 84;
    private static final int ANIM_PROGRESS_PAD_FIRST_SHRINK_END = 107;

    private static final int ANIM_PROGRESS_PAD_SECOND_EXPAND_START = 371;
    private static final int ANIM_PROGRESS_PAD_SECOND_EXPAND_END = 385;

    private static final int ANIM_VORTEX_EXPAND_START = 107;
    private static final int ANIM_VORTEX_EXPAND_END = 126;

    private static final int ANIM_ICON_START = 150;

    private static final int ANIM_VORTEX_SHRINK_START = 351;
    private static final int ANIM_VORTEX_SHRINK_END = 371;

    private static final int ANIM_WATER_RIPPLE_START = 461;

    private static final int ANIM_ALL_END = 450;

    private static final int RAM_ANIM_DURATION = 120;

    private static final int BOOST_ANIMATION_DURATION = 5800;

    private enum State {
        NORMAL, // Displaying a static memory usage
        BOOSTING, // Playing a full boost animation
        ANIMATING, // Playing fade in animation or RAM display changing animation, etc.
    }

    private State mState = State.NORMAL;

    private enum BoostAnimationMode {
        FULL, // For in-app animation
        SIMPLIFIED, // For foreign animation
    }

    private BoostType mDrawType = BoostType.RAM;
    private BoostType mResultType = BoostType.RAM;
    private BoostAnimationMode mMode;

    private Context mContext;

    private OvershootInterpolator mOvershootInterpolator;
    private TimeInterpolator mProgressBarInterpolator;
    private TimeInterpolator mProgressFigureInterpolator;
    private TimeInterpolator mVortexRotateInterpolator;
    private TimeInterpolator mDotInterpolator;

    private Paint mProgressBarPaint;
    private Paint mProgressFigurePaint;
    private Paint mProgressPercentageMarkPaint;
    private Paint mBitmapPaint;

    private Bitmap mBatteryIcon;
    private Bitmap mCpuTemperatureIcon;

    /**
     * RAM usage are expressed in percentage.
     */
    private int mDisplayedRamUsage;
    private int mFakeRamUsage = -1;
    private int mPercentageBoosted;
    private boolean mHidden;
    private boolean mShouldAnimateAppIcons;

    private int mCenterX, mCenterY;
    private float mIconRadius, mTrackMidRadius, mTrackStrokeWidth;
    private float mFigureTextSize, mPercentageMarkerTextSize;
    private Rect mTempRect;
    private RectF mTempRectF;

    /**
     * Animation progress measured by frame index. -1 for not animating.
     */
    private int mFrame = -1;

    private long mLastDrawBeginTime = -1;
    private int mFrameMissCount;

    private Bitmap mVortex;
    private Bitmap mCircleIn;
    private Bitmap mCircleMiddle;
    private Bitmap mCircleOut;
    private Bitmap mDot;

    private Bitmap mBoostPad;

    private List<Scene> mDots = new ArrayList<>();
    private List<Scene> mIcons = new ArrayList<>();

    private Bitmap[] mIconBitmaps;

    private int mBeforeBoostRamUsage;
    private int mAfterBoostRamUsage;

    BoostIconDrawable(Context context, int iconSize) {
        HSLog.d("BoostImageSize", "iconSize: " + iconSize);

        mContext = context;
        mBoostPad = Utils.decodeResourceWithFallback(context.getResources(), R.drawable.boost_pad);

        mMode = BoostAnimationMode.FULL;

        mIconRadius = iconSize / 2f;
        mTrackMidRadius = mIconRadius * PROGRESS_TRACK_MID_RADIUS_RATIO;
        mTrackStrokeWidth = mIconRadius * PROGRESS_TRACK_STROKE_WIDTH_RATIO;
        mFigureTextSize = iconSize * FIGURE_TEXT_SIZE_RATIO;
        mPercentageMarkerTextSize = iconSize * PERCENTAGE_MARKER_TEXT_SIZE_RATIO;

        mCenterX = mCenterY = Math.round(iconSize / 2f);

        mProgressBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressBarPaint.setStyle(Paint.Style.STROKE);
        mProgressBarPaint.setStrokeCap(Paint.Cap.ROUND);

        mProgressFigurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPercentageMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

        mOvershootInterpolator = (OvershootInterpolator) LauncherAnimUtils.OVERSHOOT;
        mProgressBarInterpolator = LauncherAnimUtils.DECELERATE_QUINT;
        mProgressFigureInterpolator = new ProgressFigureInterpolator();
        mVortexRotateInterpolator = new AccelerateDecelerateInterpolator();
        mDotInterpolator = new AccelerateInterpolator(2);

        mTempRect = new Rect();
        mTempRectF = new RectF();

        RamUsageDisplayUpdater ramUsageDisplayUpdater = RamUsageDisplayUpdater.getInstance();
        ramUsageDisplayUpdater.addRamUsageChangeListener(this);
        mDisplayedRamUsage = ramUsageDisplayUpdater.getDisplayedRamUsage();
    }

    @Override
    public void onDisplayedRamUsageChange(int displayedRamUsage, boolean isImmediatelyUpdate) {
        HSLog.i(TAG, "BoostIconDrawable onDisplayedRamUsageChange displayedRamUsage: " + displayedRamUsage + ", isImmediatelyUpdate: " + isImmediatelyUpdate);
        if (isImmediatelyUpdate) {
            stop();
            mFakeRamUsage = mDisplayedRamUsage;
            mDisplayedRamUsage = displayedRamUsage;
            invalidateSelf();
        } else {
            if (mState != State.BOOSTING) {
                mFakeRamUsage = mDisplayedRamUsage;
                mDisplayedRamUsage = displayedRamUsage;
                startRamUsageAnimation();
            }
        }
    }

    @Override
    public void onBoostComplete(int afterBoostRamUsage) {
        HSLog.i("BoostAnimationLogic", "Boost completed, before: " + mDisplayedRamUsage + ", after: " + afterBoostRamUsage);
        invalidateSelf();
    }

    private void startRamUsageAnimation() {
        mLastDrawBeginTime = -1;
        mFrame = -1;
        mState = State.ANIMATING;
        invalidateSelf();
    }

    public void setBoostType(BoostType type) {
        mResultType = type;
    }

    public void setDrawType(BoostType drawType) {
        mDrawType = drawType;
    }

    void setBoostAnimationMode(BoostAnimationMode mode) {
        mMode = mode;
    }

    private void postResult() {
        LauncherTipManager.getInstance().showFinishBoostAlert(mContext, BoostSource.IconDrawable, mResultType, mPercentageBoosted);
        // This function may cause NeedBoostTip live for 5 seconds. May block other tip show.
        // So we show it at last.
        BoostConditionManager.getInstance().reportBoostDone(mPercentageBoosted);
    }

    /**
     * Start boosting animation.
     * <p/>
     * Implementation of method on {@link android.graphics.drawable.Animatable}.
     */
    @Override
    public void start() {
        if (mIconBitmaps == null) {
            // Start the animation after loading the icons
            ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
                @Override
                public void run() {
                    initAnimationResources();
                    ConcurrentUtils.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            start();
                        }
                    });
                }
            });
            return;
        }

        LauncherTipManager.getInstance().setAnimationRunning(true, 0);

        // Set 0 as the starting frame
        mLastDrawBeginTime = -1;
        mFrameMissCount = 0;
        mShouldAnimateAppIcons = true;
        if (mMode == BoostAnimationMode.FULL) {
            mFrame = ANIM_PROGRESS_PAD_FIRST_SHRINK_START;
        } else if (mMode == BoostAnimationMode.SIMPLIFIED) {
            mFrame = ANIM_VORTEX_EXPAND_START;
        } else {
            // Unsupported
            return;
        }
        mFakeRamUsage = -1;
        mBeforeBoostRamUsage = RamUsageDisplayUpdater.getInstance().getDisplayedRamUsage();
        mAfterBoostRamUsage = RamUsageDisplayUpdater.getInstance().startBoost();
        if (mAfterBoostRamUsage <= 0) {
            mAfterBoostRamUsage = mBeforeBoostRamUsage;
            mShouldAnimateAppIcons = false;
        }
        mFakeRamUsage = mAfterBoostRamUsage;
        mPercentageBoosted = mBeforeBoostRamUsage - mAfterBoostRamUsage;

        mState = State.BOOSTING;
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                postResult();
            }
        }, BOOST_ANIMATION_DURATION);

        for (AnimationCallback callback : mCallbacks) {
            callback.onAnimationStart(this);
        }

        // Use boost animation as a sample to start runtime device performance evaluation
        DeviceEvaluator.startEvaluation();

        invalidateSelf();
    }

    @Override
    public void stop() {
        if (DEBUG_VERBOSE) {
            HSLog.i(TAG, "boost icon actions: animation stop!");
        }
        boolean runningBoostAnimation = (mState == State.BOOSTING);
        mState = State.NORMAL;
        mFrame = -1;
        if (runningBoostAnimation) {
            // Release icon bitmaps to save memory
            if (mIconBitmaps != null) {
                releaseAnimationResources();
            }

            for (AnimationCallback callback : mCallbacks) {
                callback.onAnimationEnd(this);
            }

            DeviceEvaluator.stopEvaluation();
            LauncherTipManager.getInstance().setAnimationRunning(false, 0);
        }
    }

    @Override
    public boolean isRunning() {
        return mState == State.BOOSTING;
    }

    @DebugLog
    private synchronized void initAnimationResources() {
        HSLog.d(TAG, "Init animation resources");

        Resources res = HSApplication.getContext().getResources();
        int resSizeLimit = (int) (2 * mIconRadius);
        mVortex = BitmapUtils.decodeSampledBitmap(res, R.drawable.boost_center_blackhole, resSizeLimit);
        mCircleOut = BitmapUtils.decodeSampledBitmap(res, R.drawable.boost_circle_out, resSizeLimit);
        mCircleIn = ViewUtils.getRotateAndScaleBitmap(mCircleOut, -90f, 0.78f, 0.78f, mCenterX, mCenterY);
        mCircleMiddle = ViewUtils.getRotateAndScaleBitmap(mCircleOut, 90f, 0.9f, 0.9f, mCenterX, mCenterY);
        mDot = Utils.decodeResourceWithFallback(res, R.drawable.boost_black_hole_dot);

        BoostAnimationManager boostAnimationManager = new BoostAnimationManager(0, 0);
        mIconBitmaps = boostAnimationManager.getBoostAppIconBitmaps(mContext);

        mDots.clear();
        Random random = new Random();
        for (int i = ANIM_VORTEX_EXPAND_END + 10; i < ANIM_VORTEX_SHRINK_START - 15; i += 12) {
            Scene dot = new Scene();
            dot.startFrame = i;
            dot.endFrame = 19 + i;

            float radius = random.nextFloat() * 0.5f + 1.2f;
            double radians = random.nextDouble() * 2 * Math.PI;
            dot.offsetX = (float) (radius * Math.sin(radians) * mIconRadius);
            dot.offsetY = (float) (radius * Math.cos(radians) * mIconRadius);
            mDots.add(dot);
        }

        mIcons.clear();
        for (int i = 0; i < BoostAnimationManager.COUNT_ICON; i++) {
            Scene icon = new Scene();
            icon.startFrame = (int) (ANIM_ICON_START +
                    BoostAnimationManager.TRANSLATE_START_DELAY_ICONS[i] * 60 / 1000 * ICON_DELAY_RATIO);
            icon.endFrame = (int) (icon.startFrame + (BoostAnimationManager.X_ICONS[i].length - 1)
                    * ICON_MOVE_RATIO);
            mIcons.add(icon);
        }
    }

    private synchronized void releaseAnimationResources() {
        HSLog.d(TAG, "Release animation resources");

        mVortex = null;
        mCircleIn = null;
        mCircleMiddle = null;
        mCircleOut = null;
        mDot = null;
        mIconBitmaps = null;
        mDots.clear();
        mIcons.clear();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        long drawBeginTimeMillis = SystemClock.uptimeMillis();
        if (mLastDrawBeginTime != -1) {
            int frameDelta = Math.round((drawBeginTimeMillis - mLastDrawBeginTime) / LauncherAnimUtils.FRAME_PERIOD_MILLIS);
            mFrame += frameDelta;
            if (frameDelta > 1) {
                mFrameMissCount += frameDelta - 1;
                if (DEBUG_VERBOSE) {
                    HSLog.v("AnimationProfiling", "Missed " + (frameDelta - 1) + " frame(s)");
                }
            }
        }
        mLastDrawBeginTime = drawBeginTimeMillis;

        if (mState == State.BOOSTING) {
            int endFrame = (mMode == BoostAnimationMode.FULL) ? ANIM_ALL_END : ANIM_VORTEX_SHRINK_END;
            if (mFrame <= endFrame) {
                drawProgressPad(canvas);
                drawVortex(canvas);
                drawCircle(canvas);
                drawHaloDots(canvas);
                if (mShouldAnimateAppIcons) {
                    drawBoostIcons(canvas);
                }
                drawBoostText(canvas);

                if (DEBUG_VERBOSE) {
                    long frameDrawTime = SystemClock.uptimeMillis() - drawBeginTimeMillis;
                    HSLog.v("AnimationProfiling", "Frame #" + mFrame + " draw time: " + frameDrawTime + " ms");
                }
                scheduleNextFrame();
            } else {
                HSLog.d("AnimationProfiling",
                        String.format(Locale.US, "Animation finished, missed %d (%.1f %%) frames", mFrameMissCount,
                                100f * mFrameMissCount / ANIM_ALL_END));
                mDisplayedRamUsage = mFakeRamUsage;
                stop();
                if (!mHidden) {
                    draw(canvas);
                }
                HSLog.i("BoostAnimationLogic", "Animation end, update displayed percentage to " + mDisplayedRamUsage);
            }
        } else if (mState == State.NORMAL) {
            drawProgressPad(canvas);
        } else { // mState == State.ANIMATING
            drawProgressPad(canvas);
            if (mFrame <= RAM_ANIM_DURATION) {
                scheduleNextFrame();
            } else {
                stop();
            }
        }
    }

    private void scheduleNextFrame() {
        if (DEBUG_VERBOSE) {
            HSLog.i(TAG, "boost icon actions: schedule next frame!");
        }
        scheduleSelf(new Runnable() {
            @Override
            public void run() {
                invalidateSelf();
            }
        }, SystemClock.uptimeMillis());
    }

    public void hide() {
        mHidden = true;
    }

    @Override
    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException("BoostIconDrawable does not support setAlpha().");
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        throw new UnsupportedOperationException("BoostIconDrawable does not support setColorFilter().");
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    private void drawProgressPad(Canvas canvas) {
        if (mState != State.BOOSTING) {
            drawProgressPadAtNormalSize(canvas);
        } else {
            // Draw expanding pad
            if (mFrame >= ANIM_PROGRESS_PAD_FIRST_EXPAND_START && mFrame <= ANIM_PROGRESS_PAD_FIRST_EXPAND_END) {
                float t = getProgress(mFrame, ANIM_PROGRESS_PAD_FIRST_EXPAND_START, ANIM_PROGRESS_PAD_FIRST_EXPAND_END);
                float inter = mOvershootInterpolator.getInterpolation(t);
                drawProgressPad(canvas, inter);
            }

            // Keep drawing pad at fixed size
            else if (mFrame < ANIM_PROGRESS_PAD_FIRST_SHRINK_START || mFrame > ANIM_PROGRESS_PAD_SECOND_EXPAND_END) {
                drawProgressPadAtNormalSize(canvas);
            }

            // Draw shrinking pad
            else if (mFrame <= ANIM_PROGRESS_PAD_FIRST_SHRINK_END) {
                float t = getProgress(mFrame, ANIM_PROGRESS_PAD_FIRST_SHRINK_START, ANIM_PROGRESS_PAD_FIRST_SHRINK_END);
                float inter = mOvershootInterpolator.getInterpolation(1 - t);
                drawProgressPad(canvas, inter);
            }

            // Draw expanding pad for the second time
            else if (mFrame <= ANIM_PROGRESS_PAD_SECOND_EXPAND_END) {
                mDrawType = null;
                float t = getProgress(mFrame, ANIM_PROGRESS_PAD_SECOND_EXPAND_START, ANIM_PROGRESS_PAD_SECOND_EXPAND_END);
                float inter = mOvershootInterpolator.getInterpolation(t);
                drawProgressPad(canvas, inter);
            }
        }

        if (mDrawType == null || mDrawType == BoostType.RAM) {
            drawProgress(canvas);
        }
    }

    private void drawProgressPadAtNormalSize(Canvas canvas) {
        mTempRectF.set(mCenterX - mIconRadius, mCenterY - mIconRadius, mCenterX + mIconRadius, mCenterY + mIconRadius);
        if (mDrawType == BoostType.BATTERY) {
            Bitmap icon = getBatteryIcon();
            mBitmapPaint.setAlpha(0xff);
            canvas.drawBitmap(icon, null, mTempRectF, mBitmapPaint);
        } else if (mDrawType == BoostType.CPU_TEMPERATURE) {
            Bitmap icon = getCpuTemperatureIcon();
            mBitmapPaint.setAlpha(0xff);
            canvas.drawBitmap(icon, null, mTempRectF, mBitmapPaint);
        } else {
            mBitmapPaint.setAlpha(0xff);
            canvas.drawBitmap(mBoostPad, null, mTempRectF, mBitmapPaint);
        }
    }

    private void drawProgressPad(Canvas canvas, float interpolation) {
        int alpha = Math.min((int) interpolation * 0xff, 0xff);
        Bitmap icon;
        if (mDrawType == BoostType.BATTERY) {
            icon = getBatteryIcon();
            mBitmapPaint.setAlpha(alpha);
        } else if (mDrawType == BoostType.CPU_TEMPERATURE) {
            icon = getCpuTemperatureIcon();
            mBitmapPaint.setAlpha(alpha);
        } else { // boostType == BoostType.RAM || boostType == null
            icon = mBoostPad;
            mBitmapPaint.setAlpha(0xff);
        }
        mTempRectF.set(mCenterX - interpolation * mIconRadius, mCenterY - interpolation * mIconRadius,
                mCenterX + interpolation * mIconRadius, mCenterY + interpolation * mIconRadius);
        canvas.drawBitmap(icon, null, mTempRectF, mBitmapPaint);
    }

    private Bitmap getBatteryIcon() {
        if (mBatteryIcon == null) {
            mBatteryIcon = Utils.decodeResourceWithFallback(HSApplication.getContext().getResources(),
                    R.drawable.ic_boost_battery);
        }
        return mBatteryIcon;
    }

    private Bitmap getCpuTemperatureIcon() {
        if (mCpuTemperatureIcon == null) {
            mCpuTemperatureIcon = Utils.decodeResourceWithFallback(HSApplication.getContext().getResources(),
                    R.drawable.ic_boost_temperature);
        }
        return mCpuTemperatureIcon;
    }

    private void drawProgress(Canvas canvas) {
        if (mState == State.NORMAL) {
            @ColorInt int color = BoostIcon.getProgressColor(mDisplayedRamUsage);
            drawProgressBar(canvas, mDisplayedRamUsage, color, 1f, 0xff);
            drawProgressText(canvas, mDisplayedRamUsage, color, 1f, 0xff);
        } else if (mState == State.ANIMATING) {
            float t = getProgress(mFrame, 0, RAM_ANIM_DURATION);
            float inter = mProgressBarInterpolator.getInterpolation(t);
            float percentage = mFakeRamUsage + inter * (mDisplayedRamUsage - mFakeRamUsage);
            @ColorInt int color = getProgressColor(mFakeRamUsage, mDisplayedRamUsage, percentage);
            drawProgressBar(canvas, percentage, color, 1f, 0xff);
            drawProgressText(canvas, Math.round(percentage), color, 1f, 0xff);
        } else { // mState == State.BOOSTING
            // Percentage 0% --> before boost
            if (mFrame > ANIM_PROGRESS_PAD_FIRST_EXPAND_END && mFrame < ANIM_PROGRESS_PAD_FIRST_SHRINK_START) {
                float t = getProgress(mFrame, ANIM_PROGRESS_PAD_FIRST_EXPAND_END, ANIM_PROGRESS_PAD_FIRST_SHRINK_START);
                float barInter = mProgressBarInterpolator.getInterpolation(t);
                float barPercentage = 0f + barInter * mDisplayedRamUsage;
                @ColorInt int color = getProgressColor(0f, mDisplayedRamUsage, barPercentage);
                drawProgressBar(canvas, barPercentage, color, 1f, 0xff);
                float textInter = mProgressFigureInterpolator.getInterpolation(t);
                int textPercentage = Math.round(0 + textInter * mDisplayedRamUsage);
                drawProgressText(canvas, textPercentage, color, barInter, (int) Math.min(0xff, 0xff * barInter));
            }

            // Text fade out before boost fan animation
            else if (mFrame >= ANIM_PROGRESS_PAD_FIRST_SHRINK_START && mFrame <= ANIM_PROGRESS_PAD_FIRST_SHRINK_END) {
                float t = getProgress(mFrame, ANIM_PROGRESS_PAD_FIRST_SHRINK_START, ANIM_PROGRESS_PAD_FIRST_SHRINK_END);
                float inter = mOvershootInterpolator.getInterpolation(1 - t);
                @ColorInt int color = BoostIcon.getProgressColor(mDisplayedRamUsage);
                drawProgressBar(canvas, mDisplayedRamUsage, color, inter, (int) Math.min(0xff, 0xff * inter));
                drawProgressText(canvas, mDisplayedRamUsage, color, inter, (int) Math.min(0xff, 0xff * inter));
            }

            // Text fade in after boost fan animation
            else if (mFrame >= ANIM_PROGRESS_PAD_SECOND_EXPAND_START && mFrame <= ANIM_PROGRESS_PAD_SECOND_EXPAND_END) {
                float t = getProgress(mFrame, ANIM_PROGRESS_PAD_SECOND_EXPAND_START, ANIM_PROGRESS_PAD_SECOND_EXPAND_END);
                float inter = mOvershootInterpolator.getInterpolation(t);
                @ColorInt int color = BoostIcon.getProgressColor(mDisplayedRamUsage);
                drawProgressBar(canvas, mDisplayedRamUsage, color, inter, (int) Math.min(0xff, 0xff * inter));
                drawProgressText(canvas, mDisplayedRamUsage, color, inter, (int) Math.min(0xff, 0xff * inter));
            }

            // Percentage before --> after boost
            else if (mFrame > ANIM_PROGRESS_PAD_SECOND_EXPAND_END) {
                float t = getProgress(mFrame, ANIM_PROGRESS_PAD_SECOND_EXPAND_END, ANIM_ALL_END);
                float inter = mProgressBarInterpolator.getInterpolation(t);
                float percentage = mDisplayedRamUsage + inter * (mFakeRamUsage - mDisplayedRamUsage);
                @ColorInt int color = getProgressColor(mDisplayedRamUsage, mFakeRamUsage, percentage);
                drawProgressBar(canvas, percentage, color, 1f, 0xff);
                drawProgressText(canvas, Math.round(percentage), color, 1f, 0xff);
            }
        }
    }

    private void drawProgressBar(Canvas canvas, float percentage, @ColorInt int color, float scale, int alpha) {
        mProgressBarPaint.setAlpha(alpha);
        float trackStrokeWidth = mTrackStrokeWidth * scale;
        mProgressBarPaint.setStrokeWidth(trackStrokeWidth);
        mProgressBarPaint.setColor(color);

        // Draw the progress bar
        float radius = mTrackMidRadius * scale;
        mTempRectF.set(mCenterX - radius, mCenterY - radius, mCenterX + radius, mCenterY + radius);
        float angle = 360f * percentage / 100f;
        canvas.drawArc(mTempRectF, -90f, angle, false, mProgressBarPaint);
    }

    private void drawProgressText(Canvas canvas, int percentage, @ColorInt int color, float scale, int alpha) {
        mProgressFigurePaint.setAlpha(alpha);
        mProgressFigurePaint.setColor(color);
        mProgressFigurePaint.setTextSize(mFigureTextSize * scale);
        mProgressPercentageMarkPaint.setAlpha(alpha);
        mProgressPercentageMarkPaint.setColor(color);
        mProgressPercentageMarkPaint.setTextSize(mPercentageMarkerTextSize * scale);

        String percentString = String.valueOf(percentage);
        mProgressFigurePaint.getTextBounds(percentString, 0, percentString.length(), mTempRect);
        float x;
        if (mFrame >= ANIM_VORTEX_EXPAND_START && mFrame <= ANIM_VORTEX_SHRINK_END) {
            x = scale < 0.99f ? (float) Math.floor(mCenterX - 0.73f * mTempRect.width()) :
                    (float) Math.floor(0.47f * mCenterX);
        } else {
            x = (float) Math.floor(mCenterX - 0.73f * mTempRect.width());
        }
        canvas.drawText(percentString, x,
                (float) Math.floor(mCenterY + 0.5f * mTempRect.height()),
                mProgressFigurePaint);
        canvas.drawText("%",
                (float) Math.floor(mCenterX + 0.4f * mTempRect.width()),
                (float) Math.floor(mCenterY + 0.5f * mTempRect.height()),
                mProgressPercentageMarkPaint);
    }

    @ColorInt
    private static int getProgressColor(float start, float end, float current) {
        @ColorInt int startColor = BoostIcon.getProgressColor((int) start);
        if (start == end) return startColor;
        @ColorInt int endColor = BoostIcon.getProgressColor((int) end);
        float startProportion = (current - start) / (end - start);
        return BoostTipUtils.interpolateColorHsv(startColor, endColor, startProportion);
    }

    private void drawVortex(Canvas canvas) {
        if (mFrame <= ANIM_VORTEX_SHRINK_START) {
            mBitmapPaint.setAlpha(0xff);
        } else {
            mBitmapPaint.setAlpha((int) (0xff * getBoostScale()));
        }
        float t = getProgress(mFrame, ANIM_VORTEX_EXPAND_START, ANIM_VORTEX_SHRINK_END);
        float angle = mVortexRotateInterpolator.getInterpolation(t) * VORTEX_ROTATE_TOTAL_DEGREE;

        float padRadius = getBoostScale() * mIconRadius * VORTEX_RADIUS_RATIO;
        if (padRadius <= 0) return;
        mTempRectF.set(mCenterX - padRadius, mCenterY - padRadius, mCenterX + padRadius, mCenterY + padRadius);
        rotateDrawBitmap(mVortex, canvas, angle);
    }

    private void drawCircle(Canvas canvas) {
        float t = getProgress(mFrame, ANIM_VORTEX_EXPAND_START, ANIM_VORTEX_SHRINK_END);
        float angle = mVortexRotateInterpolator.getInterpolation(t) * CIRCLE_ROTATE_TOTAL_DEGREE;

        float padRadiusRatio = getBoostScale();
        float padRadius = padRadiusRatio * mIconRadius * CIRCLE_IN_RADIUS_RATIO;
        mTempRectF.set(mCenterX - padRadius, mCenterY - padRadius, mCenterX + padRadius, mCenterY + padRadius);
        if (mFrame < ANIM_VORTEX_SHRINK_START) {
            mBitmapPaint.setAlpha((int) (getProgress(mFrame, ANIM_VORTEX_EXPAND_START, (int) (ANIM_VORTEX_EXPAND_START + 28 * 2.4)) * 0xff));
        } else {
            mBitmapPaint.setAlpha((int) (0xff * getBoostScale()));
        }
        rotateDrawBitmap(mCircleIn, canvas, angle);

        padRadius = padRadiusRatio * mIconRadius * CIRCLE_MIDDLE_RADIUS_RATIO;
        mTempRectF.set(mCenterX - padRadius, mCenterY - padRadius, mCenterX + padRadius, mCenterY + padRadius);
        if (mFrame < ANIM_VORTEX_SHRINK_START) {
            mBitmapPaint.setAlpha((int) (getProgress(mFrame, (int) (ANIM_VORTEX_EXPAND_START + 8 * 2.4), (int) (ANIM_VORTEX_EXPAND_START + 36 * 2.4)) * 0xff));
        } else {
            mBitmapPaint.setAlpha((int) (0xff * getBoostScale()));
        }
        rotateDrawBitmap(mCircleMiddle, canvas, angle);

        padRadius = padRadiusRatio * mIconRadius * CIRCLE_OUT_RADIUS_RATIO;
        mTempRectF.set(mCenterX - padRadius, mCenterY - padRadius, mCenterX + padRadius, mCenterY + padRadius);
        if (mFrame < ANIM_VORTEX_SHRINK_START) {
            mBitmapPaint.setAlpha((int) (getProgress(mFrame, (int) (ANIM_VORTEX_EXPAND_START + 16 * 2.4), (int) (ANIM_VORTEX_EXPAND_START + 44 * 2.4)) * 0xff));
        } else {
            mBitmapPaint.setAlpha((int) (0xff * getBoostScale()));
        }
        rotateDrawBitmap(mCircleOut, canvas, angle);
    }

    private void drawHaloDots(Canvas canvas) {
        if (mFrame < ANIM_VORTEX_EXPAND_END || mFrame > ANIM_VORTEX_SHRINK_START) {
            return;
        }
        synchronized (this) {
            for (Scene dot : mDots) {
                if (mFrame < dot.startFrame || mFrame > dot.endFrame) {
                    continue;
                }
                float inter = 1 - mDotInterpolator.getInterpolation(getProgress(mFrame, dot.startFrame, dot.endFrame));
                mBitmapPaint.setAlpha((int) (0.2f * 255 + 0.8f * inter * 255));
                mTempRectF.set(mCenterX - dot.offsetX * inter, mCenterY - dot.offsetY * inter, mCenterX - dot.offsetX * inter + DOT_TOTAL_SIZE * inter, mCenterY - dot.offsetY * inter + DOT_TOTAL_SIZE * inter);
                canvas.drawBitmap(mDot, null, mTempRectF, mBitmapPaint);
            }
        }
    }

    private void drawBoostIcons(Canvas canvas) {
        if (mFrame < ANIM_VORTEX_EXPAND_END || mFrame > ANIM_VORTEX_SHRINK_START) {
            return;
        }
        synchronized (this) {
            for (int i = 0; i < mIcons.size(); i++) {
                if (mIconBitmaps[i] == null) {
                    continue;
                }
                Scene icon = mIcons.get(i);
                if (mFrame < icon.startFrame || mFrame >= icon.endFrame) {
                    continue;
                }
                float ratio = (mFrame - icon.startFrame) / ICON_MOVE_RATIO;
                double posX = (ratio - Math.floor(ratio)) * BoostAnimationManager.X_ICONS[i][(int) Math.floor(ratio) + 1] + (Math.floor(ratio) + 1 - ratio) * BoostAnimationManager.X_ICONS[i][(int) Math.floor(ratio)];
                double posY = (ratio - Math.floor(ratio)) * BoostAnimationManager.Y_ICONS[i][(int) Math.floor(ratio) + 1] + (Math.floor(ratio) + 1 - ratio) * BoostAnimationManager.Y_ICONS[i][(int) Math.floor(ratio)];
                int x = mCenterX - (int) ((540f - posX) / 450 * mIconRadius);
                int y = mCenterY - (int) ((780f - posY) / 450 * mIconRadius);
                float t = 1 - getProgress(mFrame, icon.startFrame, icon.endFrame) * 0.8f;
                mBitmapPaint.setAlpha((int) (255 * t));
                mTempRectF.set(x - ICON_TOTAL_SIZE * t / 2, y - ICON_TOTAL_SIZE * t / 2, x + ICON_TOTAL_SIZE * t / 2, y + ICON_TOTAL_SIZE * t / 2);

                float inputRotate = getProgress(mFrame, icon.startFrame, icon.endFrame);
                float rotateAngle = inputRotate * inputRotate * 180;
                rotateDrawBitmap(mIconBitmaps[i], canvas, rotateAngle - 45);
            }
        }
    }

    private void drawBoostText(Canvas canvas) {
        if (mFrame > ANIM_VORTEX_EXPAND_START && mFrame < ANIM_VORTEX_SHRINK_END) {
            int progress = (int) (mBeforeBoostRamUsage + (mAfterBoostRamUsage - mBeforeBoostRamUsage)
                    * mVortexRotateInterpolator.getInterpolation(getProgress(mFrame, ANIM_VORTEX_EXPAND_END, ANIM_VORTEX_SHRINK_START)));
            drawProgressText(canvas, progress, Color.WHITE, getBoostScale() * 1.1f, (int) (255 * getBoostScale()));
        }
    }

    private float getBoostScale() {
        float scale = 0;
        if (mFrame <= ANIM_VORTEX_EXPAND_END) {
            scale = getProgress(mFrame, ANIM_VORTEX_EXPAND_START, ANIM_VORTEX_EXPAND_END);
        } else if (mFrame < ANIM_VORTEX_SHRINK_START) {
            scale = 1f;
        } else if (mFrame <= ANIM_VORTEX_SHRINK_END) {
            scale = (1 - getProgress(mFrame, ANIM_VORTEX_SHRINK_START, ANIM_VORTEX_SHRINK_END));
        }
        return scale;
    }

    private void rotateDrawBitmap(Bitmap bitmap, Canvas canvas, float angleDegree) {
        canvas.rotate(angleDegree, mCenterX, mCenterY);
        canvas.drawBitmap(bitmap, null, mTempRectF, mBitmapPaint);
        canvas.rotate(-angleDegree, mCenterX, mCenterY);
    }

    private float getProgress(int currentFrame, int startFrame, int endFrame) {
        float progressUnclamped = (float) (currentFrame - startFrame) / (endFrame - startFrame);
        return Math.max(0f, Math.min(progressUnclamped, 1f));
    }

    private static class ProgressFigureInterpolator implements TimeInterpolator {
        @Override
        public float getInterpolation(float input) {
            input = Math.max(0f, Math.min(input, 1f));
            return input;
        }
    }

    private static class Scene {
        int startFrame;
        int endFrame;
        float offsetX;
        float offsetY;
    }
}
