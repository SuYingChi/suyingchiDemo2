package com.ihs.inputmethod.uimodules.ui.gif.riffsy.control;

import java.util.ArrayList;

public final class GifCategory {

    public static final String TAB_RECENT="gif_tab_recent";
    public static final String TAB_FAVORITE ="gif_tab_favorite";
    public static final String TAB_REACTIONS ="gif_tab_reactions";
    public static final String TAB_EXPLORE ="gif_tab_explore" ;
    public static final String TAB_TRENDING ="gif_tab_trending" ;

    public static final String TAB_EMOJI ="gif_tab_emoji" ;//for emoji search


    private String mCurrentCategoryId = "";
    private String mCurrentExtendedCategoryId = "";
    private String mCurrentLogCategoryId = "";
    private ArrayList<String> shownCategories=new ArrayList<>();


    public GifCategory(){
        shownCategories.add(TAB_RECENT);
        shownCategories.add(TAB_FAVORITE);
        shownCategories.add(TAB_REACTIONS);
        shownCategories.add(TAB_EXPLORE);
        shownCategories.add(TAB_TRENDING);
    }
    
    public ArrayList<String> getShownCategories() {
        return shownCategories;
    }

    public String getCurrentCategoryId() {
        return mCurrentCategoryId;
    }

    public String getCurrentExtendedCategoryId() {
        return mCurrentExtendedCategoryId;
    }


    public void setCurrentCategoryId(final String categoryId) {
        mCurrentCategoryId = categoryId;
        setCurrentExtendedCategoryId(categoryId);
        setCurrentLogCategoryId(categoryId);
    }

    public void setCurrentExtendedCategoryId(final String categoryId) {
        mCurrentExtendedCategoryId = categoryId;
    }

    public void setCurrentLogCategoryId(final String categoryId) {
        mCurrentLogCategoryId = categoryId;
        if(categoryId.startsWith("gif_tab_")){
            mCurrentLogCategoryId=categoryId.substring(8);
        }
    }

    public String getCurrentLogCategoryId() {
        return mCurrentLogCategoryId;
    }

    static boolean isUserTab(final String categoryName){
        return TAB_RECENT.equals(categoryName)||TAB_FAVORITE.equals(categoryName);
    }

    public static boolean isTagTab(final String categoryName) {
        return TAB_REACTIONS.equals(categoryName)||TAB_EXPLORE.equals(categoryName)||TAB_EMOJI.equals(categoryName);
    }

}
