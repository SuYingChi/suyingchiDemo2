package com.ihs.inputmethod.feature.boost;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.boost.animation.BlackHoleLayout;
import com.honeycomb.launcher.dialog.BoostTip;
import com.honeycomb.launcher.model.LauncherFiles;
import com.honeycomb.launcher.util.PreferenceHelper;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.utils.HSLog;

import hugo.weaving.DebugLog;

/**
 * An activity for launching boost outside out launcher.
 */
public class BoostActivity extends HSActivity {

    public static final String INTENT_KEY_START_SOURCE = "start_source";
    public static final String INTENT_KEY_BOOST_TYPE = "boost_type";

    public static final int START_SOURCE_FOREIGN_LAUNCHER = 0;

    private ViewGroup mContent;

    private int mStartSource;
    private BoostType mBoostType;

    @DebugLog
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_boost);

        mContent = (ViewGroup) findViewById(android.R.id.content);

        handleIntent(getIntent());

        startForeignIconAnimation();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();// check out if ad shown needed this time.

        // Write down count.
        if (PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getInt(BoostTipUtils.PREF_KEY_BOOST_TIP_SHOW_COUNT, 0) > 0) {
            BoostTipUtils.incrementShowCount();
        }
    }

    private void handleIntent(Intent intent) {
        mStartSource = intent.getIntExtra(INTENT_KEY_START_SOURCE, 0);

        mBoostType = BoostType.values()[intent.getIntExtra(INTENT_KEY_BOOST_TYPE,0)];
        logBoostClickedEvent();
    }

    private void startForeignIconAnimation() {
        HSAnalytics.logEvent("Boost_Animation_Viewed", "type", "FloatingWindow");
        final BlackHoleLayout blackHoleLayout = new BlackHoleLayout(BoostActivity.this);
        blackHoleLayout.setBoostType(mBoostType);
        blackHoleLayout.setBoostSource(BoostSource.BoostActivity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContent.addView(blackHoleLayout, params);
        blackHoleLayout.setBlackHoleAnimationListener(new BlackHoleLayout.BlackHoleAnimationListener() {
            @Override
            public void onEnd() {
                finishWithoutAnimation();
            }
        });
        blackHoleLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                blackHoleLayout.startAnimation();
            }
        }, 300);
    }

    private void finishWithoutAnimation() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        finishWithoutAnimation();
        super.onBackPressed();
    }

    private void logBoostClickedEvent() {
        switch (mStartSource) {
            case START_SOURCE_FOREIGN_LAUNCHER:
                HSAnalytics.logEvent("Boost_Shortcut_Clicked");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        HSLog.d(BoostTip.TAG, "BoostActivity stopped");
        super.onStop();
    }
}
