package com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.player_messages;


import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.PlayerMessageState;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.manager.VideoPlayerManagerCallback;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.ui.VideoPlayerView;

/**
 * This PlayerMessage clears MediaPlayer instance that was used inside {@link VideoPlayerView}
 */
public class ClearPlayerInstance extends PlayerMessage {

    public ClearPlayerInstance(VideoPlayerView videoPlayerView, VideoPlayerManagerCallback callback) {
        super(videoPlayerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.clearPlayerInstance();
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.CLEARING_PLAYER_INSTANCE;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.PLAYER_INSTANCE_CLEARED;
    }
}
