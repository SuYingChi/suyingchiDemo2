package com.ihs.booster.common.asynctask;

import android.content.pm.PackageManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.booster.boost.common.viewdata.BoostApp;

import java.util.List;

/**
 * Created by sharp on 15/8/10.
 */
public abstract class MBAsyncTask extends BoostAsyncTask<Void, BoostProgress, List<BoostApp>> {
    protected int mAppCount = 0;
    protected OnProcessListener mOnProcessListener;
    protected long mDataSize = 0;
    protected PackageManager packageManager;

    public MBAsyncTask(OnProcessListener actionListener) {
        packageManager = HSApplication.getContext().getPackageManager();
        this.mOnProcessListener = actionListener;
    }

    protected abstract List<BoostApp> doInBackground(Void... params);

    @Override
    protected void onProgressUpdate(BoostProgress... taskProces) {
        if (mOnProcessListener != null) {
            mOnProcessListener.onProgressUpdated(taskProces[0]);
        }
    }

    @Override
    protected void onPostExecute(List<BoostApp> result) {
        if (mOnProcessListener != null) {
            mOnProcessListener.onCompleted(result);
        }
    }

    @Override
    protected void onPreExecute() {
        if (mOnProcessListener != null) {
            mOnProcessListener.onStarted();
        }
    }

    public interface OnProcessListener {
        void onStarted();

        void onProgressUpdated(BoostProgress boostProgress);

        void onCompleted(List<BoostApp> apps);
    }
}
