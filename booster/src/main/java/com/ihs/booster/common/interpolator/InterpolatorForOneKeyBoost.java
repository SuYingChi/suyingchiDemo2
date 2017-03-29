package com.ihs.booster.common.interpolator;

import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by sharp on 15/10/13.
 */
public class InterpolatorForOneKeyBoost extends AccelerateDecelerateInterpolator {

    public InterpolatorForOneKeyBoost() {
        super();
    }

    @Override
    public float getInterpolation(float input) {
        float x = input * 2.0f;
        if (input < 0.5f) {
            return 0.5f * x * x * x * x * x;
        }
        x = (input - 0.5f) * 2 - 1;
        return 0.5f * x * x * x * x * x + 1;
    }

}
