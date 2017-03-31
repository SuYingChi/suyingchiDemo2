package com.ihs.booster.utils;

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

/**
 * Created by sharp on 16/4/7.
 */
public class ViewUtils {
    public static TextView getTextSizeChangedView(TextView textView) {
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB && VERSION.SDK_INT < VERSION_CODES.KITKAT) {
            String text = textView.getText().toString();
            String DOUBLE_BYTE_WORDJOINER = "\n";
            if (!text.endsWith(DOUBLE_BYTE_WORDJOINER)) {
                textView.append(DOUBLE_BYTE_WORDJOINER);
            }
        }
        return textView;
    }

    public static void rotateView(View view, float angle) {
        view.clearAnimation();
        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
            RotateAnimation animation = new RotateAnimation(angle, angle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(1);
            animation.setFillAfter(true);
            view.startAnimation(animation);
        } else {
            view.setRotation(angle);
        }
    }

    public static float measureText(String text, float textSize) {
        Paint paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setTextSize(textSize);
        return paintLine.measureText(text);
    }

    public static boolean isViewBeCovered(final View view) {
        View currentView = view;
        Rect currentViewRect = new Rect();
        boolean partVisible = currentView.getGlobalVisibleRect(currentViewRect);
        boolean totalHeightVisible = (currentViewRect.bottom - currentViewRect.top) >= view.getMeasuredHeight();
        boolean totalWidthVisible = (currentViewRect.right - currentViewRect.left) >= view.getMeasuredWidth();
        boolean totalViewVisible = partVisible && totalHeightVisible && totalWidthVisible;
        if (!totalViewVisible)//if any part of the view is clipped by any of its parents,return true
        {
            return true;
        }

        while (currentView.getParent() instanceof ViewGroup) {
            ViewGroup currentParent = (ViewGroup) currentView.getParent();
            if (currentParent.getVisibility() != View.VISIBLE)//if the parent of view is not visible,return true
            {
                return true;
            }

            int start = indexOfViewInParent(currentView, currentParent);
            for (int i = start + 1; i < currentParent.getChildCount(); i++) {
                Rect viewRect = new Rect();
                view.getGlobalVisibleRect(viewRect);
                View otherView = currentParent.getChildAt(i);
                Rect otherViewRect = new Rect();
                otherView.getGlobalVisibleRect(otherViewRect);
                if (Rect.intersects(viewRect, otherViewRect))//if view intersects its older brother(covered),return true
                {
                    return true;
                }
            }
            currentView = currentParent;
        }
        return false;
    }

    private static int indexOfViewInParent(View view, ViewGroup parent) {
        int index;
        for (index = 0; index < parent.getChildCount(); index++) {
            if (parent.getChildAt(index) == view) {
                break;
            }
        }
        return index;
    }
}
