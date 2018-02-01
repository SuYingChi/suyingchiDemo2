package com.ihs.inputmethod.feature.common;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An utility class for concurrent works.
 */
public class ConcurrentUtils {

    private static final String TAG = ConcurrentUtils.class.getSimpleName();

    private static final String THREAD_TAG_POOL = "launcher-pool-thread-";
    private static final String THREAD_TAG_SERIAL = "launcher-serial-thread";

    private static final int NUMBER_OF_ALIVE_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private static final ThreadPoolExecutor sExecutor;
    private static final ThreadFactory sDefaultThreadFactory = Executors.defaultThreadFactory();

    private static final Executor sSingleThreadExecutor;

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    static {
        int poolSize = Math.max(2, NUMBER_OF_ALIVE_CORES * 2 - 1);
        sExecutor = new ThreadPoolExecutor(poolSize,
                poolSize, // Max pool size, not used as we are providing an unbounded queue to the executor
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT, new LinkedBlockingDeque<Runnable>(),
                new ThreadFactory() {
                    private AtomicInteger mThreadCount = new AtomicInteger(0);

                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        Thread thread = sDefaultThreadFactory.newThread(r);
                        thread.setName(THREAD_TAG_POOL + mThreadCount.getAndIncrement());
                        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        return thread;
                    }
                }
        );
        sSingleThreadExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = sDefaultThreadFactory.newThread(r);
                thread.setName(THREAD_TAG_SERIAL);
                thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                return thread;
            }
        });
    }

    public static void postOnThreadPoolExecutor(Runnable r) {
        sExecutor.execute(r);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
    public static void postOnSingleThreadExecutor(Runnable r) {
        sSingleThreadExecutor.execute(r);
    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static void postOnMainThread(Runnable r) {
//        sMainHandler.post(r);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    @SuppressWarnings("unchecked")
//    public static void execute(AsyncTask task) {
//        task.executeOnExecutor(sExecutor, (Object[]) null);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    @SuppressWarnings("TryWithIdenticalCatches")
//    public static Object callWithTimeout(Callable<Object> callable, long timeout, TimeUnit unit) throws TimeoutException {
//        Future<Object> future = sExecutor.submit(callable);
//        Object returnValue = null;
//        try {
//            returnValue = future.get(timeout, unit);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            HSLog.i(TAG, "Invocation timed out, interrupt");
//            future.cancel(true);
//            throw e;
//        }
//        return returnValue;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}
