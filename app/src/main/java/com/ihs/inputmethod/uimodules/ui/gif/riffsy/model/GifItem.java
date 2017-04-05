package com.ihs.inputmethod.uimodules.ui.gif.riffsy.model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsapphire on 16/1/20.
 */
public class GifItem{

	public String id="";//name character
	public String url="";//path

	public String gif;//image
	public String mp4;

	public String hdGif;

	public boolean isTag=false;

	public String getUrl() {
		return url;
	}

	public String getId() {
		return id;
	}

	public String getGifUrl(){
		return gif;
	}

	public String getHdGifUri() {
		return hdGif;
	}

	public String getMp4Url(){
		return mp4;
	}

	public boolean isTag(){
		return isTag;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GifItem data = (GifItem) o;
		return id.equals(data.id);
	}

	public static class Builder{

		public JSONArray item;

		public Builder(JSONArray item){
			this.item=item;
		}

		public List<GifItem> buildList(){
			List<GifItem> datas=new ArrayList<>();
			try {
				for (int i=0;i<item.length();i++){
					JSONObject object= (JSONObject) item.get(i);
					GifItem data=getData(object);
					if(data!=null){
						datas.add(data);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return datas;
		}

		protected GifItem getData(JSONObject object) throws JSONException {
			GifItem data=new GifItem();
			data.id=object.optString("id");
			data.url=object.optString("url");
			JSONObject media= (JSONObject) object.optJSONArray("media").get(0);

			JSONObject gif=media.optJSONObject("nanogif");
			if(gif==null){
				gif=media.optJSONObject("tinygif");
			}
			if(gif==null){
				gif=media.optJSONObject("gif");
			}
			data.gif=gif.optString("url",null);

			JSONObject hdGif=media.optJSONObject("gif");
			if(hdGif == null) {
				hdGif=media.optJSONObject("tinygif");
			}
			if(hdGif==null){
				hdGif=media.optJSONObject("nanogif");
			}
			data.hdGif=hdGif.optString("url",null);

			JSONObject mp4=media.optJSONObject("loopedmp4");
			if(mp4==null){
				mp4=media.optJSONObject("nanomp4");
			}
			if(mp4==null){
				mp4=media.optJSONObject("tinymap4");
			}
			if(mp4==null){
				mp4=media.optJSONObject("mp4");
			}
			data.mp4=mp4.optString("url",null);

			return data;
		}

	}

}
