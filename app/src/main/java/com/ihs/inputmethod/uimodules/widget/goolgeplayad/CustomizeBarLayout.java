package com.ihs.inputmethod.uimodules.widget.goolgeplayad;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by Arthur on 17/5/18.
 */

public class CustomizeBarLayout extends FrameLayout {
    private int lastX;
    private int lastY;

    private boolean lockedMode  = true;
    private OnCustomizeBarListener customizeBarListener;

    public interface OnCustomizeBarListener{
        void onHide();
    }

    public CustomizeBarLayout(@NonNull Context context,OnCustomizeBarListener customizeBarListener) {
        super(context);
        this.customizeBarListener = customizeBarListener;
        init();

    }
    private FrameLayout container ;

    private void init() {
        View inflate = inflate(getContext(), R.layout.customize_bar_bg, null);
        addView(inflate);
        container = (FrameLayout) findViewById(R.id.fl_container);

        findViewById(R.id.fl_iv_strip).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int x = (int) event.getX();
                int y = (int) event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //记录触摸点的坐标
                        lastX = x;
                        lastY = y;
                        lockedMode = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //计算偏移量
                        int offsetX = x - lastX;
                        int offsetY = y - lastY;
                        //在当前left,top,right,bottom的基础上加上便宜量
                        HSLog.e("" + offsetY);

                        if (offsetY > 10 && lockedMode) {
                            if (container.getVisibility() != GONE) {
                                customizeBarListener.onHide();
                            }
                            lockedMode = false;
                        }
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//            getLayoutParams().height = (int) (HSDisplayUtils.getScreenHeightForContent()*0.14);
    }

    public CustomizeBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomizeBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setContent(View child){
        if(child.getParent()!=null){
            ((ViewGroup) child.getParent()).removeView(child);
        }
        container.addView(child,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }


}
