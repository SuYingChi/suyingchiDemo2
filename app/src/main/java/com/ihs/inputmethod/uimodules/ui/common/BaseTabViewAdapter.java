package com.ihs.inputmethod.uimodules.ui.common;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.HashMap;
import java.util.List;


/**
 * for common use
 */

public abstract class BaseTabViewAdapter extends RecyclerView.Adapter<BaseTabViewAdapter.TagViewHolder> {

	public interface OnTabChangeListener {
		void onTabChanged(String tabName);
	}

	private OnTabChangeListener listener;

	private List<String> tabs;
	private volatile String currentTab="";
	private HashMap<String,Drawable> tabViews;
	private HashMap<String,View> tabImageViews;

	public BaseTabViewAdapter(List<String> tabs, OnTabChangeListener listener){
		this.listener =listener;
		this.tabs =tabs;
		tabViews =new HashMap<>();
		tabImageViews =new HashMap<>();
	}

	@Override
	public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new TagViewHolder(LayoutInflater.from(parent.getContext()).inflate(
				R.layout.common_tab_icon, parent,false));
	}

	@Override
	public void onBindViewHolder(final TagViewHolder holder,final int position) {
		final String tab= tabs.get(position);
		Drawable tabView;
		if(tabViews.get(tab)==null){
			tabView=getTabView(tab);
			tabViews.put(tab,tabView);
		}else {
			tabView= tabViews.get(tab);
		}
		holder.iv.setImageDrawable(tabView);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onTabClicked(tab,false);
			}
		});

		tabImageViews.put(tab,holder.iv);
		if(tab.equals(currentTab)){
			holder.iv.setSelected(true);
		}
	}

	protected abstract Drawable getTabView(String tab);


	private void onTabClicked(final String tab,final boolean force) {
		if(force){
			setCurrentTab(tab);
		}else if(!currentTab.equals(tab)){
			setCurrentTab(tab);
		}

	}

	private void setCurrentTab(String tab) {
		setTabSelected(tab);
		if(listener !=null){
			listener.onTabChanged(currentTab);
		}
	}

	private void clearSelection() {
		for(final String tab: tabs){
			final View iv= tabImageViews.get(tab);
			if(iv!=null)
				iv.setSelected(false);
		}
	}

	public void addTab(final int index,final String tab){
		if(!tabs.contains(tab)){
			if(index>-1&&index< tabs.size()){
				tabs.add(index,tab);
			}else {
				tabs.add(tabs.size(),tab);
			}
			clearSelection();
			notifyDataSetChanged();
		}
	}

	public void removeTab(final String tab){
		if(tabs.contains(tab)){
			tabs.remove(tab);
			clearSelection();
			notifyDataSetChanged();
		}
	}

	@Override
	public int getItemCount() {
		if(tabs ==null){
			return 0;
		}
		return tabs.size();
	}

	/**
	 * will call onTabChanged method
	 * @param tab
	 * @param defaultTab
	 *
	 */
	public void setCurrentTab(final String tab,final String defaultTab) {
		if(tabs.size()==0){
			return;
		}
		if(tabs.contains(tab)){
			onTabClicked(tab,true);
		}else{
			setCurrentTab(defaultTab, tabs.get(0));
		}
	}

	protected int getTabIndex(final String tab){
		return tabs.indexOf(tab);
	}

	protected View getTabImage(String tab){
		return tabImageViews.get(tab);
	}

	/**
	 * just select tab
	 * @param tab
	 */
	public void setTabSelected(final String tab) {
		if(tabs.size()==0||!tabs.contains(tab)){
			return;
		}
		currentTab=tab;
		clearSelection();
		final View iv= tabImageViews.get(tab);
		if(iv!=null)
			iv.setSelected(true);

	}

	public void clearTabs(){
		tabs.clear();
		tabImageViews.clear();
	}

	public void setTabs(List<String> tabs){
		if(tabs!=null){
			this.tabs=tabs;
			clearSelection();
			notifyDataSetChanged();
		}
	}

	protected class TagViewHolder extends RecyclerView.ViewHolder {
		ImageView iv;
		TagViewHolder(View view) {
			super(view);
			iv= (ImageView) view.findViewById(R.id.tab_icon_iv);
		}
	}
	
	
	protected Drawable getBtnDrawable(final String resName){
		final StateListDrawable tabbarBtnDrawable = new StateListDrawable();
		Drawable pressedDrawable=HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources(resName+"_selected");
		Drawable drawable=HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources(resName+"_unselected");
		tabbarBtnDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
		tabbarBtnDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
		tabbarBtnDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
		tabbarBtnDrawable.addState(new int[]{}, drawable);
		return tabbarBtnDrawable;
	}
}
