package com.ihs.inputmethod.feature.lucky.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.R;
import com.honeycomb.launcher.lucky.GameConfig;
import com.honeycomb.launcher.lucky.GameState;
import com.honeycomb.launcher.lucky.TargetInfo;
import com.honeycomb.launcher.util.Thunk;
import com.ihs.commons.utils.HSLog;

import java.util.List;

public class GameScene extends View {

    private static final String TAG = GameScene.class.getSimpleName();

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean PROFILE = false && BuildConfig.DEBUG;

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_PROJECTION = false && BuildConfig.DEBUG;

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_VERBOSE = false && BuildConfig.DEBUG;

    private static final @ColorInt
    int BELT_COLOR_FAR = 0xff3e4d87;
    private static final @ColorInt
    int BELT_COLOR_NEAR = 0xff84a6ff;
    private static final @ColorInt
    int DIVIDER_COLOR_FAR = 0xff354179;
    private static final @ColorInt
    int DIVIDER_COLOR_NEAR = 0xffa6bdff;
    private static final @ColorInt
    int BORDER_LINE_COLOR = 0xff00fffe;
    private static final @ColorInt
    int BORDER_OUTER_LINE_COLOR = 0xaa184bcd;

    private static final float TARGET_ALPHA_FAR = 0.5f;
    private static final float TARGET_OPAQUE_POSITION_RATIO = 0.4f;
    private static final float TARGET_PHYSICS_CENTER_Y_RATIO = 0.65f;

    private static final float HORIZONTAL_POSITION_EXTENSION = 0.07f;

    private static final float EMPTY_ARM_CLOSE_ANGLE_RADIANS = 0.36f;

    /** (Bottom of banner - top of mechanical arm) / banner height */
    private static final float ARM_TOP_OFFSET_RATIO = 0.27f;

    private GameState mState;

    private Paint mBeltPaint;
    private Paint mActiveZonePaint;
    private Paint mDividerPaint;
    private Paint mBorderLinePaint;
    private Paint mBorderLineOuterPaint;
    private Paint mBitmapPaint;

    private Bitmap mActiveZoneShaderBitmap;
    private Bitmap mBorderLightBitmapLeft;
    private Bitmap mBorderLightBitmapRight;

    private Arm mArm;

    private Projector mPrj;

    private float mBannerHeight;

    private final float mDividerWidth;
    private final float mLargeBoxWidth;
    private final float mSmallBoxWidth;
    private final float mBombWidth;
    private final float mArmBottomOffsetUp;
    private final float mBorderOffsetInner;
    private final float mBorderOffsetOuter;
    private final float mEmptyCatchTranslationY;

    /**
     * Manually maintains two software drawing caches containing the game board for performance.
     */
    private DrawingCacheInfo[] mBoardCaches;

    private static final int DRAW_COMPONENT_BELT = 0;
    private static final int DRAW_COMPONENT_BORDER_LIGHTS = 1;

    private float[] mOutVal = new float[1];
    @Thunk float[] mInCoord = new float[2];
    @Thunk float[] mOutCoord = new float[2];
    private int[] mCoords = new int[2];
    private Rect mRect = new Rect();
    @Thunk RectF mRectF = new RectF();

    private long mLastDrawTime;

