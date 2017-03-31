package com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request;


import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.GifCategory;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.callback.UICallback;

public class TrendRequest extends BaseRequest{

    public TrendRequest(final UICallback callback, final String categoryName, final int limit) {
        super(callback);
        this.categoryName=categoryName;
	    setUrl(categoryName);
        addParams("limit",limit+"");
        this.limit=limit;
    }


    private  void setUrl(String categoryName) {
        if(categoryName.equals(GifCategory.TAB_TRENDING)){
            this.url="http://api.riffsy.com/v1/trending";
        }
    }
}
