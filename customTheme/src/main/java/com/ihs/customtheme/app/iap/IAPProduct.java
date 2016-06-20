package com.ihs.customtheme.app.iap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.theme.HSCustomThemeItemBase;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jixiang on 16/5/6.
 */
public class IAPProduct {
    private final static String THEME_CONTENT = "themeContents";
    private final static String CUSTOM_THEME_BACKGROUND = "custom_theme_backgrounds";
    private final static String CUSTOM_THEME_BACKGROUND_ALL = "custom_theme_backgrounds_all";
    private final static String CUSTOM_THEME_FONT = "custom_theme_fonts";
    private final static String CUSTOM_THEME_FONT_ALL = "custom_theme_fonts_all";

    private final static String IAP_ID = "iapID";
    private final static String NAME = "name";
    private final static String PRICE = "price";

    private List<String> allFeePayProductIds = new ArrayList<>();
    private Set<String> ownProductIds = new HashSet<>();

    private String purchaseAllBackgroundProductId = "";
    private String purchaseAllBackgroundProductPrice = "";
    private String purchaseAllFontProductId = "";
    private String purchaseAllFontProductPrice = "";
    private Map<String, Object> themeContentIapConfigurationMap;

    private final static String IAP_PRODUCT_PREFERENCE = "com.keyboard.preference.iap";
    private final static String IAP_OWN_PRODUCT_ID = "ownProductId";
    public void init() {
        loadIAPInfo();
    }

    private void loadIAPInfo() {
        long start = System.currentTimeMillis();
        AssetManager am = HSApplication.getContext().getAssets();
        InputStream customThemeIAPConfigInputStream = null;
        try {
            customThemeIAPConfigInputStream = am.open("custom_theme/theme_iap.yaml");
        } catch (IOException e) {
            return;
        }

        if (customThemeIAPConfigInputStream == null) {
            return;
        }

        Yaml yaml = new Yaml();
        Map<String, Object> themeIapConfigurationMap = (Map) yaml.load(customThemeIAPConfigInputStream);
        themeContentIapConfigurationMap = (Map<String, Object>) themeIapConfigurationMap.get(THEME_CONTENT);

        //load all productIds
        allFeePayProductIds.clear();
        loadAllProductIds();
        HSLog.d("AAAAA allFeePayProductIds:" + allFeePayProductIds);

        long cast = System.currentTimeMillis() - start;
        HSLog.d("AAAAA init use time :" + cast);
    }

    private void loadAllProductIds() {
        Map<String, String> backgroundThemeAllMap = (Map<String, String>) themeContentIapConfigurationMap.get(CUSTOM_THEME_BACKGROUND_ALL);
        if (backgroundThemeAllMap != null) {
            purchaseAllBackgroundProductId = backgroundThemeAllMap.get(IAP_ID);
            purchaseAllBackgroundProductPrice = backgroundThemeAllMap.get(PRICE);
            allFeePayProductIds.add(purchaseAllBackgroundProductId);
        }

        Map<String, String> fontThemeAllMap = (Map<String, String>) themeContentIapConfigurationMap.get(CUSTOM_THEME_FONT_ALL);
        if (fontThemeAllMap != null) {
            purchaseAllFontProductId = fontThemeAllMap.get(IAP_ID);
            purchaseAllFontProductPrice = fontThemeAllMap.get(PRICE);
            allFeePayProductIds.add(purchaseAllFontProductId);
        }

        loadTypeProductIds(CUSTOM_THEME_BACKGROUND);
        loadTypeProductIds(CUSTOM_THEME_FONT);
    }

    private void loadTypeProductIds(String type) {
        ArrayList<Map<String, String>> typeMap = (ArrayList<Map<String, String>>) themeContentIapConfigurationMap.get(type);
        if (typeMap != null) {
            for (Map<String, String> map : typeMap) {
                if (map.containsKey(IAP_ID)) {
                    allFeePayProductIds.add(map.get(IAP_ID));
                }
            }
        }
    }

    /**
     * 查询所有付费的产品id
     *
     * @return
     */
    public List<String> getAllFeePayProductIds() {
        return allFeePayProductIds;
    }

    /**
     * 获取默认的价格
     *
     * @return
     */
    public String getThemePrice(HSCustomThemeItemBase item) {
        String productType = "";
        switch (item.getItemType()) {
            case BACKGROUND:
                productType = CUSTOM_THEME_BACKGROUND;
                break;
            case FONT:
                productType = CUSTOM_THEME_FONT;
                break;
        }
        ArrayList<Map<String, String>> typeMap = (ArrayList<Map<String, String>>) themeContentIapConfigurationMap.get(productType);
        if (typeMap != null) {
            for (Map<String, String> map : typeMap) {
                if (item.getItemName().equals(map.get(NAME))) {
                    return map.get(PRICE);
                }
            }
        }
        return "";
    }

