package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by yanxia on 2017/4/5.
 */

public class NativeAdViewButtonHelper {

    private static final int MSG_CHANGE_AD_BUTTON_BACKGROUND_NEW_COLOR = 1;
    private static final int MSG_CHANGE_AD_BUTTON_BACKGROUND_ORIGIN_COLOR = 2;

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            NativeAdView nativeAdView = (NativeAdView) msg.obj;
            switch (msg.what) {
                case MSG_CHANGE_AD_BUTTON_BACKGROUND_NEW_COLOR:
                    if (null != nativeAdView && nativeAdView.isAdLoaded()) {
                        TextView adButtonView = (TextView) nativeAdView.findViewById(R.id.ad_call_to_action);
                        if (null != adButtonView) {
                            adButtonView.getBackground().setColorFilter(nativeAdView.getContext().getResources().getColor(R.color.ad_button_green_state), PorterDuff.Mode.SRC_ATOP);
                        }
                    }
                    break;
                case MSG_CHANGE_AD_BUTTON_BACKGROUND_ORIGIN_COLOR:
                    if (null != nativeAdView && nativeAdView.isAdLoaded()) {
                        TextView adButtonView = (TextView) nativeAdView.findViewById(R.id.ad_call_to_action);
                        if (null != adButtonView) {
                            adButtonView.getBackground().setColorFilter(nativeAdView.getContext().getResources().getColor(R.color.ad_button_blue), PorterDuff.Mode.SRC_ATOP);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static void autoHighlight(final NativeAdView nativeAdView) {
        Point p = new Point();
        WindowManager wm = (WindowManager) nativeAdView.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(p);
        final Rect screenRect = new Rect(0, 0, p.x, p.y);
        final ViewTreeObserver.OnScrollChangedListener onScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                onViewEnvironmentChanged(nativeAdView, screenRect);
            }
        };
        final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                onViewEnvironmentChanged(nativeAdView, screenRect);
            }
        };
        final View.OnAttachStateChangeListener onAttachStateChangeListener = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                addNativeAdViewListener(nativeAdView, onScrollChangedListener, onGlobalLayoutListener);
                onViewEnvironmentChanged(nativeAdView, screenRect);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                removeNativeAdViewListener(nativeAdView, onScrollChangedListener, onGlobalLayoutListener);
                onViewEnvironmentChanged(nativeAdView, screenRect);
            }
        };
        nativeAdView.addOnAttachStateChangeListener(onAttachStateChangeListener);
    }

    private static void addNativeAdViewListener(NativeAdView nativeAdView, ViewTreeObserver.OnScrollChangedListener onScrollChangedListener, ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        if (null != nativeAdView) {
            nativeAdView.getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
            nativeAdView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    private static void removeNativeAdViewListener(NativeAdView nativeAdView, ViewTreeObserver.OnScrollChangedListener onScrollChangedListener, ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        if (null != nativeAdView) {
            nativeAdView.getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                nativeAdView.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
            } else {
                nativeAdView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            }
        }
    }

    private static Rect getScreenVisibleRect(NativeAdView nativeAdView, Rect screenRect) {
        int[] out = new int[2];
        nativeAdView.getLocationOnScreen(out);
        int left = out[0];
        int top = out[1];
        int right = nativeAdView.getMeasuredWidth() + out[0];
        right = right >= screenRect.right ? screenRect.right : right;
        int bottom = nativeAdView.getMeasuredHeight() + out[1];
        bottom = bottom >= screenRect.bottom ? screenRect.bottom : bottom;
        return new Rect(left, top, right, bottom);
    }

    private static boolean isViewEnvironmentReady(NativeAdView nativeAdView, Rect screenRect) {
        boolean ready;
        ready = nativeAdView.getVisibility() == View.VISIBLE;
        ready = ready && nativeAdView.getWindowVisibility() == View.VISIBLE;
        ready = ready && nativeAdView.isShown();
        ready = ready && !getScreenVisibleRect(nativeAdView, screenRect).isEmpty();
        ready = ready && screenRect.contains(getScreenVisibleRect(nativeAdView, screenRect));
        return ready;
    }

    private static void onViewEnvironmentChanged(NativeAdView nativeAdView, Rect screenRect) {
        boolean ready = isViewEnvironmentReady(nativeAdView, screenRect);
        if (ready) {
            Message message = handler.obtainMessage();
            message.obj = nativeAdView;
            message.what = MSG_CHANGE_AD_BUTTON_BACKGROUND_NEW_COLOR;
            handler.sendMessageDelayed(message, 1500);
        } else {
            Message message = handler.obtainMessage();
            handler.removeMessages(MSG_CHANGE_AD_BUTTON_BACKGROUND_NEW_COLOR, nativeAdView);
            message.obj = nativeAdView;
            message.what = MSG_CHANGE_AD_BUTTON_BACKGROUND_ORIGIN_COLOR;
            handler.sendMessage(message);
        }
    }
}
