package com.ihs.commons.notificationcenter;

import com.ihs.commons.utils.HSBundle;

/**
 * Created by Arthur on 18/1/17.
 */

public interface INotificationObserver {
    public void onReceive(String eventName, HSBundle hsBundle);
}
