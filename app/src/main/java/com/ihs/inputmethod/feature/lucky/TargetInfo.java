package com.ihs.inputmethod.feature.lucky;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.ihs.inputmethod.feature.lucky.TargetInfo.Type.BOMB;
import static com.ihs.inputmethod.feature.lucky.TargetInfo.Type.SMALL_BOX;

public class TargetInfo {

    private static final int TARGET_COUNT_PER_ROW = 2;

    private static final int SMALL_BOX_COLOR_RAND_ARRAY_LENGTH = 50;
    private static final int SMALL_BOX_COLOR_RED = 0;
    private static final int SMALL_BOX_COLOR_GREEN = 1;

    private static final float LARGE_BOX_ARM_CLOSE_ANGLE_RADIANS = 0.23f;
    private static final float SMALL_BOX_ARM_CLOSE_ANGLE_RADIANS = 0.30f;
    private static final float BOMB_ARM_CLOSE_ANGLE_RADIANS = 0.48f;

    /** Whether this target is in the middle of catching animation that catches itself. */
    public static final int FLAG_IN_CATCH_ANIMATION = 0x00000001;

    /** Whether this target is already caught and thus should be hidden from game board. */
    public static final int FLAG_CAUGHT = 0x00000002;

    /** Applies only to small boxes. Set for red boxes, not set for green ones. */
    public static final int FLAG_RED_COLOR = 0x00000004;

    private static final @DrawableRes
    int[] PRELOAD_BITMAPS = new int[] {
            R.drawable.large_box_1,
            R.drawable.large_box_2,
            R.drawable.large_box_3,
            R.drawable.large_box_4,
            R.drawable.small_box_red_1,
            R.drawable.small_box_red_2,
            R.drawable.small_box_red_3,
            R.drawable.small_box_red_4,
            R.drawable.small_box_green_1,
            R.drawable.small_box_green_2,
            R.drawable.small_box_green_3,
            R.drawable.small_box_green_4,
            R.drawable.lucky_bomb,
    };

    private static Random sRand = new Random();
    private static List<Integer> sTrackIndices = new ArrayList<>(TARGET_COUNT_PER_ROW);
    private static List<Integer> sSmallBoxColors = new ArrayList<>(SMALL_BOX_COLOR_RAND_ARRAY_LENGTH);
    private static int sSmallBoxColorIndex;
    private static List<Type> sTypes = new ArrayList<>(TARGET_COUNT_PER_ROW);

    @SuppressLint("UseSparseArrays")
    private static Map<Integer, Bitmap> sBitmapCache = new HashMap<>(13);

    public enum Type {
        LARGE_BOX,
        SMALL_BOX,
        BOMB,
    }

    public enum Color {
        UNSPECIFIC,
        GOLDEN,
        RED,
        GREEN,
    }

    // Target property
    public Type type;
    public int trackIndex;
    public int flags;
    int birthTime;

    // Calculated value
    public float translation; // Calculated by birth time and belt translation

    private Context mContext;

    private TargetInfo(Context context) {
        mContext = context;
    }

    static void init(GameConfig config) {
        // Heat up bitmap cache in advance to avoid loading bitmaps during animation
        Context context = HSApplication.getContext();
        for (@DrawableRes int resId : PRELOAD_BITMAPS) {
            getOrDecodeBitmap(context, resId);
        }

        // Initialize a random list for determining small box color faster during animation
        for (int i = 0; i < SMALL_BOX_COLOR_RAND_ARRAY_LENGTH; i++) {
            if (sRand.nextFloat() < config.getSmallBoxRedProbability()) {
                sSmallBoxColors.add(SMALL_BOX_COLOR_RED);
            } else {
                sSmallBoxColors.add(SMALL_BOX_COLOR_GREEN);
            }
        }
    }


    static void release() {
        sBitmapCache.clear();
    }

