package com.ihs.inputmethod.uimodules.ui.sticker;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.CommonDownloadManager;
import com.ihs.inputmethod.uimodules.utils.DownloadUtils;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.ihs.inputmethod.uimodules.widget.ProgressButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

public class StoreStickerDetailActivity extends HSAppCompatActivity {
    public static final String STICKER_GROUP_BUNDLE = "sticker";

    private static final int STICKER_SPAN_COUNT = 4;
    private StickerGroup stickerGroup;
    private RecyclerView recyclerView;
    private StoreStickerDetailAdapter storeStickerDetailAdapter;
    private ImageView stickerDetailImage;
    private TextView stickerGroupName;
    private TextView stickerGroupCount;
    private ProgressButton stickerDownloadButton;
    private ImageView backButton;
    private ImageView unlockImage;

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
        super.onCreate(savedInstanceState);
        stickerGroup = getIntent().getParcelableExtra(STICKER_GROUP_BUNDLE);
        downloadItem = DownloadUtils.createStickerGroupDownloadItem(stickerGroup.getStickerGroupName());

        setContentView(R.layout.activity_store_sticker_detail);

        stickerDetailImage = (ImageView) findViewById(R.id.sticker_detail_preview_iv);
        ImageLoader.getInstance().displayImage(stickerGroup.getStickerGroupPreviewImageUri(),
                new ImageViewAware(stickerDetailImage), displayImageOptions);
        stickerGroupName = (TextView) findViewById(R.id.sticker_group_name_tv);
        stickerGroupName.setText(stickerGroup.getDownloadDisplayName());
        if (stickerGroupName.length() > 11) { // 超过一定字数 减少字体大小防止和下载按钮冲突
            int size = (int) stickerGroupName.getTextSize() - 8;
            stickerGroupName.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
        String stickerCount = stickerGroup.getShowCount() + " Stickers";
        stickerGroupCount = (TextView) findViewById(R.id.sticker_group_count_tv);
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
        stickerDownloadButton.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.colorPrimary));
        setDownloadButton();

        backButton = (ImageView) findViewById(R.id.store_sticker_detail_back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        unlockImage = findViewById(R.id.sticker_detail_unlock_iv);

        unlockImage.setVisibility(View.GONE);

        recyclerView = (RecyclerView) findViewById(R.id.store_sticker_detail_rv);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getBaseContext(), STICKER_SPAN_COUNT);
        recyclerView.setLayoutManager(gridLayoutManager); //这样做 Lollipop系统及以上 ScrollView才有惯性滑动效果
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.store_sticker_grid_item_spacing);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels, STICKER_SPAN_COUNT));
        storeStickerDetailAdapter = new StoreStickerDetailAdapter(stickerGroup);
//        recyclerView.setFocusable(false);
        recyclerView.setAdapter(storeStickerDetailAdapter);

        PreviewImageView previewImageView = new PreviewImageView(this,stickerGroup);
        storeStickerDetailAdapter.setOnItemLongClickListener(previewImageView);
        ScrollView scrollView = findViewById(R.id.display_content);
        recyclerView.setOnTouchListener(previewImageView);

        CameraNewMarkUtil.setElementVisited(CameraNewMarkUtil.TAG_STICKER, stickerGroup.getStickerGroupName());
    }

    private void setDownloadButton() {
        int color = getResources().getColor(R.color.colorPrimary);
        int radius = getResources().getDimensionPixelSize(R.dimen.corner_radius);
        stickerDownloadButton.initState();
        if (stickerGroup.isStickerGroupDownloaded()) {
            stickerDownloadButton.setTextColor(Color.WHITE);
            stickerDownloadButton.setText(getString(R.string.apply_btn));
            stickerDownloadButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(color, radius));
        } else {
            stickerDownloadButton.setTextColor(color);
            stickerDownloadButton.setText(getString(R.string.download_capital));
            stickerDownloadButton.setBackgroundDrawable(RippleDrawableUtils.getTransparentButtonBackgroundDrawable(color, radius));
        }
        stickerDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadStickerGroup();
            }
        });
    }

    private void downloadStickerGroup() {
        DownloadUtils.startDownloadForeground(StoreStickerDetailActivity.this,
                DownloadUtils.createStickerGroupDownloadItem(stickerGroup.getStickerGroupName()),
                stickerDetailImage != null ? stickerDetailImage.getDrawable() : null,
                new DownloadUtils.OnDownloadAlertListener() {
                    @Override
                    public void onUnlockActionClicked() {
                        if (unlockImage != null && unlockImage.getVisibility() == View.VISIBLE) {
                            unlockImage.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAlertShow() {

                    }

                    @Override
                    public void onDismiss(boolean success) {

                    }
                }, "details", true);
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
