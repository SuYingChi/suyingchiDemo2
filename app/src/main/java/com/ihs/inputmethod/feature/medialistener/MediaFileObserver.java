package com.ihs.inputmethod.feature.medialistener;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.utils.HSLog;

import java.io.File;

/**
 * Created by Arthur on 18/1/9.
 */

public abstract class MediaFileObserver extends ContentObserver {
    private Context context;
    private String lastFileName = "";


    public MediaFileObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
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

    protected abstract void onMediaFileCreate(String path, String fileName);

    private synchronized void logPic(String path, String fileName) throws Exception {
        if (!fileName.equals(lastFileName)) {
            if (new File(path).lastModified() >= System.currentTimeMillis() - 10000) {
                HSLog.e("take pic", path);
                lastFileName = fileName;
                onMediaFileCreate(path, fileName);
                HSAnalytics.logEvent("picture_capture", "path", new File(path).getParentFile().getName());
            }
        }
    }

    private class MyTask extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... uris) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uris[0], new String[]{
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
