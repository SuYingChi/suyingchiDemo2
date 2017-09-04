package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.app.WallpaperInfo;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.crashlytics.android.core.CrashlyticsCore;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.feature.common.LauncherConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guonan.lv on 17/9/2.
 */

public class CategoryInfo implements Parcelable {

    public String identifier;
    public String iconUrl;
    public String categoryName;
    public String bannerUrl;
    public int bannerPlaceholderBgColor;
    public List<WallpaperInfo> wallpapers = new ArrayList<>();

    private CategoryInfo() {
    }

    protected CategoryInfo(Parcel in) {
        identifier = in.readString();
        iconUrl = in.readString();
        categoryName = in.readString();
        bannerUrl = in.readString();
        in.readTypedList(wallpapers, WallpaperInfo.CREATOR);
    }

    public static final Creator<CategoryInfo> CREATOR = new Creator<CategoryInfo>() {
        @Override
        public CategoryInfo createFromParcel(Parcel in) {
            return new CategoryInfo(in);
        }

        @Override
        public CategoryInfo[] newArray(int size) {
            return new CategoryInfo[size];
        }
    };

    @SuppressWarnings("unchecked")
    public static CategoryInfo ofConfig(Map<String, ?> config) {
        CategoryInfo info = new CategoryInfo();
        try {
            info.identifier = (String) config.get("Identifier");
            info.iconUrl = (String) config.get("Icon");
            info.categoryName = LauncherConfig.getMultilingualString(config, "CategoryName");
            info.bannerUrl = (String) config.get("Banner");
            String colorString = (String) config.get("BannerPlaceholderBg");
            if (colorString == null) {
                colorString = "#777777";
            }
            info.bannerPlaceholderBgColor = Color.parseColor(colorString);
        } catch (Exception e) {
            HSLog.w("Wallpaper.Config", "Error loading wallpaper config, please check config format");
            CrashlyticsCore.getInstance().logException(e);
        }
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(iconUrl);
        dest.writeString(categoryName);
        dest.writeString(bannerUrl);
        dest.writeTypedList(wallpapers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof String) {
            return categoryName != null && categoryName.equalsIgnoreCase((String) o);
        } else if (!(o instanceof CategoryInfo)) {
            return false;
        }
        CategoryInfo that = (CategoryInfo) o;
        return categoryName != null ? categoryName.equals(that.categoryName) : that.categoryName == null;
    }

    @Override
    public int hashCode() {
        return categoryName != null ? categoryName.hashCode() : 0;
    }
}

