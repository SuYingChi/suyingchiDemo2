package com.ihs.inputmethod.uimodules.ui.facemoji;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Default executor for rendering tasks - {@link java.util.concurrent.ScheduledThreadPoolExecutor}
 * with Runtime.getRuntime().availableProcessors() + 1 worker thread and {@link java.util.concurrent.ThreadPoolExecutor.DiscardPolicy}.
 */
final class FacemojiRenderingExecutor extends ScheduledThreadPoolExecutor {

	// Lazy initialization via inner-class holder
    private static final class InstanceHolder {
		private static final FacemojiRenderingExecutor INSTANCE = new FacemojiRenderingExecutor();
	}

	static FacemojiRenderingExecutor getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private FacemojiRenderingExecutor() {
		super(Runtime.getRuntime().availableProcessors() + 1, new DiscardPolicy());
	}
}