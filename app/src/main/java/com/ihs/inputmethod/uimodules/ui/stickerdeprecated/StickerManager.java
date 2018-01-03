package com.ihs.inputmethod.uimodules.ui.stickerdeprecated;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSMapUtils;
import com.ihs.commons.utils.HSPlistParser;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.managers.HSPictureManager;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.api.utils.HSPictureUtils;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareChannel;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareUtils;
import com.ihs.inputmethod.uimodules.ui.stickerdeprecated.bean.BaseStickerItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StickerManager {
	private static final String TAG="StickerManager";
	private static final String STATIC_FILE_SUFFIX = ".static";
	private static final int STATIC_FILE_SUFFIX_LEN = STATIC_FILE_SUFFIX.length();

	private final static String STICKER_PLIST_FILE_NAME = "StickerKeyboard.plist";

	private static final String STICKER_ROOT_DIR_NAME = "Stickers";
	private static final String[] sCategoryDirName = {
			"Recents",
			"YellowSmile",
			"BlueSmile",
			"Love",
			"Sports",
			"PreparedFood"};

	public static final int ID_UNSPECIFIED = -1;
	public static final int ID_RECENTS = 0;
	public static final int ID_PREPARED_FOOD = 5;

	// Plist file keys
	private static final String KEY_L0_DATA          = "Data";
	private static final String KEY_L1_APPLICATION   = "Application";
	private static final String KEY_L2_STICKER       = "Sticker";
	private static final String KEY_L4_CONTENT       = "Content";
	private static final String KEY_L4_CLICKED_IMAGE = "ClickedImage";
	private static final String KEY_L4_TAB_IMAGE     = "TabImage";
	private static final String KEY_L4_TITLE         = "Title";
	private static final String KEY_L5_IMAGE         = "Image";
	private static final String KEY_L4_LAPID        = "IapIdentifier";
	private Context mContext;
	private static StickerManager mInstance;

	private int currentSize=StickerCategory.SIZE;

	private HashMap<String,String> allStickers=new HashMap<>();
	private ArrayList<StickerCategory> mStickerCaches = new ArrayList<>();


	private StickerManager(Context context){
		this.mContext=context;
	}

	public static void init(){
		if(mInstance==null){
			mInstance=new StickerManager(HSApplication.getContext());
			AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
				@Override
				public void run() {
					mInstance.loadStickerFile();
				}
			});
		}
	}

	public static StickerManager getInstance(){
		if(mInstance==null){
			init();
		}
		return mInstance;
	}

	private void loadStickerFile(){
		InputStream inputStream = null;
		try {
			inputStream = mContext.getAssets().open(STICKER_PLIST_FILE_NAME);
			Map<String, Object> stickerMap = (Map<String, Object>) HSPlistParser.parse(inputStream, HSPlistParser.isPAEncrypted(STICKER_PLIST_FILE_NAME));
			allStickers.clear();
			mStickerCaches.clear();
			loadStickers(stickerMap);
		} catch (IOException e) {
			Log.e(TAG, "load file error"+e.getMessage());
			return;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void loadStickers(Map<String, Object> stickerMap){
		List<?> stickersList = HSMapUtils.getList(stickerMap, KEY_L0_DATA, KEY_L1_APPLICATION, KEY_L2_STICKER);
		StickerCategory si;
		String fileName;
		Map<String, String> imageMap;
		int category=ID_RECENTS;
		List<?> recentList=null;
		for(Object o:stickersList){
			si = new StickerCategory();
			si.clickedImage = HSMapUtils.getString((Map<String, ?>)o, KEY_L4_CLICKED_IMAGE);
			si.tabImage = HSMapUtils.getString((Map<String, ?>)o, KEY_L4_TAB_IMAGE);
			si.title = HSMapUtils.getString((Map<String, ?>)o, KEY_L4_TITLE);
			si.identifier=HSMapUtils.getString((Map<String, ?>)o, KEY_L4_LAPID);
			Log.d(TAG,"load sticker category "+si.title);
			if(category!=ID_RECENTS){
				List<?> imageList  = HSMapUtils.getList((Map<String, ?>)o, KEY_L4_CONTENT);
				for(Object oImage:imageList){
					imageMap = (Map<String, String>)oImage;
					fileName = imageMap.get(KEY_L5_IMAGE);
					BaseStickerItem item=new BaseStickerItem();
					item.name=getStickerFileNameTrimStatic(fileName);
					item.url=Uri.parse("asset:///"+getStickerFilePath(category,getStickerFileNameTrimStatic(fileName))).toString();
					allStickers.put(fileName,item.url);
					si.list.add(item);
				}
				si.pageSize=(int)Math.ceil((si.list.size()*1.0)/currentSize);
			}else{
				si.pageSize=1;
				recentList= HSMapUtils.getList((Map<String, ?>)o, KEY_L4_CONTENT);
			}

			mStickerCaches.add(si);
			category++;
		}
		//recent
		si=mStickerCaches.get(0);
		if(recentList!=null){
			for(Object oImage:recentList){
				imageMap = (Map<String, String>)oImage;
				fileName = imageMap.get(KEY_L5_IMAGE);
				BaseStickerItem item=new BaseStickerItem();
				item.name=getStickerFileNameTrimStatic(fileName);
				item.url=allStickers.get(fileName);
				si.list.add(item);
			}
			si.pageSize=(int)Math.ceil((si.list.size()*1.0)/currentSize);
		}

	}

	private String getStickerFileNameTrimStatic(final String fileName) {
		if(fileName.endsWith(STATIC_FILE_SUFFIX))
			return fileName.substring(0, fileName.length() - STATIC_FILE_SUFFIX_LEN);
		return fileName;
	}

	private String getStickerFilePath(final int category, final String fileName) {
		return STICKER_ROOT_DIR_NAME + "/" + sCategoryDirName[category] + "/" + fileName;
	}


	public String getCategoryTabIconName(int category){
		if(category>ID_UNSPECIFIED&&category<mStickerCaches.size()&&category<=ID_PREPARED_FOOD){
			return mStickerCaches.get(category).tabImage.toLowerCase();
		}
		return mStickerCaches.get(0).tabImage.toLowerCase();
	}

	public List<BaseStickerItem> getStickerList(int category){
		if(category>ID_UNSPECIFIED&&category<mStickerCaches.size()&&category<=ID_PREPARED_FOOD){
			return mStickerCaches.get(category).list;
		}
		return new ArrayList<>();
	}

	public int getStickerPagerSize(int category){
		if(category>ID_UNSPECIFIED&&category<mStickerCaches.size()){
			StickerCategory temp=mStickerCaches.get(category);
			temp.pageSize=(int)Math.ceil((temp.list.size()*1.0)/currentSize);
			return temp.pageSize;
		}
		return 0;
	}

	public List<BaseStickerItem> getStickerListData(int category,int page){
		List<BaseStickerItem> allData=getStickerList(category);
		List<BaseStickerItem> data=new ArrayList<>();
		int start = currentSize * page;
		int end = start + currentSize;
		end = allData.size() > end ? end : allData.size();
		for(int i=start;i<end;i++){
			data.add(allData.get(i));
		}
		return data;
	}

	public int getCurrentSize(){
		return currentSize;
	}

	public void saveSize(int size){
		currentSize=size;
	}

	static class StickerCategory {
		static final int SIZE=8;
		static final int COL=4;
		static final int ROW=2;
		static final int ROW_LANDSCAPE=1;
		String identifier;
		String clickedImage;
		String tabImage;
		String title;
		int pageSize;
		ArrayList<BaseStickerItem> list=new ArrayList<>();
	}

	public static void shareImage(Uri uri, final String url) {
		final String packageName = HSInputMethod.getCurrentHostAppPackageName();
		final Pair<Integer, String> pair = ShareUtils.getStickerShareMode(packageName);
		final int mode = pair.first;
		final String mimeType = "image/*";
        final String targetFilePath = HSPictureManager.getCacheDirectory() + url.hashCode()+".png";
        String type="save";
		try {
			InputStream inputStream = HSApplication.getContext().getAssets().open(uri.getPath().substring(1));

			// TODO: Replace with FileUtils function.
			copyFile(inputStream, new File(targetFilePath));

			switch (mode) {
				case HSPictureUtils.IMAGE_SHARE_MODE_INTENT:
					type="send";
					HSFileUtils.copyFile(uri.getPath(), targetFilePath);
                    MediaController.getShareManager().shareImageByIntent(Uri.fromFile(new File(targetFilePath)), ShareChannel.CURRENT);
                    break;
				case HSPictureUtils.IMAGE_SHARE_MODE_EXPORT:
					type="save";
                    MediaController.getShareManager().shareImageByExport(uri, targetFilePath);
                    break;

				default:
					type="save";
                    MediaController.getShareManager().shareImageByExport(uri, targetFilePath);
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void copyFile(final InputStream in,final File file) throws IOException {
		byte[] b = getByte(in);
		if(file.exists()){
			file.delete();
		}
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(b);
		fileOutputStream.close();
	}

	private static byte[] getByte(InputStream inputStream) throws IOException {
		byte[] b = new byte[1024];
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int len = -1;
		while ((len = inputStream.read(b)) != -1) {
			byteArrayOutputStream.write(b, 0, len);
		}
		byteArrayOutputStream.close();
		inputStream.close();
		return byteArrayOutputStream.toByteArray();
	}


}
