package com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.DownloadManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download.DownloadTask;

/**
 * Created by wenbinduan on 2016/11/28.
 */

public final class LoadScrollListener implements RecyclerView.OnChildAttachStateChangeListener {
	
	@Override
	public void onChildViewAttachedToWindow(final View view) {
		if(!(view instanceof GifView) ){
			return;
		}
		final GifView gifView= (GifView)view;
		gifView.hideFavoriteView();
		DownloadTask task=gifView.getTask();
		if(task!=null){
			if(!task.isDone()&&!task.isRunning()){
				View progress = view.findViewById(R.id.progress);
				progress.setVisibility(View.VISIBLE);
				DownloadManager.getInstance().submitTask(task);
			}
		}
	}
	
	@Override
	public void onChildViewDetachedFromWindow(final View view) {
		if(!(view instanceof GifView) ){
			return;
		}
		GifView gifView= (GifView)view;
		gifView.hideFavoriteView();
		DownloadTask task=gifView.getTask();
		if(task!=null){
			if(!task.isDone()&&task.isRunning()){
				DownloadManager.getInstance().cancelTask(task);
			}
		}
	}
}