    public GameScene(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();

        mBeltPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBeltPaint.setStyle(Paint.Style.FILL);
        mBeltPaint.setDither(true);

        mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDividerPaint.setStyle(Paint.Style.FILL);
        mDividerPaint.setDither(true);

        mActiveZonePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mActiveZonePaint.setStyle(Paint.Style.FILL);

        mBorderLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderLinePaint.setStyle(Paint.Style.STROKE);
        mBorderLinePaint.setStrokeWidth(res.getDimension(R.dimen.lucky_game_border_line_width));
        mBorderLinePaint.setColor(BORDER_LINE_COLOR);

        mBorderLineOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderLineOuterPaint.setStyle(Paint.Style.STROKE);
        mBorderLineOuterPaint.setStrokeWidth(res.getDimension(R.dimen.lucky_game_border_outer_line_width));
        mBorderLineOuterPaint.setColor(BORDER_OUTER_LINE_COLOR);

        mBitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mBitmapPaint.setStyle(Paint.Style.FILL);

        mDividerWidth = res.getDimension(R.dimen.lucky_game_belt_divider_width);
        mLargeBoxWidth = res.getDimension(R.dimen.lucky_game_large_box_width);
        mSmallBoxWidth = res.getDimension(R.dimen.lucky_game_small_box_width);
        mBombWidth = res.getDimension(R.dimen.lucky_game_bomb_width);
        mArmBottomOffsetUp = res.getDimension(R.dimen.lucky_game_arm_bottom_offset_up);
        mBorderOffsetInner = res.getDimension(R.dimen.lucky_game_border_line_offset_inner);
        mBorderOffsetOuter = res.getDimension(R.dimen.lucky_game_border_line_offset_outer);
        mEmptyCatchTranslationY = res.getDimension(R.dimen.lucky_game_empty_catch_arm_anim_translation_y);

        mActiveZoneShaderBitmap = BitmapFactory.decodeResource(res, R.drawable.lucky_belt_active_zone);
        mBorderLightBitmapLeft = BitmapFactory.decodeResource(res, R.drawable.lucky_light_left);
        mBorderLightBitmapRight = BitmapFactory.decodeResource(res, R.drawable.lucky_light_right);
    }

    public void setState(GameState state) {
        mState = state;
    }

    public void setBannerHeight(int bannerHeight) {
        mBannerHeight = bannerHeight;

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int boardHeight = viewHeight - bannerHeight;
        mPrj = new Projector(bannerHeight, viewWidth, boardHeight);

        mBoardCaches = new DrawingCacheInfo[] {
                new DrawingCacheInfo(viewWidth, boardHeight, DRAW_COMPONENT_BELT),
                new DrawingCacheInfo(viewWidth, boardHeight, DRAW_COMPONENT_BORDER_LIGHTS)};

        mArm = new Arm(getContext(), bannerHeight * (1f - ARM_TOP_OFFSET_RATIO));

        Shader beltShader = new LinearGradient(0f, bannerHeight, viewWidth / 2, viewHeight,
                BELT_COLOR_FAR, BELT_COLOR_NEAR, Shader.TileMode.CLAMP);
        mBeltPaint.setShader(beltShader);

        Shader dividerShader = new LinearGradient(0f, bannerHeight, 0f, viewHeight,
                DIVIDER_COLOR_FAR, DIVIDER_COLOR_NEAR, Shader.TileMode.CLAMP);
        mDividerPaint.setShader(dividerShader);

        Shader activeZoneShader = new BitmapShader(mActiveZoneShaderBitmap,
                Shader.TileMode.MIRROR, Shader.TileMode.CLAMP);
        mActiveZonePaint.setShader(activeZoneShader);

        // Heat up drawing cache in advance to avoid first-frame jitter in animation
        buildDrawingCache(mBoardCaches[0]);
        buildDrawingCache(mBoardCaches[1]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPrj == null) {
            HSLog.i(TAG, "Projector not initialized, skip drawing");
            return;
        }
        super.onDraw(canvas);

        if (PROFILE) {
            profile();
        }

        drawGameBoard(canvas);

        // Draw all targets except one in catch animation. The drawing of this target is deferred until arm is drawn.
        PendingTarget pendingTarget = drawTargets(canvas);

        // Draw the arm. Report pulled-up distance of the target being caught.
        float pullUpY = drawArm(canvas, pendingTarget);

        if (pendingTarget != null) {
            // The the pending target, if there is one.
            drawPendingTarget(canvas, pendingTarget, -pullUpY);
        }
    }

    private void profile() {
        long now = SystemClock.uptimeMillis();
        if (mLastDrawTime > 0) {
            long timeSinceLastDraw = now - mLastDrawTime;
            if (DEBUG_VERBOSE) HSLog.v(TAG + ".Profile", "onDraw(): " + timeSinceLastDraw + " ms since last call");
        }
        mLastDrawTime = now;
    }

