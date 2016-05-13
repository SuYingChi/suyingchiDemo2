package com.keyboard.inputmethod.panels.gif.utils;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Pair;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSMapUtils;
import com.ihs.inputmethod.base.utils.HSToastUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class ShareUtils {

    // image share method
    public static final int IMAGE_SHARE_MODE_EXPORT = 0;
    public static final int IMAGE_SHARE_MODE_INTENT = 1;
    public static final int IMAGE_SHARE_MODE_LINK   = 2;

    public static final String IMAGE_SHARE_FORMAT_MP4 = "mp4";
    public static final String IMAGE_SHARE_FORMAT_GIF = "gif";
    public static final String IMAGE_SHARE_FORMAT_LINK = "link";

    public static final String MIME_MP4 = "video/mpeg";

    // remote config path
    private static final String RMTCFG_KEY_L1_APPLICATION = "Application";
    private static final String RMTCFG_KEY_L2_SEND_STRATEGY = "SendStrategy";
    private static final String RMTCFG_KEY_L4_PACKAGE_NAME = "PackageName";
    private static final String RMTCFG_KEY_L4_SEND_MODE = "SendMode";
    private static final String RMTCFG_KEY_L4_SOURCE_FORMAT = "SourceFormat";

    private static final String PACKAGE_NAME_ANDROID_MMS = "com.android.mms";
    private static final String[] SUPPORTTED_PACKAGE_NAMES = { PACKAGE_NAME_ANDROID_MMS };


    private static final Pair<Integer, String> SHARE_MODE_DEFAULT = new Pair<>(IMAGE_SHARE_MODE_LINK, IMAGE_SHARE_FORMAT_LINK);

    public static Pair<Integer, String> refreshCurrentShareMode(final String packageName) {
        final List<?> smList = HSConfig.getList(RMTCFG_KEY_L1_APPLICATION, RMTCFG_KEY_L2_SEND_STRATEGY);
        for (Object o : smList) {
            final String supportPackageName = HSMapUtils.getString((Map<String, ?>) o, RMTCFG_KEY_L4_PACKAGE_NAME);
            if (supportPackageName.equals(packageName)) {
                final Integer mode = HSMapUtils.getInteger((Map<String, ?>) o, RMTCFG_KEY_L4_SEND_MODE);
                final String format = HSMapUtils.getString((Map<String, ?>) o, RMTCFG_KEY_L4_SOURCE_FORMAT);
                return new Pair<>(mode, format);
            }
        }
        final HashSet<String> supportedPackageNames = new HashSet<>(Arrays.asList(SUPPORTTED_PACKAGE_NAMES));
        if (supportedPackageNames.contains(packageName)) {
            return new Pair<>(IMAGE_SHARE_MODE_INTENT, IMAGE_SHARE_FORMAT_GIF);
        }

        return SHARE_MODE_DEFAULT;
    }

    public static void shareImageByIntent(final Uri uri, final String mimeType, final String packageName) throws Exception {
        try{
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setPackage(packageName);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType(mimeType);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            HSApplication.getContext().startActivity(shareIntent);
        }catch (Exception e){
            throw new Exception("Can't share by intent");
        }
    }

    public static void shareImageByExport(final String filePath) {
        HSToastUtils.toastCenterLong("The picture was saved to your gallery.");
        MediaScannerConnection.scanFile(HSApplication.getContext(), new String[]{filePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {

            }
        });
    }

}
