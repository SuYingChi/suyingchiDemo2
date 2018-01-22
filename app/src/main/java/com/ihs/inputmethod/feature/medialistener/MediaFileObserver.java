package com.ihs.inputmethod.feature.medialistener;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.io.File;

/**
 * Created by Arthur on 18/1/9.
 */

public abstract class MediaFileObserver extends ContentObserver {

    protected MediaFileObserver(Handler handler) {
        super(handler);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        MyTask myTask = new MyTask();
        myTask.execute(uri);
        super.onChange(selfChange, uri);
    }

    private static synchronized void logPic(String path, String fileName) throws Exception {
        if (new File(path).lastModified() >= System.currentTimeMillis() - 10000) {
            HSLog.e("take pic", path);
            HSAnalytics.logEvent("picture_capture", "path", new File(path).getParentFile().getName());
        }
    }

    private static class MyTask extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... uris) {
            Cursor cursor = null;
            try {
                cursor = HSApplication.getContext().getContentResolver().query(uris[0], new String[]{
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATA
                }, null, null, null);
                if (cursor != null && cursor.moveToLast()) {
                    int displayNameColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    String fileName = cursor.getString(displayNameColumnIndex);
                    String path = cursor.getString(dataColumnIndex);
                    logPic(path, fileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        }

    }
}
