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

    public static void shutDown(){
        executorService.shutdownNow();
    }
}
