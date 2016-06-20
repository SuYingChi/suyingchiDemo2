package com.ihs.customtheme.app.iap;

import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.theme.HSCustomThemeItemBase;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jixiang on 16/5/5.
 */
public class IAPManager {
    private int appID = 603; //找IAP服务器人员获取和pkg对应到id
    private String deviceID = "";//唯一标示一台机器到key，如果需要关心哪些设备购买了产品，则需要设置
    private final IAPProduct productManager;
    public final static String NOTIFICATION_IAP_PURCHASE_SUCCESS = "keyboard_iap_purchase_success";

    private static IAPManager instance;
    private IAPManager() {
        productManager = new IAPProduct();
        deviceID = new DeviceUuidFactory(HSApplication.getContext()).getDeviceUuid().toString();
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

    // 初始化 LibAsset，且会自动 restore。restore:已经在谷歌付费了并获取到发票了，但是服务器将该发票拿去谷歌确认时没成功，这时restore到工作就是发票再确认到过程
    public void init() {
        long start = System.currentTimeMillis();

        productManager.init();
        HSIAPManager.getInstance().enableLog();
        // 此类产品id，购买后需要消费才能再次购买，适合 金币类的产品
        ArrayList<String> needCosumeids = new ArrayList<String>();
        // 此类产品id，购买后不需要消费，适合资格类、等级类、权限类的产品，我们的产品都是属于noNeedCosumeids类型到，即一次购买就永久拥有资格
        ArrayList<String> noNeedCosumeids = new ArrayList<String>();
        noNeedCosumeids.addAll(productManager.getAllFeePayProductIds());
        HSIAPManager.getInstance().start(needCosumeids, noNeedCosumeids, appID, deviceID);

        long cast = System.currentTimeMillis() - start;
        HSLog.d("AAAAA init use time :" + cast);
    }

    public void queryOwnProductIds(IAPManager.IIAPQueryOwnProductListener listener){
        productManager.queryOwnProductIds(listener);
    }

    /**
     * 判断是否已购买此商品
     * @param item
     * @return
     */
    public boolean isOwnProduct(HSCustomThemeItemBase item){
        return productManager.isOwnProduct(item);
    }

    public String getThemePrice(HSCustomThemeItemBase item){
        return productManager.getThemePrice(item);
    }

    public String getAllPrice(HSCustomThemeItemBase item){
        return productManager.getAllPrice(item);
    }

    public String getProductId(HSCustomThemeItemBase item){
        return productManager.getProductId(item);
    }

    public String getCurrentTypeWholeItemProductId(HSCustomThemeItemBase item){
        return productManager.getCurrentTypeWholeItemProductId(item);
    }

    private final void addNewPurchaseProductId(String id){
        productManager.addOwnProductId(id);
    }

    /**
     * IAP errorCode
     */
    // Google error codes
    public static final int BILLING_RESPONSE_RESULT_OK = 0;
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    // IAB Helper error codes
    public static final int IAP_ERROR_BASE = -1000;
    public static final int IAP_REMOTE_EXCEPTION = -1001;
    public static final int IAP_BAD_RESPONSE = -1002;
    public static final int IAP_VERIFICATION_FAILED = -1003;
    public static final int IAP_SEND_INTENT_FAILED = -1004;
    public static final int IAP_USER_CANCELLED = -1005;
    public static final int IAP_UNKNOWN_PURCHASE_RESPONSE = -1006;
    public static final int IAP_MISSING_TOKEN = -1007;
    public static final int IAP_UNKNOWN_ERROR = -1008;
    public static final int IAP_SUBSCRIPTIONS_NOT_AVAILABLE = -1009;
    public static final int IAP_INVALID_CONSUMPTION = -1010;
    public static final int IAP_NO_PRODUCT = -1011;
    public static final int IAP_NO_SETUP = -1012;
    public static final int IAP_PURCHASING = -1013;
    public static final int IAP_NO_GOOGLEPLAY_SERVICE = -1014;
    public static final int IAP_PURCHASE_PARAMTER_ERROR = 400;

    // IAP server error codes
    public static final int IAPVERIFY_UNKNOWN_ERROR = 100;
    public static final int IAPVERIFY_NEED_RETRY = 201;
    public static final int IAPVERIFY_PARAMTER_ERROR = 400;
    public static final int IAPVERIFY_RECEIPT_INVALID = 401;
    public static final int IAPVERIFY_OLD_RECEIPT = 402;
    public static final int IAPVERIFY_SERVER_ERROR = 500;
    public static final int IAPVERIFY_CONNECTION_ERROR = 1000;
    /** IAP errorCode*/


    /**
     * 处理确认成功，即真正意义上的购买成功
     * @param productid
     * @param jsonObject
     */
    public void onVerifySuccessed(String productid, JSONObject jsonObject){
        //添加已购买产品id
        addNewPurchaseProductId(productid);
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_IAP_PURCHASE_SUCCESS);
    }

    /**
     * 处理确认失败，即真正意义上的购买失败
     * @param productId
     * @param errorCode
     */
    public void onVerifyFailed(String productId, int errorCode) {
        String message = "ERROR:";
        switch (errorCode) {
            case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
                message += "BILLING_UNAVAILABLE";
                break;
            case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
                message += "ITEM_UNAVAILABLE";
                break;
            case BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
                message += "ITEM_ALREADY_OWNED";
                break;
            default:
                message += "UNKNOWN";
                break;
        }

        Toast.makeText(HSApplication.getContext(), message, Toast.LENGTH_LONG).show();
    }


    public interface IIAPQueryOwnProductListener{
        void onQueryFinish();
    }

    public interface IIAPPurchaseListener{
        void onPurchaseSuccess();
        void onPurchaseFailded();
    }
}
