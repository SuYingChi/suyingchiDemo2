package com.keyboard.colorkeyboard.app;

import android.content.Intent;
import android.os.Bundle;

import com.ihs.inputmethod.api.HSDeepLinkActivity;
import com.keyboard.colorkeyboard.R;

/**
 * Created by wenbinduan on 2016/11/8.
 */

public final class SplashActivity extends HSDeepLinkActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setData(getIntent().getData());
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.stand, R.anim.splash);
    }
}
