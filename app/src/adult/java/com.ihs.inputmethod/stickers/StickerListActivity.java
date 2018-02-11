package com.ihs.inputmethod.stickers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.GridLayoutManager;

import com.ihs.inputmethod.base.BaseListActivity;
import com.ihs.inputmethod.stickers.adapter.StickerAdapter;
import com.ihs.inputmethod.stickers.model.StickerModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDownloadManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils;
import com.ihs.inputmethod.utils.DownloadUtils;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.kc.utils.KCAnalytics;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import static com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils.STICKER_DOWNLOAD_ZIP_SUFFIX;

/**
 * Created by jixiang on 18/1/20.
 */

public class StickerListActivity extends BaseListActivity implements StickerAdapter.OnStickerClickListener {
    private StickerAdapter stickerAdapter;
    private List<StickerModel> stickerModelList;

    public static void startThisActivity(Activity activity) {
        activity.startActivity(new Intent(activity, StickerListActivity.class));
    }

    @Override
    protected void initView() {
        showDownloadIcon(true);

        stickerAdapter = new StickerAdapter(this, this);
        stickerModelList = getData();
        stickerAdapter.setDataList(stickerModelList);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(stickerAdapter);
    }

    @Override
    protected int getTitleTextResId() {
        return R.string.activity_adult_stickers_title;
    }

    private List<StickerModel> getData() {
        List<StickerModel> stickerModelList = new ArrayList<>();

        List<StickerGroup> stickerGroupList = StickerDataManager.getInstance().getNeedDownloadStickerGroupList();
        StickerModel stickerHomeModel;
        for (StickerGroup stickerGroup : stickerGroupList) {
            stickerHomeModel = new StickerModel();
            stickerHomeModel.stickerGroup = stickerGroup;
            stickerModelList.add(stickerHomeModel);
        }
        return stickerModelList;
    }

    @Override
    public void onStickerClick(int position) {
        StickerGroup stickerGroup = stickerModelList.get(position).stickerGroup;
        final String stickerGroupName = stickerGroup.getStickerGroupName();
        final String stickerGroupDownloadedFilePath = StickerUtils.getStickerFolderPath(stickerGroupName) + STICKER_DOWNLOAD_ZIP_SUFFIX;

        // 移除点击过的new角标
        StickerDataManager.getInstance().removeNewTipOfStickerGroup(stickerGroup);
        stickerAdapter.notifyItemChanged(position);

        DownloadUtils.getInstance().startForegroundDownloading(this, stickerGroupName,
                stickerGroupDownloadedFilePath, stickerGroup.getStickerGroupDownloadUri(),
                new BitmapDrawable(ImageLoader.getInstance().loadImageSync(stickerGroup.getStickerGroupDownloadPreviewImageUri())), new AdLoadingView.OnAdBufferingListener() {
                    @Override
                    public void onDismiss(boolean success, boolean manually) {
                        if (success) {
                            KCAnalytics.logEvent("sticker_download_succeed", "StickerGroupName", stickerGroupName);
                            StickerDownloadManager.getInstance().unzipStickerGroup(stickerGroupDownloadedFilePath, stickerGroup);

                            if (position > 0 && position < stickerModelList.size()) {
                                stickerModelList.remove(position);
                                stickerAdapter.notifyItemRemoved(position);
                            }
                        }
                    }

                });
    }
}
