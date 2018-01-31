package com.ihs.device.clean.memory;

import java.util.List;

/**
 * Created by Arthur on 2018/1/31.
 */

public class HSAppMemoryManager {
    public interface MemoryTaskListener extends HSAppMemoryManager.MemoryTaskNoProgressListener {
        void onStarted();

        void onProgressUpdated(int var1, int var2, HSAppMemory var3);
    }

    public interface MemoryTaskNoProgressListener {
        void onSucceeded(List<HSAppMemory> var1, long var2);

        void onFailed(int var1, String var2);
    }
}
