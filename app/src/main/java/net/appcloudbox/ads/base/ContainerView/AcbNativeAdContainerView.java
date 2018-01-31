package net.appcloudbox.ads.base.ContainerView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.appcloudbox.ads.base.AcbNativeAd;

/**
 * Created by Arthur on 2018/1/31.
 */

public class AcbNativeAdContainerView extends View {
    private TextView adTitleView;
    private ViewGroup adChoiceView;
    private AcbNativeAdIconView adIconView;
    private TextView adActionView;
    private TextView adBodyView;
    private AcbNativeAdPrimaryView adPrimaryView;
    private TextView adSubTitleView;

    public AcbNativeAdContainerView(Context context) {
        super(context);
    }


    public void hideAdCorner() {

    }

    public void addContentView(View containerView) {

    }

    public void setAdTitleView(TextView adTitleView) {
        this.adTitleView = adTitleView;
    }

    public void setAdChoiceView(ViewGroup adChoiceView) {
        this.adChoiceView = adChoiceView;
    }

    public void setAdIconView(AcbNativeAdIconView adIconView) {
        this.adIconView = adIconView;
    }

    public void fillNativeAd(AcbNativeAd acbNativeAd) {

    }

    public void setAdActionView(TextView adActionView) {
        this.adActionView = adActionView;
    }

    public void setAdBodyView(TextView adBodyView) {
        this.adBodyView = adBodyView;
    }

    public void setAdPrimaryView(AcbNativeAdPrimaryView adPrimaryView) {
        this.adPrimaryView = adPrimaryView;
    }

    public void setAdActionView(View action) {

    }

    public AcbNativeAdIconView getAdIconView() {
        return adIconView;
    }

    public void setAdSubTitleView(TextView adSubTitleView) {
        this.adSubTitleView = adSubTitleView;
    }
}
