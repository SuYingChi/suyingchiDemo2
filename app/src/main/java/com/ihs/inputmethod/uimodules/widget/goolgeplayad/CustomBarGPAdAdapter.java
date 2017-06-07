package com.ihs.inputmethod.uimodules.widget.goolgeplayad;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.acb.adadapter.AcbNativeAd;
import com.acb.adadapter.ContainerView.AcbNativeAdContainerView;
import com.acb.adadapter.ContainerView.AcbNativeAdIconView;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arthur on 17/5/16.
 */

public class CustomBarGPAdAdapter extends RecyclerView.Adapter {

    private Context context = HSApplication.getContext();
    private List<AcbNativeAd> adList = new ArrayList<>();

     public void addAd(AcbNativeAd ad) {
        this.adList.add(ad);
        notifyItemInserted(adList.indexOf(ad));
    }

    public void clearAdList() {
        adList.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(new FrameLayout(context));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        FrameLayout itemView = (FrameLayout) holder.itemView;
        itemView.removeAllViews();

        View containerView = View.inflate(context, R.layout.customize_bar_gp_ad, null);
        final AcbNativeAdContainerView acbNativeAdContainerView = new AcbNativeAdContainerView(HSApplication.getContext());
        acbNativeAdContainerView.setClickViewList(null);
        containerView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
        acbNativeAdContainerView.addContentView(containerView);
        AcbNativeAdIconView adIconView = (AcbNativeAdIconView) containerView.findViewById(R.id.ad_icon);
        acbNativeAdContainerView.setAdTitleView((TextView) containerView.findViewById(R.id.ad_title));
        acbNativeAdContainerView.setAdChoiceView((ViewGroup) containerView.findViewById(R.id.ad_choice));
        acbNativeAdContainerView.setAdIconView(adIconView);
        acbNativeAdContainerView.setAdActionView(containerView.findViewById(R.id.ad_call_to_action));
        acbNativeAdContainerView.fillNativeAd(adList.get(position));
        ((FrameLayout)holder.itemView).addView(acbNativeAdContainerView);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return adList.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder
    {
        MyViewHolder(View view)
        {
            super(view);
        }
    }

}
