package com.ihs.inputmethod.uimodules.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.panel.HSThemeSelectPanel;
import com.ihs.inputmethod.uimodules.ui.theme.ui.panel.HSThemeSelectRecycler;


/**
 * Created by jixiang on 16/10/13.
 */

public class NewThemePromptView extends LinearLayout{

    private View rootView;
    private View triangleView;
    private TextView promptTextView;
    private int centerX;
    private AnimatorSet animatorSet;
    private HSThemeSelectPanel hsThemeSelectPanel;

    public NewThemePromptView(Context context, HSThemeSelectPanel hsThemeSelectPanel) {
        super(context);
        this.hsThemeSelectPanel = hsThemeSelectPanel;
        initView(context);
    }


    private void initView(Context context) {
        rootView = View.inflate(context, R.layout.theme_prompt_view, this);
        triangleView = rootView.findViewById(R.id.triangle_view);
        promptTextView = rootView.findViewById(R.id.prompt_text_view);
    }

    public void setPromptText(String text){
        promptTextView.setText(text);
    }

    /**
     * 更新提示view的位置，先判断HSThemeSelectRecycler的指定position的item距离屏幕底的位置是否够显示提示，如果不够，需要滚动
     * @param hsThemeSelectRecycler
     * @param position
     */
    public void prepareShowBelow(final HSThemeSelectRecycler hsThemeSelectRecycler, final int position){
        rootView.measure(0, 0);
        View view = hsThemeSelectRecycler.getLayoutManager().findViewByPosition(position);
        if(view == null){
            clearTip();
            return;
        }

        ViewGroup rootViewParent = (ViewGroup) hsThemeSelectRecycler.getParent();
        int rootHeight = rootView.getMeasuredHeight();
        int rootParentHeight = rootViewParent.getHeight();
        int[] loc = new int[2];
        view.getLocationInWindow(loc);
        int x = loc[0];
        int y = loc[1];

        rootViewParent.getLocationInWindow(loc);
        int recyclerItemAlignParentTop = y - loc[1];
        int offset = (int) (10* HSApplication.getContext().getResources().getDisplayMetrics().density); //提示view需要上移一段距离，不是完全显示在mThemeSelectRecyclerView的position位置的view下面
        int topMargin = view.getHeight()+recyclerItemAlignParentTop - offset;
        centerX = x+view.getWidth()/2;

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        if(rootParentHeight - topMargin < rootHeight ){
            GridLayoutManager gridLayoutManager = (GridLayoutManager) hsThemeSelectRecycler.getLayoutManager();
            gridLayoutManager.scrollToPositionWithOffset(0, - (rootHeight - (rootParentHeight - topMargin) ) -hsThemeSelectRecycler.computeVerticalScrollOffset() );//正数为向下滚动偏移量
            layoutParams.gravity = Gravity.BOTTOM;
        }else {
            layoutParams.gravity = Gravity.TOP;
            layoutParams.setMargins(0,topMargin,0,0);
        }
        updateChildView();
        requestLayout();
        setVisibility(VISIBLE);
        playShakeAnim();
    }

    private void updateChildView() {
        LayoutParams layoutParams = (LayoutParams) triangleView.getLayoutParams();
        int triangleLeftMargin = centerX - triangleView.getMeasuredWidth()/2;
        layoutParams.setMargins(triangleLeftMargin,0,0,0);
        triangleView.requestLayout();

        promptTextView.measure(0, 0);
        //update prompt view position
        int promptTextViewWidth = promptTextView.getMeasuredWidth();
        LayoutParams layoutParams1 = (LayoutParams) promptTextView.getLayoutParams();
        if(promptTextViewWidth < centerX*2 ){ //if promptTextView width is small than recyclerView item width,let promptTextView center in recyclerView item
            layoutParams1.setMargins((centerX*2 -promptTextViewWidth)/2,0,0,0);
        }else {
            layoutParams1.setMargins(0,0,0,0);
        }
        promptTextView.requestLayout();
    }

    private boolean isRepeat = false;
    private final static int DISMISS_NEW_TIP_DELAY = 1000;
    private final static int HANDLE_WHAT_DISMISS_NEW_TIP = 1;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLE_WHAT_DISMISS_NEW_TIP:
                    clearTip();
                    break;
            }
        }
    };

    private void clearTip() {
        setVisibility(View.GONE);
        if(hsThemeSelectPanel!=null) {
            //TODO:cjx hide new tip in the further
//            HSKeyboardPanelTab panelTab = hsThemeSelectPanel.getPanelTab();
//            if (panelTab != null) {
//                panelTab.showNewTip(false);
//            }
        }
    }

    private void playShakeAnim() {
        isRepeat = false;
        if(animatorSet == null) {
            animatorSet = new AnimatorSet();
            ObjectAnimator shakeUpAnim = ObjectAnimator.ofFloat(this, "translationY", 0, -30);
            shakeUpAnim.setDuration(200);
            ObjectAnimator shakeBackAnim = ObjectAnimator.ofFloat(this, "translationY", -30, 0);
            shakeBackAnim.setDuration(200);
            animatorSet.playSequentially(shakeUpAnim, shakeBackAnim);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (!isRepeat) {
                        isRepeat = true;
                        animation.start();
                    }else {
                        handler.sendEmptyMessageDelayed(HANDLE_WHAT_DISMISS_NEW_TIP,DISMISS_NEW_TIP_DELAY);
                    }
                }
            });
        }else{
            if(animatorSet.isRunning()) {
                animatorSet.cancel();
            }
            handler.removeMessages(HANDLE_WHAT_DISMISS_NEW_TIP);
        }
        animatorSet.start();
    }

    public void hideTip(){
        if(animatorSet!= null){
            if(animatorSet.isRunning()){
                animatorSet.cancel();
            }
        }
        clearTip();
    }

    public void release(){
        hideTip();
        hsThemeSelectPanel = null;
    }
}
