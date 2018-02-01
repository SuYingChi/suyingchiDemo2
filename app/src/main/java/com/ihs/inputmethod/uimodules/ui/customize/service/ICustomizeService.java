package com.ihs.inputmethod.uimodules.ui.customize.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;



public interface ICustomizeService extends IInterface
{
    /** Local-side IPC implementation stub class. */
    abstract class Stub extends Binder implements ICustomizeService
    {
        private static final java.lang.String DESCRIPTOR = "ICustomizeService";
        /** Construct the stub at attach it to the interface. */
        public Stub()
        {
            this.attachInterface(this, DESCRIPTOR);
        }
        /**
         * Cast an IBinder object into an ICustomizeService interface,
         * generating a proxy if needed.
         */
        public static ICustomizeService asInterface(IBinder obj)
        {
            if ((obj==null)) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin!=null)&&(iin instanceof ICustomizeService))) {
                return ((ICustomizeService)iin);
            }
            return new ICustomizeService.Stub.Proxy(obj);
        }
        @Override public IBinder asBinder()
        {
            return this;
        }
        @Override public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException
        {
            switch (code)
            {
                case INTERFACE_TRANSACTION:
                {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_getCurrentTheme:
                {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _result = this.getCurrentTheme();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_setCurrentTheme:
                {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    this.setCurrentTheme(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_browseMarketApp:
                {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    long _result = this.browseMarketApp(_arg0);
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                }
                case TRANSACTION_getDefaultSharedPreferenceString:
                {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    java.lang.String _arg1;
                    _arg1 = data.readString();
                    java.lang.String _result = this.getDefaultSharedPreferenceString(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_preChangeWallpaperFromLauncher:
                {
                    data.enforceInterface(DESCRIPTOR);
                    this.preChangeWallpaperFromLauncher();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_putDefaultSharedPreferenceString:
                {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    java.lang.String _arg1;
                    _arg1 = data.readString();
                    this.putDefaultSharedPreferenceString(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_notifyWallpaperFeatureUsed:
                {
                    data.enforceInterface(DESCRIPTOR);
                    this.notifyWallpaperFeatureUsed();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_notifyWallpaperSetEvent:
                {
                    data.enforceInterface(DESCRIPTOR);
                    this.notifyWallpaperSetEvent();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getOnlineWallpaperConfig:
                {
                    data.enforceInterface(DESCRIPTOR);
                    java.util.List _result = this.getOnlineWallpaperConfig();
                    reply.writeNoException();
                    reply.writeList(_result);
                    return true;
                }
                case TRANSACTION_getOnlineThemeConfig:
                {
                    data.enforceInterface(DESCRIPTOR);
                    java.util.Map _result = this.getOnlineThemeConfig();
                    reply.writeNoException();
                    reply.writeMap(_result);
                    return true;
                }
                case TRANSACTION_logWallpaperEvent:
                {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    java.lang.String _arg1;
                    _arg1 = data.readString();
                    this.logWallpaperEvent(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_killWallpaperProcess:
                {
                    data.enforceInterface(DESCRIPTOR);
                    this.killWallpaperProcess();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_notifyWallpaperOrThemeExit:
                {
                    data.enforceInterface(DESCRIPTOR);
                    this.notifyWallpaperOrThemeExit();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_notifyWallpaperPackageClicked:
                {
                    data.enforceInterface(DESCRIPTOR);
                    this.notifyWallpaperPackageClicked();
                    reply.writeNoException();
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }
        private static class Proxy implements ICustomizeService
        {
            private IBinder mRemote;
            Proxy(IBinder remote)
            {
                mRemote = remote;
            }
            @Override public IBinder asBinder()
            {
                return mRemote;
            }
// --Commented out by Inspection START (18/1/11 下午2:41):
//            public java.lang.String getInterfaceDescriptor()
//            {
//                return DESCRIPTOR;
//            }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
            @Override public java.lang.String getCurrentTheme() throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                java.lang.String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getCurrentTheme, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readString();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
            @Override public void setCurrentTheme(java.lang.String theme) throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(theme);
                    mRemote.transact(Stub.TRANSACTION_setCurrentTheme, _data, _reply, 0);
                    _reply.readException();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            @Override public long browseMarketApp(java.lang.String packageName) throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                long _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(packageName);
                    mRemote.transact(Stub.TRANSACTION_browseMarketApp, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readLong();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
            @Override public java.lang.String getDefaultSharedPreferenceString(java.lang.String key, java.lang.String defaultValue) throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                java.lang.String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(defaultValue);
                    mRemote.transact(Stub.TRANSACTION_getDefaultSharedPreferenceString, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readString();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
            @Override public void preChangeWallpaperFromLauncher() throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_preChangeWallpaperFromLauncher, _data, _reply, 0);
                    _reply.readException();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            @Override public void putDefaultSharedPreferenceString(java.lang.String key, java.lang.String value) throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(value);
                    mRemote.transact(Stub.TRANSACTION_putDefaultSharedPreferenceString, _data, _reply, 0);
                    _reply.readException();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            @Override public void notifyWallpaperFeatureUsed() throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_notifyWallpaperFeatureUsed, _data, _reply, 0);
                    _reply.readException();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            @Override public void notifyWallpaperSetEvent() throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_notifyWallpaperSetEvent, _data, _reply, 0);
                    _reply.readException();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            @Override public java.util.List getOnlineWallpaperConfig() throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                java.util.List _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getOnlineWallpaperConfig, _data, _reply, 0);
                    _reply.readException();
                    java.lang.ClassLoader cl = this.getClass().getClassLoader();
                    _result = _reply.readArrayList(cl);
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
            @Override public java.util.Map getOnlineThemeConfig() throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                java.util.Map _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getOnlineThemeConfig, _data, _reply, 0);
                    _reply.readException();
                    java.lang.ClassLoader cl = this.getClass().getClassLoader();
                    _result = _reply.readHashMap(cl);
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
            @Override public void logWallpaperEvent(java.lang.String action, java.lang.String label) throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeString(label);
                    mRemote.transact(Stub.TRANSACTION_logWallpaperEvent, _data, _reply, 0);
                    _reply.readException();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            @Override public void killWallpaperProcess() throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_killWallpaperProcess, _data, _reply, 0);
                    _reply.readException();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            @Override public void notifyWallpaperOrThemeExit() throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_notifyWallpaperOrThemeExit, _data, _reply, 0);
                    _reply.readException();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            @Override public void notifyWallpaperPackageClicked() throws RemoteException
            {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_notifyWallpaperPackageClicked, _data, _reply, 0);
                    _reply.readException();
                }
                finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
        static final int TRANSACTION_getCurrentTheme = (IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_setCurrentTheme = (IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_browseMarketApp = (IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_getDefaultSharedPreferenceString = (IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_preChangeWallpaperFromLauncher = (IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_putDefaultSharedPreferenceString = (IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_notifyWallpaperFeatureUsed = (IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_notifyWallpaperSetEvent = (IBinder.FIRST_CALL_TRANSACTION + 7);
        static final int TRANSACTION_getOnlineWallpaperConfig = (IBinder.FIRST_CALL_TRANSACTION + 8);
        static final int TRANSACTION_getOnlineThemeConfig = (IBinder.FIRST_CALL_TRANSACTION + 9);
        static final int TRANSACTION_logWallpaperEvent = (IBinder.FIRST_CALL_TRANSACTION + 10);
        static final int TRANSACTION_killWallpaperProcess = (IBinder.FIRST_CALL_TRANSACTION + 11);
        static final int TRANSACTION_notifyWallpaperOrThemeExit = (IBinder.FIRST_CALL_TRANSACTION + 12);
        static final int TRANSACTION_notifyWallpaperPackageClicked = (IBinder.FIRST_CALL_TRANSACTION + 13);
    }
    java.lang.String getCurrentTheme() throws RemoteException;
    void setCurrentTheme(java.lang.String theme) throws RemoteException;
    long browseMarketApp(java.lang.String packageName) throws RemoteException;
    java.lang.String getDefaultSharedPreferenceString(java.lang.String key, java.lang.String defaultValue) throws RemoteException;
    void preChangeWallpaperFromLauncher() throws RemoteException;
    void putDefaultSharedPreferenceString(java.lang.String key, java.lang.String value) throws RemoteException;
    void notifyWallpaperFeatureUsed() throws RemoteException;
    void notifyWallpaperSetEvent() throws RemoteException;
    java.util.List getOnlineWallpaperConfig() throws RemoteException;
    java.util.Map getOnlineThemeConfig() throws RemoteException;
    void logWallpaperEvent(java.lang.String action, java.lang.String label) throws RemoteException;
    void killWallpaperProcess() throws RemoteException;
    void notifyWallpaperOrThemeExit() throws RemoteException;
    void notifyWallpaperPackageClicked() throws RemoteException;
}
