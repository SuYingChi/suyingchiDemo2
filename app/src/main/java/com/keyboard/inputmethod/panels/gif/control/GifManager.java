package com.keyboard.inputmethod.panels.gif.control;

import android.net.Uri;
import android.util.Pair;

import com.ihs.inputmethod.api.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.base.utils.FileUtils;
import com.keyboard.inputmethod.panels.gif.dao.DaoHelper;
import com.keyboard.inputmethod.panels.gif.emojisearch.ESManager;
import com.keyboard.inputmethod.panels.gif.model.GifItem;
import com.keyboard.inputmethod.panels.gif.net.download.GifDownloadTask;
import com.keyboard.inputmethod.panels.gif.net.request.BaseRequest;
import com.keyboard.inputmethod.panels.gif.utils.ConstantsUtils;
import com.keyboard.inputmethod.panels.gif.utils.ShareUtils;
import com.keyboard.rainbow.utils.Constants;

import java.io.File;

public final class GifManager {

    private static GifManager mInstance;

    private GifManager() {

    }

    public static GifManager getInstance() {
        if (mInstance == null) {
           init();
        }
        return mInstance;
    }

    public static void init() {
        if (mInstance == null) {
            synchronized (GifManager.class) {
                if (mInstance == null) {
                    mInstance = new GifManager();
                }
            }
        }
        DaoHelper.init();
        DataManager.init();
        ESManager.init();
    }


    public void notifyImageClicked(final GifItem item, final String packageName, final GifDownloadTask.Callback callback) {
        if (item != null) {
	        DataManager.getInstance().sendRecentDataToLocal(item);
            share(item,packageName,callback);
        }
    }


	synchronized public void sendRequest(final BaseRequest request) {
		if (request != null) {
			RequestHandler.handleRequest(request);
		}
	}

    public void share(final GifItem gif, final String packageName, final GifDownloadTask.Callback callback) {
        if(!ConstantsUtils.isSDCardEnabled()){
            HSInputMethod.sendText(gif.getUrl());
            HSGoogleAnalyticsUtils.logAppEvent(Constants.KEYBOARD_GIF_SHARE,"link");
            return;
        }

        File uri = ConstantsUtils.getDownloadGifUri(gif.id);
        // send mode
        final Pair<Integer, String> pair = ShareUtils.refreshCurrentShareMode(packageName);

        final int mode = pair.first;
        final String format = pair.second;
        final String mimeType;

        if (format.equals(ShareUtils.IMAGE_SHARE_FORMAT_MP4)) {
            if (gif.getMp4Url()!=null) {
                HSGoogleAnalyticsUtils.logAppEvent(Constants.KEYBOARD_GIF_SHARE,"intent");
                shareMp4(gif,callback);
                return;
            }
        }

        mimeType = "image/*";
        final String targetFilePath = ConstantsUtils.getImageExportFolder() + "/"+ gif.getId()+".gif";
        switch (mode) {
            // image
            case ShareUtils.IMAGE_SHARE_MODE_INTENT:
                FileUtils.copyFile(uri.getAbsolutePath(), targetFilePath);
                try {
                    ShareUtils.shareImageByIntent(Uri.fromFile(new File(targetFilePath)), mimeType, packageName);
                    HSGoogleAnalyticsUtils.logAppEvent(Constants.KEYBOARD_GIF_SHARE,"intent");
                } catch (Exception e) {
                    HSInputMethod.sendText(gif.getUrl());
                    HSGoogleAnalyticsUtils.logAppEvent(Constants.KEYBOARD_GIF_SHARE,"link");
                }
                break;
            // save to gallery
            case ShareUtils.IMAGE_SHARE_MODE_EXPORT:
                FileUtils.copyFile(uri.getAbsolutePath(), targetFilePath);
                ShareUtils.shareImageByExport(targetFilePath);
                HSGoogleAnalyticsUtils.logAppEvent(Constants.KEYBOARD_GIF_SHARE,"export");
                break;
            case ShareUtils.IMAGE_SHARE_MODE_LINK:
	            HSInputMethod.sendText(gif.getUrl());
                HSGoogleAnalyticsUtils.logAppEvent(Constants.KEYBOARD_GIF_SHARE,"link");
                break;
            default:
	            HSInputMethod.sendText(gif.getUrl());
                HSGoogleAnalyticsUtils.logAppEvent(Constants.KEYBOARD_GIF_SHARE,"link");
        }
    }

    private void shareMp4(final GifItem gif, GifDownloadTask.Callback callback) {
       DownloadManager.getInstance().loadMp4(gif.id+".mp4", gif.getMp4Url(), callback);
    }

}