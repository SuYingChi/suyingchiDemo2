package com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.player_messages;

import android.media.MediaPlayer;

import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.PlayerMessageState;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.manager.VideoPlayerManagerCallback;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.ui.VideoPlayerView;

/**
 * This PlayerMessage calls {@link MediaPlayer#reset()} on the instance that is used inside {@link VideoPlayerView}
 */
public class Reset extends PlayerMessage {
    public Reset(VideoPlayerView videoPlayerView, VideoPlayerManagerCallback callback) {
        super(videoPlayerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.reset();
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.RESETTING;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.RESET;
    }
}
