package com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request;


import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.callback.UICallback;

public final class SearchRequest extends BaseRequest {


    public SearchRequest(final UICallback callback,final String keyWord,final int limit) {
        super(callback);
	    this.url= "http://api.riffsy.com/v1/search";
	    categoryName=keyWord;
	    addParams("tag",keyWord);
	    addParams("limit",limit+"");
	    this.limit=limit;
    }

	public SearchRequest(final UICallback callback,final String url,final String keyWord) {
		super(callback);
		this.url= url;
		categoryName=keyWord;
		clearParams();
	}
}
