package com.ihs.booster.common;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class MBThread {

    private HandlerThread thread;
    private Handler handler;
    private boolean canceled = false;

    public MBThread(String name) {
        thread = new HandlerThread(name, Thread.MAX_PRIORITY);
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    public void destroy() {
        canceled = true;
        untilDone(new Runnable() {
            @Override
            public void run() {

            }
        });
        thread.quit();
    }

    private void untilDone(final Runnable action) {
        if (Thread.currentThread() == thread) {
            if (action != null) {
                action.run();
            }
            return;
        }
        final Object mLock = new Object();
        synchronized (mLock) {
            try {
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (action != null) {
                            action.run();
                        }
                        synchronized (mLock) {
                            mLock.notify();
                        }
                    }
                };
                handler.post(myRunnable);
                mLock.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void run(final Runnable action) {
        Runnable runnable = getValidRunnable(action);
        if (Thread.currentThread() != thread) {
            handler.post(runnable);
        } else {
            runnable.run();
        }
    }

    private Runnable getValidRunnable(final Runnable action) {
        return new Runnable() {
            @Override
            public void run() {
                if (!canceled && action != null) {
                    action.run();
                }
            }
        };
    }

    public Handler getHandler() {
        return handler;
    }

    public void waitUntilDone(Runnable action) {
        untilDone(getValidRunnable(action));
    }

    public Object waitUntilDone(Callable action) {
        return untilDone(getValidCallable(action));
    }

    private Object untilDone(Callable action) {
        if (action == null) {
            action = new Callable() {
                @Override
                public Object call() throws Exception {
                    return null;
                }
            };
        }
        if (Thread.currentThread() == thread) {
            try {
                return action.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        FutureTask<Object> future = new FutureTask<Object>(action);
        handler.post(future);
        Object obj = null;
        try {
            obj = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    private Callable getValidCallable(final Callable action) {
        return new Callable() {
            @Override
            public Object call() throws Exception {
                if (!canceled && action != null) {
                    try {
                        return action.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
    }

    public void disable(final Runnable runnable) {
        canceled = true;
        untilDone(runnable);
    }

    public Object disable(final Callable<?> callable) {
        canceled = true;
        return untilDone(callable);
    }

    public void enable(final Runnable action) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (action != null) {
                    action.run();
                }
                canceled = false;
            }
        };
        untilDone(runnable);
    }
}
