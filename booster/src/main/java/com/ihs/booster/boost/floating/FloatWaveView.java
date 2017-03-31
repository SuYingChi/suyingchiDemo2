package com.ihs.booster.boost.floating;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.PopupWindow;

import com.ihs.booster.HSBoostManager;
import com.ihs.booster.R;
import com.ihs.booster.utils.L;

public class FloatWaveView extends View {


    //至少要清理多少百分比来做动画 mini 0 max 100
    public static final int LEAST_CLEAN_THRESHOLD = 20;

    private static final int COMMON_MOVE_SPEED = 10;
    private static final int[] COLOR_ARRAY = {
            //0-10
            0xFF41b831,
            //10-20
            0xFF41b831,
            //20-30
            0xFF60c21e,
            //30-40
            0xFF9acc12,
            //40-50
            0xFFffcc00,
            //50-60
            0xFFffba00,
            //60-70
            0xFFffa200,
            //70-80
            0xFFfe930d,
            //80-90
            0xFFfc7628,
            //90-100
            0xFFfa5843};
    private static final int PERIOD_DRAW = 16;// 绘制周期
    private static final int X_STEP = 5; // X轴的量化步长-越小曲线月平滑，但是计算量越大

    private static final int EVENT_REFRESH_VIEW = 100;
    private static final int EVENT_DIM = 0;
    private static final int EVENT_LIGHT_UP = 1;
    private static final int EVENT_DISMISS_WARNING = 2;

    private static final int FLAG_LAYER_FIRST = 1;
    private static final int FLAG_LAYER_SECOND = 2;

    private static final int PADDING_FOR_SHADOW = 15;
    private static final float MEM_PERCENT_FONT_SIZE_FRACTION = 2.5f;
    private static final float UNIT_FONT_SIZE_FRACTION = 2.9f;


    private static final float INIT_WAVE_SPEED = 0.05f;
    private static final int INIT_WAVE_HEIGHT = 11;
    private static final int INIT_WAVE_PERIOD = 11;

    private static final int DURATION_TOUCH_ANIM = 4000;
    private static final int DURATION_FLIP_ANIM = 500;
    private static final int DURATION_FLUSH_WATER = 3000;
    private static final int DURATION_HEIGHT_CHANGED = 1000;

    private static final int POP_WINDOW_DISMISS_TIME = 5000;

    private static final String TEXT_FOR_GET_BOUNDARY = "88";

    private static final float SCALE_PERCENT_TO_WAVE_MOVE_SPEED = 0.003f;
    private static final float SCALE_PERCENT_TO_WAVE_PERIOD = 0.1f;
    private static final float SCALE_PERCENT_TO_WAVE_HEIGHT = 0.005f;
    private static final float SCALE_PERCENT_TO_HEIGHT_FROM_TOP = 0.01f;

    private static final int ALPHA_SECOND_PAINT = 150;
    private static final float ALPHA_WHEN_DIM = 1f;
    private static final float SECOND_WAVE_SPEED_OFFSET = 1.7f;

    private static final int FLIP_HIDE_LOGO_DEGREE = 270;
    private static final int FLIP_START_ANIM_DEGREE = 333;

    private Paint firstPaint, secondPaint, logoPaint;
    private Path wavePath;

    public boolean softTouchExecuting = false;
    public boolean warningShowing = false;

    public int dimAfterSeconds = 5;

    private ArgbEvaluator argbEvaluator;
    private TextPaint numPaint, unitPaint;

    private int width, height;

    private int memPercent;

    private float waveMoveDistByStep;// 波形移动
    private float wavePeriod;// 波形的周期
    private float waveHeight;// 波形的幅度
    private float waveMoveSpeed;// 波形的移动速度
    private float currentHeightFromTop;
    private float paddingAround; //偏移量 用于给两边留白 为光晕做准备

    private View viewForPopup;

    private boolean firstVisible;

    private Rect percentStringRect, percentUnitRect, canvasRect;

    private PopupWindow popupWindow;
    private Bitmap logoPic;
    private Bitmap background;

    private ValueAnimator touchAnimator;
    ValueAnimator heightCounterAnimator, speedCounterAnimator, shadowCounterAnimator, paintColorAnimator;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case EVENT_DIM:
                    break;

                case EVENT_LIGHT_UP:
                    break;

                case EVENT_DISMISS_WARNING:
                    break;

                case EVENT_REFRESH_VIEW:
                    refreshView();
                    break;

