package com.keyboard.inputmethod.panels.gif.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ihs.inputmethod.theme.HSKeyboardThemeManager;
import com.keyboard.rainbow.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by dsapphire on 16/1/11.
 */
public final class TabViewAdapter extends RecyclerView.Adapter<TabViewAdapter.TagViewHolder> {

	public interface OnTabChangeListener {
		void onTabChanged(String tabName);
	}

	private OnTabChangeListener mListener;

	private List<String> mTabs;
	private LayoutInflater mInflater;
	private volatile String currentTab="";
	private HashMap<String,Drawable> mTabViews;
	private HashMap<String,View> mTabImageViews;

	private volatile int mWidth;
	public TabViewAdapter(List<String> tabs,Context context, OnTabChangeListener listener){
		mInflater = LayoutInflater.from(context);
		this.mListener =listener;
		mTabs=tabs;
		mTabViews=new HashMap<>();
		mTabImageViews=new HashMap<>();
		mWidth= (int) (context.getResources().getDisplayMetrics().widthPixels*0.835);
	}

	@Override
	public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new TagViewHolder(mInflater.inflate(
				R.layout.gif_panel_tab_icon, parent, false));
	}

	@Override
	public void onBindViewHolder(final TagViewHolder holder,final int position) {
		final String tab=mTabs.get(position);
		Drawable tabView;
		if(mTabViews.get(tab)==null){
			//tab view icon
			tabView= HSKeyboardThemeManager.getTabbarCategoryIconDrawable(tab);
			mTabViews.put(tab,tabView);
		}else {
			tabView=mTabViews.get(tab);
		}
		holder.iv.setImageDrawable(tabView);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onTabClicked(tab,false);
			}
		});

		mTabImageViews.put(tab,holder.iv);
		if(tab.equals(currentTab)){
			//first time
			holder.iv.setSelected(true);
		}

		RecyclerView.LayoutParams lp= (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
		lp.width=mWidth/mTabs.size();
		holder.itemView.setLayoutParams(lp);
	}

	private void onTabClicked(final String tab,final boolean force) {
		if(force){
			setCurrentTab(tab);
		}else if(!currentTab.equals(tab)){
			setCurrentTab(tab);
		}

	}

	private void setCurrentTab(String tab) {
		currentTab=tab;
		if(mListener!=null){
			mListener.onTabChanged(currentTab);
		}
		clearSelection();
		final View iv=mTabImageViews.get(tab);
		if(iv!=null)
			iv.setSelected(true);
	}

	private void clearSelection() {
		for(final String tab:mTabs){
			final View iv=mTabImageViews.get(tab);
			if(iv!=null)
				iv.setSelected(false);
		}
	}

	public void addTab(final int index,final String tab){
		if(!mTabs.contains(tab)){
			if(index>-1&&index<mTabs.size()){
				mTabs.add(index,tab);
			}else {
				mTabs.add(mTabs.size(),tab);
			}
			clearSelection();
			notifyDataSetChanged();
		}
	}

	public void removeTab(final String tab){
		if(mTabs.contains(tab)){
			mTabs.remove(tab);
			clearSelection();
			notifyDataSetChanged();
		}
	}

	public float getTabX(final String tab){
		if(mTabImageViews.get(tab)!=null){
			return mWidth/mTabs.size()*mTabs.indexOf(tab) + mTabImageViews.get(tab).getX();
		}
		return -1;
	}

	public float getTabY(final String tab){
		if(mTabImageViews.get(tab)!=null){
			return mTabImageViews.get(tab).getY();
		}
		return -1;
	}

	@Override
	public int getItemCount() {
		if(mTabs==null){
			return 0;
		}
		return mTabs.size();
	}


	public void setCurrentTab(final String tab,final String defaultTab) {
		if(mTabs.size()==0){
			return;
		}
		if(mTabs.contains(tab)){
			onTabClicked(tab,true);
		}else{
			setCurrentTab(defaultTab,mTabs.get(0));
		}
	}

	class TagViewHolder extends RecyclerView.ViewHolder {
		ImageView iv;
		public TagViewHolder(View view) {
			super(view);
			iv= (ImageView) view.findViewById(R.id.gif_tab_icon_iv);
		}
	}
}
