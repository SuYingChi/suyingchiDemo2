package com.ihs.inputmethod.uimodules.mediacontroller.converts;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.uimodules.mediacontroller.ISequenceFramesImageItem;

/**
 * Created by ihandysoft on 16/4/12.
 */
public class SyncWorkHandler {

    private static SyncWorkHandler mSyncWorkHandler;

    private Handler mHandler;

    private static boolean isRunning;

    /**
     * the sub thread with looper
     */
    private HandlerThread mHandlerThread;

    public synchronized static SyncWorkHandler getInstance(){
        if(mSyncWorkHandler == null){
            mSyncWorkHandler = new SyncWorkHandler("WorkThread");
        }
        return mSyncWorkHandler;
    }

    private SyncWorkHandler(String name){
        this.mHandlerThread = new HandlerThread(name);
        mHandlerThread.start();
        this.mHandler = new Handler(mHandlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return true;
            }
        });
    }

    /**
     * generate mp4 or gif
     */
    public static abstract class MediaConvertRunnable implements Runnable{

        private ISequenceFramesImageItem mSequnceFramesImage;
        private String          mFaceName;
        private String          mFormat;
        private boolean         mShare;

        private INotificationObserver mImeActionObserver = new INotificationObserver() {
            @Override
            public void onReceive(String eventName, HSBundle notificaiton) {
                mShare = false;
                onInterrupted();
            }
        };

        public MediaConvertRunnable(final ISequenceFramesImageItem sticker, final String faceName, String format){
            this.mSequnceFramesImage = sticker;
            this.mFaceName = faceName;
            this.mFormat = format;
            this.mShare = true;

            HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_HIDE_WINDOW, mImeActionObserver);
        }

        @Override
        public void run() {
            isRunning = true;
            try {
                String filePath = ConvertManager.getInstance().convertSequenceFramesImage(mSequnceFramesImage, mFaceName, mFormat).getAbsolutePath();

                if (mShare) {
                    onExecuteFinished(filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            HSGlobalNotificationCenter.removeObserver(mImeActionObserver);

            isRunning = false;
        }

        /**
         * mp4 or gif has generated, then be called
         * @param filePath mp4 or gif file path
         */
        public abstract void onExecuteFinished(String filePath);
        public abstract void onInterrupted();
    }

    /**
     * translate the runnable object to mHandler
     * @param runnable
     */
    public void post(Runnable runnable){
        mHandler.post(runnable);
    }

    public boolean isRunning(){
        return isRunning;
    }

}
