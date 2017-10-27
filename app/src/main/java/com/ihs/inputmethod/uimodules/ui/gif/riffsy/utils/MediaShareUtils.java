package com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils;

import android.content.Intent;
import android.net.Uri;

import com.ihs.app.framework.HSApplication;

public final class MediaShareUtils {

    public static final String MIME_MP4 = "video/mpeg";

    public static void shareImageByIntent(final Uri uri, final String mimeType, final String packageName) throws Exception {
        try{
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setPackage(packageName);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType(mimeType);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            HSApplication.getContext().startActivity(shareIntent);
        }catch (Exception e){
            throw new Exception("Can't share by intent");
        }
    }

}
