package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.widget.Toast;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.DirectoryUtils;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.MediaShareUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanxia on 2017/6/6.
 */

public class StickerUtils {

    public static final String ASSETS_STICKER_FILE_PATH = "Stickers";
    public static final String STICKER_TAB_IMAGE_SUFFIX = "-tab.png";
    public static final String STICKER_DOWNLOAD_ZIP_SUFFIX = ".zip";
//    public static final String STICKER_IMAGE_PNG_SUFFIX = ".png";
//    public static final String STICKER_IMAGE_GIF_SUFFIX = ".gif";

    private static Map<String, String> map = new HashMap<>();
    private static final float STICKER_BACKGROUND_ASPECT_RATIO = 1.7f;
    private static final float STICKER_ZOOM_RATIO = 0.7f;
    private static final String ERROR_COLOR = "error_color";

    public static String getStickerRootFolderPath() {
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_STICKER_FILE_PATH;
    }

    /**
     * get the sticker file local saved path:
     * example:/data/user/0/com.keyboard.colorkeyboard/files/Stickers/YellowSmile3-2.gif
     *
     * @param sticker
     * @return
     */
    public static String getStickerLocalPath(Sticker sticker) {
        String stickerRootFolderPath = getStickerRootFolderPath();
        StringBuilder stringBuilder = new StringBuilder(stickerRootFolderPath).append(File.separator)
                .append(sticker.getStickerGroupName()).append(File.separator)
                .append(sticker.getStickerName()).append(sticker.getStickerFileSuffix());
        return stringBuilder.toString();
    }

    /**
     * get the sticker asset file path
     * example:Stickers/YellowSmile3/
     *
     * @param sticker
     * @return
     */
    public static String getStickerAssetFolderPath(Sticker sticker) {
        return ASSETS_STICKER_FILE_PATH + File.separator + sticker.getStickerGroupName();
    }

    public static String getStickerFolderPath(String stickerGroupName) {
        return getStickerRootFolderPath() + File.separator + stickerGroupName;
    }

    public static String getStickerDownloadBaseUrl() {
        return HSConfig.getString("Application", "Server", "StickerDownloadBaseURL");
    }

    private static String getStickerAssetsPath(Sticker sticker) {
        return ASSETS_STICKER_FILE_PATH + "/" + sticker.getStickerGroupName() + "/" + sticker.getStickerName() + sticker.getStickerFileSuffix();
    }

    private static String getStickerFilePath(Sticker sticker) {
        return getStickerFolderPath(sticker.getStickerGroupName()) + "/" + sticker.getStickerName() + sticker.getStickerFileSuffix();
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

    public static boolean isEditTextSupportSticker(String packageName) {
        return DirectoryUtils.isSDCardEnabled() && StickerPrefsUtil.getInstance().isAppSupportSticker(packageName);

    }

    public static void sendStickerToPackage(final Sticker sticker, final String packageName) {
        if (!DirectoryUtils.isSDCardEnabled() || !StickerPrefsUtil.getInstance().isAppSupportSticker(packageName)) {
            Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.sticker_share_toast), Toast.LENGTH_SHORT).show();
            return;
        }
        StickerPrefsUtil.getInstance().recordStickerSelect(sticker.getStickerName());

        final String targetExternalFilePath = DirectoryUtils.getImageExportFolder() + "/" + sticker.getStickerName() + sticker.getStickerFileSuffix();
        addDifferentBackgroundForSticker(sticker, packageName, targetExternalFilePath);
        File externalImageFile = new File(targetExternalFilePath);
        if (!externalImageFile.exists()) {
            Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.sticker_send_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        addDifferentBackgroundForSticker(sticker, packageName, targetExternalFilePath);

        MediaShareUtils.share(sticker.getStickerFileSuffix(), packageName, externalImageFile, "");

        HSAnalytics.logEvent("keyboard_sticker_share_mode");
    }

    private static void copyStickerFileToSDCard(Sticker sticker, String destinationPath) {
        if (sticker.getStickerUri().startsWith("assets:")) {
            AssetManager assetManager = HSApplication.getContext().getAssets();
            String stickerAssetPath = getStickerAssetsPath(sticker);
            try {
                InputStream inputStream = assetManager.open(stickerAssetPath);
                HSFileUtils.copyFile(inputStream, new File(destinationPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (sticker.getStickerUri().startsWith("file:")) {
            String stickerDownloadedPath = getStickerFilePath(sticker);
            HSFileUtils.copyFile(new File(stickerDownloadedPath), new File(destinationPath));
        }
    }

    private static void addDifferentBackgroundForSticker(Sticker sticker, String packageName, String outputFilePath) {
        if (sticker.getStickerFileSuffix().equals(Sticker.STICKER_IMAGE_GIF_SUFFIX)) {
            copyStickerFileToSDCard(sticker, outputFilePath);
            return;
        }
        FileOutputStream out = null;
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        // 获取PNG图片的宽高信息
        if (sticker.getStickerUri().startsWith("assets:")) {
            String stickerAssetPath = getStickerAssetsPath(sticker);
            InputStream inputStream = null;
            try {
                inputStream = HSApplication.getContext().getAssets().open(stickerAssetPath);
                BitmapFactory.decodeStream(inputStream, null, option);
            } catch (IOException e) {
                e.printStackTrace();
                copyStickerFileToSDCard(sticker, outputFilePath);
                return;
            }
        } else if (sticker.getStickerUri().startsWith("file:")) {
            String stickerDownloadedPath = getStickerFilePath(sticker);
            BitmapFactory.decodeFile(stickerDownloadedPath, option);
        }
        int height = option.outHeight;
        int width = option.outWidth;

        int backgroundWidth = (int) (height * STICKER_BACKGROUND_ASPECT_RATIO); // 背景宽度
        Bitmap backgroundBitmap = createBitmapAndGcIfNecessary(backgroundWidth, height); //创建背景图
        Bitmap stickerShareBitmapTemp = ImageLoader.getInstance().loadImageSync(sticker.getStickerUri());
        if (stickerShareBitmapTemp == null) {
            copyStickerFileToSDCard(sticker, outputFilePath);
            return;
        }
        Bitmap stickerShareBitmap = Bitmap.createScaledBitmap(stickerShareBitmapTemp, (int) (width * STICKER_ZOOM_RATIO), (int) (height * STICKER_ZOOM_RATIO), true);
        Canvas canvas = new Canvas(backgroundBitmap);

        canvas.drawColor(Color.parseColor(getBackgroundColor(packageName))); // 为背景着色

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
            copyStickerFileToSDCard(sticker, outputFilePath);
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
            List<Map<String, Object>> configList = (List<Map<String, Object>>) HSConfig.getList("Application", "StickersGifs", "Sticker", "StickerBackground");
            for (Map<String, Object> configMap : configList) {
                String configPackageName = (String) configMap.get("packageName");
                if (configPackageName.equals(packageName)) {
                    String color = (String) configMap.get("backgroundColor");
                    map.put(configPackageName, color);
                    return color;
                }
            }
            return "#FFFFFF";
        }
    }

    public static String getGroupNameByStickerName(String stickerName) {
        int indexOfConnector = stickerName.indexOf('-');
        if (indexOfConnector > 0) {
            return stickerName.substring(0, indexOfConnector);
        } else {
            HSLog.e("tag sticker suggestion name wrong format");
            return null;
        }
    }
}
