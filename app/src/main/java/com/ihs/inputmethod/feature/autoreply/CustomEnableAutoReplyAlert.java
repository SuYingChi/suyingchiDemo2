package com.ihs.inputmethod.feature.autoreply;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by yingchi.su on 2018/3/9.
 */

public class CustomEnableAutoReplyAlert extends AlertDialog implements View.OnClickListener {

    public CustomEnableAutoReplyAlert(Context context) {
        super(context, R.style.DesignDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_enable_auto_reply_alert);

        TextView titleTextView = findViewById(R.id.tv_title);
        titleTextView.setText(title);

        TextView messageTextView = findViewById(R.id.tv_message);
        messageTextView.setText(message);

        ImageView imageView = findViewById(R.id.iv_image);
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

    @Override
    public void onClick(View v) {

    }
}