    private void drawGameBoard(Canvas canvas) {
        for (DrawingCacheInfo cacheInfo : mBoardCaches) {
            if (cacheInfo.hasBuilt) {
                // Draw from cache
                GameState.CatchAnimator catchAnim = mState.getCatchAnimator();
                if (cacheInfo.component != DRAW_COMPONENT_BORDER_LIGHTS || catchAnim == null) {
                    mBitmapPaint.setAlpha(0xff);
                } else {
                    float alpha = catchAnim.getBorderLightAlpha();
                    mBitmapPaint.setAlpha((int) (0xff * alpha));
                }
                float revealRatio = 1f;
                if (cacheInfo.component == DRAW_COMPONENT_BORDER_LIGHTS) {
                    revealRatio = mState.getBorderLightsRevealRatio();
                }
                mRect.set(0, 0, cacheInfo.cache.getWidth(),
                        (int) (revealRatio * cacheInfo.cache.getHeight()));
                mRectF.set(0, mBannerHeight, cacheInfo.cache.getWidth(),
                        mBannerHeight + revealRatio * cacheInfo.cache.getHeight());
                canvas.drawBitmap(cacheInfo.cache, mRect, mRectF, mBitmapPaint);
            } else {
                buildDrawingCache(cacheInfo);

                // Draw directly to on-screen canvas
                performDrawBoardComponent(canvas, cacheInfo.component, false);
            }
        }
    }

    private void buildDrawingCache(DrawingCacheInfo cacheInfo) {
        if (cacheInfo.enabled) {
            Canvas cacheCanvas = new Canvas(cacheInfo.cache);
            cacheCanvas.translate(0, -mBannerHeight);

            // Draw to cache
            performDrawBoardComponent(cacheCanvas, cacheInfo.component, true);
            cacheInfo.hasBuilt = true;
        }
    }

    /**
     * @param cacheDraw {@code true} if we're drawing to cache bitmap, {@code false} if we're drawing to screen.
     */
    private void performDrawBoardComponent(Canvas canvas, int which, boolean cacheDraw) {
        switch (which) {
            case DRAW_COMPONENT_BELT:
                performDrawBelt(canvas);
                break;
            case DRAW_COMPONENT_BORDER_LIGHTS:
                performDrawBorderLights(canvas, cacheDraw);
                break;
        }
    }

    private void performDrawBelt(Canvas canvas) {
        GameConfig config = mState.getConfig();
        float activeRowIndex = config.getActiveRowIndex();
        int trackCount = config.getTrackCount();
        int rowCount = config.getRowCount();
        float toleranceV = config.getCatchToleranceVertical();

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Draw belt color
        mRectF.set(0, 0, viewWidth, viewHeight);
        mPrj.drawRect(canvas, mRectF, mBeltPaint);

        // Draw track dividers
        float perTrackWidth = getWidth() / (float) trackCount;
        for (int i = 1; i < trackCount; i++) {
            float dividerX = i * perTrackWidth;
            mRectF.set(dividerX - mDividerWidth / 2, 0, dividerX + mDividerWidth / 2, viewHeight);
            mPrj.drawRect(canvas, mRectF, mDividerPaint);
        }

        // Draw active zone
        float activeZoneTop = ((activeRowIndex - 0.5f - 0.5f * toleranceV) / (float) rowCount) * viewHeight;
        float activeZoneBottom = ((activeRowIndex - 0.5f + 0.5f * toleranceV) / (float) rowCount) * viewHeight;
        mRectF.set(0, activeZoneTop, viewWidth, activeZoneBottom);
        mPrj.drawRect(canvas, mRectF, mActiveZonePaint, mActiveZoneShaderBitmap);

        // Draw border lines
        mPrj.drawLine(canvas, -mBorderOffsetOuter, 0f, -mBorderOffsetOuter, viewHeight, mBorderLinePaint);
        mPrj.drawLine(canvas, -mBorderOffsetInner, 0f, -mBorderOffsetInner, viewHeight, mBorderLinePaint);
        mPrj.drawLine(canvas, viewWidth + mBorderOffsetInner, 0f, viewWidth + mBorderOffsetInner, viewHeight, mBorderLinePaint);
        mPrj.drawLine(canvas, viewWidth + mBorderOffsetOuter, 0f, viewWidth + mBorderOffsetOuter, viewHeight, mBorderLinePaint);
        mRectF.set(-mBorderOffsetOuter - mBorderOffsetInner, 0f, 0f, viewHeight);
        mPrj.drawRect(canvas, mRectF, mBorderLineOuterPaint);
        mRectF.set(viewWidth, 0f, viewWidth + mBorderOffsetOuter + mBorderOffsetInner, viewHeight);
        mPrj.drawRect(canvas, mRectF, mBorderLineOuterPaint);
    }

