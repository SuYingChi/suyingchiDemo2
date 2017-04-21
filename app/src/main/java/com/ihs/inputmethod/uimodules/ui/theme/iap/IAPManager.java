package com.ihs.inputmethod.uimodules.ui.theme.iap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.PurchaseSlotsActivity;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.keyboard.core.themes.custom.elements.KCBaseElement;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jixiang on 16/5/5.
 * 内购
 * 1、用于自定义主题的内购（目前超过2个会提示购买展示位）
 * 2、每个主题的元素（背景、字体等）
 */
public class IAPManager {
    private final static int APP_ID_FOR_RAINBOW = 603; //找IAP服务器人员获取和pkg对应到id
    private int appID = APP_ID_FOR_RAINBOW; //找IAP服务器人员获取和pkg对应到id
    private String deviceID = "";//唯一标示一台机器到key，如果需要关心哪些设备购买了产品，则需要设置
    private final IAPProduct iapProduct;
    public final static String NOTIFICATION_IAP_PURCHASE_SUCCESS = "keyboard_iap_purchase_success";
    private static IAPManager instance;


    /**
     * IAP errorCode  Google error codes
     */
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;

    private IAPManager() {
        iapProduct = new IAPProduct();
        deviceID = new DeviceUuidFactory(HSApplication.getContext()).getDeviceUuid().toString();
        init();
    }

    public static IAPManager getManager() {
        if (instance == null) {
            synchronized (IAPManager.class) {
                if (instance == null) {
                    instance = new IAPManager();
                }
            }
        }
        return instance;
    }

    private void init() {
        iapProduct.init();
        HSIAPManager.getInstance().enableLog();
        // 此类产品id，购买后需要消费才能再次购买，适合 金币类的产品
        ArrayList<String> needCosumeids = new ArrayList<String>();
        // 此类产品id，购买后不需要消费，适合资格类、等级类、权限类的产品，我们的产品都是属于noNeedCosumeids类型到，即一次购买就永久拥有资格
        ArrayList<String> noNeedCosumeids = new ArrayList<String>();
        noNeedCosumeids.addAll(iapProduct.getAllFeePayProductIds());
        appID = HSConfig.optInteger(APP_ID_FOR_RAINBOW, "Application", "IAP", "AppID");
        HSIAPManager.getInstance().start(needCosumeids, noNeedCosumeids, appID, deviceID);
    }

    public void queryOwnProductIds() {
        iapProduct.queryOwnProductIds();
    }

    public void onConfigChange() {
        iapProduct.onConfigChange();
    }

    /**
     * 判断是否已购买此商品
     *        //备注 2017.04.14 取消iap弹窗 liuyu1需求
     * @param item
     * @return
     */
    public boolean isOwnProduct(KCBaseElement item) {
        return true; //&& iapProduct.isOwnProduct(item);
    }

    public boolean isOwnProduct(String productId) {
        return iapProduct.isOwnProduct(productId);
    }

    public boolean hasPurchaseNoAds() {
        String productId = HSConfig.getString("Application", "RemoveAds", "iapID");
        return isOwnProduct(productId);
    }

    public void purchaseNoAds() {
        String productId = HSConfig.getString("Application", "RemoveAds", "iapID");
        purchaseProduct(productId);
    }

    public String getThemePrice(KCBaseElement item) {
        return iapProduct.getThemePrice(item);
    }

    public String getAllPrice(KCBaseElement item) {
        return iapProduct.getAllPrice(item);
    }

    public String getProductId(KCBaseElement item) {
        return iapProduct.getProductId(item);
    }

    public String getCurrentTypeWholeItemProductId(KCBaseElement item) {
        return iapProduct.getCurrentTypeWholeItemProductId(item);
    }

    public String getUnlimitedSlotsProductId() {
        return iapProduct.getUnlimitedSlotsProductId();
    }

    public String getUnlimitedSlotsProductPrice() {
        return iapProduct.getPurchaseUnlimitedSlotsProductPrice();
    }

