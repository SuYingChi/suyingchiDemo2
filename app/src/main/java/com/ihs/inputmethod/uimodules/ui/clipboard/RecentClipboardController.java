package com.ihs.inputmethod.uimodules.ui.clipboard;

import com.ihs.inputmethod.uimodules.ui.common.adapter.PinsClipPanelViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.adapter.RecentClipboardAdapter;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class RecentClipboardController implements PinsClipPanelViewAdapter.DeleteRecenet{

   RecentClipPanelView recentClipPanelView;
    RecentClipboardAdapter recentClipboardAdapter;

    RecentClipboardController(RecentClipPanelView recentClipPanelView,RecentClipboardAdapter recentClipboardAdapter){
       this.recentClipPanelView = recentClipPanelView;
       this.recentClipboardAdapter = recentClipboardAdapter;
   }

    @Override
    public void deletePinsItem(String pinsContentItem) {
        recentClipboardAdapter.deleteAndFresh(pinsContentItem);
    }
}
