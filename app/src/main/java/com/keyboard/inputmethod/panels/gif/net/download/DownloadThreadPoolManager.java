package com.keyboard.inputmethod.panels.gif.net.download;


import com.keyboard.inputmethod.panels.gif.control.DownloadManager;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by dsapphire on 15/12/15.
 */
public final class DownloadThreadPoolManager {
	private final ThreadPoolExecutor threadPool;

	private HashMap<String,DownloadTask> tasks;
	private HashMap<String,FutureTask<?>> futureTasks;
	private static DownloadThreadPoolManager instance;

	public static DownloadThreadPoolManager getInstance(){
		if(instance==null){
			synchronized (DownloadManager.class){
				if(instance==null){
					instance=new DownloadThreadPoolManager();
				}
			}
		}
		return instance;
	}
	private DownloadThreadPoolManager(){
		threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				if(r instanceof DownloadTask){
					((DownloadTask) r).removeDownloadFile();
					removeTask(((DownloadTask) r).getDownloadedFileName());
				}
			}
		});
		tasks=new HashMap<>();
		futureTasks=new HashMap<>();
	}


	public void removeTask(final String downloadFileName){
		tasks.remove(downloadFileName);
		futureTasks.remove(downloadFileName);
	}

	public boolean isTaskAdded(final String downloadFileName){
		return tasks.get(downloadFileName)!=null;
	}

	public void submitTask(final DownloadTask task){
		FutureTask<?> future= (FutureTask<?>) threadPool.submit(task);
		tasks.put(task.getDownloadedFileName(),task);
		futureTasks.put(task.getDownloadedFileName(),future);
	}

	public boolean cancelTask(final DownloadTask task){
		FutureTask futureTask=futureTasks.get(task.getDownloadedFileName());
		if(futureTask!=null){
			if(!futureTask.isDone()){
				futureTask.cancel(true);
				task.removeDownloadFile();
				removeTask(task.getDownloadedFileName());
				task.setRunning(false);
				return true;
			}
		}
		return false;
	}
}
