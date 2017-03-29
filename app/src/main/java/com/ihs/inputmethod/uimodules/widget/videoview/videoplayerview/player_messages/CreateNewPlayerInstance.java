package com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.player_messages;


import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.PlayerMessageState;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.manager.VideoPlayerManagerCallback;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.ui.VideoPlayerView;

/**
 * This PlayerMessage creates new MediaPlayer instance that will be used inside {@link VideoPlayerView}
 */
public class CreateNewPlayerInstance extends PlayerMessage {

    public CreateNewPlayerInstance(VideoPlayerView videoPlayerView, VideoPlayerManagerCallback callback) {
        super(videoPlayerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.createNewPlayerInstance();
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.CREATING_PLAYER_INSTANCE;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.PLAYER_INSTANCE_CREATED;
    }
}
