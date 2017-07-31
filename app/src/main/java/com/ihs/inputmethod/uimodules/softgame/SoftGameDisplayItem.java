package com.ihs.inputmethod.uimodules.softgame;


import android.text.TextUtils;

import org.json.JSONObject;

/**
 * Created by yanxia on 2017/7/28.
 */

public class SoftGameDisplayItem {
    public static final int TYPE_GAME = 0;
    public static final int TYPE_AD = 1;
    private JSONObject jsonObject;
    private int type;

    public SoftGameDisplayItem(int type) {
        this.type = type;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public int getType() {
        return type;
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

    public String getTypeText() {
        if (jsonObject == null) {
            return "";
        } else {
            return jsonObject.optString("type");
        }
    }
}
