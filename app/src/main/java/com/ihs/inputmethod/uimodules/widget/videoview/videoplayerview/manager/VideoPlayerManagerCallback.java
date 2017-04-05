package com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.manager;


import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.PlayerMessageState;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.meta.MetaData;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.ui.VideoPlayerView;

/**
 * This callback is used by {@link com.volokh.danylo.video_player_manager.player_messages.PlayerMessage}
 * to get and set data it needs
 */
public interface VideoPlayerManagerCallback {

    void setCurrentItem(MetaData currentItemMetaData, VideoPlayerView newPlayerView);

    void setVideoPlayerState(VideoPlayerView videoPlayerView, PlayerMessageState playerMessageState);

    PlayerMessageState getCurrentPlayerState();
}
