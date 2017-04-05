package com.ihs.inputmethod.uimodules.mediacontroller.shares;

import com.ihs.inputmethod.api.framework.HSInputMethod;

/**
 * Created by ihandysoft on 16/6/2.
 */
public enum ShareChannel {

    MESSAGE("message", "com.android.mms"),
    FACEBOOK("facebook", "com.facebook.katana"),
    EMAIL("email", "com.android.email"),
    MESSENGER("messenger", "com.facebook.orca"),
    WHATSAPP("whatsapp", "com.whatsapp"),
    TWITTER("twitter", "com.twitter.android"),
    CURRENT("", "current");

    private String appName;
    private String packageName;

    ShareChannel(String appName, String packageName){
        this.appName = appName;
        this.packageName = packageName;
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

    public static ShareChannel getChannel(String packageName){
        for(ShareChannel channel : values()){
            if(channel.getPackageName().equals(packageName)){
                return channel;
            }
        }
        return CURRENT;
    }
}
