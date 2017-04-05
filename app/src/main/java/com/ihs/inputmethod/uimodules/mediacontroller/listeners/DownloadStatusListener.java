package com.ihs.inputmethod.uimodules.mediacontroller.listeners;

import java.io.File;

/**
 * Created by ihandysoft on 16/6/1.
 */
public interface DownloadStatusListener {

    void onDownloadProgress(File file, float percent);
    void onDownloadSucceeded(File file);
    void onDownloadFailed(File file);
}
