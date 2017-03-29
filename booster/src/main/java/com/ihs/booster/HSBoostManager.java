package com.ihs.booster;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.booster.boost.common.FloatWindowManager;
import com.ihs.booster.boost.floating.FloatPrefManager;
import com.ihs.booster.boost.floating.FloatWaveView;
import com.ihs.booster.constants.MBConfig;
import com.ihs.booster.manager.MBMemoryManager;
import com.ihs.booster.utils.GAnalyticsUtils;

public class HSBoostManager {
    private static HSBoostManager instance;

    private FloatWaveView floatWaveView;

    public synchronized static HSBoostManager getInstance() {
        if (null == instance) {
            instance = new HSBoostManager();
        }
        return instance;
    }

    public void launchBoost() {
        if (MBConfig.ENABLE_BOOST) {
            showShortcutAnimation();
//            ObjectAnimator scaleWidthAnimator = ObjectAnimator.ofFloat(floatWaveView, "scaleX", 1f, 0.8f, 1f);
//            ObjectAnimator scaleHeightAnimator = ObjectAnimator.ofFloat(floatWaveView, "scaleY", 1f, 0.8f, 1f);
//            AnimatorSet transAnimatorSet = new AnimatorSet();
//            transAnimatorSet.play(scaleHeightAnimator).with(scaleWidthAnimator);
//            transAnimatorSet.setDuration(400);
//            transAnimatorSet.setInterpolator(new OvershootInterpolator());
//            transAnimatorSet.addListener(new AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
//            transAnimatorSet.start();
//            ObjectAnimator paintColorAnimator = ObjectAnimator.ofInt(floatWaveView, "paintColor", getColorArray(startValue, endValue));
//            paintColorAnimator.setDuration(400);
//            paintColorAnimator.setEvaluator(new ArgbEvaluator());
//            paintColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                    int current = Integer.valueOf(valueAnimator.getAnimatedValue().toString());
//                    setPaintColor(current);
//                }
//            });
            FloatWindowManager.getInstance().createBoostWindow();
        }
    }

    public View getBoostShortcutView(Context context) {
        if (MBConfig.ENABLE_BOOST) {
            if (floatWaveView == null) {
                floatWaveView = new FloatWaveView(context);
            }
            return floatWaveView;
        } else {
            return new View(context);
        }
    }

    public void showShortcutAnimation() {
        if (MBConfig.ENABLE_BOOST) {
            if (floatWaveView != null) {
                float memPercent = getMemoryUsedPercent();
                floatWaveView.flushWater(memPercent, memPercent);
            }
        }
    }

    public void startBoostedAnimation() {
        if (MBConfig.ENABLE_BOOST) {
            if (floatWaveView != null) {
                float memPercentBeforeClean = FloatPrefManager.getMemPercentBeforeClean();
                if (memPercentBeforeClean > 0) {
                    float cleanedPercent = memPercentBeforeClean - MBMemoryManager.getInstance().getRealTimeUsedPercent();
                    float afterPercent = 0f;
                    if (cleanedPercent < memPercentBeforeClean * FloatWaveView.LEAST_CLEAN_THRESHOLD / 100) {
                        afterPercent = memPercentBeforeClean * (100 - FloatWaveView.LEAST_CLEAN_THRESHOLD) / 100;
                    } else {
                        afterPercent = memPercentBeforeClean - cleanedPercent;
                    }
                    floatWaveView.flushWater(memPercentBeforeClean, afterPercent);
                    FloatPrefManager.setLastBoostedMemUsage(Float.valueOf(afterPercent).intValue());
                }
            }
        }
    }

    public float getMemoryUsedPercent() {
        return MBMemoryManager.getInstance().getUsedPercentFake();
    }

    private HSBoostManager() {
        if (getProcessName().equalsIgnoreCase(HSApplication.getContext().getPackageName())) {
            initInMainProcess();
        }
    }

    private void initInMainProcess() {
        // Native AD


        GAnalyticsUtils.init(HSApplication.getContext());
    }

    public String getProcessName() {
        String processName = HSApplication.getContext().getPackageName();
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) HSApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null && manager.getRunningAppProcesses() != null) {
            for (RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
                if (processInfo.pid == pid) {
                    processName = processInfo.processName;
                    break;
                }
            }
        }
        return processName;
    }
}
