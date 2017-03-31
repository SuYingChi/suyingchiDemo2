package com.ihs.booster.common.expandablelist.data;

public class BaseItemData {
    protected boolean isChecked = true;
    private BaseGroupData groupData;

    public BaseGroupData getGroupData() {
        return groupData;
    }

    public void setGroupData(BaseGroupData groupData) {
        this.groupData = groupData;
    }

    public BaseItemData() {
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isSelected) {
        this.isChecked = isSelected;
    }

    public void switchCheckedState() {
        isChecked = !isChecked;
    }
}
