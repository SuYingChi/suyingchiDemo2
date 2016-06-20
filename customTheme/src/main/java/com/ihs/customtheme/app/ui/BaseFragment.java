package com.ihs.customtheme.app.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.customtheme.app.iap.IAPManager;

/**
 * Created by jixiang on 16/5/19.
 */
public abstract class BaseFragment extends Fragment implements INotificationObserver {
    /**
     * IAP购买成功之后会收到通知,需要刷新数据，子类自己重写，
     */
    abstract void notifyDataSetChange();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSGlobalNotificationCenter.addObserver(IAPManager.NOTIFICATION_IAP_PURCHASE_SUCCESS,this);
    }

    @Override
    public void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(IAPManager.NOTIFICATION_IAP_PURCHASE_SUCCESS,this);
        super.onDestroy();
    }

    /**
     * 购买成功的回调，用于刷新数据
     * @param s
     * @param hsBundle
     */
    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        notifyDataSetChange();
    }
}
