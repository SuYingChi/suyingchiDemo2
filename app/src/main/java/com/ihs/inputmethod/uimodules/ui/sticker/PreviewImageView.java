package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.utils.HSConfigUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sollian on 2017/7/14.
 */

public class PreviewImageView
        implements View.OnTouchListener, StoreStickerDetailAdapter.OnItemLongClickListener {
    private static final int VIEW_HEIGHT = DisplayUtils.dip2px(116);
    private static final int VIEW_WIDTH = DisplayUtils.dip2px(106);

    private final Context context;
    private final int[] gvLoc = new int[2];
    private final List<Pair<String, Point>> gifInfos = new ArrayList<>();
    private List<View> elementList = new ArrayList<>();

    private int parentWidth;
    private int childWidth;
    private int childHeight;

    private final WindowManager wm;
    private final WindowManager.LayoutParams layoutParams;

    private ImageView vGif;
    private View parentView;
    private StickerGroup stickerGroup;
    private RequestManager requestManager;
    private View lastPressedItem = null;


    public PreviewImageView(Context context, StickerGroup stickerGroup) {
        this.context = context;
        this.stickerGroup = stickerGroup;
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.format = PixelFormat.TRANSLUCENT;

        layoutParams.gravity = Gravity.START | Gravity.TOP;
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.sticker_store_image_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        requestManager = Glide.with(context).applyDefaultRequestOptions(requestOptions);
    }

    private View touchedView = new View(HSApplication.getContext());

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (vGif == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;

            case MotionEvent.ACTION_MOVE:
                v.getParent().getParent().requestDisallowInterceptTouchEvent(true);
                float rawX = event.getRawX();
                float rawY = event.getRawY();

                int size = gifInfos.size();
                boolean findTarget = false;
                for (int i = 0; i < size; i++) {
                    Pair<String, Point> info = gifInfos.get(i);
                    touchedView = elementList.get(i);
                    Point loc = info.second;
                    if (rawX >= loc.x && rawX <= loc.x + childWidth
                            && rawY > loc.y && rawY < loc.y + childHeight) {
                        if (info.first.equals(vGif.getTag())) {
                            // 如果是正在播放的gif则返回
                            return true;
                        }

                        findTarget = true;
                        updateGif(info.first, new int[]{info.second.x, info.second.y});
                        touchedView.setPressed(true);
                        if (lastPressedItem != null) {
                            lastPressedItem.setPressed(false);
                        }
                        lastPressedItem = touchedView;
                        break;
                    }
                    touchedView.setPressed(false);
                }
                if (!findTarget) {
                    pauseGif();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                v.getParent().getParent().requestDisallowInterceptTouchEvent(false);
                hideGif();
                touchedView.setPressed(false);
                gifInfos.clear();
                elementList.clear();
                break;
            default:
                break;
        }
        return true;
    }

    @NonNull
    private String getImageUrl(int position) {
        String stickerGroupName = stickerGroup.getStickerGroupName();
        String stickerImageSerialNumber;
        if (position < 10) {
            stickerImageSerialNumber = "-0" + position; // -00, -01, -02...
        } else {
            stickerImageSerialNumber = "-" + position;
        }
        @SuppressWarnings("StringBufferReplaceableByString") StringBuilder stringBuilder = new StringBuilder(HSConfigUtils.getRemoteContentDownloadURL()).append(StickerGroup.STICKER_REMOTE_ROOT_DIR_NAME)
                .append("/").append(stickerGroupName).append("/").append(stickerGroupName).append("/").append(stickerGroupName).append(stickerImageSerialNumber).append(stickerGroup.getPicFormat());
        return stringBuilder.toString();
    }

    private void showGif(String picUrl, int[] loc) {
        if (vGif == null) {
            vGif = new ImageView(context);
            vGif.setPadding(0, 0, 0, DisplayUtils.dip2px(10));
            final VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(context.getResources(), R.drawable.sticker_pop_preview_bg, null);
            vGif.setBackgroundDrawable(vectorDrawableCompat);
//            vGif.setBackgroundResource(R.drawable.sticker_pop_preview_bg);
        }
        updateLayoutParams(loc);
        if (vGif.getParent() != null) {
            wm.removeView(vGif);
        }
        wm.addView(vGif, layoutParams);
        vGif.setTag(null);
        requestManager.load(picUrl).into(vGif);
        vGif.setTag(picUrl);
    }

    private void pauseGif() {
        if (vGif.getParent() != null) {
            wm.removeView(vGif);
        }
        vGif.setTag(null);
    }

    private void updateGif(String picUrl, int[] loc) {
        updateLayoutParams(loc);
        if (vGif.getParent() == null) {
            wm.addView(vGif, layoutParams);
        } else {
            wm.updateViewLayout(vGif, layoutParams);
        }
        vGif.setTag(null);
        requestManager.load(picUrl).into(vGif);
        vGif.setTag(picUrl);
    }

    private void hideGif() {
        if (vGif.getParent() != null) {
            wm.removeView(vGif);
        }
        vGif.setTag(null);
        vGif = null;
        parentView = null;
    }

    private void updateLayoutParams(int[] location) {
        layoutParams.y = location[1] - layoutParams.height;
        layoutParams.x = location[0];
        if (layoutParams.y < 0) {
            layoutParams.y = 0;
        }
        if (layoutParams.x < 0) {
            layoutParams.x = 0;
        }
        if (layoutParams.x > parentWidth - VIEW_WIDTH / 2) {
            layoutParams.x = parentWidth - VIEW_WIDTH / 2;
        }
    }

    @Override
    public void onItemLongClick(RecyclerView parent, View view, int position) {

        int[] loc = new int[2];
        view.getLocationOnScreen(loc);

        parentView = parent;
        parent.getLocationOnScreen(gvLoc);
        parentWidth = parent.getWidth();
        childWidth = view.getWidth();
        childHeight = view.getHeight();
        layoutParams.width = childWidth;
        layoutParams.height = childHeight + DisplayUtils.dip2px(10);

        String stickerImageUri = getImageUrl(position);

        showGif(stickerImageUri, loc);

        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        int start = layoutManager.findFirstVisibleItemPosition();
        int end = layoutManager.findLastVisibleItemPosition();
        for (int i = 0; i <= end - start; i++) {
            View v = parent.getChildAt(i);
            if (null == v) {
                continue;
            }
            v.getLocationOnScreen(loc);
            String rid = getImageUrl(i);
            Point point = new Point(loc[0], loc[1]);
            Pair<String, Point> pair = new Pair<>(rid, point);
            gifInfos.add(pair);
            elementList.add(v);
        }
    }
}