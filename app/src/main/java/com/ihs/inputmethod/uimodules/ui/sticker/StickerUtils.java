package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.ClipDescription;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareUtils;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.DirectoryUtils;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.MediaShareUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Created by yanxia on 2017/6/6.
 */

public class StickerUtils {

    public static final String ASSETS_STICKER_FILE_PATH = "Stickers";
    public static final String STICKER_TAB_IMAGE_SUFFIX = "-tab.png";
    public static final String STICKER_DOWNLOAD_ZIP_SUFFIX = ".zip";
    public static final String STICKER_IMAGE_PNG_SUFFIX = ".png";

    private static Map<String, String> map = new HashMap<>();
    private static final float STICKER_BACKGROUND_ASPECT_RATIO = 1.7f;
    private static final float STICKER_ZOOM_RATIO = 0.7f;
    private static final String ERROR_COLOR = "error_color";

    public static String getStickerRootFolderPath() {
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_STICKER_FILE_PATH;
    }

    public static String getStickerFolderPath(String stickerGroupName) {
        return getStickerRootFolderPath() + File.separator + stickerGroupName;
    }

    public static String getStickerDownloadBaseUrl() {
        return HSConfig.getString("Application", "Server", "StickerDownloadBaseURL");
    }

    private static Map<String, List<String>> cachedDirectoryContents = new HashMap<>();

    private static boolean isFileInAssets(String directory, String file) {
        List<String> contents = new ArrayList<>();
        if (cachedDirectoryContents.containsKey(directory)) {
            contents = cachedDirectoryContents.get(directory);
        } else {
            try {
                contents = Arrays.asList(HSApplication.getContext().getAssets().list(directory));
            } catch (IOException e) {
                e.printStackTrace();
            }
            cachedDirectoryContents.put(directory, contents);
        }

        return contents.contains(file);
    }

    private static Map<String, StickerGroup> cachedStickerGroupContents = new HashMap<>();

    static StickerGroup getStickerGroupByName(String stickerGroupName) {
        StickerGroup stickerGroupTemp = null;
        if (cachedStickerGroupContents.containsKey(stickerGroupName)) {
            stickerGroupTemp = cachedStickerGroupContents.get(stickerGroupName);
        } else {
            for (StickerGroup stickerGroup : StickerDataManager.getInstance().getStickerGroupList()) {
                if (stickerGroup.getStickerGroupName().equals(stickerGroupName)) {
                    stickerGroupTemp = stickerGroup;
                    cachedStickerGroupContents.put(stickerGroupName, stickerGroup);
                }
            }
        }
        return stickerGroupTemp;
    }

    public static void share(final Sticker sticker, final String packageName) {
        if (!DirectoryUtils.isSDCardEnabled()) {
            HSInputMethod.inputText(sticker.getStickerRemoteUri());
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_sticker_share_mode", "link");
            return;
        }

        final String targetExternalFilePath = DirectoryUtils.getImageExportFolder() + "/" + sticker.getStickerName() + STICKER_IMAGE_PNG_SUFFIX;
        final String mimeType = "image/*";

        final Map<String, Object> shareModeMap = MediaShareUtils.getShareModeMap(packageName);
        final boolean canSendDirectly = (Boolean) shareModeMap.get(MediaShareUtils.IMAGE_SHARE_MODE_MAP_KEY_SEND_DIRECTLY);

        // 可以直接发送到对话列表中
        if (canSendDirectly) {
            String[] mimeTypes = EditorInfoCompat.getContentMimeTypes(HSInputMethodService.getInstance().getCurrentInputEditorInfo());
            boolean pngSupported = false;
            for (String mime_Type : mimeTypes) {
                if (ClipDescription.compareMimeTypes(mime_Type, "image/png")) {
                    pngSupported = true;
                }
            }
            if (pngSupported) {
                copyStickerFileToSDCard(sticker, targetExternalFilePath);
                addDifferentBackgroundForSticker(targetExternalFilePath, packageName, targetExternalFilePath);
                File uri = new File(targetExternalFilePath);
                commitPNGImage(Uri.fromFile(uri), "");
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_sticker_share_mode", "direct_send_png");
                return;
            }
        }

        final int mode = (int) shareModeMap.get(MediaShareUtils.IMAGE_SHARE_MODE_MAP_KEY_MODE);

        switch (mode) {
            // image
            case MediaShareUtils.IMAGE_SHARE_MODE_INTENT:
                copyStickerFileToSDCard(sticker, targetExternalFilePath);
                addDifferentBackgroundForSticker(targetExternalFilePath, packageName, targetExternalFilePath);
                try {
                    MediaShareUtils.shareImageByIntent(Uri.fromFile(new File(targetExternalFilePath)), mimeType, packageName);
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_sticker_share_mode", "intent");
                } catch (Exception e) {
                    HSInputMethod.inputText(sticker.getStickerRemoteUri());
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_sticker_share_mode", "link");
                }
                break;
            // save to gallery
            case ShareUtils.IMAGE_SHARE_MODE_EXPORT:
                copyStickerFileToSDCard(sticker, targetExternalFilePath);
                MediaShareUtils.shareImageByExport(targetExternalFilePath);
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_sticker_share_mode", "export");
                break;
            case MediaShareUtils.IMAGE_SHARE_MODE_LINK:
                HSInputMethod.inputText(sticker.getStickerRemoteUri());
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_sticker_share_mode", "link");
                break;
            default:
                HSInputMethod.inputText(sticker.getStickerRemoteUri());
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_sticker_share_mode", "link");
        }
    }

