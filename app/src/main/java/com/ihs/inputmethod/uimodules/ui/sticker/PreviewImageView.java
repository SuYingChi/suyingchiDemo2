package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
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
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sollian on 2017/7/14.
 */

public class PreviewImageView
        implements View.OnTouchListener {
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
    private StickerGroup stickerGroup;
    private RequestManager requestManager;
    private View lastPressedItem = null;
    private RecyclerView recyclerView;

    public PreviewImageView(Context context, RecyclerView recyclerView, StickerGroup stickerGroup) {
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
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        requestManager = Glide.with(context).applyDefaultRequestOptions(requestOptions);
        this.recyclerView = recyclerView;
    }

    private float firstDownX;
    private float firstDownY;
    private Runnable longClickRunnable;
    private Handler handler = new Handler();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstDownX = event.getX();
                firstDownY = event.getY();
                final View childViewUnder = recyclerView.findChildViewUnder(event.getX(), event.getY());
                longClickRunnable = new Runnable() {
                    @Override
                    public void run() {
                        onItemLongClick(childViewUnder, recyclerView.getChildAdapterPosition(childViewUnder));
                    }
                };
                handler.postDelayed(longClickRunnable, 500);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isLongPressTrigged) {
                    v.getParent().getParent().requestDisallowInterceptTouchEvent(true);
                    float rawX = event.getRawX();
                    float rawY = event.getRawY();

                    int size = gifInfos.size();
                    boolean findTarget = false;
                    for (int i = 0; i < size; i++) {
                        Pair<String, Point> info = gifInfos.get(i);
                        View touchedView = elementList.get(i);
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
                    }
                    if (!findTarget) {
                        pauseGif();
                    }
                    return true;
                } else {
                    if (event.getX() - firstDownX >= 5 || event.getY() - firstDownY >= 5) {
                        handler.removeCallbacks(longClickRunnable);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isLongPressTrigged) {
                    v.getParent().getParent().requestDisallowInterceptTouchEvent(false);
                    hideGif();
                    if (lastPressedItem != null) {
                        lastPressedItem.setPressed(false);
                    }
                    gifInfos.clear();
                    elementList.clear();
                    isLongPressTrigged = false;
                    return true;
                } else {
                    handler.removeCallbacks(longClickRunnable);
                }
                break;
            default:
                break;
        }
        return false;
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
        @SuppressWarnings("StringBufferReplaceableByString") StringBuilder stringBuilder = new StringBuilder(StickerUtils.getStickerDownloadBaseUrl()).append(stickerGroupName).append("/").append(stickerGroupName).append("/").append(stickerGroupName).append(stickerImageSerialNumber).append(stickerGroup.getPicFormat());
        return stringBuilder.toString();
    }

    private void showGif(String picUrl, int[] loc) {
        if (vGif == null) {

            vGif = new ImageView(context);
            vGif.setPadding(DisplayUtils.dip2px(5), DisplayUtils.dip2px(5), DisplayUtils.dip2px(5), DisplayUtils.dip2px(15));
            final VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(context.getResources(), R.drawable.sticker_pop_preview_bg, null);
            vGif.setBackgroundDrawable(vectorDrawableCompat);
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
    }

    private void updateLayoutParams(int[] location) {
        layoutParams.y = location[1] - layoutParams.height;
        layoutParams.x = location[0] - (layoutParams.width / 2 - childWidth / 2);
        if (layoutParams.y < 0) {
            layoutParams.y = 0;
        }
        if (layoutParams.x < 0) {
            layoutParams.x = 0;
        }
        if (layoutParams.x > parentWidth - layoutParams.width / 2) {
            layoutParams.x = parentWidth - layoutParams.width / 2;
        }
    }

    private boolean isLongPressTrigged = false;

    public void onItemLongClick(View view, int position) {
        isLongPressTrigged = true;
        lastPressedItem = view;
        lastPressedItem.setPressed(true);
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);

        recyclerView.getLocationOnScreen(gvLoc);
        parentWidth = recyclerView.getWidth();
        childWidth = view.getWidth();
        childHeight = view.getHeight();
        layoutParams.width = (int) (childWidth * 1.5);
        layoutParams.height = (int) (childHeight * 1.5 + DisplayUtils.dip2px(10));

        String stickerImageUri = getImageUrl(position);

        showGif(stickerImageUri, loc);

        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        int start = layoutManager.findFirstVisibleItemPosition();
        int end = layoutManager.findLastVisibleItemPosition();
        for (int i = 0; i <= end - start; i++) {
            View v = recyclerView.getChildAt(i);
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