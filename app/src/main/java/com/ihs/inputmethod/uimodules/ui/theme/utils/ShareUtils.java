package com.ihs.inputmethod.uimodules.ui.theme.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ihs.commons.utils.HSLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jixiang on 16/8/12.
 */
public class ShareUtils {

    /**
     * 默认分享，不带过滤功能
     *
     * @param activity      activity
     * @param activityTitle Activity的名字
     * @param msgTitle      消息标题
     * @param msgText       消息内容
     * @param imgPath       图片路径，不分享图片则传null
     */
    public static void share(Activity activity, String activityTitle, String msgTitle, String msgText,
                             String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (TextUtils.isEmpty(imgPath)) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                intent.setType("image/*");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(Intent.createChooser(intent, activityTitle));
    }


    /**
     * 可过滤包含黑名单关键字的包名的分享
     * @param context
     * @param dialogTitle
     * @param msgTitle
     * @param msgText
     * @param imgPath
     */
    public static void shareImageFilterBlackList(Context context, String dialogTitle, String msgTitle, String msgText,
                                                 String imgPath) {

        //black pkName list
        String[] blacklist = new String[]{"bluetooth" /** 蓝牙 */,    "com.android.nfc"/** Android Beam */ ,      "calendar"/** 日历 */};
        //priority first  pkName list
        String[] priorityFirstList = new String[]{"com.facebook.katana","com.whatsapp","com.snapchat.android","com.android.mms","android.email"};

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (TextUtils.isEmpty(imgPath)) {
            shareIntent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                shareIntent.setType("image/*");
                Uri u = Uri.fromFile(f);
                shareIntent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, msgText);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
// ... anything else you want to add
// invoke custom chooser
        context.startActivity(generateCustomChooserIntent(context,shareIntent, blacklist,dialogTitle,priorityFirstList));
    }
    // Method:
    private static Intent generateCustomChooserIntent(Context context, Intent prototype, String[] blacklist, String dialogTitle,String[] priorityFirstList) {
        List<Intent> targetedShareIntents = new ArrayList<Intent>();
        List<HashMap<String, String>> intentMetaInfo = new ArrayList<HashMap<String, String>>();
        Intent chooserIntent;

        Intent[] priorityPkNameIntent = new Intent[priorityFirstList.length];

        Intent dummy = new Intent(prototype.getAction());
        dummy.setType(prototype.getType());
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(dummy, 0);

        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo : resInfo) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (resolveInfo.activityInfo == null)
                    continue;

                boolean isContainBlackList = false;
                for(String keyword:blacklist){
                    if(packageName.contains(keyword)){
                        isContainBlackList = true;
                        break;
                    }
                }

                if(isContainBlackList){
                    continue;
                }

                HashMap<String, String> info = new HashMap<String, String>();
                info.put("packageName", resolveInfo.activityInfo.packageName);
                info.put("className", resolveInfo.activityInfo.name);
                info.put("simpleName", String.valueOf(resolveInfo.activityInfo.loadLabel(context.getPackageManager())));
                intentMetaInfo.add(info);
                HSLog.d("jx,share pkName:"+packageName+", label Name:"+String.valueOf(resolveInfo.activityInfo.loadLabel(context.getPackageManager())));
            }

            if (!intentMetaInfo.isEmpty()) {
                // sorting for nice readability
                Collections.sort(intentMetaInfo, new Comparator<HashMap<String, String>>() {
                    @Override
                    public int compare(HashMap<String, String> map, HashMap<String, String> map2) {
                        return map.get("simpleName").compareTo(map2.get("simpleName"));
                    }
                });

                // create the custom intent list
                for (HashMap<String, String> metaInfo : intentMetaInfo) {
                    boolean isPriority = false;
                    int index = 0;
                    for(int i = 0;i< priorityFirstList.length;i++){
                        if(metaInfo.get("packageName").contains(priorityFirstList[i])){
                            isPriority = true;
                            index = i;
                            break;
                        }

                    }
                    Intent targetedShareIntent = (Intent) prototype.clone();
                    targetedShareIntent.setPackage(metaInfo.get("packageName"));
                    targetedShareIntent.setClassName(metaInfo.get("packageName"), metaInfo.get("className"));
                    if(isPriority) {
                        priorityPkNameIntent[index] = targetedShareIntent;
                    }else {
                        targetedShareIntents.add(targetedShareIntent);
                    }
                }

                for(int i = priorityPkNameIntent.length -1;i>=0 ;i--){
                    if(priorityPkNameIntent[i]!=null){
                        targetedShareIntents.add(0,priorityPkNameIntent[i]);
                    }
                }


                chooserIntent = Intent.createChooser(targetedShareIntents.remove(targetedShareIntents.size() - 1), dialogTitle);
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                return chooserIntent;
            }
        }

        return Intent.createChooser(prototype, dialogTitle);
    }
}
