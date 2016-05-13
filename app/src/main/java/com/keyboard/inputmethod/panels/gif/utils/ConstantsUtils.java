package com.keyboard.inputmethod.panels.gif.utils;

import android.Manifest;
import android.os.Environment;

import com.ihs.app.framework.HSApplication;
import com.ihs.permissions.PermissionsUtil;

import java.io.File;

public final class ConstantsUtils {

    private static final String GIF_CACHE_DIR = "gif_cache";
    private static final String IMAGE_CACHE_DIR = "share_cache";
    private static final String MP4_CACHE_DIR = "mp4_cache";
    private static final String CACHE_DIR = "EmojiKeyboard";

    public static File getGifDownloadFolder() {
        return getCacheDirectory(GIF_CACHE_DIR);
    }

    public static File getMp4CacheDir() {
        final File mp4Dir=new File(getAvailableCacheDir(),MP4_CACHE_DIR);
        if (!mp4Dir.exists()) {
            mp4Dir.mkdirs();
        }
        return mp4Dir;
    }

    private static File getCacheDirectory(final String dirName) {
        File dir=new File(getAvailableCacheDir(),dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String getImageExportFolder() {
        File imageCacheFolder = new File(getAvailableCacheDir(), IMAGE_CACHE_DIR);
        if (!imageCacheFolder.exists()) {
            imageCacheFolder.mkdirs();
        }

        return imageCacheFolder.getAbsolutePath();
    }

    public static File getDownloadGifUri(final String fileName) {
        final File folderPath = getGifDownloadFolder();
        return new File(folderPath,fileName);
    }

    private static File getAvailableCacheDir(){
        File dir;
        if(!PermissionsUtil.checkAllPermissionsGranted(HSApplication.getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)){
            dir=new File(HSApplication.getContext().getFilesDir(),CACHE_DIR);
        }else  if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            dir=new File(Environment.getExternalStorageDirectory(),CACHE_DIR);
        }else{
            dir=new File(HSApplication.getContext().getFilesDir(),CACHE_DIR);
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static boolean isSDCardEnabled(){
        return PermissionsUtil.checkAllPermissionsGranted(HSApplication.getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
