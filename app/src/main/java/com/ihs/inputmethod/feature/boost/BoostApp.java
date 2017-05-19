package com.ihs.inputmethod.feature.boost;

import com.ihs.device.clean.memory.HSAppMemory;

public class BoostApp {

    private long mDataSize;
    private String mPackageName;
    private int mType;

    public BoostApp(HSAppMemory appMemory) {
        this.mPackageName = appMemory.getPackageName();
        this.mDataSize = appMemory.getSize();
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public long getSize() {
        return mDataSize;
    }

    public String getPackageName() {
        return mPackageName;
    }
}
