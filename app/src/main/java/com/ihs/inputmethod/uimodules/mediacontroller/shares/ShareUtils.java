package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSMapUtils;
import com.ihs.inputmethod.api.utils.HSPictureUtils;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by ihandysoft on 16/6/2.
 */
public class ShareUtils {

    // image share method
    public static final int IMAGE_SHARE_MODE_EXPORT = 0;
    public static final int IMAGE_SHARE_MODE_INTENT = 1;
    public static final int IMAGE_SHARE_MODE_LINK   = 2;

    public static final String IMAGE_SHARE_FORMAT_GIF = "gif";
    public static final String IMAGE_SHARE_FORMAT_LINK = "link";

    public static final String[] SUPPORTTED_PACKAGE_NAMES = {ShareChannel.MESSAGE.getPackageName()};

    /**
     * update gallery
     *
     * @param filePath
     */
    public static void updateGallery(final String filePath) {
        HSToastUtils.toastCenterLong("The picture was saved to your gallery.");
        // Update gallery
        MediaScannerConnection.scanFile(HSApplication.getContext(), new String[]{filePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.i("ExternalStorage", "Scanned " + path + ":");
                Log.i("ExternalStorage", "-> uri=" + uri);
            }
        });
    }

    /**
     * @param packageName
     * @return Pair<Integer, String> key is share mode, value is format
     */
    public static Pair<Integer, String> refreshCurrentShareMode(final String packageName) {
        Pair<Integer, String> shareMode = getConfigShareMode(packageName);
        if (shareMode != null) {
            return shareMode;
        }
        return new Pair(HSPictureUtils.IMAGE_SHARE_MODE_INTENT, IMAGE_SHARE_FORMAT_GIF);
    }

    /**
     * get share mode by package name
     *
     * @param packageName
     * @return
     */
    public static Pair<Integer, String> getSequenceFramesImageShareMode(final String packageName) {
        Pair<Integer, String> shareMode = getConfigShareMode(packageName);
        if (shareMode != null) {
            return shareMode;
        }
        // default supported apps
        if (isSupportedPackageName(packageName)) {
            return new Pair(HSPictureUtils.IMAGE_SHARE_MODE_INTENT, IMAGE_SHARE_FORMAT_GIF);
        }
        return new Pair(HSPictureUtils.IMAGE_SHARE_MODE_EXPORT, "");
    }

    private static Pair<Integer, String> getConfigShareMode(final String packageName) {
        final List<?> smList = HSConfig.getList(HSPictureUtils.RMTCFG_KEY_L1_APPLICATION, HSPictureUtils.RMTCFG_KEY_L2_SEND_STRATEGY);
        for (Object o : smList) {
            final String supportPackageName = HSMapUtils.getString((Map<String, ?>) o, HSPictureUtils.RMTCFG_KEY_L4_PACKAGE_NAME);
            if (supportPackageName.equals(packageName)) {
                final Integer mode = HSMapUtils.getInteger((Map<String, ?>) o, HSPictureUtils.RMTCFG_KEY_L4_SEND_MODE);
                final String format = HSMapUtils.getString((Map<String, ?>) o, HSPictureUtils.RMTCFG_KEY_L4_SOURCE_FORMAT);
                return new Pair(mode, format);
            }
        }
        return null;
    }

    public static Pair<Integer, String> getStickerShareMode(final String packageName) {
        final List<?> smList = HSConfig.getList(HSPictureUtils.RMTCFG_KEY_L1_APPLICATION, HSPictureUtils.RMTCFG_KEY_L2_SEND_STRATEGY);
        for (Object o : smList) {
            final String supportPackageName = HSMapUtils.getString((Map<String, ?>) o, HSPictureUtils.RMTCFG_KEY_L4_PACKAGE_NAME);
            if (supportPackageName.equals(packageName)) {
                final Integer mode = HSMapUtils.getInteger((Map<String, ?>) o, HSPictureUtils.RMTCFG_KEY_L4_SEND_MODE);
                final String format = HSMapUtils.getString((Map<String, ?>) o, HSPictureUtils.RMTCFG_KEY_L4_SOURCE_FORMAT);
                return new Pair(mode, format);
            }
        }

        // default supported apps
        final HashSet<String> supportedPackageNames = new HashSet<>(Arrays.asList(SUPPORTTED_PACKAGE_NAMES));
        if (supportedPackageNames.contains(packageName)) {
            return new Pair(IMAGE_SHARE_MODE_INTENT, "");
        }

        return new Pair(IMAGE_SHARE_MODE_EXPORT, "");
    }

    private static boolean isSupportedPackageName(final String packageName) {
        // default supported apps
        final List<String> supportedPackageNames = Arrays.asList(SUPPORTTED_PACKAGE_NAMES);
        if (supportedPackageNames.contains(packageName)) {
            return true;
        }

        return false;
    }

    public static String[] getAvailablePackages(ShareChannel channel) {
        String[] availablePackages = null;
        switch (channel) {
            case MESSAGE:
                availablePackages = getMmsPackages();
                break;
            case EMAIL:
                availablePackages = getEmailPackages();
                break;
            default:
                break;
        }
        return availablePackages;
    }

    /**
     * get available mms packages
     *
     * @return
     */
    private static String[] getMmsPackages() {
        List<String> packages = new ArrayList<>();
        PackageManager packageManager = HSApplication.getContext().getPackageManager();

        // Get the list of apps registered for SMS
        Intent intent = new Intent(Telephony.Sms.Intents.SMS_DELIVER_ACTION);
        List<ResolveInfo> smsReceivers = packageManager.queryBroadcastReceivers(intent, 0);

        // Add one entry to the map for every sms receiver (ignoring duplicate sms receivers)
        for (ResolveInfo resolveInfo : smsReceivers) {
            final ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo == null) {
                continue;
            }
            if (!Manifest.permission.BROADCAST_SMS.equals(activityInfo.permission)) {
                continue;
            }
            if (!packages.contains(activityInfo.packageName)) {
                packages.add(activityInfo.packageName);
            }
        }
        String[] result = new String[packages.size()];
        packages.toArray(result);
        return result;
    }

    /**
     * get availabe email packages
     *
     * @return
     */
    private static String[] getEmailPackages() {
        List<String> packages = new ArrayList<>();

        Uri uri = Uri.parse("mailto:" + "test@qq.com");
        Intent shareIntent = new Intent(Intent.ACTION_SENDTO, uri);

        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfos) {
            final ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (!packages.contains(activityInfo.packageName)) {
                packages.add(activityInfo.packageName);
            }
        }
        String[] result = new String[packages.size()];
        packages.toArray(result);
        return result;
    }


    public static boolean isIntentAvailable(Intent intent) {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfos == null ? false : resolveInfos.size() > 0;
    }


    public static String getMimeType(String fileFormat){
        String mimeType;

        if (Constants.MEDIA_FORMAT_MP4.equals(fileFormat)) {
            mimeType = Constants.MIME_MP4;
        } else {
            mimeType = Constants.MIME_IMAGE;
        }

        return mimeType;
    }

    /**
     * 分享
     */
    public static void shareMedia(ShareChannel shareChannel, Uri shareFileUri) {
        String filePath = shareFileUri.getPath();
        String mimeType = getMimeType(filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length()));

        if(mimeType == null || "".equals(mimeType)){
            return;
        }

        if (shareChannel == ShareChannel.MORE){
            Intent shareIntent = new Intent();
            shareIntent.setAction("android.intent.action.SEND");
            shareIntent.putExtra("android.intent.extra.STREAM", shareFileUri);
            shareIntent.setType("image/*");
            Intent chooser = Intent.createChooser(shareIntent, HSApplication.getContext().getResources().getText(R.string.label_share));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            HSApplication.getContext().startActivity(chooser);
            return;
        }

        // 可选的支持的应用包名，如果默认的包名对应的应用不能打开，则尝试可选的包名对应的应用打开
        String[] optionPackageName = getAvailablePackages(shareChannel);
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setPackage(shareChannel.getPackageName());
        shareIntent.putExtra(Intent.EXTRA_STREAM, shareFileUri);
        try {
            shareIntent.setType(mimeType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(ShareUtils.isIntentAvailable(shareIntent)) {
            HSApplication.getContext().startActivity(shareIntent);
            return;
        }else if(optionPackageName!=null && optionPackageName.length >0){
            for(String pkg : optionPackageName){
                shareIntent.setPackage(pkg);
                if(ShareUtils.isIntentAvailable(shareIntent)) {
                    HSApplication.getContext().startActivity(shareIntent);
                    return;
                }
            }
        }
        Toast.makeText(HSApplication.getContext(), "sorry,can not find appropriate share tools", Toast.LENGTH_SHORT).show();
    }
}
