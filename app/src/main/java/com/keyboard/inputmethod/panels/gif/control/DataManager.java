package com.keyboard.inputmethod.panels.gif.control;


import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.HSGoogleAnalyticsUtils;
import com.keyboard.inputmethod.panels.gif.dao.DaoHelper;
import com.keyboard.inputmethod.panels.gif.model.GifItem;
import com.keyboard.inputmethod.panels.gif.net.request.BaseRequest;
import com.keyboard.inputmethod.panels.gif.net.request.SearchRequest;
import com.ihs.inputmethod.latin.LatinIME;
import com.keyboard.colorkeyboard.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * Created by dsapphire on 16/1/22.
 */
public final class DataManager {

	private static final int RECENT_SIZE = 50;
	private static DataManager instance;
	public static final String HS_NOTIFICATION_SWITCH_LANGUAGE="DataManager_SWITCH_LANGUAGE";

	private HashMap<String,ArrayList<GifItem>> tabData=new HashMap<>();
	private HashMap<String,ArrayList<GifItem>> tagData=new HashMap<>();

	private HashMap<String,String> nextPos =new HashMap<>();
	private volatile boolean isLocalLoaded=false;

	private INotificationObserver savingRecent=new INotificationObserver() {
		@Override
		public void onReceive(String s, HSBundle hsBundle) {
			if(LatinIME.HIDE_WINDOW_EVENT.equals(s)){
				saveUserDataToDB();
			}
			if(LatinIME.HS_NOTIFICATION_SERVICE_DESTROY.equals(s)){
				HSGlobalNotificationCenter.removeObserver(savingRecent);
			}
		}
	};

	private DataManager() {
		loadUserData();
	}

	public void loadUserData() {

		if(isLocalLoaded){
			return;
		}
		isLocalLoaded=true;

		new Thread(){
			@Override
			public void run() {
				ArrayList<GifItem> recent=new ArrayList<>();
				tabData.put(GifCategory.TAB_RECENT,recent);
				recent.addAll(DaoHelper.getInstance().getAllTabData(GifCategory.TAB_RECENT));

				ArrayList<GifItem> favorite=new ArrayList<>();
				tabData.put(GifCategory.TAB_FAVORITE,favorite);
				favorite.addAll(DaoHelper.getInstance().getAllTabData(GifCategory.TAB_FAVORITE));
			}
		}.start();
	}

	public static void init() {
		if (instance == null) {
			synchronized (DataManager.class) {
				if (instance == null) {
					instance=new DataManager();
				}
			}
		}
	}

	public static DataManager getInstance() {
		if (instance == null) {
			init();
		}
		HSGlobalNotificationCenter.addObserver(LatinIME.HIDE_WINDOW_EVENT, instance.savingRecent);
		HSGlobalNotificationCenter.addObserver(LatinIME.HS_NOTIFICATION_SERVICE_DESTROY, instance.savingRecent);
		return instance;
	}

	//return null if need request remote, or return data from local or empty list to indicate empty data
	public synchronized List<?> getDataFromLocal(final BaseRequest request) {
		ArrayList<GifItem> data;
		if(request instanceof SearchRequest){
			data=tagData.get(request.categoryName);

			if(data==null){
				data=new ArrayList<>();
				tagData.put(request.categoryName,data);
			}

			if(data.size()==0){
				data.addAll(DaoHelper.getInstance().getAllTagData(request.categoryName));
			}

		}else{
			data=tabData.get(request.categoryName);

			if(data==null){
				data=new ArrayList<>();
				tabData.put(request.categoryName,data);
			}

			if(data.size()==0&&!GifCategory.isUserTab(request.categoryName)){
				data.addAll(DaoHelper.getInstance().getAllTabData(request.categoryName));
			}

			if(GifCategory.isTagTab(request.categoryName)){
				for(GifItem item:data){
					item.isTag=true;
				}
			}
		}

		if(GifCategory.isUserTab(request.categoryName)){
			//return even empty data
			return sortData(request.offset,request.limit,data);
		}

		if(data.size()<= request.offset){
			//should handle remote
			return null;
		}

		if(DaoHelper.getInstance().isRequestOutOfDate(request.categoryName)){
			new Thread(){
				@Override
				public void run() {
					DaoHelper.getInstance().clearAllData(request.categoryName);
				}
			}.start();
			clearLocalData(request.categoryName);
			return null;
		}

		if(GifCategory.isTagTab(request.categoryName)){
			return data;
		}

		return sortData(request.offset,request.limit,data);
	}

	private synchronized void clearLocalData(final String categoryName) {
		ArrayList<GifItem> data=tagData.get(categoryName);
		if(data!=null&&data.size()>0){
			data.clear();
			tagData.put(categoryName,data);
		}

		data=tabData.get(categoryName);
		if(data!=null&&data.size()>0){
			data.clear();
			tabData.put(categoryName,data);
		}

		nextPos.remove(categoryName);
	}

	private List<GifItem> sortData(final int offset, final int count, final List<GifItem> data){
		ArrayList<GifItem> list=new ArrayList<>();
		HSLog.d("request offset=" + offset + ",count=" + count+", total="+data.size());
		for(int i=offset;i<offset+count&&i<data.size();i++){
			list.add(data.get(i));
		}
		return list;
	}

