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

import static com.ihs.inputmethod.uimodules.ui.sticker.StickerPanelManager.STICKER_RECENT;

/**
 * for common use
 */

public abstract class BaseTabViewAdapter extends RecyclerView.Adapter<BaseTabViewAdapter.TagViewHolder> {

    public interface OnTabChangeListener {
        void onTabChanged(String tabName);
    }

    private OnTabChangeListener onTabChangeListener;

    protected List<String> tabNameList;
    protected volatile String currentTab = "";
    private HashMap<String, Drawable> tabViews;
    protected HashMap<String, View> tabImageViews;

    public BaseTabViewAdapter(List<String> tabNameList, OnTabChangeListener onTabChangeListener) {
        this.onTabChangeListener = onTabChangeListener;
        this.tabNameList = tabNameList;
        tabViews = new HashMap<>();
        tabImageViews = new HashMap<>();
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TagViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.common_tab_icon, parent, false));
    }

    @Override
    public void onBindViewHolder(final TagViewHolder holder, final int position) {
        final String tabName = tabNameList.get(position);
        Drawable tabView;
        if (tabViews.get(tabName) == null) {
            tabView = getTabView(tabName);
            tabViews.put(tabName, tabView);
        } else {
            tabView = tabViews.get(tabName);
        }
        if (tabView != null) {
            holder.iv_tab_icon.setImageDrawable(tabView);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTabClicked(tabName, false);
            }
        });

        tabImageViews.put(tabName, holder.iv_tab_icon);
        if (tabName.equals(currentTab)) {
            holder.iv_tab_icon.setSelected(true);
        }
    }

    protected abstract Drawable getTabView(String tab);


    private void onTabClicked(final String tab, final boolean force) {
        if (force) {
            setCurrentTab(tab);
        } else if (!currentTab.equals(tab)) {
            setCurrentTab(tab);
        }

    }

    private void setCurrentTab(String tab) {
        setTabSelected(tab);
        if (onTabChangeListener != null) {
            onTabChangeListener.onTabChanged(currentTab);
        }
    }

    protected void clearSelection() {
        for (final String tab : tabNameList) {
            final View iv = tabImageViews.get(tab);
            if (iv != null) {
                iv.setSelected(false);
            }
        }
    }

    public void addTab(final int index, final String tab) {
        if (!tabNameList.contains(tab)) {
            if (index > -1 && index < tabNameList.size()) {
                tabNameList.add(index, tab);
            } else {
                tabNameList.add(tabNameList.size(), tab);
            }
            clearSelection();
            notifyDataSetChanged();
        }
    }

    public void removeTab(final String tab) {
        if (tabNameList.contains(tab)) {
            tabNameList.remove(tab);
            clearSelection();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        if (tabNameList == null) {
            return 0;
        }
        return tabNameList.size();
    }

    /**
     * will call onTabChanged method
     *
     * @param tab
     * @param defaultTab
     */
    public void setCurrentTab(final String tab, final String defaultTab) {
        if (tabNameList.size() == 0) {
            return;
        }
        if (tabNameList.contains(tab)) {
            onTabClicked(tab, true);
        } else {
            setCurrentTab(defaultTab, tabNameList.get(0));
        }
    }

    protected int getTabIndex(final String tab) {
        return tabNameList.indexOf(tab);
    }

    protected View getTabImage(String tab) {
        return tabImageViews.get(tab);
    }

    /**
     * just select tab
     *
     * @param tab
     */
    public void setTabSelected(final String tab) {
        if (tabNameList.size() == 0 || !tabNameList.contains(tab)) {
            return;
        }
        currentTab = tab;
        clearSelection();
        final View iv = tabImageViews.get(tab);
        if (iv != null) {
            iv.setSelected(true);
        }
    }

    public void clearTabs() {
        tabNameList.clear();
        tabImageViews.clear();
    }

    public void setTabNameList(List<String> tabNameList) {
        if (tabNameList != null) {
            this.tabNameList = tabNameList;
            clearSelection();
            notifyDataSetChanged();
        }
    }

    protected class TagViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_tab_icon;

        TagViewHolder(View view) {
            super(view);
            iv_tab_icon = (ImageView) view.findViewById(R.id.tab_icon_iv);
        }
    }

    protected Drawable getBtnDrawable(final String resName) {
        final StateListDrawable tabbarBtnDrawable = new StateListDrawable();
        Drawable pressedDrawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources(resName + "_selected");
        Drawable drawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources(resName + "_unselected");
        tabbarBtnDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
        tabbarBtnDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        tabbarBtnDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        tabbarBtnDrawable.addState(new int[]{}, drawable);
        return tabbarBtnDrawable;
    }

    protected Drawable getStickerTabDrawable(String tabName) {
        if (tabName.contains(STICKER_RECENT)) {
            final StateListDrawable tab_barBtnDrawable = new StateListDrawable();
            Drawable pressedDrawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("tabbar_recent_selected");
            Drawable drawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("tabbar_recent_unselected");
            tab_barBtnDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
            tab_barBtnDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
            tab_barBtnDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
            tab_barBtnDrawable.addState(new int[]{}, drawable);
            return tab_barBtnDrawable;
        } else {
            return null;
        }
    }
}
