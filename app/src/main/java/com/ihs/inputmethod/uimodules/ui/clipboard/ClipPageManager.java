package com.ihs.inputmethod.uimodules.ui.clipboard;

import com.google.android.exoplayer2.C;

/**
 * Created by yingchi.su on 2018/1/26.
 */

public class ClipPageManager {
   private static ClipPageManager  clipPageManager;
   private PinClipPanelView pinClipPanelView;
   private RecentClipPanelView recentClipPanelView;

   private ClipPageManager(){}

    public synchronized  static ClipPageManager  getInstance(){
        if(clipPageManager == null){
           clipPageManager = new ClipPageManager();
        }
        return clipPageManager;
    }
    public  RecentClipPanelView getRecentClipPanelView() {

        return recentClipPanelView;
    }

    public PinClipPanelView getPinsPanelView() {
        return pinClipPanelView;
    }

    public void setRecentClipPanelView(RecentClipPanelView recentClipPanelView){
        this.recentClipPanelView = recentClipPanelView;
    }
    public void setPinClipPanelView(PinClipPanelView pinClipPanelView){
        this.pinClipPanelView = pinClipPanelView;
    }
}
