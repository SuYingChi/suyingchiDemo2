package com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request;

import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base.LanguageDao;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.callback.UICallback;

import java.util.HashMap;
import java.util.List;

public class BaseRequest {

	public String url=null;

	public String categoryName;

	public HashMap<String,String> params=new HashMap<>();
	public UICallback callback;

	//use for inner identify request
	public int offset=0;
	public int limit=20;

	private StringBuilder addedParams=new StringBuilder();

	public String getUrl(){
		if(addedParams!=null&&addedParams.length()>0){
			final String keyValue= HSConfig.optString(REQUEST_VALUE_KEY,"Application", "StickersGifs", "RiffsyKey");
			addedParams.append("&").append(REQUEST_KEY_KEY).append("=").append(keyValue);
			addedParams.append("&").append(REQUEST_KEY_LOCALE).append("=").append(LanguageDao.getCurrentLanguageForDB());
			final String strParams=addedParams.toString();
			clearParams();
			return url+strParams;
		}
		return url;
	}

	public HashMap<String, String> getParams() {
		return params;
	}


	public BaseRequest(final UICallback callback) {
		this.callback=callback;
		final String keyValue= HSConfig.optString(REQUEST_VALUE_KEY,"Application", "StickersGifs", "RiffsyKey");
		params.put(REQUEST_KEY_KEY, keyValue);
		params.put(REQUEST_KEY_LOCALE, LanguageDao.getCurrentLanguageForDB());
	}

	public void addParams(final String key,final String value){
		if(value==null||value.trim().length()==0){
			return;
		}
		params.put(key, value);
	}

	public void addParamsToUrl(final String key,final String value){
		if(value==null||value.trim().length()==0){
			return;
		}
		addedParams.append("&").append(key).append("=").append(value);
	}

	public void clearParams(){
		addedParams=new StringBuilder();
		params.clear();
	}

	public void handleComplete(final List<?> data){
		if(callback!=null){
			callback.onComplete(data,this);
		}
	}

	public void handleFail(){
		if(callback!=null){
			callback.onFail();
		}
	}

	public void handleFetchRemote(){
		if(callback!=null){
			callback.onFetchRemote();
		}
	}

	private final static String REQUEST_KEY_KEY ="key";
	private final static String REQUEST_KEY_LOCALE ="locale";
	private final static String REQUEST_VALUE_KEY ="ZJY5JD88TACB";//for rainbowkey

}
