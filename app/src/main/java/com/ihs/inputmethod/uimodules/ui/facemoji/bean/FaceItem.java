package com.ihs.inputmethod.uimodules.ui.facemoji.bean;

import android.net.Uri;

/**
 * Created by xu.zhang on 3/11/16.
 */
public class FaceItem {

    private Uri uri;

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    private boolean isSelected;

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public FaceItem() {
//
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public FaceItem(Uri uri) {
        this.uri = uri;
    }


    public Uri getUri() {
        return uri;
    }

    public boolean isAddButton() {
        return uri == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (null == uri) {
            return false;
        }

        FaceItem faceItem = (FaceItem) o;

        return uri.equals(faceItem.uri);

    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
