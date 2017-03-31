package com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dsapphire on 15/12/15.
 */
public abstract class DownloadTask implements Runnable{

	protected String url;
	protected File downloadedFile;
	protected String downloadedFileName;

	private volatile boolean isDone=false;
	private volatile boolean isRunning=false;

	public boolean isDone() {
		return isDone;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean running) {
		isRunning = running;
	}

	public void setDone(boolean done) {
		isDone = done;
	}

	public DownloadTask(String url, File downloadedFile) {
		this.url = url;
		this.downloadedFile = downloadedFile;
		if(downloadedFile!=null)
			downloadedFileName=downloadedFile.getAbsolutePath();
	}

	public boolean removeDownloadFile(){
		if(isDone)
			return false;
		if(downloadedFile!=null)
			if(downloadedFile.exists()){
				return downloadedFile.delete();
			}
		return false;
	}

	@Override
	public abstract void run() ;

	public String getDownloadedFileName() {
		return downloadedFileName;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DownloadTask that = (DownloadTask) o;
		return !(downloadedFileName != null ? !downloadedFileName.equals(that.downloadedFileName) : that.downloadedFileName != null);
	}

	@Override
	public int hashCode() {
		return downloadedFileName != null ? downloadedFileName.hashCode() : 0;
	}

	private long fileSize;
	private long downLoadFileSize;


	protected void downloadFile() throws IOException {
		if(downloadedFileName==null){
			throw new IOException("Download file is null.");
		}
		HttpURLConnection con = (HttpURLConnection) new URL(this.url).openConnection();
		con.setRequestMethod("GET");
		con.setConnectTimeout(1000 * 6);
		if (con.getResponseCode() == 200) {
			fileSize=con.getContentLength();
			downLoadFileSize=0;
			InputStream inputStream = con.getInputStream();
			byte[] b = getByte(inputStream);
			downloadedFile = new File(downloadedFileName);
			if(downloadedFile.exists()){
				downloadedFile.delete();
				downloadedFile=new File(downloadedFileName);

			}
			FileOutputStream fileOutputStream = new FileOutputStream(downloadedFile);
			fileOutputStream.write(b);
			fileOutputStream.close();
		}else{
			throw new FileNotFoundException("File not found.");
		}
		con.disconnect();
	}
	private byte[] getByte(InputStream inputStream) throws IOException {
		byte[] b = new byte[1024];
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int len = -1;
		while ((len = inputStream.read(b)) != -1) {
			downLoadFileSize+=len;
			byteArrayOutputStream.write(b, 0, len);
			if(listener!=null&&fileSize>0&&isRunning){
				listener.onDataReceived(downLoadFileSize,fileSize);
			}
		}
		byteArrayOutputStream.close();
		inputStream.close();
		return byteArrayOutputStream.toByteArray();
	}

	private OnDataReceivedListener listener;
	public void setListener(OnDataReceivedListener listener) {
		if(listener!=null&&this.listener==null)
			this.listener = listener;
	}

	interface OnDataReceivedListener {
		void onDataReceived(long received, long size);
	}
}
