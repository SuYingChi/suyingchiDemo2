package com.keyboard.inputmethod.panels.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

/**
 * Created by dsapphire on 16/3/21.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
	public final static String HS_NOTIFICATION_NETWORK_AVAILABLE ="hs.keyboard.network_Available";
	@Override
	public void onReceive(final Context context, final Intent intent) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifi.isAvailable() || mobile.isAvailable()) {
			HSGlobalNotificationCenter.sendNotification(HS_NOTIFICATION_NETWORK_AVAILABLE);
		}
	}
}