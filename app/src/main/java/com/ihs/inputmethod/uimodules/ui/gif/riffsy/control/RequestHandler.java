package com.ihs.inputmethod.uimodules.ui.gif.riffsy.control;


import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest.Method;
import com.ihs.inputmethod.uimodules.constants.Constants;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.TagItem;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.BaseRequest;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.SearchRequest;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.thread.AsyncThreadPools;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;

import org.json.JSONArray;

import java.util.List;

public final class RequestHandler {

    static void handleRequest(final BaseRequest request) {
       AsyncThreadPools.execute(new Runnable() {
            @Override
            public void run() {
	            final List<?> data= DataManager.getInstance().getDataFromLocal(request);
	            if(data==null){
		            if(DataManager.getInstance().isRemoteHasMore(request.categoryName)){
			            UIController.getInstance().getUIHandler().post(new Runnable() {
				            @Override
				            public void run() {
					            request.handleFetchRemote();
				            }
			            });
			            handleStandardRequestRemote(request);
			            return;
		            }
		            UIController.getInstance().getUIHandler().post(new Runnable() {
			            @Override
			            public void run() {
				            request.handleFail();
			            }
		            });
		            return;
	            }
	            UIController.getInstance().getUIHandler().post(new Runnable() {
		            @Override
		            public void run() {
			            request.handleComplete(data);
		            }
	            });
            }
        });
	    
    }


    private static void handleStandardRequestRemote(final BaseRequest request) {
        try {
            final HSHttpConnection conn = new HSHttpConnection(request.getUrl(), Method.GET);

	        if(request.getParams()!=null&&request.getParams().size()>0){
		        conn.setRequestParams(request.getParams());
	        }

            conn.setConnectTimeout(Constants.REQUEST_CONNECT_TIMEOUT);
            conn.setReadTimeout(Constants.REQUEST_READ_TIMEOUT);
	        conn.startSync();
            if (conn.isSucceeded()) {
	            JSONArray response=null;
	            GifItem.Builder builder;
				try {
					response= conn.getBodyJSON().optJSONArray("results");
				}catch (Exception e){
					e.printStackTrace();
				}
	            if(response==null){
		            response= conn.getBodyJSON().getJSONArray("tags");
		            builder=new TagItem.Builder(response);
	            }else{
		            builder=new GifItem.Builder(response);
	            }

	            final List<GifItem> list=builder.buildList();

	            final String next=conn.getBodyJSON().optString("next",null);
	            UIController.getInstance().getUIHandler().post(new Runnable() {
		            @Override
		            public void run() {
			            request.handleComplete(list);
		            }
	            });

	            if(request instanceof SearchRequest){
		            DataManager.getInstance().sendTagDataToLocal(request.categoryName,list,next);
	            }else{
		            DataManager.getInstance().sendTabDataToLocal(request.categoryName,list,next);
	            }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	    UIController.getInstance().getUIHandler().post(new Runnable() {
		    @Override
		    public void run() {
			    request.handleFail();
		    }
	    });
    }



}
