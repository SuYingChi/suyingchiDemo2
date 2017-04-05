package com.ihs.booster.utils;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    private static String cacheFolder;
    private static String fileFolder = null;

    public static float getFolderSize(String path) {//返回 MB
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        return getFolderSize(new File(path)) / 1024 / 1024;
    }

    public static float getFolderSize(File f) { //返回 byte
        float size = 0;
        File flist[] = f.listFiles();
        for (File file : flist) {
            if (file.isDirectory()) {
                size += getFolderSize(file);
            } else {
                size += file.length();
            }
        }
        return size;
    }

    public static String getDiskCachePathForVideo(String remoteUrl) {
        if (TextUtils.isEmpty(remoteUrl)) {
            return null;
        }
        String name = CommonUtils.getMD5(remoteUrl);
        File file = new File(getDiskCacheFolder() + File.separator + "video." + name);
        file.setLastModified(System.currentTimeMillis());
        return file.toString();
    }

    public static String getDiskCacheFolder() {
        if (cacheFolder == null) {
            File folder = null;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                folder = HSApplication.getContext().getExternalCacheDir();
            }
            if (folder == null) {
                folder = HSApplication.getContext().getCacheDir();
                if (folder == null) {
                    return null;
                }
            } else if (Utils.getSDAvailableSize() < 10 * 1024) {
                folder = HSApplication.getContext().getCacheDir();
                if (folder == null) {
                    return null;
                }
            }
            cacheFolder = folder.getPath() + File.separator;
        }
        new File(cacheFolder).mkdirs();
        return cacheFolder;
    }

    public static String getDiskCachePath(String remoteUrl) {
        if (TextUtils.isEmpty(remoteUrl)) {
            return null;
        }
        String folder = getDiskCacheFolder();
        String name = CommonUtils.getMD5(remoteUrl);
        return folder + name;
    }

    public static void resetCacheFolder() {
        cacheFolder = null;
        getDiskCacheFolder();
    }

    public static String getSDCacheFolder() {
        File folder = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            folder = HSApplication.getContext().getExternalCacheDir();
        }
        if (folder == null) {
            return null;
        } else {
            return folder.getPath() + File.separator;
        }
    }

    public static String getNativeCacheFolder() {
        File folder = null;
        folder = HSApplication.getContext().getCacheDir();
        if (folder == null) {
            return null;
        } else {
            return folder.getPath() + File.separator;
        }
    }

    public static String getFileFolder() {
        if (fileFolder != null && new File(fileFolder).exists()) {
            return fileFolder;
        }
        File file = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = HSApplication.getContext().getExternalFilesDir(null);
        }
        if (file == null) {
            file = HSApplication.getContext().getFilesDir();
            if (file == null) {
                return null;
            }
        }
        if (!file.exists()) {
            file.mkdir();
        }
        return fileFolder = file.getPath() + File.separator;
    }

    public static void deleteFolderFiles(String filePath) {
        File fileFolder = new File(filePath);
        if (fileFolder != null && fileFolder.exists()) {
            if (fileFolder.isDirectory()) {
                File[] list = fileFolder.listFiles();
                for (int i = 0; i < list.length; ++i) {
                    if (list[i].isFile()) {// 如果是文件，直接删除
                        list[i].delete();
                    } else {
                        deleteFolderFiles(list[i].getAbsolutePath());// 递归删除子目录下的文件
                    }
                }
                fileFolder.delete();// 删除指定的目录本身
            } else {
                fileFolder.delete();
            }
        }
    }

    public static void removeCacheFiles() {
        try {
            deleteFolderFiles(HSApplication.getContext().getCacheDir().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            deleteFolderFiles(HSApplication.getContext().getExternalCacheDir().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(String sourcePath, String targetPath) {
        copyFile(new File(sourcePath), new File(targetPath));
    }

    public static void copyFile(File sourceFile, File targetFile) {
        try {
            FileInputStream input = new FileInputStream(sourceFile);
            BufferedInputStream inBuff = new BufferedInputStream(input);

            // 新建文件输出流并对它进行缓冲
            FileOutputStream output = new FileOutputStream(targetFile);
            BufferedOutputStream outBuff = new BufferedOutputStream(output);

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();

            // 关闭流
            inBuff.close();
            outBuff.close();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制assets下的文件
     *
     * @param assetsFileName assets下的神完整路径
     */
    public static boolean copyAssetsFile(String assetsFileName, String targetPath) {
        AssetManager manager = HSApplication.getContext().getAssets();
        boolean flag = false;
        try {
            InputStream input = manager.open(assetsFileName);
            if (input != null) {
                File outFile = new File(targetPath + File.separator + assetsFileName);
                OutputStream output = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = input.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                }
                output.close();
                input.close();
                flag = true;
            }
        } catch (Exception e) {
        }
        return flag;
    }

    /**
     * 解压缩文件
     *
     * @param zipFilePath 压缩包路径
     * @param destPath    解压目标路径
     * @throws Exception
     */
    public static void unzip(String zipFilePath, String destPath) throws Exception {
        unzip(new FileInputStream(zipFilePath), destPath);
    }

    public static void unzip(InputStream inputStream, String destPath) throws Exception {
        new File(destPath).mkdirs();
        ZipInputStream inZip = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        String szName = "";
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                // 文件夹名称
                szName = szName.substring(0, szName.length() - 1);
                new File(destPath + File.separator + szName).mkdirs();
            } else {
                File file = new File(destPath + File.separator + szName);
                file.createNewFile();
                // 文件
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inZip.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }

    public static void unzipAsset(String zipAssetPath, String destPath) throws Exception {
        unzip(HSApplication.getContext().getAssets().open(zipAssetPath), destPath);
    }

    public static boolean saveResDrawableToFile(int resID, String localPath) {
        InputStream is = HSApplication.getContext().getResources().openRawResource(resID);
        if (is == null) {
            return false;
        }
        boolean result = true;
        Bitmap bmp = BitmapFactory.decodeStream(is);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(localPath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, out);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
