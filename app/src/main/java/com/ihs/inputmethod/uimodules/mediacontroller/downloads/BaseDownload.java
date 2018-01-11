package com.ihs.inputmethod.uimodules.mediacontroller.downloads;

import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dsapphire on 15/12/15.
 * <p/>
 * download the new file
 * <p/>
 * manager the download: run or stop
 * manager the download status: isDone or isRunning
 * manager the download progress by OnDataReceivedListener
 */
public abstract class BaseDownload implements Runnable, Comparable<BaseDownload> {


    /**
     * INIT prepare to start
     * RUNNING it is downloading
     * STOPPING it is stopping, but not done
     * STOPPED it is interrupt
     * SUCCESS it is DONE
     * EXCEPTION it has exception
     */
    enum DownloadStatus{
        INIT, RUNNING, STOPPING, STOPPED, SUCCESS, EXCEPTION
    }

    protected String url;
    private long priority;

    private long startTime = 0;

    protected String filePath;
    private long fileSize;
    private long downLoadFileSize;

    private DownloadStatus downloadStatus = DownloadStatus.INIT;

    private boolean stopRunning = false;

    private int position;

    protected OnDataReceivedListener onDataReceivedListener;

    public void setPosition(int position){
        this.position = position;
    }

    public int getPostion(){
        return position;
    }

    public void setStartTime(long time){
        this.startTime = time;
    }

    public long getStartTime(){
        return this.startTime;
    }

    private void setDownloadStatus(DownloadStatus status){
        this.downloadStatus = status;
    }

    private DownloadStatus getDownloadStatus(){
        return this.downloadStatus;
    }

    @Deprecated
    public BaseDownload(String url, File file) {
        this.url = url;
        if (file != null)
            filePath = file.getAbsolutePath();
    }

    public BaseDownload(String url){
        this.url = url;
    }


    @Override
    public abstract void run();

    public String getFilePath() {
        return filePath;
    }

    long getPriority() {
        return priority;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(BaseDownload another) {
        if (another == null) {
            return 1;
        }
        return priority > another.getPriority() ? -1 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseDownload that = (BaseDownload) o;
        return !(filePath != null ? !filePath.equals(that.filePath) : that.filePath != null);
    }

    @Override
    public int hashCode() {
        return filePath != null ? filePath.hashCode() : 0;
    }


    @Deprecated
    protected void download(File downloadedFile) throws Exception {

        if (downloadedFile == null || !downloadedFile.exists()) {
            setDownloadStatus(DownloadStatus.RUNNING);
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL(this.url).openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(1000 * 6);
                if (con.getResponseCode() == 200) {
                    fileSize = con.getContentLength();
                    downLoadFileSize = 0;
                    MediaController.getFileManager().saveDownloadGif(downloadedFile.getAbsolutePath(), getBytes(con.getInputStream()));
                    setDownloadStatus(DownloadStatus.SUCCESS);
                } else {
                    setDownloadStatus(DownloadStatus.EXCEPTION);
                }
            } catch (Exception e) {
                e.printStackTrace();
                setDownloadStatus(DownloadStatus.EXCEPTION);
            } finally {
                if (con != null) {
                    con.disconnect();
                }

                if (getDownloadStatus() == DownloadStatus.EXCEPTION) {
                    MediaController.getFileManager().deleteDownloadGif(filePath);
                    throw new Exception("download error");
                }

                if (!isFileCompleted()) {
                    MediaController.getFileManager().deleteDownloadGif(filePath);
                    setDownloadStatus(DownloadStatus.STOPPED);
                    throw new Exception("download stop");
                }
            }
        }
        else {
            setDownloadStatus(DownloadStatus.SUCCESS);
        }
    }

    /**
     * check file
     * <p/>
     * if file is not complete return false
     *
     * @return
     */
    private boolean isFileCompleted() {
        // download not complete
        if (fileSize != 0 && downLoadFileSize < fileSize) {
            return false;
        } else return fileSize != 0;
    }

    public boolean isOver(){
        return getDownloadStatus() == DownloadStatus.EXCEPTION
                || getDownloadStatus() == DownloadStatus.STOPPED
                || getDownloadStatus() == DownloadStatus.SUCCESS;
    }

    /**
     * stop download
     */
    public void stop() {
        stopRunning = true;
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {

        byte[] b = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int len;

        while ((len = inputStream.read(b)) != -1) {
            downLoadFileSize += len;
            byteArrayOutputStream.write(b, 0, len);
            if (onDataReceivedListener != null && fileSize > 0) {
                onDataReceivedListener.onDataReceived(downLoadFileSize, fileSize);
            }

            if (stopRunning) {
                setDownloadStatus(DownloadStatus.STOPPING);
                break;
            }
        }

        byteArrayOutputStream.close();
        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    interface OnDataReceivedListener {
        void onDataReceived(long received, long size);
    }
}
