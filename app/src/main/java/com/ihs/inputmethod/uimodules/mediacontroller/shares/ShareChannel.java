package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by ihandysoft on 16/6/2.
 */
public enum ShareChannel {

    MESSAGE("Message", "com.android.mms", R.drawable.share_icon_message),
    FACEBOOK("Facebook", "com.facebook.katana",R.drawable.share_icon_facebook),
    EMAIL("Email", "com.android.email",R.drawable.share_icon_email),
    MESSENGER("Messenger", "com.facebook.orca",R.drawable.share_icon_messenger),
    WHATSAPP("Whatsapp", "com.whatsapp",R.drawable.share_icon_whatsapp),
    TWITTER("Twitter", "com.twitter.android",R.drawable.share_icon_twitter),
    INSTAGRAM("Instagram", "com.instagram.android",R.drawable.share_icon_instagram),
    MORE("More", "",R.drawable.ic_facemoji_share_app_more),
    CURRENT("", "current",0);

    private String appName;
    private String packageName;
    private int iconId;

    ShareChannel(String appName, String packageName,int iconId){
        this.appName = appName;
        this.packageName = packageName;
        this.iconId = iconId;
    }

    public String getAppName(){
        return appName;
    }
    public String getPackageName(){
        if(packageName.equals("current")){
            return HSInputMethod.getCurrentHostAppPackageName();
        }
        return packageName;
    }

    public int getIconId(){
        return iconId;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static ShareChannel getChannel(String packageName){
//        for(ShareChannel channel : values()){
//            if(channel.getPackageName().equals(packageName)){
//                return channel;
//            }
//        }
//        return CURRENT;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}
