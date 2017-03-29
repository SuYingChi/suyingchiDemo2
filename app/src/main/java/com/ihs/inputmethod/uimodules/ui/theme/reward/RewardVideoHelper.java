package com.ihs.inputmethod.uimodules.ui.theme.reward;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.dialogs.HSAlertDialog;
import com.ihs.inputmethod.api.utils.HSNetworkConnectionUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by jixiang on 16/12/27.
 */

public class RewardVideoHelper implements RewardedVideoAdListener {
    private boolean isRewardedVideoLoading;
    private boolean showVideoAfterLoadImmediately;
    private boolean isRewarded = false;
    private boolean isClosed = false;

    private Activity activity;
    private static Activity hostActivity;
    private Handler handler;
    private RewardedVideoAd rewardedVideoAd;
    private RewardResultListener rewardResultListener;
    private AlertDialog loadingDialog;
    private AlertDialog checkUnlockingDialog;

    private final static int DISMISS_LOADING_DIALOG = 1;
    private final static int DISMISS_CHECKING_UNLOCK_DIALOG = 2;

    public RewardVideoHelper(Activity activity, RewardResultListener rewardResultListener) {
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity);
        this.activity = activity;
        handler = new Handler(activity.getMainLooper(),callback);

        //context must be activity,otherwise third part ad providers can't play video
        this.rewardResultListener = rewardResultListener;
        rewardedVideoAd.setRewardedVideoAdListener(this);
    }

    public static Activity getHostActivity() {
        return hostActivity;
    }

    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case DISMISS_LOADING_DIALOG:
                    if (isRewardedVideoLoading) {
                        showVideoAfterLoadImmediately = false;
                        dismissDialog(loadingDialog);
                        showNoVideoDialog();
                        if (rewardResultListener != null) {
                            rewardResultListener.onRewardedVideoLoadTimeout();
                        }
                    }
                    break;
                case DISMISS_CHECKING_UNLOCK_DIALOG:
                    dismissDialog(checkUnlockingDialog);
                    break;
            }
            return false;
        }
    };

    public void loadAndShowVideo() {
        hostActivity = activity;
        if (checkNetWork()) {
            if (!isRewardedVideoLoading) {
                isRewardedVideoLoading = true;
                reset();
                showLoadingDialog();
                handler.removeCallbacksAndMessages(null);
                handler.sendEmptyMessageDelayed(DISMISS_LOADING_DIALOG,10000);

                if (rewardResultListener != null) {
                    rewardResultListener.onRewardedVideoStartLoad();
                }
                if (rewardedVideoAd.isLoaded()) {
                    try {
                        rewardedVideoAd.show();
                    }catch (Exception e){
                    }
                } else {
                    showVideoAfterLoadImmediately = true;
                    loadVideo();
                }
            }
        }
    }

    private void reset(){
        isRewarded = false;
        isClosed = false;
    }

    private void showLoadingDialog() {
        if(activity!=null && !activity.isFinishing()) {
            loadingDialog = HSAlertDialog.build(activity).setView(R.layout.dialog_loading).create();
            loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    isRewardedVideoLoading = false;
                }
            });
            loadingDialog.setCancelable(false);
            loadingDialog.show();
        }
    }

    private boolean checkNetWork() {
        boolean networkConnected = HSNetworkConnectionUtils.isNetworkConnected();
        if (!networkConnected && activity!=null && !activity.isFinishing()) {
            HSAlertDialog.build(activity).setTitle(activity.getString(R.string.alert_no_network_title))
                    .setMessage(activity.getString(R.string.alert_no_network_message))
                    .setPositiveButton(activity.getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }
        return networkConnected;
    }


    public void loadVideo() {
        try {
            Bundle extras = new Bundle();
            extras.putBoolean("_noRefresh", true);
            AdRequest adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
            rewardedVideoAd.loadAd(HSConfig.optString("","Application","RewardConfig","reward_ad_unit_id"), adRequest);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showNoVideoDialog() {
        if(activity!=null && !activity.isFinishing()) {
            AlertDialog alertDialog = HSAlertDialog.build(activity).setTitle(activity.getString(R.string.alert_no_videos_available_title))
                    .setMessage(activity.getString(R.string.alert_no_videos_available_message))
                    .setPositiveButton(activity.getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (rewardResultListener != null) {
                        rewardResultListener.onRewardedFinish();
                    }
                    isRewardedVideoLoading = false;
                }
            });
            alertDialog.show();
        }
    }

    private void showUnLockingDialog() {
        if(activity!=null && !activity.isFinishing()) {
            checkUnlockingDialog = HSAlertDialog.build(activity).setView(R.layout.dialog_check_unlocking).create();
            checkUnlockingDialog.setCancelable(false);
            checkUnlockingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (rewardResultListener != null) {
                        rewardResultListener.onRewardedFinish();
                    }
                }
            });
            checkUnlockingDialog.show();
        }
    }

    private void dismissDialog(Dialog dialog) {
        try {
            if (dialog != null && dialog.isShowing() && activity!=null && !activity.isFinishing()) {
                dialog.dismiss();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void destroy() {
        activity = null;
        isRewardedVideoLoading = false;
        showVideoAfterLoadImmediately = false;
        isRewarded = false;
        isClosed = false;
        rewardedVideoAd.setRewardedVideoAdListener(null);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        HSLog.d("onRewardedVideoAdLoaded : showVideoAfterLoadImmediately :" + showVideoAfterLoadImmediately + ",rewardedVideoAd:" + rewardedVideoAd);
        if (showVideoAfterLoadImmediately && rewardedVideoAd != null) {
            dismissDialog(loadingDialog);
            try {
                rewardedVideoAd.show();
            }catch (Exception e){
            }
        }
    }

    @Override
    public void onRewardedVideoAdOpened() {
        HSLog.d("onRewardedVideoAdOpened");
    }

    @Override
    public void onRewardedVideoStarted() {
        HSLog.d("onRewardedVideoStarted");
        if (rewardResultListener != null) {
            rewardResultListener.onRewardedVideoStart();
        }
        dismissDialog(loadingDialog);
    }

    @Override
    public void onRewardedVideoAdClosed() {
        HSLog.d("onRewardedVideoAdClosed");
        isClosed = true;
        if(!checkRewardSuccess()) {
            showUnLockingDialog();
            handler.removeMessages(DISMISS_CHECKING_UNLOCK_DIALOG);
            handler.sendEmptyMessageDelayed(DISMISS_CHECKING_UNLOCK_DIALOG,10000);
        }
    }

    private boolean checkRewardSuccess() {
        if (isRewarded && (isClosed || isCurrentActivityOnTop())) {
            isRewarded = false;
            isClosed = false;
            if (rewardResultListener != null){
                rewardResultListener.onRewardedSuccess();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        HSLog.d("onRewarded rewardResultListener is null:" + (rewardResultListener == null) );
        dismissDialog(checkUnlockingDialog);
        if (rewardResultListener != null){
            rewardResultListener.onRewarded(rewardItem);
        }

        isRewarded = true;
        checkRewardSuccess();
    }

    private boolean isCurrentActivityOnTop(){
        try {
            ActivityManager mActivityManager =(ActivityManager) HSApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName currentTopActivity = mActivityManager.getRunningTasks(1).get(0).topActivity;
            if(activity!=null && currentTopActivity!=null && activity.getClass().getName().equals(currentTopActivity.getClassName())){
                return true;
            }
        }catch (Exception e){
        }
        return false;
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        HSLog.d("onRewardedVideoAdLeftApplication");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        HSLog.d("onRewardedVideoAdFailedToLoad : " + i);
        dismissDialog(loadingDialog);
        if(showVideoAfterLoadImmediately) {
            showNoVideoDialog();
        }
        if (rewardResultListener != null) {
            rewardResultListener.onRewardedVideoAdFailedToLoad(i);
        }
    }


    public interface RewardResultListener {
        void onRewardedSuccess();

        void onRewarded(RewardItem rewardItem);

        void onRewardedVideoAdFailedToLoad(int i);

        void onRewardedVideoStartLoad();

        void onRewardedVideoStart();

        void onRewardedVideoLoadTimeout();

        void onRewardedFinish();
    }
}
