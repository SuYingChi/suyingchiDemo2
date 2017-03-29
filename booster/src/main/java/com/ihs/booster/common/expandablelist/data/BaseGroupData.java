package com.ihs.booster.common.expandablelist.data;


import java.util.ArrayList;

/**
 * Created by sharp on 16/1/14.
 */
public class BaseGroupData extends BaseItemData {
    protected boolean isExpanded = true;
    protected ArrayList<BaseItemData> childrenList = new ArrayList<>();

    public BaseGroupData() {
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setIsExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public int getChildrenCount() {
        return childrenList.size();
    }

    public BaseItemData getChild(int postion) {
        return childrenList.get(postion);
    }

    public ArrayList<BaseItemData> getChildren() {
        return childrenList;
    }

    public void setChildren(ArrayList<BaseItemData> childrenList) {
        if (childrenList != null) {
            this.childrenList = childrenList;
        }
    }

    public void addChild(BaseItemData childData) {
        this.childrenList.add(childData);
    }

    public void removeChild(BaseItemData childData) {
        this.childrenList.remove(childData);
    }

    public void removeChild(int childIndex) {
        this.childrenList.remove(childIndex);
    }

    public void clearChildren() {
        this.childrenList.clear();
    }

    @Override
    public boolean isChecked() {
        boolean isChecked = true;
        for (BaseItemData itemData : childrenList) {
            if (!itemData.isChecked()) {
                isChecked = false;
                break;
            }
        }
        this.isChecked = isChecked;
        return isChecked;
    }

    @Override
    public void setIsChecked(boolean isSelected) {
        super.setIsChecked(isSelected);
        for (BaseItemData baseItemData : childrenList) {
            baseItemData.setIsChecked(isSelected);
        }
    }
}
