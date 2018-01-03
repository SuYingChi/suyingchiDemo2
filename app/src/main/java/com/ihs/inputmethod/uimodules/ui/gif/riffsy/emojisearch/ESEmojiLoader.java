package com.ihs.inputmethod.uimodules.ui.gif.riffsy.emojisearch;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.GifCategory;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.GifManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.callback.UICallback;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.BaseRequest;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.TagRequest;
import com.ihs.inputmethod.uimodules.utils.ReleaseVersionUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;


public final class ESEmojiLoader {

    private final Set<String> supportEmojis =new HashSet<>();
    private static final int SIZE_TO_EABLE_EMOJI_SEARCH = 20;
    private List<GifItem> mEmojis = new CopyOnWriteArrayList<>();
    private UICallback callback = new UICallback() {
        @Override
        public void onFetchRemote() {

        }

        @Override
        public void onFail() {

        }

        @Override
        public void onComplete(List<?> data, BaseRequest request) {
            if (request.offset == mEmojis.size() && data != null && data.size() > 0) {//Object item:data
                //dont not use for item :data, otherwise cause concurrent.
                for (Iterator<?> iterator = data.iterator(); iterator.hasNext(); ) {
                    Object item = iterator.next();
                    try {
                        GifItem emoji = (GifItem) item;
                        if (emoji != null) {
                            if (supportEmojis.contains(emoji.getId()) && !mEmojis.contains(emoji)) {
                                mEmojis.add(emoji);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public ESEmojiLoader() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                loadEmojis();
            }
        });
    }

    private void loadEmojis() {
        loadSupportEmoji();
        TagRequest emoji = new TagRequest(callback, GifCategory.TAB_EMOJI, 50);
        emoji.offset = mEmojis.size();
        GifManager.getInstance().sendRequest(emoji);
    }

    private void loadSupportEmoji() {
        final Resources res= HSApplication.getContext().getResources();
        final String packageName=HSApplication.getContext().getPackageName();
        final String release= Build.VERSION.RELEASE;

        final String[] versions=res.getStringArray(R.array.emoji_group_versions);
        final String version=getRightVersion(versions,release).replace(".","_");

        int emojiGroupArrayId=res.getIdentifier("emoji_groups"+version,"array",packageName);
        if(emojiGroupArrayId<=0){
            emojiGroupArrayId= R.array.emoji_groups;
        }
        final String[] emojiGroupNames=res.getStringArray(emojiGroupArrayId);
	    final String[] textGroupNames=res.getStringArray(R.array.emoji_groups_text);
	    
        for (final String groupName : emojiGroupNames){
            int emojiGroupId=res.getIdentifier(groupName+version,"array",packageName);
            if(emojiGroupId<=0){
                emojiGroupId=res.getIdentifier(groupName, "array", packageName);
            }
            final String[] emojiStrings = res.getStringArray(emojiGroupId);
	        final boolean isText=contain(textGroupNames,groupName);
	        if(isText){
		        Collections.addAll(supportEmojis, emojiStrings);
	        }else {
		        for(final String emoji:emojiStrings){
			        supportEmojis.add(codeToEmoji(emoji));
		        }
	        }
        }
    }
	
	private boolean contain(String[] textGroupNames, String groupName) {
		for(String text:textGroupNames){
			if(text.equals(groupName)){
				return true;
			}
		}
		return false;
	}
	
	private String codeToEmoji(final String code){
		final StringBuilder sb = new StringBuilder();
		for (final String codeInHex : code.split("U\\+")) {
			if(codeInHex.trim().length()>0){
				final int codePoint = Integer.parseInt(codeInHex.trim(), 16);
				sb.appendCodePoint(codePoint);
			}
		}
		return sb.toString();
	}

    public boolean shuoldEmojiSearchEabled() {
        final boolean should = mEmojis.size() > SIZE_TO_EABLE_EMOJI_SEARCH;
        if (!should) {
	        HSLog.d("emoji search not enabled, support emoji size = "+mEmojis.size());
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    loadEmojis();
                }
            });
        }
        return should;
    }

    private String getRightVersion(final String[] versions, final String release) {
        String version="";
        for (final String version1 : versions) {
            if (ReleaseVersionUtil.compareReleaseVersion(version1,release)) {
                version = "_"+version1;
            }
        }
        return version;
    }

    public List<GifItem> getEmojiList() {
        return mEmojis;
    }
}