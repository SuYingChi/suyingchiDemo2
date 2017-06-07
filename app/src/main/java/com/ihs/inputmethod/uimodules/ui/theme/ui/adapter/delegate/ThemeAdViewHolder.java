package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeFragment;
import com.ihs.keyboardutils.nativeads.NativeAdView;


/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeAdViewHolder extends RecyclerView.ViewHolder {

    private NativeAdView adView;

    private final INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (ThemeHomeFragment.NOTIFICATION_THEME_HOME_DESTROY.equals(s)) {
                if (adView != null) {
                    adView.release();
                    adView = null;
                }
                HSGlobalNotificationCenter.removeObserver(notificationObserver);
            }
        }
    };

    public ThemeAdViewHolder(View itemView) {
        super(itemView);
        adView = (NativeAdView) ((CardView) itemView).getChildAt(0);
        HSGlobalNotificationCenter.addObserver(ThemeHomeFragment.NOTIFICATION_THEME_HOME_DESTROY, notificationObserver);
    }
}
