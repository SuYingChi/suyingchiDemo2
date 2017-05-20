package com.ihs.inputmethod.feature.common;

import android.animation.Animator;

/**
 * This adapter class provides empty implementations of the methods from {@link Animator.AnimatorListener}.
 * Any custom listener that cares only about a subset of the methods of this listener can
 * simply subclass this adapter class instead of implementing the interface directly.
 *
 * Also, a cancel flag is added.
 */
public abstract class AnimatorListenerAdapter implements Animator.AnimatorListener {

    protected boolean mCancelled;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationCancel(Animator animation) {
        mCancelled = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationEnd(Animator animation) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationStart(Animator animation) {
    }
}