                default:
                    break;
            }
        }

    };

    public FloatWaveView(Context context) {
        super(context);
        init();
    }

    public FloatWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        wavePath = new Path();
        argbEvaluator = new ArgbEvaluator();
        percentStringRect = new Rect();
        percentUnitRect = new Rect();

        secondPaint = new Paint();
        secondPaint.setStyle(Paint.Style.FILL);
        secondPaint.setAntiAlias(true);
        secondPaint.setColor(COLOR_ARRAY[0]);

        firstPaint = new Paint();
        firstPaint.setStyle(Paint.Style.FILL);
        firstPaint.setAntiAlias(true);
        firstPaint.setColor(COLOR_ARRAY[0]);


        logoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        logoPaint.setStyle(Paint.Style.FILL);

        setMoveSpeedByPercent(COMMON_MOVE_SPEED);
//        background = BitmapFactory.decodeResource(getResources(), R.mipmap.float_bg);
        handler.sendEmptyMessage(EVENT_REFRESH_VIEW);
        setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_float_wave_bg));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initWaveParam();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (firstVisible) {
            canvas.drawBitmap(logoPic, null, canvasRect, logoPaint);
            return;
        }
//        canvas.drawBitmap(background, null, canvasRect, logoPaint);

        canvas.drawPath(getWavePath(FLAG_LAYER_SECOND), secondPaint);
        canvas.drawPath(getWavePath(FLAG_LAYER_FIRST), firstPaint);

        canvas.drawText(String.valueOf(memPercent), getWidth() / 2 - percentUnitRect.width() / 4,
                getHeight() / 2 + percentStringRect.height() / 2,
                numPaint);
        canvas.drawText("%", getWidth() / 2 + percentStringRect.width() / 2 - percentUnitRect.width() / 4,
                getHeight() / 2 + percentStringRect.height() / 2,
                unitPaint);
    }

    private void initWaveParam() {
        paddingAround = 0;// getWidth() / PADDING_FOR_SHADOW;

        width = (int) (getWidth() - paddingAround * 2);
        height = (int) (getHeight() - paddingAround * 2);

        canvasRect = new Rect(0, 0, getWidth(), getHeight());

        setWaveHeightByPercent(INIT_WAVE_HEIGHT);
        setWavePeriodByPercent(INIT_WAVE_PERIOD);

        waveMoveDistByStep = width;
        waveMoveSpeed = INIT_WAVE_SPEED;

        numPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        numPaint.setTextSize(getWidth() / MEM_PERCENT_FONT_SIZE_FRACTION);
        numPaint.setTextAlign(Paint.Align.CENTER);
        numPaint.setColor(Color.WHITE);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        numPaint.setTypeface(font);
        numPaint.getTextBounds(TEXT_FOR_GET_BOUNDARY, 0, TEXT_FOR_GET_BOUNDARY.length(), percentStringRect);

        unitPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        unitPaint.setTextSize(getWidth() / UNIT_FONT_SIZE_FRACTION);
        unitPaint.setColor(Color.WHITE);
        unitPaint.setTypeface(font);
        unitPaint.getTextBounds("%", 0, 1, percentUnitRect);

        secondPaint.setColor(COLOR_ARRAY[0]);
        firstPaint.setColor(COLOR_ARRAY[0]);

        setHeightFromTopByPercent((int) HSBoostManager.getInstance().getMemoryUsedPercent());
    }

    private void refreshView() {
        waveMoveDistByStep -= waveMoveSpeed;
        postInvalidate();
        handler.sendEmptyMessageDelayed(EVENT_REFRESH_VIEW, PERIOD_DRAW);
    }

    private Path getWavePath(int layerCode) {
        wavePath.reset();

        float r = width / 2;
        float distToCenter = r - currentHeightFromTop;

        double startDegree;
        double endDegree;

        if (distToCenter >= 0) {
            double cosAngle = Math.toDegrees(Math.acos(distToCenter / r));
            startDegree = cosAngle - 90;
            endDegree = 360 - cosAngle * 2;
        } else {
            double cosAngle = Math.toDegrees(Math.acos(-distToCenter / r));
            startDegree = 90 - cosAngle;
            endDegree = 360 - (90 + startDegree) * 2;
        }
        float x = (float) (r - Math.sqrt(r * r - distToCenter * distToCenter)) + paddingAround;

        switch (layerCode) {
            case FLAG_LAYER_FIRST:
                wavePath.moveTo(x, (float) (waveHeight * Math.sin(waveMoveDistByStep))
                        + currentHeightFromTop + paddingAround);

                for (float step = x; step <= width - x + paddingAround * 2; step += X_STEP) {

                    float y = (float) ((waveHeight * Math.sin(wavePeriod * (step) + waveMoveDistByStep)
                            + currentHeightFromTop + paddingAround));
                    wavePath.lineTo(step, y);
                }

                break;
            case FLAG_LAYER_SECOND:
                wavePath.moveTo(x, (float) (waveHeight * Math.cos(wavePeriod * (x) + waveMoveDistByStep * SECOND_WAVE_SPEED_OFFSET) // TODO magic number
                        + currentHeightFromTop + paddingAround));
                for (float step = x; step <= width - x + paddingAround * 2; step += X_STEP) {

                    float y = (float) ((waveHeight * Math.cos(wavePeriod * (step) + waveMoveDistByStep * SECOND_WAVE_SPEED_OFFSET))
                            + currentHeightFromTop + paddingAround);
                    wavePath.lineTo(step, y);

                }

                break;
        }
        wavePath.arcTo(new RectF(paddingAround, paddingAround, width + paddingAround,
                height + paddingAround), (float) startDegree, (float) endDegree);
        return wavePath;
    }

    private void setMoveSpeedByPercent(int progress) {
        waveMoveSpeed = progress * SCALE_PERCENT_TO_WAVE_MOVE_SPEED;
    }

    private void setWavePeriodByPercent(int percent) {
        wavePeriod = percent * (float) (SCALE_PERCENT_TO_WAVE_PERIOD * Math.PI / width);
    }

    private void setWaveHeightByPercent(int percent) {
        waveHeight = percent * height * SCALE_PERCENT_TO_WAVE_HEIGHT;
    }

    public void setHeightFromTopByPercent(int percent) {
        Log.d("MemPercent", percent + "");
        memPercent = percent;
        currentHeightFromTop = ((100 - percent) * SCALE_PERCENT_TO_HEIGHT_FROM_TOP) * height;
        postInvalidate();
    }

    public void setPaintColor(int paintColor) {
        firstPaint.setColor(paintColor);
        secondPaint.setColor(paintColor);
        secondPaint.setAlpha(ALPHA_SECOND_PAINT);
    }

    private int[] getColorArray(float startValue, float endValue) {
        int startIndex = Float.valueOf(startValue * COLOR_ARRAY.length / 100).intValue() - 1;
        int endIndex = Float.valueOf(endValue * COLOR_ARRAY.length / 100).intValue() - 1;
        int size = Math.abs(startIndex - endIndex) + 2;
        int[] colorArray = new int[size];
        colorArray[0] = COLOR_ARRAY[startIndex];
        if (startIndex > endIndex) {
            for (int i = startIndex; i >= endIndex; i--) {
                colorArray[startIndex - i + 1] = COLOR_ARRAY[i];
                L.l("startIndex:" + startIndex + " endIndex:" + endIndex + " / " + i + ":" + COLOR_ARRAY[i]);
            }
        } else {
            for (int i = startIndex; i <= endIndex; i++) {
                colorArray[i - startIndex + 1] = COLOR_ARRAY[i];
                L.l("startIndex:" + startIndex + " endIndex:" + endIndex + " / " + i + ":" + COLOR_ARRAY[i]);
            }

        }
        return colorArray;
    }

    public int getCurrentDisplayedMemPercent() {
        return memPercent;
    }

    public void lightUp() {
        setAlpha(1);
    }

    public void flushWater(float startValue, float endValue) {
        cancelFlushWaterAnimation();
        lightUp();
        heightCounterAnimator = ValueAnimator.ofFloat(startValue, endValue);
        heightCounterAnimator.setDuration(DURATION_FLUSH_WATER);
        heightCounterAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        heightCounterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float current = Float.valueOf(valueAnimator.getAnimatedValue().toString());
                setHeightFromTopByPercent((int) current);
            }
        });
        heightCounterAnimator.start();

        paintColorAnimator = ObjectAnimator.ofInt(this, "paintColor", getColorArray(startValue, endValue));
        paintColorAnimator.setDuration(DURATION_FLUSH_WATER);
        paintColorAnimator.setEvaluator(new ArgbEvaluator());
        paintColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int current = Integer.valueOf(valueAnimator.getAnimatedValue().toString());
                setPaintColor(current);
            }
        });
        paintColorAnimator.start();

        speedCounterAnimator = ValueAnimator.ofFloat(30, 90, COMMON_MOVE_SPEED);
        speedCounterAnimator.setDuration(DURATION_FLUSH_WATER);
        speedCounterAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        speedCounterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float current = Float.valueOf(animation.getAnimatedValue().toString());
                setMoveSpeedByPercent((int) current);
            }
        });
        speedCounterAnimator.start();
    }

    public void cancelFlushWaterAnimation() {
        if (heightCounterAnimator != null) {
            heightCounterAnimator.removeAllUpdateListeners();
            heightCounterAnimator.cancel();
            heightCounterAnimator = null;
        }
        if (speedCounterAnimator != null) {
            speedCounterAnimator.removeAllUpdateListeners();
            speedCounterAnimator.cancel();
            speedCounterAnimator = null;
        }
        if (shadowCounterAnimator != null) {
            shadowCounterAnimator.removeAllUpdateListeners();
            shadowCounterAnimator.cancel();
            shadowCounterAnimator = null;
        }
        waveMoveDistByStep = width;
        setMoveSpeedByPercent(COMMON_MOVE_SPEED);
    }

}
