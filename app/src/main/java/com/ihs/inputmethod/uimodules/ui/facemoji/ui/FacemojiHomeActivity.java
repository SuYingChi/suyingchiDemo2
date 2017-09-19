package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;

public class FacemojiHomeActivity extends HSAppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_facemoji_home);

        findViewById(R.id.btn_make_facemoji).setOnClickListener(this);
        findViewById(R.id.btn_show_my_facemoji).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_make_facemoji:
                startCameraActivity();
                break;
            case R.id.btn_show_my_facemoji:
                startMyFacemojiActivity();
                break;
        }
    }


    private void startCameraActivity() {
        Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);
    }

    private void startMyFacemojiActivity() {
        Intent i = new Intent(this, MyFacemojiActivity.class);
        startActivity(i);
    }
}
