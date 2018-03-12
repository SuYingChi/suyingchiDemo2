package com.ihs.inputmethod.feature.autoReply;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;


import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.feature.common.RoundCornerImageView;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.alerts.HSAlertDialog;
import com.kc.commons.utils.KCCommonUtils;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by yingchi.su on 2018/3/9.
 */

public class EnableAutoResponseAlertActivity extends HSAppCompatActivity {


    private AlertDialog dialog;
    private AlertDialog permissionDialog;
    private Handler handler = new Handler();
    private final int START_ACCESSIBILITY_SETTINGS_REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View alertView = View.inflate(this, R.layout.custom_enable_auto_response_alert, null);
        GifImageView autoResponseGifImageView = (GifImageView) alertView.findViewById(R.id.enable_auto_response_gifImageView);
        Uri uri = Uri.parse("android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.app_theme_new_gif);
        autoResponseGifImageView.setImageURI(uri);
        RoundCornerImageView closeBtn = (RoundCornerImageView) alertView.findViewById(R.id.auto_response_alert_close_button);
        dialog = HSAlertDialog.build(this).setView(alertView).setCancelable(true).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KCCommonUtils.dismissDialog(dialog);
            }
        });
        TextView enableAutoResponseBtn = (TextView) alertView.findViewById(R.id.btn_positive_single);
        enableAutoResponseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableAutoResponse();

            }
        });
        KCCommonUtils.showDialog(dialog);

    }

    private void enableAutoResponse() {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, START_ACCESSIBILITY_SETTINGS_REQUEST_CODE);
        View permissionAlertView = View.inflate(this, R.layout.custom_enable_auto_reply_permission_alert, null);
        GifImageView autoResponseGifImageView = (GifImageView) permissionAlertView.findViewById(R.id.enable_permission_auto_reply_animated_view);
        Uri uri = Uri.parse("android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.app_theme_new_gif);
        autoResponseGifImageView.setImageURI(uri);
        TextView btn = (TextView) permissionAlertView.findViewById(R.id.btn_positive_single);
        if (permissionDialog == null) {
            permissionDialog = HSAlertDialog.build(0).setView(permissionAlertView).setCancelable(true).create();
            permissionDialog.setCancelable(true);
            permissionDialog.setCanceledOnTouchOutside(true);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    KCCommonUtils.dismissDialog(permissionDialog);
                }
            });
        }
        final int GUIDE_DELAY = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                KCCommonUtils.showDialog(permissionDialog);
            }
        }, GUIDE_DELAY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == START_ACCESSIBILITY_SETTINGS_REQUEST_CODE){
            if(HSAccessibilityService.isAvailable()){
                KCCommonUtils.dismissDialog(dialog);
            }
        }
    }

}
