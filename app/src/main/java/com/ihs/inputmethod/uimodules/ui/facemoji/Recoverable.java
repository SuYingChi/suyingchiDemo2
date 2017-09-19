package com.ihs.inputmethod.uimodules.ui.facemoji;

/**
 * Created by ihs on 16/6/13.
 */
public interface Recoverable {
    enum State {
        Initialized,
        Saved,
        Released,
        Restored
    }
    void save();
    void release();
    void restore();
    State currentState();
}
