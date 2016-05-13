package com.keyboard.inputmethod.panels.gif.net.download;

import android.view.View;

import com.keyboard.inputmethod.panels.gif.control.DownloadManager;
import com.keyboard.inputmethod.panels.gif.control.UIController;

import java.io.File;

/**
 * Created by dsapphire on 15/12/15.
 */
public final class GifDownloadTask extends DownloadTask {

	private View view;
	private Callback callback;

	public GifDownloadTask(String url, File downloadedFile, View view, Callback callback){
		super(url,downloadedFile);
		this.callback=callback;
		this.view=view;
		initCallback();
	}

	private void initCallback(){
		setListener(new OnDataReceivedListener() {
			@Override
			public void onDataReceived(long received, long size) {
				final float percent = (float) received / size;
				UIController.getInstance().getUIHandler().post(new Runnable() {
					@Override
					public void run() {
						callback.onDownloadProgress(downloadedFile,view, percent);
					}
				});
			}
		});
	}

	@Override
	public void run() {
		try {
			setRunning(true);
			downloadFile();
			setDone(true);
			UIController.getInstance().getUIHandler().post(new Runnable() {
				@Override
				public void run() {
					callback.onDownloadSucceeded(downloadedFile, view);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			setRunning(false);
			removeDownloadFile();
			UIController.getInstance().getUIHandler().post(new Runnable() {
				@Override
				public void run() {
					callback.onDownloadFailed(view);
				}
			});
		}finally {
			setRunning(false);
			DownloadManager.getInstance().getDownloadManager().removeTask(downloadedFileName);
		}
	}

	public interface Callback{
		void onDownloadProgress(File file, View view, float percent);
		void onDownloadSucceeded(File file, View view);
		void onDownloadFailed(View view);
	}
}
