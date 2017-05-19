package com.ihs.inputmethod.feature.boost.animation;

import android.graphics.drawable.Drawable;

import com.honeycomb.launcher.compat.Animatable2Compat;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract interface for drawables used in a animated app icon.
 */
public abstract class AnimatedIconDrawable extends Drawable implements Animatable2Compat {

    protected final List<AnimationCallback> mCallbacks = new ArrayList<>();

    @Override
    public void registerAnimationCallback(AnimationCallback callback) {
        synchronized (mCallbacks) {
            mCallbacks.add(callback);
        }
    }

    @Override
    public boolean unregisterAnimationCallback(AnimationCallback callback) {
        synchronized (mCallbacks) {
            return mCallbacks.remove(callback);
        }
    }

    @Override
    public void clearAnimationCallbacks() {
        synchronized (mCallbacks) {
            mCallbacks.clear();
        }
    }
}
