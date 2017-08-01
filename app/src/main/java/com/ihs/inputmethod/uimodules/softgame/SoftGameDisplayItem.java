package com.ihs.inputmethod.uimodules.softgame;


import android.text.TextUtils;

import org.json.JSONObject;

/**
 * Created by yanxia on 2017/7/28.
 */

public class SoftGameDisplayItem {
    private JSONObject jsonObject;

    public SoftGameDisplayItem(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public String getThumbBig() {
        if (jsonObject == null) {
            return "";
        } else {
            return jsonObject.optString("thumbBig");
        }
    }

    public String getTitle() {
        if (jsonObject == null) {
            return "";
        } else {
            String title = jsonObject.optString("title");
            if (TextUtils.isEmpty(title)) {
                return jsonObject.optString("project");
            } else {
                return jsonObject.optString("title");
            }
        }
    }

    public String getType() {
        if (jsonObject == null) {
            return "";
        } else {
            return jsonObject.optString("type");
        }
    }
}
