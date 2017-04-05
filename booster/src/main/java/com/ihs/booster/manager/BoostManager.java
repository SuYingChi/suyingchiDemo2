package com.ihs.booster.manager;

import com.ihs.booster.boost.common.PrefsUtils;
import com.ihs.booster.constants.AnimationConstant;

/**
 * Created by sharp on 15/8/19.
 */
@Deprecated
public class BoostManager {
    private static BoostManager instance;

    public synchronized static BoostManager getInstance() {
        if (instance == null) {
            instance = new BoostManager();
        }
        return instance;
    }

    private BoostManager() {
    }

    public int[] getCleanAnimColorArray(Function function) {
        int temp = getColorIndex(function);
        int size = AnimationConstant.COLOR_ARRAY.length - temp + 1;
        int[] colorArray = new int[size];
        colorArray[0] = getFunctionColor(function);
        for (int i = 1; i < size; i++) {
            colorArray[i] = AnimationConstant.COLOR_ARRAY[i + temp - 1];
        }
        return colorArray;
    }


    public int getColorIndex(Function function) {
        int index;
        switch (function) {
            case MAIN:
                index = getColorIndex(getSystemScore());
                break;
            case MEMORY:
                index = getColorIndex(getMemoryAndJunkScore());
                break;
            case JUNK:
                index = getColorIndex(getMemoryAndJunkScore());
                break;
            case BATTERY:
                index = 0;
                break;
            default:
                index = 0;
                break;
        }
        return index;
    }

    public enum Function {
        MAIN,
        MEMORY,
        JUNK,
        BATTERY,
        CPU
    }

    private int getColorIndex(float score) {
        if (score < 60) {
            return 0;
        } else if (score < 80) {
            return 2;
        } else if (score < 90) {
            return 5;
        } else if (score < 95) {
            return 10;
        } else {
            return 15;
        }
    }

    public int[] getScanAnimColorArray(Function function) {
        int size = AnimationConstant.COLOR_ARRAY.length - getColorIndex(function) + 1;
        int[] colorArray = new int[size];
        for (int i = 0; i < size - 1; i++) {
            colorArray[i] = AnimationConstant.COLOR_ARRAY[AnimationConstant.COLOR_ARRAY.length - i - 1];
        }
        colorArray[size - 1] = getFunctionColor(function);
        return colorArray;
    }

    //用于计算memory和junk功能块的背景色
    public int getMemoryAndJunkScore() {
        Double scalePoint = 0d;
        int memoryPercent = 40;
        int junkPercent = 100 - memoryPercent;
        boolean isCleanMemoryExpired = PrefsUtils.isLastMemoryCleanedExpired();
        if (!isCleanMemoryExpired) {
            scalePoint = 100d;
        } else {
//            double memory_avail_percent = MBMemoryManager.getInstance().getAvailableMemorySize() / MBMemoryManager.getInstance().getTotalMemorySize();
            scalePoint = (100 - scalePoint) * memoryPercent / 100 + scalePoint;

        }
        return scalePoint.intValue();
    }


    public int getSystemScore() {
        return (int) (MBMemoryManager.getInstance().getMemoryScore());
    }

    public boolean isCleanExpired() {
        return PrefsUtils.isLastMemoryCleanedExpired();
    }

    public int getFunctionColor(Function function) {
        int index = getColorIndex(function);
        return AnimationConstant.COLOR_ARRAY[index];
    }

    public int getSystemColorForNotify(float percent) {
        return AnimationConstant.COLOR_ARRAY[Double.valueOf(getColorIndex(percent)).intValue()];
    }

    public void reset() {
        MBMemoryManager.getInstance().reset();
    }
}
