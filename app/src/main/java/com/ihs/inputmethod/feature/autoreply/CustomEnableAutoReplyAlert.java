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


    }

    @Override
    public void onClick(View v) {

    }
}
