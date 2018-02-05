package com.ihs.inputmethod.uimodules.mediacontroller;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.ihs.inputmethod.api.managers.HSDirectoryManager;
import com.ihs.inputmethod.uimodules.mediacontroller.converts.ConvertManager;
import com.ihs.inputmethod.uimodules.mediacontroller.downloads.DownloadManager;
import com.ihs.inputmethod.uimodules.mediacontroller.files.FileManager;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareManager;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class MediaController {



    private static MediaController mediaController;

    private static Configuration configuration;
    // --Commented out by Inspection (18/1/11 下午2:41):private static ConvertManager convertManager;
    // --Commented out by Inspection (18/1/11 下午2:41):private static FileManager fileManager;
    // --Commented out by Inspection (18/1/11 下午2:41):private static DownloadManager downloadManager;
    // --Commented out by Inspection (18/1/11 下午2:41):private static ShareManager shareManager;

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static MediaController getInstance(){
//        if(mediaController == null){
//            synchronized (MediaController.class){
//                mediaController = new MediaController();
//            }
//        }
//        return mediaController;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public static void setFaceNameProvider(@NonNull FaceNameProvider aFaceNameProvider) {
        faceNameProvider = aFaceNameProvider;
    }

    public static void setHandler(@NonNull Handler aHandler) {
        handler = aHandler;
    }

    public static Configuration getConfig(){
        if(configuration == null){
            synchronized (MediaController.class) {
                if(configuration == null) {
                    configuration = new Configuration();
                }
            }
        }
        return configuration;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static ConvertManager getConvertManager(){
//
//        return ConvertManager.getInstance();
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
    public static DownloadManager getDownloadManger(){

        return DownloadManager.getInstance();
    }
    public static FileManager getFileManager(){
        return FileManager.getInstance();
    }
    public static ShareManager getShareManager(){

        return ShareManager.getInstance();
    }

    public static Handler getHandler() {
        return handler;
    }

    public static FaceNameProvider getFaceNameProvider()
    {
        return faceNameProvider;
    }

    private static Handler handler;
    private static FaceNameProvider faceNameProvider;

    public static class Configuration {
        private String gifDownloadPath;
        private String originGifDownloadPath;
        private String gifSharePath;
        private String mp4DownloadPath;
        private String mp4SharePath;

        public Configuration(){
            gifDownloadPath = HSDirectoryManager.getInstance().getAPPFilePath(HSDirectoryManager.Environments.CACHE_GIF);
            originGifDownloadPath = HSDirectoryManager.getInstance().getAPPFilePath(HSDirectoryManager.Environments.CACHE_GIF_ORIGIN);
            gifSharePath = HSDirectoryManager.getInstance().getSDPackagePath(HSDirectoryManager.Environments.SHARE_GIF);
            mp4DownloadPath = HSDirectoryManager.getInstance().getSDPackagePath(HSDirectoryManager.Environments.DOWNLOAD_MP4);
            mp4SharePath = HSDirectoryManager.getInstance().getSDPackagePath(HSDirectoryManager.Environments.SHARE_MP4);
        }

// --Commented out by Inspection START (18/1/11 下午2:41):
//        public Configuration(Configuration config){
//            gifDownloadPath = config.gifDownloadPath;
//            originGifDownloadPath = config.originGifDownloadPath;
//            gifSharePath = config.gifSharePath;
//            mp4DownloadPath = config.mp4DownloadPath;
//            mp4SharePath = config.mp4SharePath;
//        }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//        public void setGifDownloadPath(String gifDownloadPath) {
//            this.gifDownloadPath = gifDownloadPath;
//        }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//        public void setGifSharePath(String gifSharePath) {
//            this.gifSharePath = gifSharePath;
//        }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//        public void setMp4DownloadPath(String mp4DownloadPath) {
//            this.mp4DownloadPath = mp4DownloadPath;
//        }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//        public void setMp4SharePath(String mp4SharePath) {
//            this.mp4SharePath = mp4SharePath;
//        }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

        public String getGifDownloadPath() {
            return gifDownloadPath;
        }

        public String getOriginGifDownloadPath() {
            return originGifDownloadPath;
        }

// --Commented out by Inspection START (18/1/11 下午2:41):
//        public void setOriginGifDownloadPath(String originGifDownloadPath) {
//            this.originGifDownloadPath = originGifDownloadPath;
//        }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

        public String getGifSharePath() {
            return gifSharePath;
        }

        public String getMp4DownloadPath() {
            return mp4DownloadPath;
        }

        public String getMp4SharePath() {
            return mp4SharePath;
        }
    }

    public interface FaceNameProvider {
        String faceName();
    }

}