    private void performDrawBorderLights(Canvas canvas, boolean cacheDraw) {
        int lightCount = mState.getConfig().getBorderLightCount();
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        float lightWidth = 4 * (mBorderOffsetOuter - mBorderOffsetInner);
        float lightHeight = lightWidth * ((float) mBorderLightBitmapLeft.getHeight() / mBorderLightBitmapLeft.getWidth());

        GameState.CatchAnimator catchAnim = mState.getCatchAnimator();
        if (cacheDraw || catchAnim == null) {
            mBitmapPaint.setAlpha(0xff);
        } else {
            float alpha = catchAnim.getBorderLightAlpha();
            mBitmapPaint.setAlpha((int) (0xff * alpha));
        }

        float revealRatio = 1f;
        if (!cacheDraw) { // For cache building, draw all lights regardless of current reveal ratio
            revealRatio = mState.getBorderLightsRevealRatio();
        }
        for (int i = 1, n = (int) (lightCount * revealRatio); i <= n; i++) {
            float lightX = -(mBorderOffsetInner + mBorderOffsetOuter) / 2f;
            float lightY = ((float) i / lightCount) * viewHeight;
            mRectF.set(lightX - lightWidth / 2f, lightY - lightHeight / 2f,
                    lightX + lightWidth / 2f, lightY + lightHeight / 2f);
            mPrj.drawBitmap(canvas, mBorderLightBitmapLeft, mRectF, mBitmapPaint);
            lightX = viewWidth + (mBorderOffsetInner + mBorderOffsetOuter) / 2f;
            mRectF.set(lightX - lightWidth / 2f, lightY - lightHeight / 2f,
                    lightX + lightWidth / 2f, lightY + lightHeight / 2f);
            mPrj.drawBitmap(canvas, mBorderLightBitmapRight, mRectF, mBitmapPaint);
        }
    }

    /**
     * Draw all targets of current game state except the one in catch animation.
     *
     * @return Drawing info of the target currently in catch animation. Or null.
     */
    private PendingTarget drawTargets(Canvas canvas) {
        List<TargetInfo> targets = mState.getTargets();
        PendingTarget pendingTarget = null;
        for (TargetInfo target : targets) {
            if (target.hasFlag(TargetInfo.FLAG_CAUGHT)) {
                continue;
            }
            mapTargetCoordinates(target, mCoords, mBitmapPaint);

            Bitmap bitmap = target.getBitmap();

            float halfWidth = 0f, halfHeight, up, down;
            switch (target.type) {
                case LARGE_BOX:
                    halfWidth = mLargeBoxWidth / 2f;
                    break;
                case SMALL_BOX:
                    halfWidth = mSmallBoxWidth / 2f;
                    break;
                case BOMB:
                    halfWidth = mBombWidth / 2f;
                    break;
            }
            halfHeight = halfWidth * ((float) bitmap.getHeight() / bitmap.getWidth());
            up = halfHeight * (TARGET_PHYSICS_CENTER_Y_RATIO / 0.5f);
            down = halfHeight * ((1f - TARGET_PHYSICS_CENTER_Y_RATIO) / 0.5f);
            mRectF.set(mCoords[0] - halfWidth, mCoords[1] - up, mCoords[0] + halfWidth, mCoords[1] + down);
            if (target.hasFlag(TargetInfo.FLAG_IN_CATCH_ANIMATION)) {
                if (pendingTarget != null) {
                    throw new GameException("Multiple targets in catching animation: "
                            + target.trackIndex + ", " + target.translation);
                }
                pendingTarget = new PendingTarget(target, bitmap, new RectF(mRectF));
            } else {
                mPrj.drawBitmap(canvas, bitmap, mRectF, mBitmapPaint);
            }
        }
        return pendingTarget;
    }

