package com.ihs.commons.connection;

import org.json.JSONObject;

/**
 * Created by Arthur on 2018/1/31.
 */

public class HSServerAPIConnection {
    private HSHttpConnection.OnConnectionFinishedListener connectionFinishedListener;
    private int connectTimeout;

    public HSServerAPIConnection(String serverUrlAdCaffe, Object get, JSONObject jsonObject) {

    }

    public void setSigKey(String s, String s1) {

    }

    public void setEncryptKey(String dddddddddddddddddddddddddddddddd, String s) {

    }

    public void setConnectionFinishedListener(HSHttpConnection.OnConnectionFinishedListener connectionFinishedListener) {
        this.connectionFinishedListener = connectionFinishedListener;
    }

    public void startAsync() {

    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
