package com.ihs.inputmethod.uimodules.softgame;

import com.ihs.commons.utils.HSLog;

import org.json.JSONObject;


import softgames.de.softgamesapilib.GameLoadedCallback;
import softgames.de.softgamesapilib.SoftgamesSDK;
import softgames.de.softgamesapilib.SoftgamesSearchConfig;

/**
 * Created by liuzhongtao on 17/7/17.
 *
 */

public class SoftGames {
    private static final String partnerId = "pub-13352-13691";

    public static void loadGamesInfoHot() {
        final SoftgamesSearchConfig sgConfig = new SoftgamesSearchConfig(partnerId);
        sgConfig.setSortByPopularity(true);
        loadGamesInfo(sgConfig);
    }

    private static void loadGamesInfo(SoftgamesSearchConfig sgConfig) {
        sgConfig.setLimit(10);
        sgConfig.addCustomField("title");
        sgConfig.addCustomField("teaserBig");
        sgConfig.addCustomField("thumb");
        sgConfig.addCustomField("type");
        sgConfig.setLocale("en");

        //look for games!
        SoftgamesSDK.loadGamesInfo(sgConfig, new GameLoadedCallback() {
            @Override
            public void onGamesLoaded(JSONObject[] jsonObjects) {
                String json = "";
                HSLog.d("onGamesLoaded: ");
                for (JSONObject jsonObject : jsonObjects) {
                    HSLog.d(jsonObject.toString());
                }
            }
        });
    }
}