    private void drawPendingTarget(Canvas canvas, PendingTarget target, float offsetY) {
        mBitmapPaint.setAlpha(0xff);
        mPrj.drawBitmap(canvas, target.bitmap, target.bounds, offsetY, mBitmapPaint);
    }

    private static class PendingTarget {
        TargetInfo target;
        Bitmap bitmap;
        RectF bounds;

        PendingTarget(TargetInfo target, Bitmap bitmap, RectF bounds) {
            this.target = target;
            this.bitmap = bitmap;
            this.bounds = bounds;
        }
    }

    private float drawArm(Canvas canvas, PendingTarget preyTarget) {
        GameConfig config = mState.getConfig();
        float activeRowIndex = config.getActiveRowIndex();
        int trackCount = config.getTrackCount();
        int rowCount = config.getRowCount();
        float toleranceV = config.getCatchToleranceVertical();

        int activeZoneTopY = Math.round(((activeRowIndex - 0.5f - 0.5f * toleranceV)
                / (float) rowCount) * getHeight());
        int activeZoneCenterY = Math.round(((activeRowIndex - 0.5f) / (float) rowCount) * getHeight());
        float armPosition = extendHorizontalPosition(mState.getArmPosition());
        float armX = ((armPosition + 0.5f) / (float) trackCount) * getWidth();

        mInCoord[0] = armX;
        mInCoord[1] = activeZoneCenterY;
        mPrj.projectPoint(mInCoord, mOutCoord);
        float prjCenterX = mOutCoord[0];

        mInCoord[1] = activeZoneTopY;
        mPrj.projectPoint(mInCoord, mOutCoord);
        float prjActiveZoneTopY = mOutCoord[1];

        GameState.CatchAnimator catchAnim = mState.getCatchAnimator();
        float closeAngle = handleCatchAnimation(prjCenterX, prjActiveZoneTopY + mArmBottomOffsetUp, preyTarget,
                catchAnim, mOutCoord, mOutVal);
        mArm.draw(canvas, mOutCoord[0], mOutCoord[1], closeAngle);

        return mOutVal[0];
    }

    /**
     * @param centerX Arm center X if NOT in catch animation.
     * @param bottom Arm bottom Y if NOT in catch animation.
     * @param prey Including bounds of the prey target in pre-projection coordinate.
     * @param animator Catch animator to obtain animation progress.
     * @param outPosition (Arm center X, arm bottom Y) coords with catch animation considered.
     * @return Hand close angle, in radians, with catch animation considered.
     */
    private float handleCatchAnimation(float centerX, float bottom,
                                       PendingTarget prey,
                                       GameState.CatchAnimator animator,
                                       float[] outPosition, float[] outPullUpY) {
        float closeAngle = 0f;
        outPullUpY[0] = 0f;

        if (animator == null) {
            outPosition[0] = centerX;
            outPosition[1] = bottom;
        } else {
            float preyCenterX;
            float preyTop;
            if (prey != null) {
                // Target caught
                mInCoord[0] = prey.bounds.centerX();
                mInCoord[1] = prey.bounds.centerY();
                mPrj.projectPoint(mInCoord, mOutCoord); // Use bounds center to calculate X
                preyCenterX = mOutCoord[0];
                mInCoord[1] = prey.bounds.top;
                mPrj.projectPoint(mInCoord, mOutCoord); // Use top edge center to calculate Y
                preyTop = mOutCoord[1];
            } else {
                // Target not caught, play empty catch animation
                preyCenterX = centerX;
                preyTop = bottom + mEmptyCatchTranslationY;
            }

            // Interpolate from normal arm position to prey (or empty) position
            int runningSections = animator.getRunningSectionFlags();
            if ((runningSections & GameState.CatchAnimator.SECTION_FLAG_ARM_UP) == 0) {
                float downProgress = animator.getArmDownProgress();
                outPosition[0] = preyCenterX * downProgress + centerX * (1f - downProgress);
                outPosition[1] = preyTop * downProgress + bottom * (1f - downProgress);
            } else {
                float upProgress = animator.getArmUpProgress();
                outPosition[0] = preyCenterX;
                outPosition[1] = bottom * upProgress + preyTop * (1f - upProgress);
                outPullUpY[0] = preyTop - mOutCoord[1];
            }

            float closeProgress = animator.getArmCloseProgress();
            float fullAngle = prey == null ? EMPTY_ARM_CLOSE_ANGLE_RADIANS : prey.target.getArmCloseAngleRadians();
            closeAngle = closeProgress * fullAngle;
        }
        return closeAngle;
    }

