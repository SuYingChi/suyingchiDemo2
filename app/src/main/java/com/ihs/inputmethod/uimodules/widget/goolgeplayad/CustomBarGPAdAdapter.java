package com.ihs.inputmethod.uimodules.widget.goolgeplayad;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
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

//    private List<String> adList = new ArrayList<>();
     public void setAdList(List<AcbNativeAd> adList) {
        this.adList = adList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(new AcbNativeAdContainerView(context));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        View containerView = View.inflate(context, R.layout.customize_bar_gp_ad, null);
        AcbNativeAdContainerView acbNativeAdContainerView = (AcbNativeAdContainerView) holder.itemView;
        acbNativeAdContainerView.addContentView(containerView);
        AcbNativeAdIconView adIconView = (AcbNativeAdIconView) containerView.findViewById(R.id.ad_icon);
        acbNativeAdContainerView.setAdTitleView((TextView) containerView.findViewById(R.id.ad_title));
//        acbNativeAdContainerView.setAdActionView(containerView.findViewById(R.id.ad_call_to_action));
        acbNativeAdContainerView.setAdIconView(adIconView);
        acbNativeAdContainerView.fillNativeAd(adList.get(position));
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
