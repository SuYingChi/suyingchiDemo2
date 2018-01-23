package com.ihs.inputmethod.stickers;

import android.support.v7.widget.GridLayoutManager;

import com.ihs.inputmethod.common.ListActivity;
import com.ihs.inputmethod.stickers.adapter.StickerAdapter;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;

/**
 * Created by jixiang on 18/1/20.
 */

public class StickerListActivity extends ListActivity {
    private StickerAdapter stickerAdapter;

    @Override
    protected void initView() {
        stickerAdapter = new StickerAdapter(this);
        stickerAdapter.setDataList(StickerDataManager.getInstance().getStickerGroupList());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(stickerAdapter);
    }
}