    /**
     * @param paint Paint used to draw this {@code target}. It's alpha would be set in this method based on distance.
     */
    private void mapTargetCoordinates(TargetInfo target, int[] outCoord, Paint paint) {
        GameConfig config = mState.getConfig();
        int trackCount = config.getTrackCount();
        int rowCount = config.getRowCount();

        float trackIndex = extendHorizontalPosition(target.trackIndex);
        float translation = target.translation;

        outCoord[0] = (int) (((trackIndex + 0.5f) / (float) trackCount) * getWidth());
        outCoord[1] = (int) (((translation - 0.5f) / (float) rowCount) * getHeight());
        float alphaUnclamped = TARGET_ALPHA_FAR +
                (1f - TARGET_ALPHA_FAR) * (translation / rowCount) / TARGET_OPAQUE_POSITION_RATIO;
        float alpha = Math.max(0f, Math.min(alphaUnclamped, 1f));
        paint.setAlpha((int) (0xff * alpha));
    }

    private float extendHorizontalPosition(float rawPosition) {
        int span = mState.getConfig().getTrackCount() - 1;
        return rawPosition * (span + 2 * HORIZONTAL_POSITION_EXTENSION) / span
                - HORIZONTAL_POSITION_EXTENSION;
    }

    public void release() {
        if (mActiveZoneShaderBitmap != null) {
            mActiveZoneShaderBitmap = null;
        }

        if (mBorderLightBitmapLeft != null) {
            mBorderLightBitmapLeft = null;
        }

        if (mBorderLightBitmapRight != null) {
            mBorderLightBitmapRight = null;
        }
    }

    private static class Arm {
        private static final int HAND_LEFT = 0;
        private static final int HAND_RIGHT = 1;

        /** Hands' angle to vertical position at normal open status, in radians (about 16 degrees) */
        private static final float HAND_OPEN_ANGLE_RADIANS = 0.281f;

        private Drawable mBody;
        private Drawable mLeftHand;
        private Drawable mRightHand;

        private final float mTop;

        private final float mHandPivotY;
        private final float mLeftHandInstallX;
        private final float mRightHandInstallX;
        private final float mHandsInstallY;

        Arm(Context context, float top) {
            Resources res = context.getResources();
            mHandPivotY = res.getDimension(R.dimen.lucky_game_arm_hand_pivot_y);
            mLeftHandInstallX = res.getDimension(R.dimen.lucky_game_arm_left_hand_install_x);
            mRightHandInstallX = res.getDimension(R.dimen.lucky_game_arm_right_hand_install_x);
            mHandsInstallY = res.getDimension(R.dimen.lucky_game_arm_hands_install_y);

            mBody = ContextCompat.getDrawable(context, R.drawable.lucky_arm_body);
            Bitmap rightHandBitmap = BitmapFactory.decodeResource(res, R.drawable.lucky_arm_hand);
            mRightHand = new BitmapDrawable(res, rightHandBitmap);
            Matrix m = new Matrix();
            m.setScale(-1f, 1f);
            Bitmap leftHandBitmap = Bitmap.createBitmap(rightHandBitmap, 0, 0,
                    rightHandBitmap.getWidth(), rightHandBitmap.getHeight(), m, false);
            mLeftHand = new BitmapDrawable(res, leftHandBitmap);

            mTop = top;
        }

