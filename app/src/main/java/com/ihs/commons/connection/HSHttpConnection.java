package com.ihs.commons.connection;

import com.ihs.commons.utils.HSError;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arthur on 2018/1/31.
 */

public class HSHttpConnection {
    private File downloadFile;
    private OnConnectionFinishedListener connectionFinishedListener;
    private JSONObject bodyJSON;
    private int connectTimeout;
    private OnDataReceivedListener dataReceivedListener;
    private int readTimeout;
    private HashMap<String, String> requestParams;
    private Object headerReceivedListener;

    public HSHttpConnection(String s) {

    }

    public HSHttpConnection(String url, Object get) {

    }


    public HSHttpConnection setConnectionFinishedListener(OnConnectionFinishedListener connectionFinishedListener) {
        this.connectionFinishedListener = connectionFinishedListener;
        return this;

    }

    public void startAsync() {

    }

    public boolean isSucceeded() {
        return false;
    }

    public JSONObject getBodyJSON() {
        return bodyJSON;
    }

    public HSHttpConnection setDataReceivedListener(OnDataReceivedListener dataReceivedListener) {
        this.dataReceivedListener = dataReceivedListener;
        return this;

    }

    public void startSync() {

    }

    public String getURL() {
        return "aa";
    }

    public HSHttpConnection setReadTimeout(int timeOutMillis) {
        return this;
    }

    public HSHttpConnection setRequestParams(HashMap<String, String> requestParams) {
        this.requestParams = requestParams;
        return this;
    }

    public void cancel() {

    }
    public HSHttpConnection setHeaders(Map<String, String> entrys) {
        return this;
    }

    public HSHttpConnection setHeaderReceivedListener(Object headerReceivedListener) {
        this.headerReceivedListener = headerReceivedListener;
        return this;
    }

    public interface OnConnectionFinishedListener {
        public void onConnectionFinished(HSHttpConnection hsHttpConnection);

        public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError);
    }

    public interface OnDataReceivedListener {
        public void onDataReceived(HSHttpConnection hsHttpConnection, byte[] bytes, long l, long l1);
    }
    public HSHttpConnection setConnectTimeout(int timeOutMillis) {
        return this;
    }

    public HSHttpConnection setDownloadFile(File file) {
        return this;
    }

}
