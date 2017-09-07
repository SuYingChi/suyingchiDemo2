package com.ihs.inputmethod.uimodules.ui.customize.item;

import android.os.Parcel;
import android.os.Parcelable;

import com.ihs.inputmethod.uimodules.ui.customize.WallpaperInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lz on 02/05/2017.
 */

public class WallpaperPackageInfo implements Parcelable {

    public String mTag;
    public String mSlogan;
    public String mLocale;
    public String mName;
    public String mAvatar;
    public List<WallpaperInfo> mWallpaperList = new ArrayList<>();

    public WallpaperPackageInfo() {

    }

    protected WallpaperPackageInfo(Parcel in) {
        mTag = in.readString();
        mSlogan = in.readString();
        mLocale = in.readString();
        mName = in.readString();
        mAvatar = in.readString();
        in.readTypedList(mWallpaperList, WallpaperInfo.CREATOR);
    }

    public static final Creator<WallpaperPackageInfo> CREATOR = new Creator<WallpaperPackageInfo>() {
        @Override
        public WallpaperPackageInfo createFromParcel(Parcel in) {
            return new WallpaperPackageInfo(in);
        }

        @Override
        public WallpaperPackageInfo[] newArray(int size) {
            return new WallpaperPackageInfo[size];
        }
    };


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTag);
        dest.writeString(mSlogan);
        dest.writeString(mLocale);
        dest.writeString(mName);
        dest.writeString(mAvatar);
        dest.writeTypedList(mWallpaperList);
    }
}