        void draw(Canvas canvas, float centerX, float bottom, float closeAngleRadians) {
            int width = mBody.getIntrinsicWidth();
            mBody.setBounds((int) centerX - width / 2, (int) mTop, (int) centerX + width / 2, (int) bottom);
            mBody.draw(canvas);

            drawHand(canvas, HAND_LEFT, closeAngleRadians);
            drawHand(canvas, HAND_RIGHT, closeAngleRadians);
        }

        private void drawHand(Canvas canvas, int which, float closeAngleRadians) {
            float isLeft = (which == HAND_LEFT ? 1f : -1f);
            float drawAngle = isLeft * (HAND_OPEN_ANGLE_RADIANS - closeAngleRadians);
            Rect bodyBounds = mBody.getBounds();
            float pivotX = bodyBounds.left + (which == HAND_LEFT ? mLeftHandInstallX : mRightHandInstallX);
            float pivotY = bodyBounds.bottom - mHandsInstallY;

            canvas.save();
            canvas.rotate((float) Math.toDegrees(drawAngle), pivotX, pivotY);
            Drawable drawable = (which == HAND_LEFT ? mLeftHand : mRightHand);
            drawable.setBounds(
                    (int) (pivotX - mRightHand.getIntrinsicWidth() / 2f),
                    (int) (pivotY - mHandPivotY),
                    (int) (pivotX + mRightHand.getIntrinsicWidth() / 2f),
                    (int) (pivotY + (mRightHand.getIntrinsicHeight() - mHandPivotY)));
            drawable.draw(canvas);
            canvas.restore();
        }
    }

    /**
     * Takes drawing commands and projects objects in [0, 0 - width, height] to a perspective position.
     */
    private class Projector {
        private static final float PROJECTION_RATIO_FAR = 0.426f;
        private static final float PROJECTION_RATIO_NEAR = 1.095f;

        private int mTopBanner;
        private int mWidth;
        private int mHeight;

        private Path mPath = new Path();
        private Matrix mMatrix = new Matrix();

        Projector(int topBanner, int width, int height) {
            mTopBanner = topBanner;
            mWidth = width;
            mHeight = height;
        }

        void drawRect(Canvas canvas, RectF rect, @NonNull Paint paint) {
            drawRect(canvas, rect, paint, null);
        }

        void drawRect(Canvas canvas, RectF rect, @NonNull Paint paint, @Nullable Bitmap shaderBitmap) {
            // Outer RECTANGLE bounds AFTER projection, used to process shader on the paint
            float left, top, right, bottom;

            mPath.reset();

            mInCoord[0] = rect.left;
            mInCoord[1] = rect.top;
            projectPoint(mInCoord, mOutCoord);
            mPath.moveTo(mOutCoord[0], mOutCoord[1]);
            top = mOutCoord[1];

            mInCoord[0] = rect.left;
            mInCoord[1] = rect.bottom;
            projectPoint(mInCoord, mOutCoord);
            mPath.lineTo(mOutCoord[0], mOutCoord[1]);
            left = mOutCoord[0];
            bottom = mOutCoord[1];

            mInCoord[0] = rect.right;
            mInCoord[1] = rect.bottom;
            projectPoint(mInCoord, mOutCoord);
            mPath.lineTo(mOutCoord[0], mOutCoord[1]);
            right = mOutCoord[0];

            mInCoord[0] = rect.right;
            mInCoord[1] = rect.top;
            projectPoint(mInCoord, mOutCoord);
            mPath.lineTo(mOutCoord[0], mOutCoord[1]);

            mPath.close();

            // Translate and scale bitmap shader on the paint (if set) to proper position
            if (shaderBitmap != null && paint.getShader() instanceof BitmapShader) {
                BitmapShader shader = (BitmapShader) paint.getShader();
                mMatrix.setTranslate(left, top);

                float scaleX = (right - left) / shaderBitmap.getWidth();
                float scaleY = (bottom - top) / shaderBitmap.getHeight();
                mMatrix.preScale(scaleX, scaleY);

                shader.setLocalMatrix(mMatrix);
            }

            canvas.drawPath(mPath, paint);
        }

