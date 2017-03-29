package com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request;


import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.GifCategory;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.callback.UICallback;

public final class TagRequest extends BaseRequest{

    public TagRequest(final UICallback callback,final String categoryName,final int limit) {
        super(callback);
        this.categoryName=categoryName;
        this.limit=limit;
        addParams("limit",limit+"");
        if(categoryName.equals(GifCategory.TAB_EXPLORE)){
            addParams("type","explore");
        }
        if(categoryName.equals(GifCategory.TAB_REACTIONS)){
            addParams("type","featured");
        }
        if(categoryName.equals(GifCategory.TAB_EMOJI)){
            addParams("type","emoji");
        }
        this.url="http://api.riffsy.com/v1/tags";
    }
}
