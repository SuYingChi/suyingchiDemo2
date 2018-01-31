package com.ihs.device.accessibility.service;

import android.os.IBinder;
import android.os.RemoteException;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by Arthur on 2018/1/31.
 */

public interface IAccEventListener {
    public IBinder asBinder();
    public void onAvailable() throws RemoteException;
    public void onEvent(AccessibilityEvent accessibilityEvent) throws RemoteException;
    public void onUnavailable(int i, String s) throws RemoteException;
}
