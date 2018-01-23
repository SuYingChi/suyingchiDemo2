package com.ihs.inputmethod.feature.medialistener;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;

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

    private static void logPic(String path) {
        File file = new File(path);
        //这里判断下是否文件是新创建的
        if (file.lastModified() >= System.currentTimeMillis() - 10000) {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                HSAnalytics.logEvent("picture_capture", "path", parentFile.getName());
            }
        }
    }

    private static class MyTask extends AsyncTask<Uri, Void, String> {

        @Override
        protected String doInBackground(Uri... uris) {
            String path = "";
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
                    path = cursor.getString(dataColumnIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return path;
        }

        @Override
        protected void onPostExecute(String path) {
            logPic(path);
        }
    }
}
