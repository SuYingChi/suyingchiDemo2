package com.ihs.inputmethod.uimodules.ui.clipboard;

import com.ihs.inputmethod.uimodules.ui.common.adapter.PinsClipPanelViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.adapter.RecentClipboardAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class PinsController implements RecentClipboardAdapter.SaveToPinsManager{



    private  PinClipPanelView pinClipPanelView;
    private PinsClipPanelViewAdapter pinsClipPanelViewAdapter;
    private List<String> pinsData= new ArrayList<String>();

    PinsController(PinClipPanelView pinClipPanelView,PinsClipPanelViewAdapter pinsClipPanelViewAdapter){
        this.pinClipPanelView = pinClipPanelView;
        this.pinsClipPanelViewAdapter = pinsClipPanelViewAdapter;

    }

    @Override
    public void saveToPins(String itemPinsContent) {
        pinsClipPanelViewAdapter.addDataAndFresh(itemPinsContent);
    }
}
