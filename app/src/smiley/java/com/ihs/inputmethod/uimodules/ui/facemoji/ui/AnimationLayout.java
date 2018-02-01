package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by jixiang on 16/4/6.
 *
 * view out layout for play Animation
 */
public class AnimationLayout extends RelativeLayout {

    private boolean mEnableClickScaleAnim = false;

    public AnimationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs, 0);
    }

    public AnimationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs, defStyleAttr);
    }

    private void initAttr(Context context, AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.AnimationLayout, defStyleAttr, 0);
        mEnableClickScaleAnim = a.getBoolean(R.styleable.AnimationLayout_enableClickScaleAnim, false);

        a.recycle();
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void setEnableClickScaleAnim(boolean enable){
//        mEnableClickScaleAnim = enable;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                handleDown();
                break;
            case MotionEvent.ACTION_UP:
                handleUp();
                break;
            case MotionEvent.ACTION_CANCEL:
                handleCancel();
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                // child not receive click event,so arrival here
                // if we do case the event,wo should recover the state before press
                if(!super.onTouchEvent(event)){
                    handleRecover();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                handleCancel();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void handleCancel() {
        handleUpAnim();
    }

    /**
     * recover state before
     */
    private void handleRecover(){
        if(mEnableClickScaleAnim){
            startScaleAnimation(1.0f,1.0f);
        }
    }

    private void handleUp() {
        handleUpAnim();
    }

    private void handleDown() {
        handleDownAnim();
    }

    private void handleUpAnim() {
        if(mEnableClickScaleAnim){
            startScaleAnimation(1.0f,1.0f);
        }
    }

    private void handleDownAnim() {
        if(mEnableClickScaleAnim){
            startScaleAnimation(0.9f,0.9f);
        }
    }

    private void startScaleAnimation(float scaleX,float scaleY){
        clearAnimation();
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(this, "scaleX", getScaleX(), scaleX), ObjectAnimator.ofFloat(this, "scaleY", getScaleY(), scaleY));
        animatorSet.setDuration(10);
        animatorSet.start();
    }
}
