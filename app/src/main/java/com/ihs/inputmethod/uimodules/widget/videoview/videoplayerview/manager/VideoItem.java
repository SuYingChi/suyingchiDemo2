package com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.manager;


import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.meta.MetaData;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.ui.VideoPlayerView;

/**
 * This is basic interface for Items in Adapter of the list. Regardless of is it {@link android.widget.ListView}
 * or {@link android.support.v7.widget.RecyclerView}
 */
public interface VideoItem {
    void playNewVideo(MetaData currentItemMetaData, VideoPlayerView player, VideoPlayerManager<MetaData> videoPlayerManager);
    void stopPlayback(VideoPlayerManager videoPlayerManager);
}
