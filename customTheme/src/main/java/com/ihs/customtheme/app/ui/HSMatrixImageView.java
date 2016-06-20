package com.ihs.customtheme.app.ui;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class HSMatrixImageView extends ImageView {
    public final static String TAG = HSMatrixImageView.class.getSimpleName();

    private GestureDetector mGestureDetector;
    /**
     * 模板Matrix，用以初始化
     */
    private Matrix mMatrix = new Matrix();
    /**
     * 图片长度
     */
    private float mImageWidth;
    /**
     * 图片高度
     */
    private float mImageHeight;
    /**
     * 原始缩放级别
     */
    private float mScale;

    /**
     * 图片可以向上多少 (下白边), 给正数
     * */
    private float mMaxDeltaYTrans;

    /**
     * 图片可以向下多少 (上白边), 给负数
     * */
    private float mMinDeltaYTrans;

    public HSMatrixImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        HSMatrixTouchListener mListener = new HSMatrixTouchListener();
        setOnTouchListener(mListener);
        mGestureDetector = new GestureDetector(getContext(), new GestureListener(mListener));

    }

    public HSMatrixImageView(Context context) {
        super(context, null);
        HSMatrixTouchListener mListener = new HSMatrixTouchListener();
        setOnTouchListener(mListener);
        mGestureDetector = new GestureDetector(getContext(), new GestureListener(mListener));
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        //大小为0 表示当前控件大小未测量  设置监听函数  在绘制前赋值
        if (getWidth() == 0) {
            ViewTreeObserver vto = getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    initData();
                    //赋值结束后，移除该监听函数
                    HSMatrixImageView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        } else {
            initData();
        }
    }

    /**
     * 初始化模板Matrix和图片的其他数据
     */
    private void initData() {
        //设置完图片后，获取该图片的坐标变换矩阵
        mMatrix.set(getImageMatrix());
        float[] values = new float[9];
        mMatrix.getValues(values);
        mImageWidth = this.getDrawable().getIntrinsicWidth();
        mImageHeight = this.getDrawable().getIntrinsicHeight();
        mScale = values[Matrix.MSCALE_X];

        float height = mImageHeight * values[Matrix.MSCALE_Y];
        // Y轴居中
        float topMargin = (getHeight() - height) / 2;
        if (topMargin != values[Matrix.MTRANS_Y]) {
            mMatrix.postTranslate(0, topMargin - values[Matrix.MTRANS_Y]);
            setImageMatrix(mMatrix);
        }
    }

    public void setMaxDeltaYTrans(float maxDeltaYTrans) {
        mMaxDeltaYTrans = maxDeltaYTrans;
    }

    public void setMinDeltaYTrans(float minDeltaYTrans) {
        mMinDeltaYTrans = minDeltaYTrans;
    }

    public class HSMatrixTouchListener implements OnTouchListener {
        /**
         * 拖拉照片模式
         */
        private static final int MODE_DRAG = 1;
        /**
         * 放大缩小照片模式
         */
        private static final int MODE_ZOOM = 2;
        /**
         * 最大缩放级别
         */
        float mMaxScale = 6;
        /**
         * 双击时的缩放级别
         */
        float mDobleClickScale = 2;
        private int mMode = 0;//
        /**
         * 缩放开始时的手指间距
         */
        private float mStartDis;
        /**
         * 当前Matrix
         */
        private Matrix mCurrentMatrix = new Matrix();

        /**
         * 用于记录开始时候的坐标位置
         */
        private PointF mStartPoint = new PointF();

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    //设置拖动模式
                    mMode = MODE_DRAG;
                    mStartPoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mMode = MODE_ZOOM;
                    mStartDis = distance(event);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    resetMatrix();
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mMode == MODE_ZOOM) {
                        setZoomMatrix(event);
                    } else if (mMode == MODE_DRAG) {
                        setDragMatrix(event);
                    }
                    break;
            }

            return mGestureDetector.onTouchEvent(event);
        }

        /**
         * 设置拖拽状态下的Matrix
         *
         * @param event
         */
        public void setDragMatrix(MotionEvent event) {
            float dx = event.getX() - mStartPoint.x; // 得到x轴的移动距离
            float dy = event.getY() - mStartPoint.y; // 得到x轴的移动距离
            //避免和双击冲突,大于10f才算是拖动
            if (Math.sqrt(dx * dx + dy * dy) > 10f) {
                mStartPoint.set(event.getX(), event.getY());
                //在当前基础上移动
                mCurrentMatrix.set(getImageMatrix());
                float[] values = new float[9];
                mCurrentMatrix.getValues(values);
                dy = checkDyBound(values, dy);
                dx = checkDxBound(values, dx);

                mCurrentMatrix.postTranslate(dx, dy);
                setImageMatrix(mCurrentMatrix);
            }
        }

        /**
         * 判断缩放级别是否是改变过
         *
         * @return true表示非初始值, false表示初始值
         */
        private boolean isZoomChanged() {
            float[] values = new float[9];
            getImageMatrix().getValues(values);
            //获取当前X轴缩放级别
            float scale = values[Matrix.MSCALE_X];
            //获取模板的X轴缩放级别，两者做比较
            return scale != mScale;
        }

        /**
         * 和当前矩阵对比，检验dy，使图像移动后不会超出ImageView边界
         *
         * @param values
         * @param dy
         * @return
         */
        private float checkDyBound(float[] values, float dy) {
            float height = getHeight();
            if (values[Matrix.MTRANS_Y] + dy > mMaxDeltaYTrans)
                dy = mMaxDeltaYTrans - values[Matrix.MTRANS_Y];
            else if (values[Matrix.MTRANS_Y] + dy < -(mImageHeight * values[Matrix.MSCALE_Y] - height) + mMinDeltaYTrans)
                dy = -(mImageHeight * values[Matrix.MSCALE_Y] - height) + mMinDeltaYTrans - values[Matrix.MTRANS_Y];
            return dy;
        }

        /**
         * 和当前矩阵对比，检验dx，使图像移动后不会超出ImageView边界
         *
         * @param values
         * @param dx
         * @return
         */
        private float checkDxBound(float[] values, float dx) {
            float width = getWidth();
            if (values[Matrix.MTRANS_X] + dx > 0) {
                dx = -values[Matrix.MTRANS_X];
            } else if (values[Matrix.MTRANS_X] + dx < -(mImageWidth * values[Matrix.MSCALE_X] - width)) {
                dx = -(mImageWidth * values[Matrix.MSCALE_X] - width) - values[Matrix.MTRANS_X];
            }
            return dx;
        }

        /**
         * 设置缩放Matrix
         *
         * @param event
         */
        private void setZoomMatrix(MotionEvent event) {
            //只有同时触屏两个点的时候才执行
            if (event.getPointerCount() < 2) return;
            float endDis = distance(event);// 结束距离
            if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                float scale = endDis / mStartDis;// 得到缩放倍数
                mStartDis = endDis;//重置距离
                mCurrentMatrix.set(getImageMatrix());//初始化Matrix
                float[] values = new float[9];
                mCurrentMatrix.getValues(values);
                scale = checkScale(scale, values);
                PointF centerF = getCenter(scale, values);
                mCurrentMatrix.postScale(scale, scale, centerF.x, centerF.y);
                setImageMatrix(mCurrentMatrix);
            }
        }

        /**
         * 获取缩放的中心点。
         *
         * @param scale
         * @param values
         * @return
         */
        private PointF getCenter(float scale, float[] values) {
            //缩放级别小于原始缩放级别时或者为放大状态时，返回ImageView中心点作为缩放中心点
            if (scale * values[Matrix.MSCALE_X] < mScale || scale >= 1) {
                return new PointF(getWidth() / 2, getHeight() / 2);
            }
            float cx = getWidth() / 2;
            float cy = getHeight() / 2;
            //以ImageView中心点为缩放中心，判断缩放后的图片左边缘是否会离开ImageView左边缘，是的话以左边缘为X轴中心
            if ((getWidth() / 2 - values[Matrix.MTRANS_X]) * scale < getWidth() / 2)
                cx = 0;
            //判断缩放后的右边缘是否会离开ImageView右边缘，是的话以右边缘为X轴中心
            if ((mImageWidth * values[Matrix.MSCALE_X] + values[Matrix.MTRANS_X]) * scale < getWidth())
                cx = getWidth();
            return new PointF(cx, cy);
        }


        /**
         * 检验scale，使图像缩放后在缩放范围以内
         *
         * @param scale
         * @param values
         * @return
         */
        private float checkScale(float scale, float[] values) {
            if (scale * values[Matrix.MSCALE_X] < mScale)
                scale = mScale / values[Matrix.MSCALE_X];
            else if (scale * values[Matrix.MSCALE_X] > mMaxScale)
                scale = mMaxScale / values[Matrix.MSCALE_X];
            return scale;
        }

        /**
         * 重置Matrix
         */
        private void resetMatrix() {
            if (checkRest()) {
                mCurrentMatrix.set(mMatrix);
                setImageMatrix(mCurrentMatrix);
            }

            /**
             * 重置图片位置
             * */
            float dx = 0;
            float dy = 0;
            float width = getWidth();
            float height = getHeight();
            mCurrentMatrix.set(getImageMatrix());//初始化Matrix
            float[] values = new float[9];
            mCurrentMatrix.getValues(values);

            if (values[Matrix.MTRANS_X] > 0) {
                dx = -values[Matrix.MTRANS_X];
            } else if (values[Matrix.MTRANS_X] < -(mImageWidth * values[Matrix.MSCALE_X] - width)) {
                dx = -(mImageWidth * values[Matrix.MSCALE_X] - width) - values[Matrix.MTRANS_X];
            }

            if (values[Matrix.MTRANS_Y] > mMaxDeltaYTrans) {
                dy = mMaxDeltaYTrans - values[Matrix.MTRANS_Y];
            } else if (values[Matrix.MTRANS_Y] < -(mImageHeight * values[Matrix.MSCALE_Y] - height) + mMinDeltaYTrans) {
                dy = -(mImageHeight * values[Matrix.MSCALE_Y] - height) + mMinDeltaYTrans - values[Matrix.MTRANS_Y];
            }

            mCurrentMatrix.postTranslate(dx, dy);
            setImageMatrix(mCurrentMatrix);
        }

        /**
         * 判断是否需要重置
         *
         * @return 当前缩放级别小于模板缩放级别时，重置
         */
        private boolean checkRest() {
            float[] values = new float[9];
            getImageMatrix().getValues(values);
            //获取当前X轴缩放级别
            float scale = values[Matrix.MSCALE_X];
            //获取模板的X轴缩放级别，两者做比较
            return scale < mScale;
        }

        /**
         * 计算两个手指间的距离
         *
         * @param event
         * @return
         */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        /**
         * 双击时触发
         */
        public void onDoubleClick() {
            float scale = isZoomChanged() ? 1 : mDobleClickScale;
            mCurrentMatrix.set(mMatrix);//初始化Matrix
            mCurrentMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
            setImageMatrix(mCurrentMatrix);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private final HSMatrixTouchListener listener;

        public GestureListener(HSMatrixTouchListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            //捕获Down事件
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //触发双击事件
            listener.onDoubleClick();
            return true;
        }
    }
}