	public final synchronized void sendTabDataToLocal(final String categoryName, final List<GifItem> list, final String next) {
		final String pos= nextPos.get(categoryName);
		if(pos!=null&&next.equals(pos)){
			return;
		}

		ArrayList<GifItem> data=tabData.get(categoryName);
		if(data==null){
			data=new ArrayList<>();
			tabData.put(categoryName,data);
		}
		data.addAll(list);

		nextPos.put(categoryName,next);
		new Thread(){
			@Override
			public void run() {
				DaoHelper.getInstance().updateRequestLastUpdateTime(categoryName);
				DaoHelper.getInstance().saveTabDataToDB(categoryName,list);
			}
		}.start();
	}

	public final synchronized void sendTagDataToLocal(final String categoryName, final List<GifItem> list, final String next) {
		final String pos= nextPos.get(categoryName);
		if(pos!=null&&next.equals(pos)){
			return;
		}

		ArrayList<GifItem> data=tabData.get(categoryName);
		if(data==null){
			data=new ArrayList<>();
			tagData.put(categoryName,data);
		}
		//save to tag
		new Thread(){
			@Override
			public void run() {
				DaoHelper.getInstance().updateRequestLastUpdateTime(categoryName);
				DaoHelper.getInstance().saveTagDataToDB(categoryName,list);
			}
		}.start();

		data.addAll(list);

		nextPos.put(categoryName,next);
	}

	public final boolean isRemoteHasMore(final String categoryName){
		if(nextPos.get(categoryName)==null){
			return true;
		}
		final String next=nextPos.get(categoryName);
		return !next.equals("0");
	}

	public final String getNextPos(final String categoryName){
		return nextPos.get(categoryName);
	}

	public synchronized void sendRecentDataToLocal(GifItem item) {
		ArrayList<GifItem> recent=tabData.get(GifCategory.TAB_RECENT);
		if(recent==null){
			recent=new ArrayList<>();
			tabData.put(GifCategory.TAB_RECENT,recent);
		}
		recent.remove(item);
		recent.add(0,item);//add recent in the first position
		if(recent.size()>RECENT_SIZE){
			recent.remove(RECENT_SIZE);
		}
	}

	public boolean isAddedToFavorite(final GifItem item) {
		ArrayList<GifItem> data = tabData.get(GifCategory.TAB_FAVORITE);
		return data != null && data.contains(item);
	}

	public synchronized void addFavorite(GifItem item) {
		ArrayList<GifItem> favorite=tabData.get(GifCategory.TAB_FAVORITE);
		if(favorite==null){
			favorite=new ArrayList<>();
			favorite.addAll(DaoHelper.getInstance().getAllTabData(GifCategory.TAB_FAVORITE));
			tabData.put(GifCategory.TAB_FAVORITE,favorite);
		}
		if(!favorite.contains(item)){
			favorite.add(item);
			HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.KEYBOARD_GIF_FAVORITE_ADDED);
		}
	}

	public synchronized void removeFavorite(GifItem item) {
		ArrayList<GifItem> favorite=tabData.get(GifCategory.TAB_FAVORITE);
		if(favorite==null){
			favorite=new ArrayList<>();
			favorite.addAll(DaoHelper.getInstance().getAllTabData(GifCategory.TAB_FAVORITE));
			tabData.put(GifCategory.TAB_FAVORITE,favorite);
		}
		favorite.remove(item);
		HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.KEYBOARD_GIF_FAVORITE_DELETE);
		if(favorite.size()==0){
			new Thread(){
				@Override
				public void run() {
					DaoHelper.getInstance().clearAllData(GifCategory.TAB_FAVORITE);
				}
			}.start();
		}
	}

	private void saveUserDataToDB(){
		new Thread(){
			@Override
			public void run() {
				final List<GifItem> recent=tabData.get(GifCategory.TAB_RECENT);
				if(recent!=null&&recent.size()>0){
					DaoHelper.getInstance().clearAllData(GifCategory.TAB_RECENT);
					DaoHelper.getInstance().saveTabDataToDB(GifCategory.TAB_RECENT,recent);
				}

				final List<GifItem> favorite=tabData.get(GifCategory.TAB_FAVORITE);
				if(favorite!=null&&favorite.size()>0){
					DaoHelper.getInstance().clearAllData(GifCategory.TAB_FAVORITE);
					DaoHelper.getInstance().saveTabDataToDB(GifCategory.TAB_FAVORITE,favorite);
				}
			}
		}.start();
	}

	public void switchLanguage() {
		DaoHelper.getInstance().switchLanguage();
		for(final String category:tabData.keySet()){
			if(!GifCategory.isUserTab(category)&&tabData.get(category)!=null){
				tabData.get(category).clear();
			}
		}

		tagData.clear();

		nextPos.clear();
		
		HSGlobalNotificationCenter.sendNotification(HS_NOTIFICATION_SWITCH_LANGUAGE);
		new Thread(){
			@Override
			public void run() {
				DaoHelper.getInstance().clearAllData();
			}
		}.start();
	}

}
