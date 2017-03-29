package com.ihs.inputmethod.uimodules.widget.videoview;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.manager.PlayerItemChangeListener;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.manager.SingleVideoPlayerManager;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.meta.MetaData;
import com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.ui.VideoPlayerView;

import java.util.HashMap;

/**
 * Created by ihandysoft on 16/12/22.
 */

class HSVideoPlayerManager {

    HashMap<String, SingleVideoPlayerManager> playerManagerHashMap = new HashMap();

    static HSVideoPlayerManager videoPlayerManager;

    private HSVideoPlayerManager() {

    }

    private static HSVideoPlayerManager getInstance() {
        if (videoPlayerManager == null) {
            synchronized (HSVideoPlayerManager.class) {
                if (videoPlayerManager == null) {
                    videoPlayerManager = new HSVideoPlayerManager();
                }
            }
        }
        return videoPlayerManager;
    }

    static void playMedia(final String path, final VideoPlayerView videoPlayerView) {
        SingleVideoPlayerManager singleVideoPlayerManager = getInstance().playerManagerHashMap.get(path);
        if (singleVideoPlayerManager == null) {
            singleVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
                @Override
                public void onPlayerItemChanged(MetaData currentItemMetaData) {
                    HSLog.e("onPlayerItemChanged====" + path);
                }
            });
            getInstance().playerManagerHashMap.put(path, singleVideoPlayerManager);
        }
        if(singleVideoPlayerManager.isInPlaybackState()) {
            singleVideoPlayerManager.pauseAnyPlayback();
        }

        singleVideoPlayerManager.playNewVideo(null, videoPlayerView, path);
    }

    static void pausePlayback(String path) {
        SingleVideoPlayerManager singleVideoPlayerManager = getInstance().playerManagerHashMap.get(path);
        if(singleVideoPlayerManager.isInPlaybackState()) {
            singleVideoPlayerManager.pauseAnyPlayback();
        }
    }
}
