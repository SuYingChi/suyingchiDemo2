package com.ihs.inputmethod.uimodules.ui.gif.riffsy.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hdd on 16/4/26.
 */
public class AsyncThreadPools {

    private static ExecutorService executorService;

    // processors count
    private static final int processorsNum = Runtime.getRuntime().availableProcessors() + 10;

    static {
        executorService = Executors.newFixedThreadPool(processorsNum);
    }

    public static void execute(Runnable runnable){
        executorService.execute(runnable);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static void shutDown(){
//        executorService.shutdownNow();
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}
