package com.ihs.inputmethod.feature.lucky;

import com.ihs.commons.config.HSConfig;

public class GameConfig {

    // Layout
    /** Number of tracks. */
    private static final int TRACK_COUNT = 4;

    /** Number of rows of targets in screen. */
    private static final int ROW_COUNT = 3;

    /** Row index (in the direction from far to near) where the arm "hangs over". */
    private static final float ACTIVE_ROW_INDEX = 2.6f;

    private static final int BORDER_LIGHT_COUNT = 13;

    // Dynamics
    private static final float BELT_SPEED = 0.8f; // Row per second
    private static final float ARM_SPEED = 1.6f; // Track per second
    private static final float CATCH_TOLERANCE_HORIZONTAL = 0.7f;
    private static final float CATCH_TOLERANCE_VERTICAL = 0.7f;

    // Randoms
    // 1. Box type
    private float mLargeBoxProb;
    private float mSmallBoxProb;
    private float mBombProb;

    // 2. Color of small box
    private float mSmallBoxRedProb;
    private float mSmallBoxGreenProb;

    // 3. Chances of small box
    private float mSmallBoxChancesProb;
    private float mSmallBoxEmptyProb;

    @SuppressWarnings("unchecked")
    GameConfig() {
        float sum;
        {
            float rawLargeBoxProb = HSConfig.optFloat(1f, "Application", "Lucky", "Types", "LargeBox");
            float rawSmallBoxProb = HSConfig.optFloat(1f, "Application", "Lucky", "Types", "SmallBox");
            float rawBombProb = HSConfig.optFloat(1f, "Application", "Lucky", "Types", "Bomb");
            rawLargeBoxProb = clampToNonNegative(rawLargeBoxProb);
            rawSmallBoxProb = clampToNonNegative(rawSmallBoxProb);
            rawBombProb = clampToNonNegative(rawBombProb);
            sum = rawLargeBoxProb + rawSmallBoxProb + rawBombProb;
            mLargeBoxProb = rawLargeBoxProb / sum;
            mSmallBoxProb = rawSmallBoxProb / sum;
            mBombProb = rawBombProb / sum;
        }
        {
            float rawSmallBoxRedProb = HSConfig.optFloat(1f, "Application", "Lucky", "SmallBoxColor", "Red");
            float rawSmallBoxGreenProb = HSConfig.optFloat(1f, "Application", "Lucky", "SmallBoxColor", "Green");
            rawSmallBoxRedProb = clampToNonNegative(rawSmallBoxRedProb);
            rawSmallBoxGreenProb = clampToNonNegative(rawSmallBoxGreenProb);
            sum = rawSmallBoxRedProb + rawSmallBoxGreenProb;
            mSmallBoxRedProb = rawSmallBoxRedProb / sum;
            mSmallBoxGreenProb = rawSmallBoxGreenProb / sum;
        }

        {
            float rawSmallBoxChancesProb = HSConfig.optFloat(1f, "Application", "Lucky", "SmallBoxContent", "Chances");
            float rawSmallBoxEmptyProb = HSConfig.optFloat(1f, "Application", "Lucky", "SmallBoxContent", "Empty");
            rawSmallBoxChancesProb = clampToNonNegative(rawSmallBoxChancesProb);
            rawSmallBoxEmptyProb = clampToNonNegative(rawSmallBoxEmptyProb);
            sum = rawSmallBoxChancesProb + rawSmallBoxChancesProb;
            mSmallBoxChancesProb = rawSmallBoxChancesProb / sum;
            mSmallBoxEmptyProb = rawSmallBoxEmptyProb / sum;
        }
    }

    private float clampToNonNegative(float raw) {
        return raw < 0f ? 0f : raw;
    }

    public int getTrackCount() {
        return TRACK_COUNT;
    }

    public int getRowCount() {
        return ROW_COUNT;
    }

    public float getActiveRowIndex() {
        return ACTIVE_ROW_INDEX;
    }

    public int getBorderLightCount() {
        return BORDER_LIGHT_COUNT;
    }

    float getBeltSpeed() {
        return BELT_SPEED;
    }

    float getArmSpeed() {
        return ARM_SPEED;
    }

    public float getCatchToleranceHorizontal() {
        return CATCH_TOLERANCE_HORIZONTAL;
    }

    public float getCatchToleranceVertical() {
        return CATCH_TOLERANCE_VERTICAL;
    }

    public float getSmallBoxChancesProbability() {
        return mSmallBoxChancesProb;
    }

    float getLargeBoxProbability() {
        return mLargeBoxProb;
    }

    @SuppressWarnings("unused")
    float getSmallBoxProbability() {
        return mSmallBoxProb;
    }

    float getBombProbability() {
        return mBombProb;
    }

    float getSmallBoxRedProbability() {
        return mSmallBoxRedProb;
    }

    @SuppressWarnings("unused")
    float getSmallBoxGreenProbability() {
        return mSmallBoxGreenProb;
    }

    float getSmallBoxEmptyProbability() {
        return mSmallBoxEmptyProb;
    }

}
