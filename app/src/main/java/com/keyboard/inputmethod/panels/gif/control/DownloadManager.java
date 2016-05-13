package com.keyboard.inputmethod.panels.gif.control;

import android.view.View;

import com.ihs.inputmethod.base.utils.HSNetworkConnectionUtils;
import com.keyboard.inputmethod.panels.gif.net.download.DownloadTask;
import com.keyboard.inputmethod.panels.gif.net.download.DownloadThreadPoolManager;
import com.keyboard.inputmethod.panels.gif.net.download.GifDownloadTask;
import com.keyboard.inputmethod.panels.gif.utils.ConstantsUtils;

import java.io.File;

public final class DownloadManager {

	private static DownloadManager instance;

	private DownloadThreadPoolManager threadManager;

	private DownloadManager(){
		threadManager= DownloadThreadPoolManager.getInstance();
	}

	public DownloadThreadPoolManager getDownloadManager(){
		return  threadManager;
	}

	public static DownloadManager getInstance() {
		if(instance==null){
			synchronized (DownloadManager.class){
				if(instance==null){
					instance=new DownloadManager();
				}
			}
		}
		return instance;
	}

	/*
	 * @return true for exists
	 */
	public void loadMp4(final String fileName, final String url,final GifDownloadTask.Callback callback){
		final File downloadedFile = new File(getMp4DownloadDir(),fileName);
		if (downloadedFile.exists() && !threadManager.isTaskAdded(downloadedFile.getAbsolutePath())) {
			UIController.getInstance().getUIHandler().post(new Runnable() {
				@Override
				public void run() {
					callback.onDownloadSucceeded(downloadedFile,null);
				}
			});
			return ;
		}

		UIController.getInstance().getUIHandler().post(new Runnable() {
			@Override
			public void run() {
				callback.onDownloadProgress(null, null, 0);
			}
		});

		if (!HSNetworkConnectionUtils.isNetworkConnected()) {
			callback.onDownloadFailed(null);
			return ;
		}
		GifDownloadTask task=new GifDownloadTask(url,downloadedFile,null,callback);
		if(!threadManager.isTaskAdded(task.getDownloadedFileName())){
			threadManager.submitTask(task);
		}
	}

	public DownloadTask loadImageTask(final String fileName, final String url, final View view, final GifDownloadTask.Callback callback){
		final File downloadedFile =new File(getGifDownloadFolder(), fileName);
		GifDownloadTask task = new GifDownloadTask(url, downloadedFile, view, callback);
		if (downloadedFile.exists() && !threadManager.isTaskAdded(downloadedFile.getAbsolutePath())) {
			task.setDone(true);
			UIController.getInstance().getUIHandler().post(new Runnable() {
				@Override
				public void run() {
					callback.onDownloadSucceeded(downloadedFile, view);
				}
			});
		}

		return task;
	}
	public void cancelTask(final DownloadTask task){
		if(task==null)
			return;
		threadManager.cancelTask(task);
	}

	public void submitTask(final DownloadTask task){
		if(task==null)
			return;
		if(!threadManager.isTaskAdded(task.getDownloadedFileName())){
			threadManager.submitTask(task);
		}
	}

	private File getGifDownloadFolder() {
		return ConstantsUtils.getGifDownloadFolder();
	}

	private File getMp4DownloadDir() {
		return ConstantsUtils.getMp4CacheDir();
	}

}
