package com.ihs.booster.utils;

import android.os.SystemClock;
import android.util.Base64;

import com.ihs.commons.utils.HSLog;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CommonUtils {

    private static volatile long lastClickTime;

    public static String getAESEncrypt(String sSrc, String sKey) {
        if (sKey == null || sKey.length() != 16) {
            return null;
        }
        try {
            byte[] raw = sKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");// "算法"
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes());
            String encrypt = Base64.encodeToString(encrypted, Base64.DEFAULT);
            return encrypt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 通过设定时间间隔来避免某些按钮的重复点击
    public static boolean isFastDoubleClick() {
        long time = SystemClock.elapsedRealtime();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static void debugAssert(String assertDesc) {
        if (HSLog.isDebugging()) {
            throw new AssertionError(assertDesc);
        }
    }

    public static void debugAssert(boolean assertValue, String assertDesc) {
        if (!assertValue && HSLog.isDebugging()) {
            throw new AssertionError(assertDesc);
        }
    }

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
}
