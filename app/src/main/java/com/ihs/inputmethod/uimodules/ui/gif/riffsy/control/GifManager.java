package com.ihs.inputmethod.uimodules.ui.gif.riffsy.control;

import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.DaoHelper;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download.GifDownloadTask;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.BaseRequest;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.DirectoryUtils;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.MediaShareUtils;

import java.io.File;

public final class GifManager {

    private static GifManager mInstance;

    private GifManager() {

    }

    public static GifManager getInstance() {
        if (mInstance == null) {
            init();
        }
        return mInstance;
    }

    public static void init() {
        if (mInstance == null) {
            synchronized (GifManager.class) {
                if (mInstance == null) {
                    mInstance = new GifManager();
                    DaoHelper.init();
                    DataManager.init();
                }
            }
        }
    }


    synchronized public void sendRequest(final BaseRequest request) {
        if (request != null) {
            RequestHandler.handleRequest(request);
        }
    }
    public void share(final GifItem gif, final String packageName, final GifDownloadTask.Callback callback) {
        if (!DirectoryUtils.isSDCardEnabled()) {
            HSInputMethod.inputText(gif.getUrl());
            return;
        }

        MediaShareUtils.share(".gif", packageName, DirectoryUtils.getDownloadGifUri(gif.id), gif.getUrl());
    }

    private void shareMp4(final GifItem gif, GifDownloadTask.Callback callback) {
        DownloadManager.getInstance().loadMp4(gif.id + ".mp4", gif.getMp4Url(), callback);
    }

    private Uri getGifContentUri(Context context, File imageFile) {
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

    /**
     * Commits a GIF image
     *
     * @param contentUri       Content URI of the GIF image to be sent
     * @param imageDescription Description of the GIF image to be sent
     */
    private void commitGifImage(Uri contentUri, String imageDescription) {
        InputContentInfoCompat inputContentInfo = new InputContentInfoCompat(contentUri,
                new ClipDescription(imageDescription, new String[]{"image/gif"}), null);
        InputConnection inputConnection = HSInputMethodService.getInstance().getCurrentInputConnection();// getCurrentInputConnection();
        EditorInfo editorInfo = HSInputMethodService.getInstance().getCurrentInputEditorInfo();// getCurrentInputEditorInfo();
        int flags = 0;
        if (android.os.Build.VERSION.SDK_INT >= 25) {
            flags |= InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        }
        InputConnectionCompat.commitContent(
                inputConnection, editorInfo, inputContentInfo, flags, null);
    }

}