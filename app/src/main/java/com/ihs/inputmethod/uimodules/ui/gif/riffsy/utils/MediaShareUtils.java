package com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils;

import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.Sticker;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerPrefsUtil;

import java.io.File;

public final class MediaShareUtils {

    public static final String MIME_MP4 = "video/mpeg";

    public static void shareImageByIntent(final Uri uri, final String mimeType, final String packageName) throws Exception {
        try {
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setPackage(packageName);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType(mimeType);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            HSApplication.getContext().startActivity(shareIntent);
        } catch (Exception e) {
            throw new Exception("Can't share by intent");
        }
    }

    public static void share(String fileSuffix, String packageName, File externalImageFile, String remoteUrl) {
        String[] mimeTypes = EditorInfoCompat.getContentMimeTypes(HSInputMethodService.getInstance().getCurrentInputEditorInfo());
        final String mimeType = "image/*";
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

            Uri uri = getImageContentUri(HSApplication.getContext(), externalImageFile);
            if (fileSuffix.equals(Sticker.STICKER_IMAGE_GIF_SUFFIX) && gifSupported) {
                if (commitStickerImage(uri, "", "image/gif")) {
                    return;
                }
            } else if (fileSuffix.equals(Sticker.STICKER_IMAGE_PNG_SUFFIX) && pngSupported) {
                if (commitStickerImage(uri, "", "image/png")) {
                    return;
                }
            } else if (fileSuffix.equals(Sticker.STICKER_IMAGE_PNG_SUFFIX) && gifSupported) {
                // 如果图片是 PNG，而应用只支持 GIF，则发送时声称是 GIF，目前是可以发出去的；以后也许需要改成需要转成真实的 GIF 格式后再发送
                if (commitStickerImage(uri, "", "image/gif")) {
                    return;
                }
            }
        }

        try {
            MediaShareUtils.shareImageByIntent(Uri.fromFile(externalImageFile), mimeType, packageName);
        } catch (Exception e) {
            HSAnalytics.logEvent("Sticker_toast_send_failed", "packageName", packageName);
            StickerPrefsUtil.getInstance().recordUnsupportApp(packageName);
            if (!TextUtils.isEmpty(remoteUrl)) {
                HSInputMethod.inputText(remoteUrl);
            } else {
                Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.sticker_share_toast), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static boolean commitStickerImage(Uri contentUri, String imageDescription, String fileType) {
        InputContentInfoCompat inputContentInfo = new InputContentInfoCompat(contentUri,
                new ClipDescription(imageDescription, new String[]{fileType}), null);
        InputConnection inputConnection = HSInputMethodService.getInstance().getCurrentInputConnection();
        EditorInfo editorInfo = HSInputMethodService.getInstance().getCurrentInputEditorInfo();
        int flags = 0;
        if (android.os.Build.VERSION.SDK_INT >= 25) {
            flags |= InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        }
        return InputConnectionCompat.commitContent(inputConnection, editorInfo, inputContentInfo, flags, null);
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
}
