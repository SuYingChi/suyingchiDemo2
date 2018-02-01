package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.ihs.feature.common.AnimatorListenerAdapter;
import com.ihs.feature.ui.ProgressFrameLayout;
import com.ihs.feature.ui.SuccessTickView;
import com.ihs.inputmethod.uimodules.R;

public class ProgressDialog extends Dialog {

    // instance will be null after dismissed
    @SuppressLint("StaticFieldLeak")
    private static ProgressDialog sCustomProgressDialog = null;

    private TextView mTextView;
    private ProgressFrameLayout mProgressFrameLayout;
    private boolean mIsCancelable;

    public ProgressDialog(Context context, int theme) {
        super(context, R.style.WallpaperLoadingDialogTheme);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public ProgressDialog(Context context) {
//        super(context, R.style.WallpaperLoadingDialogTheme);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public static ProgressDialog createDialog(Context context, String text, DialogInterface.OnDismissListener animationListener) {
        sCustomProgressDialog = new ProgressDialog(context, R.style.WallpaperLoadingDialogTheme);
        sCustomProgressDialog.setContentView(R.layout.progress_layout);
        sCustomProgressDialog.setCancelable(true);
        sCustomProgressDialog.setOnDismissListener(animationListener);
        TextView textView = sCustomProgressDialog.findViewById(R.id.dialog_loading_text_view);
        textView.setText(text);
        sCustomProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        return sCustomProgressDialog;
    }

    public static ProgressDialog createDialog(Context context, String text) {
        return createDialog(context, text, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressFrameLayout = findViewById(R.id.progress_dialog);
        mTextView = findViewById(R.id.dialog_loading_text_view);
    }

    @Override
    public void setCancelable(boolean flag) {
        super.setCancelable(flag);
        mIsCancelable = flag;
    }

    public void show() {
        Context context = getContext();
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        super.show();
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void setMessage(CharSequence message) {
//        mTextView.setText(message);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    /**
     * @param isInstant if true, the dialog will dismiss instantly
     *                  else the dialog won't dismiss util the dialog ticker animation ends.
     *                  finally, super.dismiss() will be called.
     */
    public void dismiss(boolean isInstant) {
        if (isInstant) {
            if (isShowing()) {
                try {
                    super.dismiss();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            sCustomProgressDialog = null;
        } else {
            if (isShowing()) {
                getTickView().setInternalAnimationListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ProgressDialog.this.dismiss(true);
                    }
                });
                finishAnimation();
            }
        }
    }

    /**
     * dismiss instantly without animation , use {@link #dismiss(boolean)}
     */
    @Override
    public void dismiss() {
        dismiss(true);
    }

    private void finishAnimation() {
        final ProgressFrameLayout progressLayout = getProgressFrameLayout();
        progressLayout.finish(new Runnable() {
            @Override
            public void run() {
                progressLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressLayout.setVisibility(View.INVISIBLE);
                    }
                }, 2000);
            }
        });
    }

    private SuccessTickView getTickView() {
        return getProgressFrameLayout().getSuccessTickView();
    }

    private ProgressFrameLayout getProgressFrameLayout() {
        if (mProgressFrameLayout == null) {
            mProgressFrameLayout = findViewById(R.id.progress_dialog);
        }
        return mProgressFrameLayout;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus && mIsCancelable) {
            dismiss(true);
        }
    }
}