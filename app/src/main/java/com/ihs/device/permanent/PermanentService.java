package com.ihs.device.permanent;

import android.app.Notification;

/**
 * Created by Arthur on 2018/1/31.
 */

public class PermanentService {
    public interface PermanentServiceListener {
        public Notification getForegroundNotification();

        public int getNotificationID();

        public void onServiceCreate();
    }
}
