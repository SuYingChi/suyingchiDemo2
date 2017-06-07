package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.iap.PurchaseSlotsDialog;

import org.json.JSONObject;

/**
 * Created by jixiang on 16/12/26.
 */

public class PurchaseSlotsActivity extends HSAppCompatActivity  {
    private PurchaseSlotsDialog purchaseSlotDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        purchaseSlotDialog = new PurchaseSlotsDialog(this, new PurchaseSlotsDialog.OnItemClickListener() {
            @Override
            public void onUnlockAllSlotsClick() {
                unlockAllSlots();
            }

            @Override
            public void onUnlockViaWatchVideo() {
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("iapalert_unlimitedslots_WatchVideoToUnlockOne_clicked");
            }

            @Override
            public void onCloseButtonClick() {
                finish();
            }
        });
        purchaseSlotDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        purchaseSlotDialog.show();

    }

    private void unlockAllSlots() {
        HSIAPManager.getInstance().addListener(purchaseAllSlotsIapListener);
        String purchaseId = IAPManager.getManager().getUnlimitedSlotsProductId();
        IAPManager.getManager().purchaseProduct(purchaseId);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("iapalert_unlimitedslots_unlockall_clicked");
    }

    private HSIAPManager.HSIAPListener purchaseAllSlotsIapListener = new HSIAPManager.HSIAPListener() {
        @Override
        public void onPurchaseSucceeded(String productId) {
            HSLog.d("onIAPProductPurchaseSucceeded:productId:" + productId);
        }

        @Override
        public void onPurchaseFailed(String productId, int errorCode) {
            HSLog.d("onIAPProductPurchaseFailed:productId:" + productId + ",errorCode:" + errorCode);
            finish();
        }

        @Override
        public void onVerifySucceeded(String productId, JSONObject jsonObject) {
            HSLog.d("onIAPProductVerifySucceeded:productId:" + productId);
            Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.purchase_success), Toast.LENGTH_LONG).show();
            IAPManager.getManager().onVerifySuccessed(productId, jsonObject);
            finish();
            startCustomThemeActivity();
        }

        @Override
        public void onVerifyFailed(String productId, int errorCode) {
            HSLog.d("onVerifyFailed:errorCode:" + errorCode);
            IAPManager.getManager().onVerifyFailed(productId, errorCode);
            finish();
        }
    };

    /**
     * 需先执行finish，再执行startCustomThemeActivity，保证CustomThemeActivity的RewardVideoHelper实例的设置的setRewardedVideoAdListener广告监听不被移除
     */
    private void startCustomThemeActivity() {
        IAPManager.startCustomThemeActivity(getIntent().getExtras());
    }

    private void dismissDialog(Dialog dialog) {
        try {
            if (dialog != null && dialog.isShowing() && !isFinishing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        newConfig.orientation = Configuration.ORIENTATION_PORTRAIT;
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void finish() {
        dismissDialog(purchaseSlotDialog);
        purchaseSlotDialog = null;
        super.finish();
    }

    @Override
    protected void onDestroy() {
        HSIAPManager.getInstance().removeListener(purchaseAllSlotsIapListener);
        super.onDestroy();
    }


    private void purchaseOneSlot() {
        dismissDialog(purchaseSlotDialog);
        finish();
        IAPManager.getManager().unlockSlotViaWatchVideo();
        startCustomThemeActivity();
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("iapalert_unlimitedslots_WatchVideoToUnlockOne_VideoCompleted");
    }


}
