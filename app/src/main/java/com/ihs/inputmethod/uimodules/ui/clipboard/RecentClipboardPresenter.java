package com.ihs.inputmethod.uimodules.ui.clipboard;
import com.ihs.inputmethod.uimodules.ui.common.adapter.RecentClipboardAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class RecentClipboardPresenter{

    RecentClipPanelView recentClipPanelView;
    RecentClipboardAdapter recentClipboardAdapter;
    List<String> recentClipData = new ArrayList<String>();
    PinClipPanelView pinClipPanelView;
    RecentClipboardPresenter(RecentClipPanelView recentClipPanelView){
       this.recentClipPanelView = recentClipPanelView;
       recentClipData = getRecentClipData();
        pinClipPanelView = ClipPageManager.getInstance().getPinsPanelView();
       recentClipboardAdapter = new RecentClipboardAdapter(recentClipData,pinClipPanelView);
   }

    private List<String> getRecentClipData() {
        return null;
    }


    public void deleteAndFresh(String pinsContentItem) {
         recentClipboardAdapter.deleteAndFresh(pinsContentItem);
    }
}