        void drawLine(Canvas canvas, float startX, float startY, float stopX, float stopY, @NonNull Paint paint) {
            mInCoord[0] = startX;
            mInCoord[1] = startY;
            projectPoint(mInCoord, mOutCoord);
            startX = mOutCoord[0];
            startY = mOutCoord[1];

            mInCoord[0] = stopX;
            mInCoord[1] = stopY;
            projectPoint(mInCoord, mOutCoord);
            stopX = mOutCoord[0];
            stopY = mOutCoord[1];

            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }

        /**
         * Draw with position projection and scale only. Aspect ratio is not altered. Bitmap is considered to have a
         * perspective look itself and hence is not twisted.
         */
        void drawBitmap(Canvas canvas, @NonNull Bitmap bitmap, @NonNull RectF dst, @Nullable Paint paint) {
            drawBitmap(canvas, bitmap, dst, 0f, paint);
        }

        /**
         * Draw with position projection and scale only. Aspect ratio is not altered. Bitmap is considered to have a
         * perspective look itself and hence is not twisted.
         *
         * @param offsetY Offset Y AFTER projection.
         */
        void drawBitmap(Canvas canvas, @NonNull Bitmap bitmap, @NonNull RectF dst, float offsetY, @Nullable Paint paint) {
            // Project rect center
            mInCoord[0] = (dst.left + dst.right) / 2f;
            mInCoord[1] = (dst.top + dst.bottom) / 2f;
            float halfWidth = mInCoord[0] - dst.left;
            float halfHeight = mInCoord[1] - dst.top;
            float ratio = projectPoint(mInCoord, mOutCoord);
            float centerX = mOutCoord[0];
            float centerY = mOutCoord[1] + offsetY;

            mRectF.set(centerX - halfWidth * ratio, centerY - halfHeight * ratio,
                    centerX + halfWidth * ratio, centerY + halfHeight * ratio);
            canvas.drawBitmap(bitmap, null, mRectF, paint);
        }

        float projectPoint(float[] inCoord, float[] outCoord) {
            if (DEBUG_PROJECTION) {
                outCoord[0] = inCoord[0];
                outCoord[1] = inCoord[1];
                return 1f;
            }
            float x = inCoord[0];
            float y = inCoord[1];

            float yRelative = perspective(y / (mTopBanner + mHeight));
            float projectionRatio = PROJECTION_RATIO_FAR
                    + yRelative * (PROJECTION_RATIO_NEAR - PROJECTION_RATIO_FAR);
            float centerX = mWidth / 2f;

            outCoord[0] = centerX + projectionRatio * (x - centerX);
            outCoord[1] = mTopBanner + yRelative * mHeight;

            return projectionRatio;
        }

        /**
         * [0, 1] -> [0, 1] mapping function that adjust Y value to make difference in distance looks real.
         */
        private float perspective(float p) {
            final float coefSum = PROJECTION_RATIO_NEAR + PROJECTION_RATIO_FAR;
            return ((PROJECTION_RATIO_NEAR - PROJECTION_RATIO_FAR) / coefSum) * p * p
                    + (2 * PROJECTION_RATIO_FAR / coefSum) * p;
        }
    }

    private static class DrawingCacheInfo {
        /** Whether the cache is enabled. Cache is disabled if OutOfMemory error occurs with memory allocation. */
        boolean enabled = true;

        boolean hasBuilt = false;
        Bitmap cache;

        /**
         * Which component of game board this drawing cache is holding.
         * Must be one of {@link #DRAW_COMPONENT_BELT}, {@link #DRAW_COMPONENT_BORDER_LIGHTS}.
         * */
        int component;

        DrawingCacheInfo(int width, int height, int whichComponent) {
            try {
                cache = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                HSLog.w(TAG, "Out of memory while trying to allocate drawing cache, disable cache");
                enabled = false;
            }
            component = whichComponent;
        }
    }

    private static class GameException extends RuntimeException {
        GameException(String message) {
            super(message);
        }
    }
}
