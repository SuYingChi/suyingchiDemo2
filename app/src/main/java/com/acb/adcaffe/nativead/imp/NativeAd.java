package com.acb.adcaffe.nativead.imp;

import com.ihs.app.framework.HSApplication;

/**
 * Created by Arthur on 2018/1/31.
 */

public class NativeAd {
    public String getPackageName() {
        return HSApplication.getContext().getPackageName();
    }
}
