package com.keyboard.inputmethod.panels.gif.ui.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.keyboard.inputmethod.panels.gif.control.DownloadManager;
import com.keyboard.inputmethod.panels.gif.net.download.DownloadTask;
import com.keyboard.rainbow.R;


public final class GifHorizontalScrollView extends RecyclerView {

    public GifHorizontalScrollView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.keyboardViewStyle);
    }

    public GifHorizontalScrollView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }


    public static class LoadScrollListener implements OnChildAttachStateChangeListener{

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
}
