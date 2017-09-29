package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareUtils;
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
        if (!DirectoryUtils.isSDCardEnabled()) {
            return false;
        }

        Map<String, Object> shareModeMap = MediaShareUtils.getShareModeMap(packageName);
        return (boolean) shareModeMap.get(MediaShareUtils.IMAGE_SHARE_MODE_MAP_KEY_SEND_DIRECTLY)
                || (int) shareModeMap.get(MediaShareUtils.IMAGE_SHARE_MODE_MAP_KEY_MODE) == MediaShareUtils.IMAGE_SHARE_MODE_INTENT
                || (int) shareModeMap.get(MediaShareUtils.IMAGE_SHARE_MODE_MAP_KEY_MODE) == MediaShareUtils.IMAGE_SHARE_MODE_EXPORT;
    }

    public static void share(final Sticker sticker, final String packageName) {
        if (!DirectoryUtils.isSDCardEnabled()) {
            Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.sticker_share_toast), Toast.LENGTH_SHORT).show();
            //HSInputMethod.inputText(sticker.getStickerRemoteUri());
            return;
        }
        StickerPrefsUtil.getInstance().recordStickerSelect(sticker.getStickerName());

        final String targetExternalFilePath = DirectoryUtils.getImageExportFolder() + "/" + sticker.getStickerName() + sticker.getStickerFileSuffix();
        final String mimeType = "image/*";

        final Map<String, Object> shareModeMap = MediaShareUtils.getShareModeMap(packageName);
        final boolean canSendDirectly = (Boolean) shareModeMap.get(MediaShareUtils.IMAGE_SHARE_MODE_MAP_KEY_SEND_DIRECTLY);

        // 可以直接发送到对话列表中
        if (canSendDirectly) {
            String[] mimeTypes = EditorInfoCompat.getContentMimeTypes(HSInputMethodService.getInstance().getCurrentInputEditorInfo());
            boolean pngSupported = false;
            boolean gifSupported = false;
            for (String mime_Type : mimeTypes) {
                if (ClipDescription.compareMimeTypes(mime_Type, "image/png")) {
                    pngSupported = true;
                }
                if (ClipDescription.compareMimeTypes(mime_Type, "image/gif")) {
                    gifSupported = true;
                }
            }
            if (pngSupported || gifSupported) {
                addDifferentBackgroundForSticker(sticker, packageName, targetExternalFilePath);
                File externalImageFile = new File(targetExternalFilePath);
                if (!externalImageFile.exists()) {
                    Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.sticker_send_failed), Toast.LENGTH_SHORT).show();
                    return;
                }

                Uri uri = getImageContentUri(HSApplication.getContext(), externalImageFile);
                if (sticker.getStickerFileSuffix().equals(Sticker.STICKER_IMAGE_GIF_SUFFIX) && gifSupported) {
                    commitStickerImage(uri, "", "image/gif");
                    return;
                } else if (sticker.getStickerFileSuffix().equals(Sticker.STICKER_IMAGE_PNG_SUFFIX) && pngSupported) {
                    commitStickerImage(uri, "", "image/png");
                    return;
                } else if (sticker.getStickerFileSuffix().equals(Sticker.STICKER_IMAGE_PNG_SUFFIX) && gifSupported) {
                    // 如果图片是 PNG，而应用只支持 GIF，则发送时声称是 GIF，目前是可以发出去的；以后也许需要改成需要转成真实的 GIF 格式后再发送
                    commitStickerImage(uri, "", "image/gif");
                    return;
                } else {
                    // 如果找不到把图片直接发出去的格式支持，则走原来的逻辑
                }
            }
        }

        final int mode = (int) shareModeMap.get(MediaShareUtils.IMAGE_SHARE_MODE_MAP_KEY_MODE);
        switch (mode) {
            // image
            case MediaShareUtils.IMAGE_SHARE_MODE_INTENT:
                addDifferentBackgroundForSticker(sticker, packageName, targetExternalFilePath);
                try {
                    MediaShareUtils.shareImageByIntent(Uri.fromFile(new File(targetExternalFilePath)), mimeType, packageName);
                } catch (Exception e) {
                    Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.sticker_share_toast), Toast.LENGTH_SHORT).show();
                    //HSInputMethod.inputText(sticker.getStickerRemoteUri());
                }
                break;
            // save to gallery
            case ShareUtils.IMAGE_SHARE_MODE_EXPORT:
                copyStickerFileToSDCard(sticker, targetExternalFilePath);
                MediaShareUtils.shareImageByExport(targetExternalFilePath);
                break;
            case MediaShareUtils.IMAGE_SHARE_MODE_LINK:
                Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.sticker_share_toast), Toast.LENGTH_SHORT).show();
                //HSInputMethod.inputText(sticker.getStickerRemoteUri());
                break;
            default:
                Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.sticker_share_toast), Toast.LENGTH_SHORT).show();
                //HSInputMethod.inputText(sticker.getStickerRemoteUri());
        }
    }

    private static void commitStickerImage(Uri contentUri, String imageDescription, String fileType) {
        InputContentInfoCompat inputContentInfo = new InputContentInfoCompat(contentUri,
                new ClipDescription(imageDescription, new String[]{fileType}), null);
        InputConnection inputConnection = HSInputMethodService.getInstance().getCurrentInputConnection();
        EditorInfo editorInfo = HSInputMethodService.getInstance().getCurrentInputEditorInfo();
        int flags = 0;
        if (android.os.Build.VERSION.SDK_INT >= 25) {
            flags |= InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        }
        InputConnectionCompat.commitContent(inputConnection, editorInfo, inputContentInfo, flags, null);
    }

    private static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
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
