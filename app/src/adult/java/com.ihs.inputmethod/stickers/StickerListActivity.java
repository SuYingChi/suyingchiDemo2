package com.ihs.inputmethod.stickers;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;

import com.ihs.inputmethod.common.ListActivity;
import com.ihs.inputmethod.stickers.adapter.StickerAdapter;
import com.ihs.inputmethod.stickers.model.StickerModel;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jixiang on 18/1/20.
 */

public class StickerListActivity extends ListActivity {
    private StickerAdapter stickerAdapter;

    public static void startThisActivity(Activity activity) {
        activity.startActivity(new Intent(activity, StickerListActivity.class));
    }

    @Override
    protected void initView() {
        stickerAdapter = new StickerAdapter(this);
        stickerAdapter.setDataList(getData());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(stickerAdapter);
    }

    private List<StickerModel> getData() {
        List<StickerModel> stickerModelList = new ArrayList<>();

        List<StickerGroup> stickerGroupList = StickerDataManager.getInstance().getStickerGroupList();
        StickerModel stickerHomeModel;
        for (StickerGroup stickerGroup : stickerGroupList) {
            stickerHomeModel = new StickerModel();
            stickerHomeModel.stickerGroup = stickerGroup;
            stickerModelList.add(stickerHomeModel);
        }
        return stickerModelList;
    }
}
