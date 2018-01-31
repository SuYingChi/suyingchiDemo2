package com.ihs.device.clean.junk.cache.nonapp.commonrule;

import java.util.List;
import java.util.Map;

/**
 * Created by Arthur on 2018/1/31.
 */

public class HSCommonFileCacheManager {
    public interface FileCleanTaskListener {
        void onStarted();

        void onProgressUpdated(int var1, int var2, HSCommonFileCache var3);

        void onSucceeded(List<HSCommonFileCache> var1, long var2);

        void onFailed(int var1, String var2);
    }

    public interface FileScanTaskListener extends HSCommonFileCacheManager.FileScanTaskNoProgressListener {
        void onStarted();

        void onProgressUpdated(int var1, int var2, HSCommonFileCache var3);
    }

    public interface FileScanTaskNoProgressListener {
        void onSucceeded(Map<String, List<HSCommonFileCache>> var1, long var2);

        void onFailed(int var1, String var2);
    }
}
