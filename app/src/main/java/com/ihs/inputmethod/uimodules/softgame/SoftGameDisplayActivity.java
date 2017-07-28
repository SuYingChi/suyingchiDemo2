package com.ihs.inputmethod.uimodules.softgame;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.*;
import android.support.v7.widget.DividerItemDecoration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import org.json.JSONObject;

import java.util.ArrayList;

import softgames.de.softgamesapilib.GameEventHandler;
import softgames.de.softgamesapilib.GameLoadedCallback;
import softgames.de.softgamesapilib.SoftgamesSDK;
import softgames.de.softgamesapilib.SoftgamesSearchConfig;


public class SoftGameDisplayActivity extends HSAppCompatActivity implements SoftGameItemAdapter.OnSoftGameItemClickListener {

    public static final String SOFT_GAME_PLACEMENT_MESSAGE = "soft_game_placement_msg";
    public static final int SOFT_GAME_LOAD_COUNT = 50;
    private static final String partnerId = "pub-13352-13691";

    private ArrayList<SoftGameDisplayItem> softGameDisplayItemArrayList = new ArrayList<>();

    private RecyclerView recyclerView;
    private SoftGameItemAdapter softGameItemAdapter;
    private NativeAdView nativeAdView;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soft_game_display);

        recyclerView = (RecyclerView) findViewById(R.id.soft_game_main_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(HSApplication.getContext(), LinearLayoutManager.VERTICAL, false));
        initNativeAdView();
        softGameItemAdapter = new SoftGameItemAdapter(softGameDisplayItemArrayList, this, nativeAdView);
        recyclerView.setAdapter(softGameItemAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        final SoftgamesSearchConfig sgConfig = new SoftgamesSearchConfig(partnerId);
        sgConfig.setSortByPopularity(true);
        sgConfig.setLimit(SOFT_GAME_LOAD_COUNT);
        SoftgamesSDK.loadGamesInfo(sgConfig, new GameLoadedCallback() {
            @Override
            public void onGamesLoaded(JSONObject[] jsonObjects) {
                for (JSONObject jsonObject : jsonObjects) {
                    HSLog.d("loadPopularGames: " + jsonObject.toString());
                    SoftGameDisplayItem softGameDisplayItem = new SoftGameDisplayItem(SoftGameDisplayItem.TYPE_GAME);
                    softGameDisplayItem.setJsonObject(jsonObject);
                    softGameDisplayItemArrayList.add(softGameDisplayItem);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        softGameItemAdapter.refreshData(softGameDisplayItemArrayList);
                    }
                });
            }
        });
    }

    private void initNativeAdView() {
        if (nativeAdView == null) {
            View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_theme_card, null);
            LinearLayout loadingView = (LinearLayout) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_loading_3, null);
            int width = HSDisplayUtils.getScreenWidthForContent() - HSDisplayUtils.dip2px(16);
            LinearLayout.LayoutParams loadingLP = new LinearLayout.LayoutParams(width, (int) (width / 1.9f));
            loadingView.setLayoutParams(loadingLP);
            loadingView.setGravity(Gravity.CENTER);
            nativeAdView = new NativeAdView(HSApplication.getContext(), view, loadingView);
            nativeAdView.configParams(new NativeAdParams(HSApplication.getContext().getString(R.string.ad_placement_themetryad), width, 1.9f));
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (nativeAdView != null) {
            nativeAdView.release();
            nativeAdView = null;
        }
        super.onDestroy();
    }

    @Override
    public void OnSoftGameItemClick(SoftGameDisplayItem softGameDisplayItem) {
        SoftgamesSDK.openGame(softGameDisplayItem.getJsonObject(), SoftGameDisplayActivity.this, partnerId, new GameEventHandler() {
            @Override
            public void levelFinished(int i, float v) {

            }

            @Override
            public void levelUp(int i, float v) {

            }

            @Override
            public void gameOver(int i, float v) {

            }

            @Override
            public void levelStarted(int i) {

            }
        });
    }
}
