package com.ihs.inputmethod.uimodules.ui.theme.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSBitmapUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by dsapphire on 16/5/19.
 */
public class HSCommonHeaderView extends RelativeLayout {

    Drawable buttonCancelLeftDrawable = null;
    Drawable buttonOKRightDrawable = null;
    Handler handler = new Handler(Looper.getMainLooper());
    private Button textCancel;
    private Button textOK;
    private TextView textHead;
    private String titleCancel;
    private String titleOK;
    private String titleHead;
    private boolean backButtonVisible;
    private boolean nextButtonVisible;

    public HSCommonHeaderView(Context context) {
        this(context, null);
    }

    public HSCommonHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSCommonHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setText(final String cancel, final String title, final String ok) {
        findViews();
        titleCancel = cancel;
        titleHead = title;
        titleOK = ok;
        updateTitle();
    }

    private void findViews() {
        if (textHead != null) {
            return;
        }
        textCancel = (Button) findViewById(R.id.custom_theme_title_cancel);
        textHead = (TextView) findViewById(R.id.custom_theme_title_title);
        textOK = (Button) findViewById(R.id.custom_theme_title_ok);
//        textCancel.setAllCaps(false);
//        textHead.setAllCaps(false);
//        textOK.setAllCaps(false);
    }

    private void updateTitle() {
        findViews();
        if (textCancel == null || titleHead == null) {
            return;
        }
        textHead.setText(titleHead);

        textOK.setText(titleOK);
        textCancel.setText(titleCancel);
    }

    public void setButtonVisibility(final boolean backButtonVisible, final boolean nextButtonVisible) {
        findViews();
        this.backButtonVisible = backButtonVisible;
        this.nextButtonVisible = nextButtonVisible;
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (textCancel == null) {
            return;
        }
//        if (backButtonVisible) {
//            textCancel.setCompoundDrawablesWithIntrinsicBounds(getButtonCancelLeftDrawable(), null, null, null);
//            textCancel.setCompoundDrawablePadding(10);
//        } else {
//            textCancel.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//        }
//
//        if (nextButtonVisible) {
//            textOK.setCompoundDrawablesWithIntrinsicBounds(null, null, getButtonOKRightDrawable(), null);
//            textOK.setCompoundDrawablePadding(10);
//        } else {
//            textOK.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//        }
    }

    public void setOnNavigationClickListener(final OnNavigationClickListener onNavigationClickListener) {
        textOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNavigationClickListener != null) {
                    onNavigationClickListener.onRightClick(v);
                }
            }
        });
        textCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNavigationClickListener != null) {
                    onNavigationClickListener.onLeftClick(v);
                }
            }
        });
    }

    public void setHeaderNextEnable(final boolean enable) {
        if (textOK != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textOK.setEnabled(enable);
                    // TODO: chenyuanming 26/12/2016
                    // 本来可以通过stateListDrawable.addState(new int[]{-android.R.attr.state_enabled},disabledArrowNextBitmapDrawable);方式设置,
                    // 但是负的state值在Device128上面设置无效
//                    if (nextButtonVisible) {
//                        if (enable) {
//                            textOK.setCompoundDrawablesWithIntrinsicBounds(null, null, getButtonOKRightDrawable(), null);
//                        } else {
//                            Bitmap nextBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.navigationbar_arrow_next);
//                            int arrowNextWidth = (int) (nextBitmap.getWidth() * 0.6);
//                            int arrowNextHeight = (int) (nextBitmap.getHeight() * 0.6) + 6;
//
//                            Bitmap arrowNextBitmap = Bitmap.createScaledBitmap(nextBitmap, arrowNextWidth, arrowNextHeight, true);
//                            Drawable disabledArrowNextBitmapDrawable = new BitmapDrawable(HSApplication.getContext().getResources(), arrowNextBitmap);
//                            disabledArrowNextBitmapDrawable.setColorFilter(getContext().getResources().getColor(R.color.light_button_disabled), Mode.SRC_IN);
//                            textOK.setCompoundDrawablesWithIntrinsicBounds(null, null, disabledArrowNextBitmapDrawable, null);
//                        }
//                    }
                }
            });
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    public Drawable getButtonOKRightDrawable() {
        if (buttonOKRightDrawable == null) {
            Bitmap nextBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.navigationbar_arrow_next);
            int arrowNextWidth = (int) (nextBitmap.getWidth() * 0.6);
            int arrowNextHeight = (int) (nextBitmap.getHeight() * 0.6) + 6;

            Bitmap arrowNextBitmap = Bitmap.createScaledBitmap(nextBitmap, arrowNextWidth, arrowNextHeight, true);
            BitmapDrawable arrowNextDrawable = new BitmapDrawable(HSApplication.getContext().getResources(), arrowNextBitmap);

            Bitmap pressedArrowNextBitmap = HSBitmapUtils.makeDarkBitmap(arrowNextBitmap, arrowNextWidth, arrowNextHeight, 0.2f);


            BitmapDrawable pressedArrowNextBitmapDrawable = new BitmapDrawable(HSApplication.getContext().getResources(), pressedArrowNextBitmap);
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedArrowNextBitmapDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedArrowNextBitmapDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedArrowNextBitmapDrawable);
            stateListDrawable.addState(new int[]{}, arrowNextDrawable);
            buttonOKRightDrawable = stateListDrawable;
        }
        return buttonOKRightDrawable;
    }

    public Drawable getButtonCancelLeftDrawable() {
        if (buttonCancelLeftDrawable == null) {
            Bitmap backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.navigationbar_arrow_back);
            int arrowBackWidth = (int) (backBitmap.getWidth() * 0.6);
            int arrowBackHeight = (int) (backBitmap.getHeight() * 0.6) + 6;

            Bitmap arrowBackBitmap = Bitmap.createScaledBitmap(backBitmap, arrowBackWidth, arrowBackHeight, true);
            BitmapDrawable arrowBackDrawable = new BitmapDrawable(HSApplication.getContext().getResources(), arrowBackBitmap);

            Bitmap pressedArrowBackBitmap = HSBitmapUtils.makeDarkBitmap(arrowBackBitmap, arrowBackWidth, arrowBackHeight, 0.2f);

            BitmapDrawable pressedArrowBackBitmapDrawable = new BitmapDrawable(HSApplication.getContext().getResources(), pressedArrowBackBitmap);
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedArrowBackBitmapDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedArrowBackBitmapDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedArrowBackBitmapDrawable);
            stateListDrawable.addState(new int[]{}, arrowBackDrawable);
            buttonCancelLeftDrawable = stateListDrawable;
        }
        return buttonCancelLeftDrawable;
    }

    public interface OnNavigationClickListener {
        void onLeftClick(View view);

        void onRightClick(View view);
    }
}
