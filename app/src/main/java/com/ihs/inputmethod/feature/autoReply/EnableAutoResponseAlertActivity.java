package com.ihs.inputmethod.feature.autoReply;


import android.net.Uri;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.R;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by yingchi.su on 2018/3/9.
 */

public class EnableAutoResponseAlertActivity extends HSAppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_enable_auto_response_alert);
        initView();

    }

    private void initView() {
        LinearLayout autoResponseContentAlert = (LinearLayout)findViewById(R.id.auto_respone_content_alert);
        autoResponseContentAlert.setClickable(true);
        GifImageView autoResponseGifImageView = (GifImageView)findViewById(R.id.enable_auto_response_gifImageView);
        Uri uri = Uri.parse("android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.app_theme_new_gif);
        autoResponseGifImageView.setImageURI(uri);
        ImageButton closeBtn = (ImageButton)findViewById(R.id.auto_response_alert_close_button);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView enableAutoResponseBtn = (TextView)findViewById(R.id.btn_positive_single);
        enableAutoResponseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //开启自动回复
                enableAutoResponse();
                finish();
            }
        });


    }

    private void enableAutoResponse() {
        Toast.makeText(this,"开启自动回复逻辑",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }
}
