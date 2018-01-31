package com.ihs.iap;

import android.os.Handler;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by Arthur on 2018/1/31.
 */

public class HSIAPManager {
    public static HSIAPManager getInstance() {
        return new HSIAPManager();
    }

    public void init(Object o, List<String> inAppNonConsumableSkuList) {

    }

    public boolean hasOwnedSku(String sku) {
        return this.hasOwnedSku(sku, (HSIAPManager.HSOwnedStateChangedListener)null);
    }


    public boolean hasOwnedSku(String sku, HSIAPManager.HSOwnedStateChangedListener listener) {
        return this.hasOwnedSku(sku, listener, (Handler)null);
    }

    public boolean hasOwnedSku(final String sku, final HSIAPManager.HSOwnedStateChangedListener listener, @Nullable Handler handler) {
        return false;
    }

    public interface HSOwnedStateChangedListener {
        void ownedStateChanged(String var1, boolean var2);
    }

    public interface HSQueryInventoryListener {
        void onQuerySucceeded(Object var1);

        void onQueryFailed(int var1, String var2);
    }

    public interface HSIAPStateListener {
        void onNewIAPState(boolean var1, boolean var2, boolean var3, boolean var4);
    }
}
