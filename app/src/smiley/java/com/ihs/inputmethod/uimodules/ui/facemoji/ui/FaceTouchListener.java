package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


public class FaceTouchListener implements View.OnTouchListener {

    public FaceTouchListener(final ImageView imageView) {
        this.faceImageView = imageView;
    }

    private final ImageView faceImageView;
    /** 记录是拖拉照片模式还是放大缩小照片模式 */
    private int mode = 0;// 初始状态
    /** 拖拉照片模式 */
    private static final int MODE_DRAG = 1;
    /** 放大缩小照片模式 */
    private static final int MODE_ZOOM = 2;

    /** 用于记录开始时候的坐标位置 */
    private PointF startPoint = new PointF();
    /** 用于记录拖拉图片移动的坐标位置 */
    private Matrix matrix = new Matrix();
    /** 用于记录图片要进行拖拉时候的坐标位置 */
    private Matrix currentMatrix = new Matrix();

    /** 两个手指的开始距离 */
    private float startDis;
    /** 两个手指的中间点 */
    private PointF midPoint;

    private static final int INVALID_POINTER_ID = -1;
    private PointF downPoint1, downPoint2;
    private int ptrID1 = INVALID_POINTER_ID, ptrID2 = INVALID_POINTER_ID;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 手指压下屏幕
            case MotionEvent.ACTION_DOWN:
                mode = MODE_DRAG;
                // 记录ImageView当前的移动位置
                currentMatrix.set(faceImageView.getImageMatrix());
                startPoint.set(event.getX(), event.getY());
                ptrID1 = event.getPointerId(event.getActionIndex());
                break;
            // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
            case MotionEvent.ACTION_POINTER_DOWN:
                /** 计算两个手指间的距离 */
                startDis = movedDistance(event);
                /** 计算两个手指间的中间点 */
                if (startDis > 5f) { // 两个手指并拢在一起的时候像素大于5
                    mode = MODE_ZOOM;

                    midPoint = midpoint(event);
                    //记录当前ImageView的缩放倍数
                    currentMatrix.set(faceImageView.getImageMatrix());

                    ptrID2 = event.getPointerId(event.getActionIndex());
                    if(ptrID1 != INVALID_POINTER_ID && ptrID2 != INVALID_POINTER_ID) {
                        downPoint1 = new PointF(event.getX(event.findPointerIndex(ptrID1)), event.getY(event.findPointerIndex(ptrID1)));
                        downPoint2 = new PointF(event.getX(event.findPointerIndex(ptrID2)), event.getY(event.findPointerIndex(ptrID2)));
                    }
                }
                break;
            // 手指在屏幕上移动，改事件会被不断触发
            case MotionEvent.ACTION_MOVE:
                // 拖拉图片
                if (MODE_DRAG == mode) {
                    float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                    float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
                    // 在没有移动之前的位置上进行移动
                    matrix.set(currentMatrix);
                    matrix.postTranslate(dx, dy);
                }
                // 放大缩小图片
                else if (MODE_ZOOM == mode) {
                    float endDis = movedDistance(event);// 结束距离
                    if (endDis > 5f) { // 两个手指并拢在一起的时候像素大于10
                        float scale = endDis / startDis;// 得到缩放倍数
                        matrix.set(currentMatrix);
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);

                        if(ptrID1 != INVALID_POINTER_ID && ptrID2 != INVALID_POINTER_ID) {
                            PointF movePoint1 = new PointF(event.getX(event.findPointerIndex(ptrID1)), event.getY(event.findPointerIndex(ptrID1)));
                            PointF movePoint2 = new PointF(event.getX(event.findPointerIndex(ptrID2)), event.getY(event.findPointerIndex(ptrID2)));
                            float angle = vectorAngle(downPoint1, downPoint2, movePoint1, movePoint2);
                            matrix.postRotate(angle, midPoint.x, midPoint.y);
                        }
                    }
                }
                break;
            // 手指离开屏幕
            case MotionEvent.ACTION_UP:
                // 当触点离开屏幕，但是屏幕上还有触点(手指)
            case MotionEvent.ACTION_POINTER_UP:
                // 点击取消
            case MotionEvent.ACTION_CANCEL:
                mode = 0;
                ptrID1 = INVALID_POINTER_ID;
                ptrID2 = INVALID_POINTER_ID;
                break;
        }
        faceImageView.setImageMatrix(matrix);
        return true;
    }

    /** 计算两个手指间的距离 */
    private float movedDistance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        /** 使用勾股定理返回两点之间的距离 */
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /** 计算两个手指间的中间点 */
    private PointF midpoint(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }

    private float vectorAngle(PointF line1Point1, PointF line1Point2, PointF line2Point1, PointF line2Point2) {
        PointF point1 = new PointF(line1Point2.x - line1Point1.x, line1Point2.y - line1Point1.y);
        PointF point2 = new PointF(line2Point2.x - line2Point1.x, line2Point2.y - line2Point1.y);

        float dotProduction = point1.x * point2.x + point1.y * point2.y;
        float angle = (float) Math.toDegrees(Math.acos(dotProduction /
                (Math.sqrt(point1.x * point1.x + point1.y * point1.y) * Math.sqrt(point2.x * point2.x + point2.y * point2.y))));

        Log.d("FaceTouchListener", "Angle: " + angle);
        float cross = point1.x * point2.y - point2.x * point1.y;
        if (cross < 0) {
            angle *= -1;
        }

        return angle;
    }
}
