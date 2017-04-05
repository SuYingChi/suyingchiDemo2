/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobipioneer.lockerkeyboard.ads.engine.common;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.inputmethod.uimodules.R;
import com.mobipioneer.inputmethod.panels.settings.EmojiSettingsActivity2;
import com.mobipioneer.inputmethod.panels.settings.widget.SwitchPreference;

import java.util.List;

public final class ADEItemSettingActivity extends HSActivity {

    private int adItem;
    private SwitchPreference switchPreference;

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ad_engine_item_setting);

        adItem = getIntent().getIntExtra("ad_item", -1);

        initBackButton();
        initSwitchPreference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchPreference.refreshSwitchState();
    }

    private String getAdItemTitle() {
        switch (adItem) {

            case ADEConstants.ADE_ITEM_CUSTOM_BOOST:
                return getResources().getString(R.string.custom_boost_alert_title);

            case ADEConstants.ADE_ITEM_INPUT_SECURITY_CHECK:
                return getResources().getString(R.string.input_security_check_alert_title);

            case ADEConstants.ADE_ITEM_OPTIMIZE:
                return getResources().getString(R.string.optimize_alert_title);

            default:
                return "Settings";
        }
    }

    private void initTitle() {
        TextView title = (TextView) findViewById(R.id.settings_main_title);
        title.setText(getAdItemTitle());
    }

    private void initBackButton() {
        ImageView back = (ImageView) findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ADEItemSettingActivity.this,EmojiSettingsActivity2.class));
                finish();
            }
        });
    }

    private void initSwitchPreference() {
        switch (adItem) {

            case ADEConstants.ADE_ITEM_CUSTOM_BOOST:
                switchPreference = (SwitchPreference) findViewById(R.id.custom_boost);
                break;

            case ADEConstants.ADE_ITEM_INPUT_SECURITY_CHECK:
                switchPreference = (SwitchPreference) findViewById(R.id.input_security_check);
                break;

            case ADEConstants.ADE_ITEM_OPTIMIZE:
                switchPreference = (SwitchPreference) findViewById(R.id.optimize);
                break;

            default:
                break;
        }

        if (switchPreference != null) {
            switchPreference.setVisibility(View.VISIBLE);
        }
    }


    private boolean isEmojiSettingActivityExist(){
        boolean exist = false;
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
        for(ActivityManager.RunningTaskInfo info : runningTasks){
            if(EmojiSettingsActivity2.class.getSimpleName().equals(info.getClass().getSimpleName())){
                exist = true;
                break;
            }
        }
        return exist;
    }
}
