package com.ihs.booster.common.asynctask;

import com.ihs.booster.boost.common.viewdata.BoostApp;

/**
 * Created by sharp on 15/9/7.
 */
public class BoostProgress {
    public long processIndex, total, totalDataSize;
    public BoostApp boostApp;

    public BoostProgress(long processIndex, long total, long totalDataSize, BoostApp boostApp) {
        this.processIndex = processIndex;
        this.total = total;
        this.totalDataSize = totalDataSize;
        this.boostApp = boostApp;
    }
}
