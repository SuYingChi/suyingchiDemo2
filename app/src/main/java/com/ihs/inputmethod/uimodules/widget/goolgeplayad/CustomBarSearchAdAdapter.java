package com.ihs.inputmethod.uimodules.widget.goolgeplayad;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.acb.adcaffe.nativead.AdCaffeNativeAd;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

/**
 * Created by guonan.lv on 17/11/1.
 */

public class CustomBarSearchAdAdapter extends RecyclerView.Adapter<CustomBarSearchAdAdapter.SearchAdViewHolder> {


    private Context context = HSApplication.getContext();
    private List<AdCaffeNativeAd> adList = new ArrayList<>();
    private AdCaffeOnClickListener adCaffeOnClickListerner;
    private static final int TYPE_AD = 1;

    public void addAd(AdCaffeNativeAd ad) {
        this.adList.add(ad);
        notifyItemInserted(adList.indexOf(ad));
    }

    public void setAdList(List<AdCaffeNativeAd> adList) {
        this.adList.clear();
        this.adList.addAll(adList);
        notifyDataSetChanged();
    }

    public void clearAdList() {
        adList.clear();
        notifyDataSetChanged();
    }

    public void setAdCaffeOnClickListener(AdCaffeOnClickListener adCaffeOnClickListerner) {
        this.adCaffeOnClickListerner = adCaffeOnClickListerner;
    }

    @Override
    public SearchAdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchAdViewHolder(LayoutInflater.from(context).inflate(R.layout.customize_bar_search_ad, parent, false));
    }

    @Override
    public void onBindViewHolder(SearchAdViewHolder holder, int position) {
        AdCaffeNativeAd adCaffeNativeAd = adList.get(position);

        RequestOptions requestOptions = new RequestOptions().placeholder(new ColorDrawable(ContextCompat.getColor(context, R.color.search_ad_placeholder_color)))
                .error(new ColorDrawable(ContextCompat.getColor(context, R.color.search_ad_placeholder_color))).diskCacheStrategy(DiskCacheStrategy.DATA);
        Glide.with(holder.itemView.getContext()).asBitmap().apply(requestOptions)
                .load(adCaffeNativeAd.getIconUrl()).transition(withCrossFade(500))
                .into(holder.adIcon);
        holder.itemView.setOnClickListener(v-> {
            adCaffeNativeAd.setOnClickListener(new AdCaffeNativeAd.NativeAdOnClickListener() {
                @Override
                public void onNativeAdClicked(AdCaffeNativeAd adCaffeNativeAd) {
                    if (adCaffeOnClickListerner != null) {
                        adCaffeOnClickListerner.onClick(adCaffeNativeAd);
                    }
                }

                @Override
                public void onNativeAdHandleClickFinished(AdCaffeNativeAd adCaffeNativeAd) {
                    if (adCaffeOnClickListerner != null) {
                        adCaffeOnClickListerner.onHandleClickFinish(adCaffeNativeAd);
                    }
                }
            });
            adCaffeNativeAd.handleClick();
        });

        holder.adTitle.setText(adCaffeNativeAd.getTitle());
        holder.adRating.setText(adCaffeNativeAd.getStoreRating());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_AD;
    }

    @Override
    public int getItemCount() {
        return adList.size();
    }

    class SearchAdViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView adIcon;
        TextView adTitle;
        TextView adRating;

        SearchAdViewHolder(View view) {
            super(view);

            adIcon = (RoundedImageView) view.findViewById(R.id.ad_icon);
            adTitle = (TextView) view.findViewById(R.id.ad_title);
            adRating = (TextView) view.findViewById(R.id.ad_rating_score);
        }
    }

    public interface AdCaffeOnClickListener {
        void onClick(AdCaffeNativeAd adCaffeNativeAd);
        void onHandleClickFinish(AdCaffeNativeAd adCaffeNativeAd);
    }
}