    static void newTargets(GameConfig config, int birthTime, List<TargetInfo> outAppendList) {
        generateRands(config);
        for (int i = 0; i < TARGET_COUNT_PER_ROW; i++) {
            TargetInfo target = new TargetInfo(HSApplication.getContext());
            target.birthTime = birthTime;
            target.trackIndex = sTrackIndices.get(i);
            target.type = sTypes.get(i);

            if (target.type == SMALL_BOX) {
                if (sSmallBoxColors.get(sSmallBoxColorIndex++) == SMALL_BOX_COLOR_RED) {
                    target.setFlags(FLAG_RED_COLOR, true);
                }
                sSmallBoxColorIndex %= SMALL_BOX_COLOR_RAND_ARRAY_LENGTH;
            }

            outAppendList.add(target);
        }
    }

    private static void generateRands(GameConfig config) {
        sTrackIndices.clear();
        sTypes.clear();

        int trackCount = config.getTrackCount();
        do {
            int rand = sRand.nextInt(trackCount);
            if (!sTrackIndices.contains(rand)) {
                sTrackIndices.add(rand);

                float randPercent = sRand.nextFloat();
                if (randPercent < config.getLargeBoxProbability()) {
                    // Large box
                    sTypes.add(Type.LARGE_BOX);
                } else if (randPercent >= 1f - config.getBombProbability()) {
                    // Bomb
                    sTypes.add(BOMB);
                } else {
                    // Small box
                    sTypes.add(SMALL_BOX);
                }
            }
        } while (sTrackIndices.size() < TARGET_COUNT_PER_ROW);
    }

    public Bitmap getBitmap() {
        // This method is performance-sensitive.
        @DrawableRes int resId = -1;
        switch (type) {
            case LARGE_BOX:
                switch (trackIndex) {
                    case 0:
                        resId = R.drawable.large_box_1;
                        break;
                    case 1:
                        resId = R.drawable.large_box_2;
                        break;
                    case 2:
                        resId = R.drawable.large_box_3;
                        break;
                    case 3:
                        resId = R.drawable.large_box_4;
                        break;
                }
                break;
            case SMALL_BOX:
                if (hasFlag(FLAG_RED_COLOR)) {
                    switch (trackIndex) {
                        case 0:
                            resId = R.drawable.small_box_red_1;
                            break;
                        case 1:
                            resId = R.drawable.small_box_red_2;
                            break;
                        case 2:
                            resId = R.drawable.small_box_red_3;
                            break;
                        case 3:
                            resId = R.drawable.small_box_red_4;
                            break;
                    }
                } else {
                    switch (trackIndex) {
                        case 0:
                            resId = R.drawable.small_box_green_1;
                            break;
                        case 1:
                            resId = R.drawable.small_box_green_2;
                            break;
                        case 2:
                            resId = R.drawable.small_box_green_3;
                            break;
                        case 3:
                            resId = R.drawable.small_box_green_4;
                            break;
                    }
                }
                break;
            case BOMB:
                resId = R.drawable.lucky_bomb;
                break;
        }
        return getOrDecodeBitmap(mContext, resId);
    }

    /**
     * Get the angle a mechanical arm has to close to catch this target, in radians.
     */
    public float getArmCloseAngleRadians() {
        switch (type) {
            case LARGE_BOX:
                return LARGE_BOX_ARM_CLOSE_ANGLE_RADIANS;
            case SMALL_BOX:
                return SMALL_BOX_ARM_CLOSE_ANGLE_RADIANS;
            case BOMB:
                return BOMB_ARM_CLOSE_ANGLE_RADIANS;
        }
        return 0f;
    }

    private static Bitmap getOrDecodeBitmap(Context context, @DrawableRes int resId) {
        Bitmap bitmap = sBitmapCache.get(resId);
        if (bitmap == null) {
            Resources res = context.getResources();
            bitmap = BitmapFactory.decodeResource(res, resId);
            sBitmapCache.put(resId, bitmap);
        }
        return bitmap;
    }

    void setFlags(int flags, boolean enabled) {
        if (enabled) {
            this.flags |= flags;
        } else {
            this.flags &= ~flags;
        }
    }

    public boolean hasFlag(int flags) {
        return (this.flags & flags) != 0;
    }
}
