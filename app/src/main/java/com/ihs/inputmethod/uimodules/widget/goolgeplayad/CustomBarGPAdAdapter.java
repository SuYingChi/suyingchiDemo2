package com.ihs.inputmethod.uimodules.widget.goolgeplayad;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSMarketUtils;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * Created by Arthur on 17/5/16.
 */

public class CustomBarGPAdAdapter extends RecyclerView.Adapter {


    private Context context = HSApplication.getContext();
    private List<AcbNativeAd> adList = new ArrayList<>();
    private static final int TYPE_CAM = 0;
    private static final int TYPE_AD = 1;

    private String cameraPackageName;
    private String cameraIcon;
    private String cameraName;
    private boolean cameraInserted = false;


    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .displayer(new RoundedBitmapDisplayer(HSDisplayUtils.dip2px(2)))
            .cacheInMemory(false).cacheOnDisk(true)
            .build();

     public void addAd(AcbNativeAd ad) {
        this.adList.add(ad);
        notifyItemInserted(adList.indexOf(ad));
    }

    public void addCameraInfo(Map<String, Object> cameraInfo) {
        cameraPackageName = (String) cameraInfo.get("PackageName");
        cameraIcon = (String) cameraInfo.get("icon");
        cameraName = (String) cameraInfo.get("name");
        cameraInserted = true;
    }

    public void clearAdList() {
        adList.clear();
        cameraInserted = false;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_AD) {
            return new MyViewHolder(new FrameLayout(context));
        } else if (viewType == TYPE_CAM) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.customize_bar_gp_cam, parent, false);
            return new CamViewHolder(itemView);
        } else {
            return null;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Context context = HSApplication.getContext();

        if (getItemViewType(position) == TYPE_AD) {
            FrameLayout itemView = (FrameLayout) holder.itemView;
            itemView.removeAllViews();

            View containerView = View.inflate(context, R.layout.customize_bar_gp_ad, null);
            containerView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));

            final AcbNativeAdContainerView acbNativeAdContainerView = new AcbNativeAdContainerView(context);
            acbNativeAdContainerView.hideAdCorner();
            acbNativeAdContainerView.addContentView(containerView);
            AcbNativeAdIconView adIconView = containerView.findViewById(R.id.ad_icon);
            acbNativeAdContainerView.setAdTitleView(containerView.findViewById(R.id.ad_title));
            acbNativeAdContainerView.setAdChoiceView(containerView.findViewById(R.id.ad_choice));
            acbNativeAdContainerView.setAdIconView(adIconView);
            acbNativeAdContainerView.setAdActionView(containerView.findViewById(R.id.ad_call_to_action));
            acbNativeAdContainerView.fillNativeAd(adList.get(position));
            ((FrameLayout)holder.itemView).addView(acbNativeAdContainerView);
        } else if (getItemViewType(position) == TYPE_CAM) {
            CamViewHolder camViewHolder  = (CamViewHolder) holder;
            ImageLoader.getInstance().displayImage(cameraIcon, camViewHolder.iconView, displayImageOptions);
            camViewHolder.textView.setText(cameraName);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if( cameraInserted && (position == getItemCount()-1)) {
            return TYPE_CAM;
        }
        return TYPE_AD;
    }

    @Override
    public int getItemCount() {
        if(cameraInserted) {
            return adList.size()+1;
        }
        return adList.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        MyViewHolder(View view) {
            super(view);
        }
    }

    private class CamViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconView;
        private ImageView actionView;
        private TextView textView;

        private CamViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.ad_icon);
            actionView = itemView.findViewById(R.id.ad_call_to_action);
            textView = itemView.findViewById(R.id.ad_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HSAnalytics.logEvent("GooglePlayIcon_camera_clicked"); // log camera click event
                    HSMarketUtils.browseAPP("Google", cameraPackageName);
                }
            });
        }
    }
}
