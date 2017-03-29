package com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.player_messages;


import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.PlayerMessageState;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.manager.VideoPlayerManagerCallback;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.ui.VideoPlayerView;

/**
 * This is generic PlayerMessage for setDataSource
 */
public abstract class SetDataSourceMessage extends PlayerMessage{

    public SetDataSourceMessage(VideoPlayerView videoPlayerView, VideoPlayerManagerCallback callback) {
        super(videoPlayerView, callback);
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.SETTING_DATA_SOURCE;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.DATA_SOURCE_SET;
    }
}
