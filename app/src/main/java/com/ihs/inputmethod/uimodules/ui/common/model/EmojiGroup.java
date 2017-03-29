package com.ihs.inputmethod.uimodules.ui.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenbinduan on 2016/11/14.
 */

public final class EmojiGroup {

	private final String tabName;
	private final boolean isText;

	private List<Emoji> emojiList=new ArrayList<>();


	public EmojiGroup(final String tabName, final boolean isText) {
		this.tabName = tabName;
		this.isText =isText;
	}

	public String getTabName() {
		return tabName;
	}

	public boolean isText() {
		return isText;
	}

	public List<Emoji> getEmojiList() {
		if(emojiList==null){
			emojiList=new ArrayList<>();
		}
		return emojiList;
	}

	public void addEmoji(final Emoji emoji){
		emojiList.add(emoji);
	}

	public void addEmojiToFirst(final Emoji emoji){
		emojiList.add(0,emoji);
	}

	public void removeLastEmoji(){
		emojiList.remove(emojiList.size()-1);
	}

	public void removeEmoji(final Emoji emoji){
		emojiList.remove(emoji);
	}

	public int size(){
		return emojiList.size();
	}

	public void clearEmoji(){
		emojiList.clear();
	}

	@Override
	public String toString() {
		return tabName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EmojiGroup group = (EmojiGroup) o;

		return tabName != null ? tabName.equals(group.tabName) : group.tabName == null;

	}

	@Override
	public int hashCode() {
		return tabName != null ? tabName.hashCode() : 0;
	}

	private void addDivider(final int rowCount) {
		if(emojiList.size()>0){
			for(int i=0;i<rowCount;i++){
				final Emoji divider=new Emoji("",0,isText());
				divider.setDivider(true);
				emojiList.add(divider);
			}
		}
	}
	
	public void sortTextArt(final int rowCount) {
		int[] spanCount=new int[rowCount];
		int minIndex=0;
		for(Emoji emoji:emojiList){
			spanCount[minIndex]+=emoji.getColumn();
			minIndex=getMinCountIndex(spanCount,rowCount);
		}
		int max=getMaxIndex(spanCount,rowCount);
		for(int span:spanCount){
			if(max-span>0){
				final Emoji empty=new Emoji("",max-span,isText());
				emojiList.add(empty);
			}
		}
		addDivider(rowCount);
	}

	private int getMinCountIndex(final int[] spanCount,final int rowCount) {
		int index=0;
		int  min=spanCount[0];
		for(int i=0;i<rowCount;i++){
			if(spanCount[i]<min){
				min=spanCount[i];
				index=i;
			}
		}
		return index;
	}

	private int getMaxIndex(final int[] spanCount,final int rowCount) {
		int  max=spanCount[0];
		for(int i=0;i<rowCount;i++){
			if(spanCount[i]>max){
				max=spanCount[i];
			}
		}
		return max;
	}
	
	public void sortEmoji(final int rowCount) {
		while(emojiList.size()%rowCount!=0){
			final Emoji empty=new Emoji("",1,isText());
			emojiList.add(empty);
		}
		addDivider(rowCount);
	}

}
