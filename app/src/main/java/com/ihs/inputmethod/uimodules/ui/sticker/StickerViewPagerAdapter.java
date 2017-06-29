package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ClickUtils;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.api.utils.HSZipUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.List;
import java.util.zip.ZipException;

/**
 * Created by yanxia on 2017/6/8.
 */

public class StickerViewPagerAdapter extends PagerAdapter {
    private Handler handler = new Handler();
    private View firstView;
    private List<StickerGroup> needDownloadStickerGroupList;
    private LayoutInflater inflater;
    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .showImageOnFail(null)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true).build();

    public StickerViewPagerAdapter(View firstView) {
        inflater = LayoutInflater.from(HSApplication.getContext());
        this.firstView = firstView;
    }

    public void setNeedDownloadStickerGroupList(List<StickerGroup> needDownloadStickerGroupList) {
        if (needDownloadStickerGroupList != null) {
            this.needDownloadStickerGroupList = needDownloadStickerGroupList;
            notifyDataSetChanged();
        }
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return needDownloadStickerGroupList.size() + 1;
    }

    /**
     * Determines whether a page View is associated with a specific key object
     * as returned by {@link #instantiateItem(ViewGroup, int)}. This method is
     * required for a PagerAdapter to function properly.
     *
     * @param view   Page View to check for association with <code>object</code>
     * @param object Object to check for association with <code>view</code>
     * @return true if <code>view</code> is associated with the key object <code>object</code>
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * Remove a page for the given position.  The adapter is responsible
     * for removing the view from its container, although it only must ensure
     * this is done by the time it returns from {@link #finishUpdate(ViewGroup)}.
     *
     * @param container The containing View from which the page will be removed.
     * @param position  The page position to be removed.
     * @param object    The same object that was returned by
     *                  {@link #instantiateItem(View, int)}.
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    /**
     * Create the page for the given position.  The adapter is responsible
     * for adding the view to the container given here, although it only
     * must ensure this is done by the time it returns from
     * {@link #finishUpdate(ViewGroup)}.
     *
     * @param container The containing View in which the page will be shown.
     * @param position  The page position to be instantiated.
     * @return Returns an Object representing the new page.  This does not
     * need to be a View, but can be some other container of the page.
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (position != 0 && !needDownloadStickerGroupList.isEmpty()) {
            StickerDownloadView stickerDownloadView = (StickerDownloadView) inflater.inflate(R.layout.common_sticker_panel_need_download_page, null);
            final StickerGroup stickerGroup = needDownloadStickerGroupList.get(position - 1);
            stickerDownloadView.setStickerGroup(stickerGroup);
            final ImageView sticker_download_preview = (ImageView) stickerDownloadView.findViewById(R.id.sticker_download_preview_image);
            final TextView stickerDownloadShowName = (TextView) stickerDownloadView.findViewById(R.id.sticker_download_show_name);
            stickerDownloadShowName.setText(stickerGroup.getDownloadDisplayName());
            ImageLoader.getInstance().displayImage(stickerGroup.getStickerGroupDownloadPreviewImageUri(), new ImageViewAware(sticker_download_preview), displayImageOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    int padding = HSDisplayUtils.dip2px(40);
                    view.setPadding(padding, padding, padding, padding);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    view.setPadding(0, 0, 0, 0);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
            final String stickerCategoryZipFilePath = StickerUtils.getStickerRootFolderPath() + "/" + stickerGroup.getStickerGroupName() + StickerUtils.STICKER_DOWNLOAD_ZIP_SUFFIX;
            TextView downloadButton = (TextView) stickerDownloadView.findViewById(R.id.sticker_download_button);
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ClickUtils.isFastDoubleClick()) {
                        return;
                    }
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("sticker_download_clicked", stickerGroup.getStickerGroupName());
                    final AdLoadingView adLoadingView = new AdLoadingView(HSApplication.getContext());
                    Resources resources = HSApplication.getContext().getResources();
                    Drawable thumbnailDrawable = sticker_download_preview.getDrawable();
                    adLoadingView.configParams(null, thumbnailDrawable != null ? thumbnailDrawable : resources.getDrawable(R.drawable.ic_sticker_loading_image),
                            resources.getString(R.string.sticker_downloading_label),
                            resources.getString(R.string.sticker_downloading_successful),
                            resources.getString(R.string.ad_placement_lucky),
                            new AdLoadingView.OnAdBufferingListener() {
                                @Override
                                public void onDismiss(boolean downloadSuccess) {
                                    if (downloadSuccess) {
                                        if (stickerGroup.isStickerGroupDownloaded()) {
                                            HSLog.d("sticker " + stickerGroup.getStickerGroupName() + " download succeed");
                                        } else {
                                            HSLog.e("sticker " + stickerGroup.getStickerGroupName() + " download error!");
                                        }
                                    } else {
                                        // 没下载成功
                                        HSHttpConnection connection = (HSHttpConnection) adLoadingView.getTag();
                                        if (connection != null) {
                                            connection.cancel();
                                            HSFileUtils.delete(new File(stickerCategoryZipFilePath));
                                        }
                                    }
                                }
                            }, 2000, false);
                    adLoadingView.showInDialog();

                    HSHttpConnection connection = new HSHttpConnection(stickerGroup.getStickerGroupDownloadUri());
                    connection.setDownloadFile(HSFileUtils.createNewFile(stickerCategoryZipFilePath));
                    connection.setDataReceivedListener(new HSHttpConnection.OnDataReceivedListener() {
                        @Override
                        public void onDataReceived(HSHttpConnection hsHttpConnection, byte[] bytes, long received, long totalSize) {
                            if (totalSize > 0) {
                                final float percent = (float) received * 100 / totalSize;
                                if (received >= totalSize) {
                                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("sticker_download_succeed", stickerGroup.getStickerGroupName());
                                    unzipStickerGroup(stickerCategoryZipFilePath, stickerGroup);
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        adLoadingView.updateProgressPercent((int) percent);
                                    }
                                });
                            }
                        }
                    });
                    connection.startAsync();
                    adLoadingView.setTag(connection);
                }
            });
            container.addView(stickerDownloadView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return stickerDownloadView;
        } else {
            container.addView(firstView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return firstView;
        }
    }

    private void unzipStickerGroup(String stickerCategoryZipFilePath, StickerGroup stickerGroup) {
        try {
            // 下载成功 先解压好下载的zip
            HSZipUtils.unzip(new File(stickerCategoryZipFilePath), new File(StickerUtils.getStickerRootFolderPath()));
            StickerDataManager.getInstance().updateStickerGroupList(stickerGroup);
        } catch (ZipException e) {
            e.printStackTrace();
            unzipStickerGroup(stickerCategoryZipFilePath, stickerGroup);
        }
    }
}
