package com.ihs.inputmethod.mydownload.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.stickers.adapter.StickerAdapter;
import com.ihs.inputmethod.stickers.model.StickerModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDownloadManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class MyStickerFragment extends Fragment {
    private RecyclerView recyclerView;
    private StickerAdapter stickerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mysticker, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        initView();
        return view;
    }

    private void initView() {
        stickerAdapter = new StickerAdapter(getActivity(), null);
        stickerAdapter.setDataList(getData());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(stickerAdapter);
    }

    private List<StickerModel> getData() {
        List<StickerModel> stickerModelList = new ArrayList<>();

        List<String> downloadStickerNameList = StickerDownloadManager.getInstance().getDownloadedStickerFileList();
        if (downloadStickerNameList != null) {
            List<StickerGroup> stickerGroupList = StickerDataManager.getInstance().getStickerGroupList();
            for (int i = 0; i < downloadStickerNameList.size(); i++) {
                String downloadStickerName = downloadStickerNameList.get(i);
                if (!TextUtils.isEmpty(downloadStickerName)) {
                    for (StickerGroup stickerGroup : stickerGroupList) {
                        if (downloadStickerName.equals(stickerGroup.getStickerGroupName())) {
                            StickerModel stickerHomeModel = new StickerModel();
                            stickerHomeModel.stickerGroup = stickerGroup;
                            stickerHomeModel.isDownloaded = true;
                            stickerModelList.add(stickerHomeModel);
                            break;
                        }
                    }
                }
            }
        }
        return stickerModelList;
    }
}