    private static void commitPNGImage(Uri contentUri, String imageDescription) {
        InputContentInfoCompat inputContentInfo = new InputContentInfoCompat(contentUri,
                new ClipDescription(imageDescription, new String[]{"image/png"}), null);
        InputConnection inputConnection = HSInputMethodService.getInstance().getCurrentInputConnection();
        EditorInfo editorInfo = HSInputMethodService.getInstance().getCurrentInputEditorInfo();
        int flags = 0;
        if (android.os.Build.VERSION.SDK_INT >= 25) {
            flags |= InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        }
        InputConnectionCompat.commitContent(inputConnection, editorInfo, inputContentInfo, flags, null);
    }

    private static void copyStickerFileToSDCard(Sticker sticker, String destinationPath) {
        if (sticker.getStickerUri().startsWith("assets:")) {
            AssetManager assetManager = HSApplication.getContext().getAssets();
            String stickerAssetPath = StickerUtils.ASSETS_STICKER_FILE_PATH + "/" + sticker.getStickerGroupName() + "/" + sticker.getStickerName() + StickerUtils.STICKER_IMAGE_PNG_SUFFIX;
            try {
                InputStream inputStream = assetManager.open(stickerAssetPath);
                HSFileUtils.copyFile(inputStream, new File(destinationPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (sticker.getStickerUri().startsWith("file:")) {
            String stickerDownloadedPath = StickerUtils.getStickerFolderPath(sticker.getStickerGroupName()) + "/" + sticker.getStickerName() + StickerUtils.STICKER_IMAGE_PNG_SUFFIX;
            HSFileUtils.copyFile(new File(stickerDownloadedPath), new File(destinationPath));
        }
    }

    private static void addDifferentBackgroundForSticker(String stickerFilePath, String packageName, String outputFilePath) {
        FileOutputStream out = null;
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(stickerFilePath, option); // 获取PNG图片的宽高信息
        int height = option.outHeight;
        int width = option.outWidth;
        int backgroundWidth = (int) (height * STICKER_BACKGROUND_ASPECT_RATIO); // 背景宽度
        Bitmap backgroundBitmap = createBitmapAndGcIfNecessary(backgroundWidth, height); //创建背景图
        option.inJustDecodeBounds = false;
        //  TODO: 在decodeFile中为 option.outHeight 以及 option.outWidth 设置具体数值没有效果，故创建两次bitmap, option.inSampleSize 类型为int, 无法达到需求
        Bitmap stickerShareBitmapTemp = BitmapFactory.decodeFile(stickerFilePath, option);
        Bitmap stickerShareBitmap = Bitmap.createScaledBitmap(stickerShareBitmapTemp, (int) (width * STICKER_ZOOM_RATIO), (int) (height * STICKER_ZOOM_RATIO), true);
        Canvas canvas = new Canvas(backgroundBitmap);

        String color = getBackgroundColor(packageName);
        if (!color.equals(ERROR_COLOR)) {
            canvas.drawColor(Color.parseColor(color)); // 为背景着色
        }

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        // 将sticker 居中画出
        canvas.drawBitmap(stickerShareBitmap, (backgroundWidth - stickerShareBitmap.getWidth()) / 2, (height - stickerShareBitmap.getHeight()) / 2, p);
        try {
            out = new FileOutputStream(new File(outputFilePath));
            backgroundBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                backgroundBitmap.recycle();
                stickerShareBitmapTemp.recycle();
                stickerShareBitmap.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Bitmap createBitmapAndGcIfNecessary(int width, int height) {
        try {
            return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            System.gc();
            return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
    }

    private static String getBackgroundColor(String packageName) {
        if (map.containsKey(packageName)) {
            return map.get(packageName);
        } else {
            List<Map<String, Object>> configList = (List<Map<String, Object>>) HSConfig.getList("Application", "StickerBackground");
            for (Map<String, Object> configMap : configList) {
                String configPackageName = (String) configMap.get("packageName");
                if (configPackageName.equals(packageName)) {
                    String color = (String) configMap.get("backgroundColor");
                    map.put(configPackageName, color);
                    return color;
                }
            }
            return ERROR_COLOR;
        }
    }
}
