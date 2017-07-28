package com.ihs.inputmethod.uimodules.softgame;

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
}
