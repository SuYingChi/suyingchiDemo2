package com.ihs.inputmethod.uimodules.ui.theme.iap;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.uimodules.R;
import com.keyboard.core.themes.custom.elements.KCBackgroundElement;
import com.keyboard.core.themes.custom.elements.KCBaseElement;
import com.keyboard.core.themes.custom.elements.KCFontElement;
import com.keyboard.core.themes.custom.elements.KCSoundElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jixiang on 16/5/6.
 */
public class IAPProduct {
    private final static String CUSTOM_THEME_UNLIMITED_SLOTS = "custom_theme_unlimited_slots";
    private final static String CUSTOM_THEME_BACKGROUND = "custom_theme_backgrounds";
    private final static String CUSTOM_THEME_BACKGROUND_ALL = "custom_theme_backgrounds_all";
    private final static String CUSTOM_THEME_FONT = "custom_theme_fonts";
    private final static String CUSTOM_THEME_FONT_ALL = "custom_theme_fonts_all";
	private final static String CUSTOM_THEME_SOUND = "custom_theme_click_sounds";
	private final static String CUSTOM_THEME_SOUND_ALL = "custom_theme_click_sounds_all";

    private final static String IAP_ID = "iapID";
    private final static String NAME = "name";
    private final static String PRICE = "price";

    private Set<String> allFeePayProductIds = new HashSet<>();
    private Set<String> ownIAPProductIds = new HashSet<>();
    private Set<String> ownRewardedProductIds = new HashSet<>();

    private String purchaseAllBackgroundProductId = "";
    private String purchaseAllBackgroundProductPrice = "";
    private String purchaseAllFontProductId = "";
    private String purchaseAllFontProductPrice = "";
	private String purchaseAllSoundProductId = "";
    private String purchaseAllSoundProductPrice = "";
    private String purchaseUnlimitedSlotsProductId = "";
    private String purchaseUnlimitedSlotsProductPrice = "";
    private Map<String, Object> themeContentIapConfigurationMap;

    private final static String IAP_PRODUCT_PREFERENCE = "com.keyboard.preference.iap";
    private final static String IAP_OWN_PRODUCT_ID = "ownProductId";
    private final static String REWARDED_OWN_SLOT_COUNT = "rewarded_own_slot_count";
    private final static String REWARDED_OWN_PRODUCT_IDS = "rewarded_own_product_ids";


    public void onConfigChange() {
        init();
    }

    public void init() {
        loadIAPInfo();

        SharedPreferences sp = HSApplication.getContext().getSharedPreferences(IAP_PRODUCT_PREFERENCE, Context.MODE_PRIVATE);
        ownIAPProductIds = sp.getStringSet(IAP_OWN_PRODUCT_ID, new HashSet<String>());
    }

