package com.ihs.inputmethod.uimodules.mediacontroller.files;

import android.net.Uri;

import com.ihs.inputmethod.uimodules.mediacontroller.IManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class FileManager implements IManager {

    private static FileManager fileManager;

    private FileManager(){}

    public static FileManager getInstance(){
        if(fileManager == null){
            synchronized (FileManager.class){
                if(fileManager == null){
                fileManager = new FileManager();
                }
            }
        }
        return fileManager;
    }

    public void saveDownloadGif(String filePath, byte[] bytes){
        try {
            GifFiles.withDownload(filePath).write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteDownloadGif(String filePath){
        GifFiles.withDownload(filePath).deleteFile();
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public File getDownloadGif(String filePath, String url){
//        return GifFiles.withDownload(filePath).getGif(url);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public Uri getDownloadGifUri(String filePath, String url){
//        return GifFiles.withDownload(filePath).getGifUri(url);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public File getDownloadGif(String filePath){
        return GifFiles.withDownload(filePath).getFile();
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public File getDownloadMP4(String filePath){
//        return MP4Files.withDownload(filePath).getFile();
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
// --Commented out by Inspection START (18/1/11 下午2:41):
//    public File getDownloadMP4(String filePath, String url){
//        return MP4Files.withDownload(filePath).getMP4(url);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)


}
