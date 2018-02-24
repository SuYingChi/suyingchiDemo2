package com.ihs.inputmethod.uimodules.ui.sticker;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.CommonDownloadManager;
import com.ihs.inputmethod.uimodules.utils.DownloadUtils;
import com.ihs.inputmethod.uimodules.widget.ProgressButton;
import com.kc.utils.KCAnalytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import static com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils.STICKER_DOWNLOAD_ZIP_SUFFIX;

public class StoreStickerDetailActivity extends HSAppCompatActivity {
    public static final int RESULT_CODE_SUCCESS = 1;
    public static final int RESULT_CODE_FAILED = 0;
    public static final String STICKER_GROUP_BUNDLE = "sticker";

    private static final int STICKER_SPAN_COUNT = 4;
    private StickerGroup stickerGroup;
    private ImageView stickerDetailImage;
    private ProgressButton stickerDownloadButton;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .showImageOnFail(null)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true).build();

    private DownloadItem downloadItem;
    private CommonDownloadManager.OnDownloadUpdateListener listener = new CommonDownloadManager.OnDownloadUpdateListener() {
        @Override
        public void onDownloadStart(DownloadItem downloadItem) {

        }

        @Override
        public void onDownloadProgressUpdate(DownloadItem downloadItem, float percent) {
            stickerDownloadButton.setProgress(percent);
        }

        @Override
        public void onDownloadSuccess(DownloadItem downloadItem, long downloadTime) {

        }

        @Override
        public void onDownloadFailure(DownloadItem downloadItem) {
            setDownloadButton();
        }

        @Override
        public void onUncompressSuccess(DownloadItem downloadItem) {
            setDownloadButton();
        }

        @Override
        public void onUncompressFailure(DownloadItem downloadItem) {
            setDownloadButton();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        stickerGroup = getIntent().getParcelableExtra(STICKER_GROUP_BUNDLE);
        downloadItem = DownloadUtils.createStickerGroupDownloadItem(stickerGroup.getStickerGroupName());

        setContentView(R.layout.activity_store_sticker_detail);

        stickerDetailImage = (ImageView) findViewById(R.id.sticker_detail_preview_iv);
        ImageLoader.getInstance().displayImage(stickerGroup.getDetailPreviewUrl(),
                new ImageViewAware(stickerDetailImage), displayImageOptions);
        TextView stickerGroupName = (TextView) findViewById(R.id.sticker_group_name_tv);
        stickerGroupName.setText(stickerGroup.getDownloadDisplayName());
        if (stickerGroupName.length() > 11) { // 超过一定字数 减少字体大小防止和下载按钮冲突
            int size = (int) stickerGroupName.getTextSize() - 8;
            stickerGroupName.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
        String stickerCount = stickerGroup.getShowCount() + " " + getResources().getString(R.string.stickers);
        TextView stickerGroupCount = (TextView) findViewById(R.id.sticker_group_count_tv);
        stickerGroupCount.setText(stickerCount);

        stickerDownloadButton = findViewById(R.id.store_sticker_download_btn);
        stickerDownloadButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (stickerDownloadButton.getWidth() > 0 && stickerDownloadButton.getHeight() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        stickerDownloadButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    CommonDownloadManager.getInstance().addOnDownloadUpdateListener(downloadItem, listener);

                }
            }
        });
        setDownloadButton();

        ImageView backButton = (ImageView) findViewById(R.id.store_sticker_detail_back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView unlockImage = findViewById(R.id.sticker_detail_unlock_iv);

        unlockImage.setVisibility(View.GONE);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.store_sticker_detail_rv);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getBaseContext(), STICKER_SPAN_COUNT);
        recyclerView.setLayoutManager(gridLayoutManager); //这样做 Lollipop系统及以上 ScrollView才有惯性滑动效果
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.store_sticker_grid_item_spacing);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels, STICKER_SPAN_COUNT));
        StoreStickerDetailAdapter storeStickerDetailAdapter = new StoreStickerDetailAdapter(stickerGroup);
        recyclerView.setAdapter(storeStickerDetailAdapter);

        PreviewImageView previewImageView = new PreviewImageView(this, recyclerView, stickerGroup);
        recyclerView.setOnTouchListener(previewImageView);

        CameraNewMarkUtil.setElementVisited(CameraNewMarkUtil.TAG_STICKER, stickerGroup.getStickerGroupName());
        setResult(RESULT_CODE_FAILED);
    }

    private void setDownloadButton() {
        int color = getResources().getColor(R.color.charging_screen_alert_negative_action);
        stickerDownloadButton.initState();
        if (stickerGroup.isStickerGroupDownloaded()) {
            stickerDownloadButton.setText(getString(R.string.my_theme_downloaded_theme_title));
            stickerDownloadButton.setBackgroundColor(color);
            stickerDownloadButton.setEnabled(false);
        } else {
            stickerDownloadButton.setText(getString(R.string.download_capital));
        }
        stickerDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadStickerGroup();
            }
        });
    }

    private void downloadStickerGroup() {
        final String stickerGroupDownloadedFilePath = StickerUtils.getStickerFolderPath(stickerGroup.getStickerGroupName()) + STICKER_DOWNLOAD_ZIP_SUFFIX;
        KCAnalytics.logEvent("sticker_download_clicked", "stickerGroupName", stickerGroup.getStickerGroupName(), "form", "detail");
        com.ihs.inputmethod.utils.DownloadUtils.getInstance().startForegroundDownloading(this, stickerGroup.getStickerGroupName(),
                stickerGroupDownloadedFilePath, stickerGroup.getStickerGroupDownloadUri(),
                stickerDetailImage != null ? stickerDetailImage.getDrawable() : null, (success, manually) -> {
                    if (success) {
                        KCAnalytics.logEvent("sticker_download_succeed", "stickerGroupName", stickerGroup.getStickerGroupName(), "from", "detail");
                        StickerDownloadManager.getInstance().unzipStickerGroup(stickerGroupDownloadedFilePath, stickerGroup);
                        setDownloadButton();
                        setResult(RESULT_CODE_SUCCESS);
                    }
                }, false);
    }


    @Override
    protected void onDestroy() {
        CommonDownloadManager.getInstance().removeOnDownloadUpdateListener(downloadItem, listener);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    private class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;
        private int spanCount;
        private int spacingInPixels;

        SpaceItemDecoration(int space, int spanCount) {
            this.space = space;
            this.spanCount = spanCount;
            this.spacingInPixels = DisplayUtils.dip2px(4);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (parent.getChildLayoutPosition(view) / spanCount == 0) {
                outRect.top = 0;
            } else {
                outRect.top = space;
            }
            outRect.left = spacingInPixels;
            outRect.right = spacingInPixels;
        }
    }
}