    private void loadIAPInfo() {
        //load all productIds
        allFeePayProductIds.clear();
        themeContentIapConfigurationMap = (Map<String, Object>) HSConfig.getMap("Application", "ThemeContents");
        loadAllProductIds();
        //fetch own rewarded product ids
        SharedPreferences sp = HSApplication.getContext().getSharedPreferences(IAP_PRODUCT_PREFERENCE, Context.MODE_PRIVATE);
        ownRewardedProductIds = sp.getStringSet(REWARDED_OWN_PRODUCT_IDS, new HashSet<String>());
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
	    Map<String, String> SoundThemeAllMap = (Map<String, String>) themeContentIapConfigurationMap.get(CUSTOM_THEME_SOUND_ALL);
	    if (fontThemeAllMap != null) {
		    purchaseAllSoundProductId = SoundThemeAllMap.get(IAP_ID);
		    purchaseAllSoundProductPrice = SoundThemeAllMap.get(PRICE);
		    allFeePayProductIds.add(purchaseAllSoundProductId);
	    }

        Map<String, String> themeSlotMap = (Map<String, String>) themeContentIapConfigurationMap.get(CUSTOM_THEME_UNLIMITED_SLOTS);
        if (themeSlotMap != null) {
            purchaseUnlimitedSlotsProductId = themeSlotMap.get(IAP_ID);
            purchaseUnlimitedSlotsProductPrice = themeSlotMap.get(PRICE);
            allFeePayProductIds.add(purchaseUnlimitedSlotsProductId);
        }


        loadTypeProductIds(CUSTOM_THEME_BACKGROUND);
        loadTypeProductIds(CUSTOM_THEME_FONT);
        loadTypeProductIds(CUSTOM_THEME_SOUND);
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
    public Set<String> getAllFeePayProductIds() {
        return allFeePayProductIds;
    }

    /**
     * 获取默认的价格
     *
     * @return
     */
    public String getThemePrice(KCBaseElement item) {
        String productType = "";
        if(item instanceof KCBackgroundElement) {
            productType = CUSTOM_THEME_BACKGROUND;
        }
        else if(item instanceof KCFontElement) {
            productType = CUSTOM_THEME_FONT;
        }
        else if(item instanceof KCSoundElement) {
            productType=CUSTOM_THEME_SOUND;
        }
        ArrayList<Map<String, String>> typeMap = (ArrayList<Map<String, String>>) themeContentIapConfigurationMap.get(productType);
        if (typeMap != null) {
            for (Map<String, String> map : typeMap) {
                if (item.getName().equals(map.get(NAME))) {
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
    public String getAllPrice(KCBaseElement item) {
        String allThemePriceStr = HSApplication.getContext().getResources().getString(R.string.all_theme_price);
        String price = "";
        String category = "";
        Context context=HSApplication.getContext();
        if(item instanceof KCBackgroundElement) {
            category = context.getString(R.string.custom_theme_backgrounds);
            price = purchaseAllBackgroundProductPrice;
        }
        else if(item instanceof KCFontElement) {
            category = context.getString(R.string.custom_theme_fonts);
            price = purchaseAllFontProductPrice;
        }
        else if(item instanceof KCSoundElement) {
            category=context.getString(R.string.custom_theme_sounds);
            price=purchaseAllSoundProductPrice;
        }

        return String.format(allThemePriceStr, category, price);
    }

    /**
     * 获取ProductId
     *
     * @param item
     * @return
     */
    public String getProductId(KCBaseElement item) {

        String productType = "";
        if(item instanceof KCBackgroundElement) {
            productType = CUSTOM_THEME_BACKGROUND;
        }
        else if(item instanceof KCFontElement){
            productType = CUSTOM_THEME_FONT;
        }
        else if(item instanceof KCSoundElement) {
            productType=CUSTOM_THEME_SOUND;
        }

        ArrayList<Map<String, String>> typeMap = (ArrayList<Map<String, String>>) themeContentIapConfigurationMap.get(productType);
        if (typeMap != null) {
            for (Map<String, String> map : typeMap) {
                if (item.getName().equals(map.get(NAME))) {
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
    public String getCurrentTypeWholeItemProductId(KCBaseElement item) {
        String productId = "";
        if(item instanceof KCBackgroundElement) {
            productId = purchaseAllBackgroundProductId;
        }
        else if(item instanceof KCFontElement) {
            productId = purchaseAllFontProductId;
        }
        else if(item instanceof KCSoundElement) {
            productId=purchaseAllSoundProductId;
        }
        return productId;
    }


    public String getUnlimitedSlotsProductId() {
        return purchaseUnlimitedSlotsProductId;
    }

    public String getPurchaseUnlimitedSlotsProductPrice() {
        return purchaseUnlimitedSlotsProductPrice;
    }
    public int getOwnSlotCount() {
        SharedPreferences sp = HSApplication.getContext().getSharedPreferences(IAP_PRODUCT_PREFERENCE, Context.MODE_PRIVATE);
        return sp.getInt(REWARDED_OWN_SLOT_COUNT, 0);
    }

    /**
     * 查询拥有的商品id集合
     */
    public void queryOwnProductIds() {
        HSIAPManager.getInstance().queryOwnedProducts(new HSIAPManager.HSQueryOwnedProductListener() {
            @Override
            public void onQuerySucceeded(List<String> list) {
                ownIAPProductIds.clear();
                if (list != null && list.size() > 0) {
                    ownIAPProductIds.addAll(list);
                }
                saveOwnIAPProductIds();
            }

            @Override
            public void onQueryFailed(int i) {
            }
        });
    }

    /**
     * 保存已购买商品id到sharedpreference
     */
    private void saveOwnIAPProductIds() {
        SharedPreferences sp = HSApplication.getContext().getSharedPreferences(IAP_PRODUCT_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putStringSet(IAP_OWN_PRODUCT_ID, ownIAPProductIds).apply();
    }

    /**
     * 保存Rewarded Slot数量
     */
    private void saveRewardedSlot() {
        SharedPreferences sp = HSApplication.getContext().getSharedPreferences(IAP_PRODUCT_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt(REWARDED_OWN_SLOT_COUNT, sp.getInt(REWARDED_OWN_SLOT_COUNT, 0) +1).apply();
    }

    /**
     * 保存Rewarded product id 集合
     */
    private void saveRewardedProductIds() {
        SharedPreferences sp = HSApplication.getContext().getSharedPreferences(IAP_PRODUCT_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putStringSet(REWARDED_OWN_PRODUCT_IDS, ownRewardedProductIds).apply();
    }


    /**
     * 添加购买了的商品id
     *
     * @param productId
     */
    public void addOwnIAPProductId(String productId) {
        ownIAPProductIds.add(productId);
        saveOwnIAPProductIds();
    }

    public void addOwnRewardedProductId(String productId) {
        ownRewardedProductIds.add(productId);
        saveRewardedProductIds();
    }

    public void addOneSlot() {
        saveRewardedSlot();
    }

    /**
     * 是否拥有了该商品
     *
     * @param item
     * @return
     */
    public boolean isOwnProduct(KCBaseElement item) {
	    String productId = "";
        if(item instanceof KCBackgroundElement) {
            if (ownIAPProductIds.contains(purchaseAllBackgroundProductId)) {
                return true;
            }
        }
        else if(item instanceof KCFontElement){
            if (ownIAPProductIds.contains(purchaseAllFontProductId)) {
                return true;
            }
        }
        else if(item instanceof KCSoundElement) {
            if (ownIAPProductIds.contains(purchaseAllSoundProductId)) {
                return true;
            }
        }

	    productId = getProductId(item);
	    return TextUtils.isEmpty(productId) || ownIAPProductIds.contains(productId) || ownRewardedProductIds.contains(productId);
    }

    public boolean isOwnProduct(String productId) {
        return ownIAPProductIds.contains(productId) || ownRewardedProductIds.contains(productId);
    }

}
