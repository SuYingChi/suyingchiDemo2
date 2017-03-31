package com.ihs.booster.common.event;

import java.util.ArrayList;
import java.util.HashMap;

public class MBCallableCenter {

    private final HashMap<String, ArrayList<HSCallable>> mapCallableList = new HashMap<String, ArrayList<HSCallable>>();

    public void addCallable(String tag, HSCallable HSCallable) {
        synchronized (mapCallableList) {
            ArrayList<HSCallable> HSCallableList = mapCallableList.get(tag);
            if (HSCallableList == null) {
                HSCallableList = new ArrayList<HSCallable>();
                mapCallableList.put(tag, HSCallableList);
            }
            HSCallableList.add(HSCallable);
        }
    }

    public void removeCallable(HSCallable HSCallable) {
        synchronized (mapCallableList) {
            for (ArrayList<HSCallable> HSCallableList : mapCallableList.values()) {
                HSCallableList.remove(HSCallable);
            }
        }
    }

    public Object getFirst(String tag, Object data) {
        synchronized (mapCallableList) {
            ArrayList<HSCallable> HSCallableList = mapCallableList.get(tag);
            if (HSCallableList != null) {
                for (HSCallable HSCallable : HSCallableList) {
                    return HSCallable.call(data);
                }
            }
        }
        return null;
    }

    public int getIntegerSum(String tag, Object data) {
        ArrayList<Object> list = getEach(tag, data);
        int sum = 0;
        for (Object value : list) {
            sum += (Integer) value;
        }
        return sum;
    }

    public ArrayList<Object> getEach(String tag, Object data) {
        ArrayList<Object> list = new ArrayList<Object>();
        synchronized (mapCallableList) {
            ArrayList<HSCallable> HSCallableList = mapCallableList.get(tag);
            if (HSCallableList != null) {
                for (HSCallable HSCallable : HSCallableList) {
                    Object value = HSCallable.call(data);
                    list.add(value);
                }
            }
        }
        return list;
    }

    public interface HSCallable {
        Object call(Object data);
    }

}
