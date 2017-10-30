package com.ihs.inputmethod.uimodules.ui.gif.riffsy.control;

import android.view.View;

import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download.DownloadTask;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download.DownloadThreadPoolManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download.GifDownloadTask;
import com.ihs.inputmethod.api.utils.HSNetworkConnectionUtils;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.DirectoryUtils;

import java.io.File;

import static com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.DirectoryUtils.getDownloadGifUri;

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
		final File downloadedFile = getDownloadGifUri(fileName);
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
		return DirectoryUtils.getGifDownloadFolder();
	}

	private File getMp4DownloadDir() {
		return DirectoryUtils.getMp4CacheDir();
	}

}
