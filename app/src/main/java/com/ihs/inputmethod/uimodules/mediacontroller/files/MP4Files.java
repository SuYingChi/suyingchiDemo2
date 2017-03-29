package com.ihs.inputmethod.uimodules.mediacontroller.files;

import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;

import java.io.File;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class MP4Files {

    public static DownloadFile withDownload(String fileName){
        return new DownloadFile(fileName);
    }

    public static class DownloadFile{

        private String fileName;

        public DownloadFile(String fileName){
            this.fileName = fileName;
        }

        public File getFile(){
            return new File(MediaController.getConfig().getMp4DownloadPath(), fileName);
        }

        /**
         * filename is not contain suffix
         * @param url
         * @return
         */
        public File getMP4(String url){
            // get mp4 cache directory
            final int index = url.lastIndexOf('.');
            final String extendedName = url.substring(index);
            return new File(MediaController.getConfig().getMp4DownloadPath(), fileName + extendedName);
        }

    }
}
