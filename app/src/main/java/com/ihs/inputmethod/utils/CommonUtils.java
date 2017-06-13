package com.ihs.inputmethod.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.ihs.app.framework.HSApplication;

import java.security.MessageDigest;

public class CommonUtils {

    public static String getMD5(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(content.getBytes());

            StringBuilder builder = new StringBuilder();

            for (byte b : digest.digest()) {
                builder.append(Integer.toHexString(b >> 4 & 0xf));
                builder.append(Integer.toHexString(b & 0xf));
            }

            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Check if network of given type is currently available.
     *
     * @param type one of {@link ConnectivityManager#TYPE_MOBILE}, {@link ConnectivityManager#TYPE_WIFI},
     *             {@link ConnectivityManager#TYPE_WIMAX}, {@link ConnectivityManager#TYPE_ETHERNET},
     *             {@link ConnectivityManager#TYPE_BLUETOOTH}, or other types defined by {@link ConnectivityManager}.
     *             Pass -1 for ANY type
     */
    public static boolean isNetworkAvailable(final int type) {
        ConnectivityManager cm = (ConnectivityManager) HSApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            return false;
        }

        NetworkInfo[] networkInfos = cm.getAllNetworkInfo();

        if (networkInfos != null) {
            for (NetworkInfo networkInfo : networkInfos) {
                if (networkInfo.getState() != null && isTypeMatchAndConnected(networkInfo, type)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isTypeMatchAndConnected(@NonNull NetworkInfo networkInfo, int type) {
        return (type == -1 || networkInfo.getType() == type) && networkInfo.isConnected();
    }
}
