package com.ihs.inputmethod.uimodules.softgame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.iap.RemoveAdsManager;

import net.appcloudbox.ads.interstitialads.AcbInterstitialAdLoader;


public class GameActivity extends AppCompatActivity {

    private static final int TIME_OUT_LIMIT = 5000;
    private Handler handler = new Handler();
    private boolean adShowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();//getIntent将该项目中包含的原始intent检索出来，将检索出来的intent赋值给一个Intent类型的变量intent
        String url = intent.getStringExtra("url");
        if (android.text.TextUtils.isEmpty(url)) {
            finish();
            return;
        }
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            String adPlacement = getResources().getString(R.string.placement_full_screen_game);
            KCInterstitialAd.load(adPlacement);
            AcbInterstitialAdLoader loader = KCInterstitialAd.loadAndShow(adPlacement, "", "", new KCInterstitialAd.OnAdShowListener() {
                @Override
                public void onAdShow(boolean b) {
                    adShowed = b;
                }
            }, null);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!adShowed) {
                        loader.cancel();
                    }
                    KCInterstitialAd.load(adPlacement);
                }
            }, TIME_OUT_LIMIT);

        }
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url);
                return true; // then it is not handled by default action
            }
        });
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        handler.removeCallbacksAndMessages(null);
        KCInterstitialAd.show(getResources().getString(R.string.placement_full_screen_game), "", "");
        super.onBackPressed();
    }
}
