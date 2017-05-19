package com.ihs.inputmethod.feature.boost.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.honeycomb.launcher.compat.Animatable2Compat;
import com.honeycomb.launcher.customize.onetap.OneTapWallpaperIconDrawable;
import com.honeycomb.launcher.desktop.BubbleTextView;
import com.honeycomb.launcher.desktop.Workspace;
import com.honeycomb.launcher.icon.IconCache;
import com.honeycomb.launcher.model.CustomFeatureInfo;
import com.honeycomb.launcher.model.ShortcutInfo;
import com.honeycomb.launcher.settings.icon.IconSize;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;

public class AnimatedBubbleTextView extends BubbleTextView implements Drawable.Callback {

    private Handler mAnimationHandler = new Handler();

    /**
     * This is to record layers in the view hierarchy that does not clip their children.
     */
    private List<ViewParent> mNoClipLayers = new ArrayList<>();

    /**
     * This is to record the root view to notify when this view is redrawn.
     */
    private ViewGroup mRoot;

    private int mFeatureType;

    public AnimatedBubbleTextView(Context context) {
        super(context);
    }

    public AnimatedBubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedBubbleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private Animatable2Compat.AnimationCallback mAnimationCallback = new Animatable2Compat.AnimationCallback() {

        @Override
        public void onAnimationStart(Drawable drawable) {
            super.onAnimationStart(drawable);

            if (clearCallbacksIfNeeded()) {
                return;
            }

            ViewParent parent = getParent();
            mNoClipLayers.clear();

            // Go all the way up through the view hierarchy. This prevents animation effects from being clipped by
            // container's bounds.
            while (parent != null) {
                HSLog.d("ViewHierarchyDebug", "UP: " + parent.getClass().getSimpleName());
                if (parent instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) parent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && !viewGroup.getClipChildren()) {
                        // For JELLY_BEAN_MR1 (API 17), just enable all child clipping on animation end unconditionally,
                        // as getClipChildren() is added in JELLY_BEAN_MR2 (API 18).
                        mNoClipLayers.add(viewGroup);
                    } else {
                        disableClipChildren(viewGroup);
                    }
                }
                parent = parent.getParent();
            }
        }

        @Override
        public void onAnimationEnd(Drawable drawable) {
            super.onAnimationEnd(drawable);

            if (clearCallbacksIfNeeded()) {
                return;
            }

            ViewParent parent = getParent();
            while (parent != null) {
                if (parent instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) parent;
                    if (!mNoClipLayers.contains(viewGroup)) {
                        enableClipChildren(viewGroup);
                    }
                }
                parent = parent.getParent();
            }
            mNoClipLayers.clear();
            mRoot = null;
        }
    };

    private void disableClipChildren(ViewGroup ancestor) {
        ancestor.setClipChildren(false);

        // The last assignment to mRoot leaves the true root
        mRoot = ancestor;
    }

    private void enableClipChildren(ViewGroup ancestor) {
        if (!mNoClipLayers.contains(ancestor)) {
            ancestor.setClipChildren(true);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRoot != null) {
            mRoot.invalidate();
        }
    }

    private boolean clearCallbacksIfNeeded() {
        Workspace workspace = mLauncher.getWorkspace();
        if (workspace == null) {
            HSLog.w("ContextValidation", "Launcher is recreated");
            AnimatedIconDrawable icon = getAnimatedIcon();
            if (icon != null) {
                getAnimatedIcon().clearAnimationCallbacks();
            }
            return true;
        }
        return false;
    }

    @Override
    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache,
                                      boolean promiseStateChanged) {
        CustomFeatureInfo featureInfo = (CustomFeatureInfo) info;
        AnimatedIconDrawable iconDrawable;
        mFeatureType = featureInfo.featureType;
        if (mFeatureType == CustomFeatureInfo.FEATURE_TYPE_BOOST) {
            iconDrawable = new BoostIconDrawable(getContext(), mIconSize);
        } else if (mFeatureType == CustomFeatureInfo.FEATURE_TYPE_ONE_TAP_WALLPAPER) {
            iconDrawable = new OneTapWallpaperIconDrawable(getContext(), mIconSize);
        } else {
            throw new RuntimeException("Shortcut info is not a boost.");
        }

        mLauncher.resizeIconDrawable(iconDrawable);
        setIcon(iconDrawable, mIconSize);
        if (info.contentDescription != null) {
            setContentDescription(info.contentDescription);
        }
        setText(info.title);
        setTag(info);

        if (promiseStateChanged || info.isPromise()) {
            applyState(promiseStateChanged);
        }
    }

    @Override
    public void updateDisplayPosition(DisplayPosition position) {
        mDisplayPosition = position;
        initSize();
        if (mFeatureType == CustomFeatureInfo.FEATURE_TYPE_BOOST) {
            Drawable topDrawable = new BoostIconDrawable(getContext(), mIconSize);
            setIcon(topDrawable);
        } else if (mFeatureType == CustomFeatureInfo.FEATURE_TYPE_ONE_TAP_WALLPAPER) {
            Drawable topDrawable = new OneTapWallpaperIconDrawable(getContext(), mIconSize);
            setIcon(topDrawable);
        }
    }

    @Override
    public void tune() {
        initSize();
        if (mFeatureType == CustomFeatureInfo.FEATURE_TYPE_BOOST) {
            Drawable topDrawable = new BoostIconDrawable(getContext(), mIconSize);
            setIcon(topDrawable);
        } else if (mFeatureType == CustomFeatureInfo.FEATURE_TYPE_ONE_TAP_WALLPAPER) {
            Drawable topDrawable = new OneTapWallpaperIconDrawable(getContext(), mIconSize);
            setIcon(topDrawable);
        }
        setTextColor(IconSize.getIconFontColor());
    }

    @Override
    protected Drawable setIcon(Drawable icon, int iconSize) {
        if (getAnimatedIcon() != null) {
            // Remove registered animation callbacks before setting a new icon
            getAnimatedIcon().unregisterAnimationCallback(mAnimationCallback);
        }
        if (icon instanceof AnimatedIconDrawable) {
            ((AnimatedIconDrawable) icon).registerAnimationCallback(mAnimationCallback);
        }
        return super.setIcon(icon, iconSize);
    }

    public AnimatedIconDrawable getAnimatedIcon() {
        return (AnimatedIconDrawable) getIcon();
    }

    public void startIconAnimation() {
        AnimatedIconDrawable animatableIcon = getAnimatedIcon();
        if (animatableIcon.isRunning()) {
            animatableIcon.stop();
        }
        animatableIcon.start();
    }

    public boolean isAnimating() {
        AnimatedIconDrawable animatedIcon = getAnimatedIcon();
        return !(animatedIcon == null || !animatedIcon.isRunning());
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        if (who == getIcon()) {
            invalidate();
        }
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        mAnimationHandler.postAtTime(what, who, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        mAnimationHandler.removeCallbacksAndMessages(who);
    }
}