    /**
     * 获取每种产品所有的价格
     *
     * @return
     */
    public String getAllPrice(HSCustomThemeItemBase item) {
        switch (item.getItemType()) {
            case BACKGROUND:
                return purchaseAllBackgroundProductPrice;
            case FONT:
                return purchaseAllFontProductPrice;
        }
        return "";
    }

    /**
     * 获取ProductId
     *
     * @param item
     * @return
     */
    public String getProductId(HSCustomThemeItemBase item) {
        String productType = "";
        switch (item.getItemType()) {
            case BACKGROUND:
                productType = CUSTOM_THEME_BACKGROUND;
                break;
            case FONT:
                productType = CUSTOM_THEME_FONT;
                break;
        }
        ArrayList<Map<String, String>> typeMap = (ArrayList<Map<String, String>>) themeContentIapConfigurationMap.get(productType);
        if (typeMap != null) {
            for (Map<String, String> map : typeMap) {
                if (item.getItemName().equals(map.get(NAME))) {
                    return map.get(IAP_ID);
                }
            }
        }
        return "";
    }

    /**
     * 获取ProductId
     *
     * @param item
     * @return
     */
    public String getCurrentTypeWholeItemProductId(HSCustomThemeItemBase item) {
        String productId = "";
        switch (item.getItemType()) {
            case BACKGROUND:
                productId = purchaseAllBackgroundProductId;
                break;
            case FONT:
                productId = purchaseAllFontProductId;
                break;
        }
        return productId;
    }

    /**
     * 查询拥有的商品id集合
     */
    public void queryOwnProductIds(final IAPManager.IIAPQueryOwnProductListener iapQueryOwnProductListener) {
        HSIAPManager.getInstance().queryOwnedProducts(new HSIAPManager.HSQueryOwnedProductListener() {
            @Override
            public void onQuerySucceeded(List<String> list) {
                ownProductIds.clear();
                HSLog.d("AAAAA 查询到购买成功了产品有:" + list);
                Log.d("jx","AAAAA 查询到购买成功了产品有:" + list);
                if (list != null && list.size() > 0) {
                    ownProductIds.addAll(list);
                }
                saveOwnProductIds();
                if(iapQueryOwnProductListener!=null){
                    iapQueryOwnProductListener.onQueryFinish();
                }
            }

            @Override
            public void onQueryFailed(int i) {
                HSLog.d("AAAAA 查询已购买商品失败");
                Log.d("jx","AAAAA 查询已购买商品失败");
                if(ownProductIds.size() == 0){ //查询服务器数据失败，并且ownProductIds.size == 0,则获取本地数据
                    SharedPreferences sp = HSApplication.getContext().getSharedPreferences(IAP_PRODUCT_PREFERENCE, Context.MODE_PRIVATE);
                    Set<String> stringSet = sp.getStringSet(IAP_OWN_PRODUCT_ID,new HashSet<String>());
                    ownProductIds.addAll(stringSet);
                }
                if(iapQueryOwnProductListener!=null){
                    iapQueryOwnProductListener.onQueryFinish();
                }
            }
        });
    }

    /**
     * 保存已购买商品id到sharedpreference
     */
    private void saveOwnProductIds() {
        SharedPreferences sp = HSApplication.getContext().getSharedPreferences(IAP_PRODUCT_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putStringSet(IAP_OWN_PRODUCT_ID,ownProductIds);
        edit.commit();
    }


    /**
     * 添加购买了的商品id
     * @param productId
     */
    public void addOwnProductId(String productId){
        ownProductIds.add(productId);
        saveOwnProductIds();
    }

    /**
     * 是否拥有了该商品
     *
     * @param item
     * @return
     */
    public boolean isOwnProduct(HSCustomThemeItemBase item) {
        String productId = "";
        switch (item.getItemType()) {
            case BACKGROUND:
                if (ownProductIds.contains(purchaseAllBackgroundProductId)) {
                    return true;
                }
                break;
            case FONT:
                if (ownProductIds.contains(purchaseAllFontProductId)) {
                    return true;
                }
                break;
        }
        productId = getProductId(item);
        if (TextUtils.isEmpty(productId)) {
            return true;
        }
        return ownProductIds.contains(productId);
    }

}
