package com.ihs.inputmethod.uimodules.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;

public class CustomDesignAlert extends AlertDialog implements View.OnClickListener {
    private static final String TAG = CustomDesignAlert.class.getSimpleName();
    private int imageResId;
    private CharSequence title;
    private CharSequence message;
    private CharSequence positiveButtonText;
    private CharSequence negativeButtonText;
    private View.OnClickListener positiveButtonClickListener;
    private View.OnClickListener negativeButtonClickListener;
    private View.OnClickListener privacyButtonClickListener;
    private boolean enablePrivacy;

    public CustomDesignAlert(@NonNull Context context) {
        super(context, R.style.DesignDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_design_alert);

        TextView titleTextView = (TextView) findViewById(R.id.tv_title);
        titleTextView.setText(title);

        TextView messageTextView = (TextView) findViewById(R.id.tv_message);
        messageTextView.setText(message);

        ImageView imageView = (ImageView) findViewById(R.id.iv_image);
        if (imageResId != 0) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(imageResId);
        }

        if (hasSingleButton()) {
            enableSingleButton();
        } else {
            enableButtons();
        }

        enablePrivacy(enablePrivacy);

        int width = (int) getContext().getResources().getFraction(R.fraction.design_dialog_width, HSDisplayUtils.getScreenWidthForContent(), HSDisplayUtils.getScreenWidthForContent());
        findViewById(R.id.root_view).getLayoutParams().width = width;

        findViewById(R.id.iv_image).getLayoutParams().height = width / 2;

        if (!(getContext() instanceof Activity)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(HSApplication.getContext())) {
                getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            } else {
                getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
    }

    private boolean hasSingleButton() {
        return TextUtils.isEmpty(negativeButtonText);
    }

    private void enableSingleButton() {
        findViewById(R.id.ll_single_button).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_buttons).setVisibility(View.GONE);

        float radius = getContext().getResources().getDimension(R.dimen.design_base_corner_radius);

        TextView positiveButton = (TextView) findViewById(R.id.btn_positive_single);
        positiveButton.setText(positiveButtonText);
        positiveButton.setOnClickListener(this);
        positiveButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(0xff4db752, radius));
    }

    private void enablePrivacy(boolean enable) {
        if (enable) {
            findViewById(R.id.fl_privacy).setVisibility(View.VISIBLE);
            TextView textView = (TextView) findViewById(R.id.tv_privacy);
            textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            textView.setOnClickListener(this);
        } else {
            findViewById(R.id.fl_privacy).setVisibility(View.GONE);
        }
    }

    private void enableButtons() {
        findViewById(R.id.ll_single_button).setVisibility(View.GONE);
        findViewById(R.id.ll_buttons).setVisibility(View.VISIBLE);

        float radius = getContext().getResources().getDimension(R.dimen.design_base_corner_radius);

        TextView positiveButton = (TextView) findViewById(R.id.btn_positive);
        positiveButton.setText(positiveButtonText);
        positiveButton.setOnClickListener(this);
        positiveButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(0xff3d6efa, radius));

        TextView negativeButton = (TextView) findViewById(R.id.btn_negative);
        negativeButton.setText(negativeButtonText);
        negativeButton.setOnClickListener(this);
        negativeButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, radius));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        dismiss();

        if (id == R.id.btn_positive_single) {
            if (positiveButtonClickListener != null) {
                positiveButtonClickListener.onClick(findViewById(R.id.btn_positive_single));
            }
        } else if (id == R.id.btn_positive) {
            if (positiveButtonClickListener != null) {
                positiveButtonClickListener.onClick(findViewById(R.id.btn_positive));
            }
        } else if (id == R.id.btn_negative) {
            if (negativeButtonClickListener != null) {
                negativeButtonClickListener.onClick(findViewById(R.id.btn_negative));
            }
        } else if (id == R.id.tv_privacy) {
            if (privacyButtonClickListener != null) {
                privacyButtonClickListener.onClick(findViewById(R.id.tv_privacy));
            }
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
    }

    @Override
    public void setMessage(CharSequence message) {
        this.message = message;
    }

    public void setEnablePrivacy(boolean enablePrivacy, View.OnClickListener listener) {
        this.enablePrivacy = enablePrivacy;
        this.privacyButtonClickListener = listener;
    }

    public void setPositiveButton(CharSequence text, View.OnClickListener listener) {
        HSLog.d(TAG, text.toString());
        this.positiveButtonText = text;
        this.positiveButtonClickListener = listener;
    }

    public void setNegativeButton(CharSequence text, View.OnClickListener listener) {
        HSLog.d(TAG, text.toString());
        this.negativeButtonText = text;
        this.negativeButtonClickListener = listener;
    }

    public void setImageResource(int resId) {
        this.imageResId = resId;
    }

    private boolean isInvalid() {
        return TextUtils.isEmpty(positiveButtonText);
    }

    @Override
    public void show() {
        if (isInvalid()) {
            HSLog.e(TAG, "Invalid dialog");
            return;
        }
        try {
            super.show();
            /**
             * 设置dialog宽度全屏
             */
            android.view.WindowManager.LayoutParams params = getWindow().getAttributes();  //获取对话框当前的参数值、
            params.width = HSDisplayUtils.getScreenWidthForContent();    //宽度设置全屏宽度
            getWindow().setAttributes(params);     //设置生效
        }catch (Exception e){
        }


    }
}
