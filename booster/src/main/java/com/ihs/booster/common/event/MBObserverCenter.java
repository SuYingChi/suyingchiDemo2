package com.ihs.booster.common.event;

import com.ihs.commons.utils.HSLog;
import com.ihs.booster.utils.CommonUtils;
import com.ihs.booster.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Observer;

public class MBObserverCenter {

    private final HashMap<String, MBObservable> mapEventObservable = new HashMap<String, MBObservable>();
    private final HashMap<Object, ArrayList<Observer>> mapOwnerObservers = new HashMap<Object, ArrayList<Observer>>();

    public void addObserver(Object owner, String event, Observer observer) {
        if (observer == null) {
            return;
        }

        HSLog.d(event + " " + owner.toString());
        CommonUtils.debugAssert(!(owner instanceof Observer), "owner can't be a Observer instance.");

        MBObservable observable;
        synchronized (mapEventObservable) {
            observable = mapEventObservable.get(event);
            if (observable == null) {
                observable = new MBObservable();
                mapEventObservable.put(event, observable);
            }
        }
        observable.addObserver(observer);

        ArrayList<Observer> observers;
        synchronized (mapOwnerObservers) {
            observers = mapOwnerObservers.get(owner);
            if (observers == null) {
                observers = new ArrayList<Observer>();
                mapOwnerObservers.put(owner, observers);
            }
        }
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        synchronized (mapEventObservable) {
            Iterator<Entry<String, MBObservable>> it = mapEventObservable.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, MBObservable> entry = it.next();
                MBObservable observable = entry.getValue();
                observable.deleteObserver(observer);
                if (observable.countObservers() == 0) {
                    it.remove();
                }
            }
        }
        synchronized (mapOwnerObservers) {
            Iterator<Entry<Object, ArrayList<Observer>>> it = mapOwnerObservers.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Object, ArrayList<Observer>> entry = it.next();
                ArrayList<Observer> observers = entry.getValue();
                observers.remove(observer);
                if (observers.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    public void removeObservers(Object owner, String event) {
        HSLog.d(event + " " + owner.toString());
        CommonUtils.debugAssert(!(owner instanceof Observer), "owner can't be a Observer instance.");
        synchronized (mapEventObservable) {
            MBObservable eventObservable = mapEventObservable.get(event);
            if (eventObservable == null) {
                return;
            }
            synchronized (mapOwnerObservers) {
                ArrayList<Observer> ownerObservers = mapOwnerObservers.get(owner);
                if (ownerObservers != null) {
                    Iterator<Observer> it = ownerObservers.iterator();
                    while (it.hasNext()) {
                        Observer observer = it.next();
                        int count = eventObservable.countObservers();
                        eventObservable.deleteObserver(observer);
                        if (eventObservable.countObservers() < count) {//有删除
                            it.remove();
                            if (eventObservable.countObservers() == 0) {
                                mapEventObservable.remove(event);
                                break;
                            }
                        }
                    }
                    if (ownerObservers.isEmpty()) {
                        mapOwnerObservers.remove(owner);
                    }
                }
            }
        }
    }

    public void removeObservers(Object owner) {
        CommonUtils.debugAssert(!(owner instanceof Observer), "owner can't be a Observer instance.");
        ArrayList<Observer> observersToDelete = null;
        synchronized (mapOwnerObservers) {
            observersToDelete = mapOwnerObservers.remove(owner);
        }
        if (observersToDelete != null) {
            synchronized (mapEventObservable) {
                Iterator<Entry<String, MBObservable>> it = mapEventObservable.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, MBObservable> entry = it.next();
                    MBObservable observable = entry.getValue();
                    for (Observer observer : observersToDelete) {
                        observable.deleteObserver(observer);
                    }
                    if (observable.countObservers() == 0) {
                        it.remove();
                    }
                }
            }
            observersToDelete.clear();
        }
    }

    public void notifyOnUIThread(String event) {
        notifyOnUIThread(event, null);
    }

    public void notifyOnUIThread(String event, final Object data) {
        HSLog.d(event + " " + data);
        final MBObservable observable;
        synchronized (mapEventObservable) {
            observable = mapEventObservable.get(event);
        }
        if (observable != null) {
            Runnable action = new Runnable() {
                @Override
                public void run() {
                    observable.notifyObservers(data);
                }
            };
            Utils.runOnUiThread(action);
        }
    }

}
