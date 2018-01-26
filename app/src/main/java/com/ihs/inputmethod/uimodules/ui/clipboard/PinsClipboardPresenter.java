package com.ihs.inputmethod.uimodules.ui.clipboard;

import com.ihs.inputmethod.uimodules.ui.common.adapter.PinsClipPanelViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.adapter.RecentClipboardAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class PinsClipboardPresenter{



    private  PinClipPanelView pinClipPanelView;
    private PinsClipPanelViewAdapter pinsClipPanelViewAdapter;
    private List<String> pinsData= new ArrayList<String>();
    RecentClipPanelView recentClipPanelView;

    PinsClipboardPresenter(PinClipPanelView pinClipPanelView){
        this.pinClipPanelView = pinClipPanelView;
        pinsData = getPinsClipData();
        recentClipPanelView = ClipPageManager.getInstance().getRecentClipPanelView();
        pinsClipPanelViewAdapter= new PinsClipPanelViewAdapter(pinsData,recentClipPanelView);

    }

    private RecentClipPanelView getRecentClipPanelView() {
        return null;
    }

    private List<String> getPinsClipData() {

        return null;
    }


    public void addDataAndFresh(String itemPinsContent) {
        pinsClipPanelViewAdapter.addDataAndFresh(itemPinsContent);
    }
}
