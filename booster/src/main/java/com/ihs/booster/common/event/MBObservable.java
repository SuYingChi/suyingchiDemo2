package com.ihs.booster.common.event;

import java.util.Observable;

public class MBObservable extends Observable {

    @Override
    public void notifyObservers(Object data) {
        setChanged();
        super.notifyObservers(data);
    }
}
