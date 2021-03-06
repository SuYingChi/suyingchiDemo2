package com.ihs.inputmethod.uimodules.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.ihs.inputmethod.uimodules.R;

import java.util.Locale;

/**
 * Created by yanxia on 2017/11/22.
 */

public class ProgressButton extends AppCompatButton {

    private float mProgress; //当前进度
    @SuppressWarnings("FieldCanBeLocal")
    private static float MAX_PROGRESS = 100f; //最大进度：默认为100
    @SuppressWarnings("FieldCanBeLocal")
    private static float MIN_PROGRESS = 0f;//最小进度：默认为0
    //private GradientDrawable mProgressDrawable;// 加载进度时的进度颜色
    private boolean isShowProgress;  //是否展示进度
    private boolean isFinish = true; //状态是否结束
    private float cornerRadius;

    private String progressText; //进度提示文本
    private int textColor; //进度提示文本颜色
    private float textSize; //文本大小
    private Paint textPaint;
    private Rect textRect;

    private Paint pgPaint;
    private Bitmap pgBitmap; //进度条 bitmap
    private Canvas pgCanvas;
    private int progressColor;
    private int borderWidth;
    private RectF bgRectF;
    private BitmapShader bitmapShader;

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);

        //mProgressDrawable = new GradientDrawable();
        //mProgressDrawable.setShape(GradientDrawable.RECTANGLE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textRect = new Rect();

        pgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pgPaint.setStyle(Paint.Style.FILL);

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.ProgressButton);

        try {
            float defValue = getResources().getDimension(R.dimen.corner_radius);
            cornerRadius = attr.getDimension(R.styleable.ProgressButton_buttonCornerRadius, defValue);

            int defTextColor = ContextCompat.getColor(context, R.color.colorPrimary);
            textColor = attr.getColor(R.styleable.ProgressButton_finishColor, defTextColor);

            float defTextSize = getResources().getDimension(R.dimen.camera_detect_face_message_size);
            textSize = attr.getDimension(R.styleable.ProgressButton_progressTextSize, defTextSize);

            isShowProgress = attr.getBoolean(R.styleable.ProgressButton_showProgressNum, true);

            int defaultProgressColor = getResources().getColor(R.color.colorPrimary);
            progressColor = attr.getColor(R.styleable.ProgressButton_progressColor, defaultProgressColor);

            int defBorderWidth = (int) getResources().getDimension(R.dimen.transparent_button_frame_stoke_width);
            borderWidth = (int) attr.getDimension(R.styleable.ProgressButton_backgroundBorderWidth, defBorderWidth);

        } finally {
            attr.recycle();
        }
        isFinish = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mProgress >= MIN_PROGRESS && mProgress <= MAX_PROGRESS && !isFinish) {
            //float scale = mProgress / MAX_PROGRESS;
            //float indicatorWidth = (float) getMeasuredWidth() * scale;
            //mProgressDrawable.setBounds(0, 0, (int) indicatorWidth, getMeasuredHeight());
            //mProgressDrawable.draw(canvas);
            drawProgress(canvas);
            if (isShowProgress) {
                drawProgressText(canvas);
                drawColorProgressText(canvas);
            }
            if (mProgress >= MAX_PROGRESS) {
                isFinish = true;
            }
        }
        super.onDraw(canvas);
    }

    /**
     * 进度
     */
    private void drawProgress(Canvas canvas) {
        float right = (mProgress / MAX_PROGRESS) * getMeasuredWidth();
        Bitmap bitmap = getPgBitmap();
        pgCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        pgCanvas.save(Canvas.ALL_SAVE_FLAG);
        pgCanvas.clipRect(0, 0, right, getMeasuredHeight());
        pgCanvas.drawColor(progressColor);
        pgCanvas.restore();
        //控制显示区域
        BitmapShader bitmapShader = getBitmapShader(bitmap);
        pgPaint.setColor(progressColor);
        pgPaint.setShader(bitmapShader);
        if (cornerRadius > getMeasuredHeight() / 2) {
            canvas.drawRoundRect(getBgRectF(), getMeasuredHeight() / 2, getMeasuredHeight() / 2, pgPaint);
        } else {
            canvas.drawRoundRect(getBgRectF(), cornerRadius, cornerRadius, pgPaint);
        }
    }

    private Bitmap getPgBitmap() {
        if (pgBitmap == null) {
            pgBitmap = Bitmap.createBitmap(getMeasuredWidth() - borderWidth, getMeasuredHeight() - borderWidth, Bitmap.Config.ARGB_8888);
            pgCanvas = new Canvas(pgBitmap);
        }
        return pgBitmap;
    }

    private RectF getBgRectF() {
        if (bgRectF == null) {
            bgRectF = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        }
        return bgRectF;
    }

    private BitmapShader getBitmapShader(Bitmap bitmap) {
        if (bitmapShader == null) {
            bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        }
        return bitmapShader;
    }

    /**
     * 进度提示文本
     *
     * @param canvas
     */
    private void drawProgressText(Canvas canvas) {
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        progressText = getProgress();
        textPaint.getTextBounds(progressText, 0, progressText.length(), textRect);
        int tWidth = textRect.width();
        int tHeight = textRect.height();
        float xCoordinate = (getMeasuredWidth() - tWidth) / 2;
        float yCoordinate = (getMeasuredHeight() + tHeight) / 2;
        canvas.drawText(progressText, xCoordinate, yCoordinate, textPaint);
    }

    private void drawColorProgressText(Canvas canvas) {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        int tWidth = textRect.width();
        int tHeight = textRect.height();
        float xCoordinate = (getMeasuredWidth() - tWidth) / 2;
        float yCoordinate = (getMeasuredHeight() + tHeight) / 2;
        float progressWidth = (mProgress / MAX_PROGRESS) * getMeasuredWidth();
        if (progressWidth > xCoordinate) {
            canvas.save(Canvas.ALL_SAVE_FLAG);
            float right = Math.min(progressWidth, xCoordinate + tWidth * 1.15f);
            canvas.clipRect(xCoordinate, 0, right, getMeasuredHeight());
            canvas.drawText(progressText, xCoordinate, yCoordinate, textPaint);
            canvas.restore();
        }
    }

    private String getProgress() {
        return String.format(Locale.getDefault(), "%3.0f %%", mProgress);
    }

    public void setProgress(float progress) {
        isFinish = false;
        setText("");
        mProgress = progress;
        invalidate();
    }

    public void initState() {
        isFinish = true;
        mProgress = MIN_PROGRESS;
        invalidate();
    }
}
