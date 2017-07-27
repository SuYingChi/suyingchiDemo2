package com.ihs.inputmethod.uimodules.softgame;

import com.ihs.commons.utils.HSLog;

import org.json.JSONObject;


import softgames.de.softgamesapilib.GameLoadedCallback;
import softgames.de.softgamesapilib.SoftgamesSDK;
import softgames.de.softgamesapilib.SoftgamesSearchConfig;

/**
 * Created by liuzhongtao on 17/7/17.
 */

public class SoftGames {
    private static final String partnerId = "pub-13352-13691";

    public static void loadPopularGames() {
        final SoftgamesSearchConfig sgConfig = new SoftgamesSearchConfig(partnerId);
        sgConfig.setSortByPopularity(true);
        sgConfig.setLimit(5);
        SoftgamesSDK.loadGamesInfo(sgConfig, new GameLoadedCallback() {
            @Override
            public void onGamesLoaded(JSONObject[] jsonObjects) {
                for (JSONObject jsonObject : jsonObjects) {
                    HSLog.d("loadPopularGames: " + jsonObject.toString());
                }
            }
        });
    }

    private static void loadGamesInfo() {
        final SoftgamesSearchConfig sgConfig = new SoftgamesSearchConfig(partnerId);
        sgConfig.setLimit(10);
        sgConfig.addCustomField("title");
        sgConfig.addCustomField("thumbBig");
        sgConfig.setLocale("en");

        //look for games!
        SoftgamesSDK.loadGamesInfo(sgConfig, new GameLoadedCallback() {
            @Override
            public void onGamesLoaded(JSONObject[] jsonObjects) {
                for (JSONObject jsonObject : jsonObjects) {
                    HSLog.d("loadGamesInfo: " + jsonObject.toString());
                }
            }
        });
    }
}
