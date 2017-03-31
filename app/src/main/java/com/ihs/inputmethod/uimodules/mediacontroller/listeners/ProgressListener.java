package com.ihs.inputmethod.uimodules.mediacontroller.listeners;

/**
 * Created by ihandysoft on 16/6/1.
 */
public interface ProgressListener {

    void startProgress();
    void stopProgress();

    ProgressListener EMPTY_LISTENER = new ProgressListener() {
        @Override
        public void startProgress() {}

        @Override
        public void stopProgress() {}
    };
}
