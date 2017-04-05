package com.ihs.inputmethod.uimodules.mediacontroller.files;

import android.net.Uri;

import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.api.utils.HSFileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class GifFiles {

    public static DownloadFile withDownload(String fileName){
        return new DownloadFile(fileName);
    }

    public static class DownloadFile{

        private String fileName;

        public DownloadFile(String fileName){
            this.fileName = fileName;
        }

        public void write(byte[] bytes) throws IOException {
            FileOutputStream fileOutputStream = new FileOutputStream(newFile());
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        }

        /**
         * filename is absolute path
         * @return
         */
        private File newFile(){
            deleteFile();
            return HSFileUtils.createNewFile(fileName);
        }

        /**
         * filename is absolute path
         */
        public void deleteFile(){
            File file = new File(fileName);
            if(file.exists() && file.isFile()) {
                HSFileUtils.delete(file);
            }
        }

        /**
         * filename is not contain suffix
         * @param url
         * @return
         */
        public File getGif(String url){
            // get gif cache directory
            final int index = url.lastIndexOf('.');
            final String extendedName = url.substring(index);
            return new File(MediaController.getConfig().getGifDownloadPath(), fileName + extendedName);
        }

        public File getOriginGif(String url){
            // get gif cache directory
            final int index = url.lastIndexOf('.');
            final String extendedName = url.substring(index);
            return new File(MediaController.getConfig().getOriginGifDownloadPath(), fileName + extendedName);
        }

        public Uri getGifUri(String url){
            return Uri.fromFile(new File(MediaController.getConfig().getGifDownloadPath(), getGif(url).getName()));
        }

        public File getFile() {
            // get gif cache directory
            return new File(MediaController.getConfig().getGifDownloadPath(), fileName);
        }
    }

}
