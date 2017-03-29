package com.ihs.inputmethod.uimodules.ui.gif.riffsy.model;


import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by dsapphire on 16/1/20.
 */
public final class TagItem extends GifItem{

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public String getMp4Url() {
		return null;
	}

	public static class Builder extends GifItem.Builder{

		public Builder(JSONArray item){
			super(item);
		}

		@Override
		protected TagItem getData(JSONObject object){
			TagItem data=new TagItem();
			data.isTag=true;
			data.gif=object.optString("image");
			data.id=object.optString("character",null);//for emoji
			if(data.id==null){
				data.id=object.optString("name");//for tag
				if(data.id.toLowerCase().endsWith("gif keyboard")){
					return null;
				}
			}
			data.url=object.optString("path",null);
			return data;
		}
	}

}
