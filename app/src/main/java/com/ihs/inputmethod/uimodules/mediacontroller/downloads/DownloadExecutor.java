package com.ihs.inputmethod.uimodules.mediacontroller.downloads;


import java.util.concurrent.ExecutorService;

/**
 * Created by ihandysoft on 16/5/14.
 */
public class DownloadExecutor implements Runnable {

    private DownloadQueue queue;
    private ExecutorService service;

    public DownloadExecutor(ExecutorService service, DownloadQueue queue){
        this.queue = queue;
        this.service = service;
    }

    @Override
    public void run() {
        while(true){
            BaseDownload baseDownload = queue.take();
            if(baseDownload != null){
                start(baseDownload);
            }
        }
    }

    void start(BaseDownload download){
        service.execute(download);
    }

}
