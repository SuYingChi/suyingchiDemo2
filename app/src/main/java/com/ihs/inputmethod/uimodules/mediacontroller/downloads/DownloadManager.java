package com.ihs.inputmethod.uimodules.mediacontroller.downloads;

import com.ihs.inputmethod.uimodules.mediacontroller.IManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ihandysoft on 16/6/1.
 */
public class DownloadManager implements IManager {

    private static DownloadManager downloadManager;

    private ExecutorService executorService;
    private DownloadExecutor downloadExecutor;
    private DownloadQueue downloadQueue;

    // the cycle schedule time that cache queue will be loop
    private static final long CYCLE_SCHEDULE_TIME = 300;

    /**
     * schedule the cache downloads to thread pool
     */
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private Runnable schedule = new Runnable() {
        @Override
        public void run() {
            downloadQueue.scheduleCacheQueue();
        }
    };

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        if (downloadManager == null) {
            synchronized (DownloadManager.class) {
                if (downloadManager == null) {
                    downloadManager = new DownloadManager();
                }
            }
        }
        return downloadManager;
    }


// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * stop download
//     */
//    public void shutdownDownloadThreadPool() {
//        this.stopSchedule();
//        this.executorService.shutdownNow();
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)


    /**
     * put new download
     *
     * @param baseDownload
     */
    public void put(BaseDownload baseDownload) {
        downloadQueue.put(baseDownload);
    }

    /**
     * start schedule
     */
    private void startSchedule() {
        service.scheduleAtFixedRate(schedule, CYCLE_SCHEDULE_TIME, CYCLE_SCHEDULE_TIME, TimeUnit.MILLISECONDS);
    }

    private void stopSchedule() {
        service.shutdownNow();
    }

    public DownloadQueue getDownloadQueue() {
        return this.downloadQueue;
    }
}