    public int getOwnSlotsCount() {
        return iapProduct.getOwnSlotCount();
    }


    public void unlockProductViaWatchVideo(String productId) {
        //添加已购买产品id
        iapProduct.addOwnRewardedProductId(productId);
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_IAP_PURCHASE_SUCCESS);
    }

    public void unlockSlotViaWatchVideo() {
        iapProduct.addOneSlot();
    }


    /**
     * 处理确认成功，即真正意义上的购买成功
     *
     * @param productId
     * @param jsonObject
     */
    public void onVerifySuccessed(String productId, JSONObject jsonObject) {
        //添加已购买产品id
        iapProduct.addOwnIAPProductId(productId);
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_IAP_PURCHASE_SUCCESS);
    }

    /**
     * 处理确认失败，即真正意义上的购买失败
     *
     * @param productId
     * @param errorCode
     */
    public void onVerifyFailed(String productId, int errorCode) {
        Context context = HSApplication.getContext();
        String message = context.getString(R.string.purchase_error);
        switch (errorCode) {
            case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
                message += context.getString(R.string.purchase_error_bill);
                break;
            case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
                message += context.getString(R.string.purchase_error_item);
                break;
            case BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
                message += context.getString(R.string.purchase_error_item_owned);
                break;
            default:
                message += context.getString(R.string.unknown).toUpperCase();
                break;
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public void startCustomThemeActivityIfSlotAvailable(Activity activity, Bundle bundle) {
        if (!hasFreeThemeSlot()) {
            showPurchaseSlotDialog(activity, bundle);
        } else {
            startCustomThemeActivity(bundle);
        }
    }

    public static void startCustomThemeActivity(final Bundle bundle) {
        HSInputMethod.hideWindow();
        String currentAppName = HSInputMethod.getCurrentHostAppPackageName();
        String myPkName = HSApplication.getContext().getPackageName();
        int delay = 0;
        if (myPkName != null && myPkName.equals(currentAppName)) { //延迟100ms，让试用键盘可以有足够时间消失掉
            delay = 100;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent intent = new Intent();
                intent.setClass(HSApplication.getContext(), com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity.class);
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                HSApplication.getContext().startActivity(intent);
            }
        }, delay);
    }

    private void showPurchaseSlotDialog(Activity activity, Bundle bundle) {
        final Intent intent = new Intent();
        intent.setClass(HSApplication.getContext(), PurchaseSlotsActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (activity == null && HSInputMethodService.getInstance() != null) {
            EditorInfo currentInputEditorInfo = HSInputMethodService.getInstance().getCurrentInputEditorInfo();
            if (!((currentInputEditorInfo != null && currentInputEditorInfo.packageName != null && currentInputEditorInfo.packageName.equals(HSApplication.getContext().getPackageName())))) {
                //如果当前键盘在其他应用弹出
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        }
        HSApplication.getContext().startActivity(intent);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_iapalert_unlimitedslots_showed");
    }

    public void purchaseProduct(String productId) {
        if (!TextUtils.isEmpty(productId)) {
            if (HSIAPManager.getInstance().isSupportIAPService()) {
                try {
                    HSIAPManager.getInstance().purchaseIAPProduct(productId);
                } catch (Exception e) {
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_iap_purchase_exception_occur", productId);
                    e.printStackTrace();
                    tipMessage(HSApplication.getContext().getString(R.string.purchase_error_message));
                }
            } else {
                tipMessage(HSApplication.getContext().getString(R.string.purchase_google_play_unavailable));
            }
        }
    }


    private boolean hasFreeThemeSlot() {
        return HSApplication.getContext().getResources().getBoolean(R.bool.config_slots_unlimited)
                || KCCustomThemeManager.getInstance().getAllCustomThemes().size() < 2 + getOwnSlotsCount()
                || IAPManager.getManager().isOwnProduct(IAPManager.getManager().getUnlimitedSlotsProductId());
    }


    private void tipMessage(String message) {
        Toast.makeText(HSApplication.getContext(), message, Toast.LENGTH_LONG).show();
    }
}
