package com.ihs.inputmethod.uimodules.mediacontroller.downloads;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by hdd on 16/5/14.
 */
public class DownloadQueue {

    private static DownloadQueue taskQueue;

    // task queue
    private BlockingQueue<BaseDownload> downloads = new LinkedBlockingQueue<>();
    // running task queue
    private ConcurrentLinkedQueue<BaseDownload> runningDownloads = new ConcurrentLinkedQueue<>();
    // cache
    private ConcurrentLinkedQueue<BaseDownload> cacheDownloads = new ConcurrentLinkedQueue<>();

    // the min time that task will be stayed in cache queue
    private static final long MIN_CACHE_TIME = 300;
    // cache size
    private static final int CACHE_SIZE = 8;

    /**
     * schedule cache queue
     */
    synchronized void scheduleCacheQueue(){
        Iterator<BaseDownload> iter = cacheDownloads.iterator();
        while(iter.hasNext()) {
            BaseDownload baseDownload = iter.next();
            long time = (System.currentTimeMillis() - baseDownload.getStartTime());
            if (time >= MIN_CACHE_TIME) {
                try {
                    downloads.put(baseDownload);
                    iter.remove();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private DownloadQueue(){

    }

    public synchronized static DownloadQueue getInstance(){
        if(taskQueue == null){
            taskQueue = new DownloadQueue();
        }
        return taskQueue;
    }

    /**
     * add a download to cache queue
     *
     * @param download
     */
    public void put(BaseDownload download){
        addToCacheQueue(download);
    }

    private synchronized void addToCacheQueue(BaseDownload download){
        if(cacheDownloads.contains(download)){
            cacheDownloads.remove(download);
        }
        download.setStartTime(System.currentTimeMillis());
        this.cacheDownloads.add(download);

        if(cacheDownloads.size() >= CACHE_SIZE){
            cacheDownloads.remove(0);
        }
    }

    /**
     * take a download task from queue
     *
     * if the download is running, put the download to cache schedule
     * else put the download to thread pool
     * @return
     */
    public BaseDownload take(){

        try {
            BaseDownload download = this.downloads.take();
            if(download != null){
                if(runningDownloads.contains(download)){
                    /**
                     * if this download is running
                     * delay it to run
                     */
                    download.setStartTime(System.currentTimeMillis());
                    put(download);
                    return null;
                }
                else {
                    runningDownloads.add(download);
                }
            }
            return download;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * call when download task is over, stopped, exception
     * never call this method by yourself
     * @param download
     */
    void removeRunningDownload(BaseDownload download){
        runningDownloads.remove(download);
    }

}
