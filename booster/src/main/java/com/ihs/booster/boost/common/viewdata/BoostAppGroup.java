package com.ihs.booster.boost.common.viewdata;


import com.ihs.booster.common.expandablelist.data.BaseGroupData;
import com.ihs.booster.common.expandablelist.data.BaseItemData;

/**
 * Created by sharp on 16/1/14.
 */
public class BoostAppGroup extends BaseGroupData {
    private int iconResID = 0;
    private String groupTitle;

    public BoostAppGroup() {

    }

    public BoostAppGroup(String groupTitle) {
        setGroupTitle(groupTitle);
    }

    public BoostAppGroup(String groupTitle, boolean isChecked) {
        setGroupTitle(groupTitle);
        setIsChecked(isChecked);
    }


    public int getIconResID() {
        return iconResID;
    }

    public void setIconResID(int iconResID) {
        this.iconResID = iconResID;
    }


    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public long getCheckedSize() {
        long size = 0;
        for (BaseItemData boostApp : childrenList) {
            if (boostApp.isChecked()) {
                size += ((BoostApp) boostApp).getSize();
            }
        }
        return size;
    }

    public long getTotalSize() {
        long size = 0;
        for (BaseItemData boostApp : childrenList) {
            size += ((BoostApp) boostApp).getSize();
        }
        return size;
    }
}
