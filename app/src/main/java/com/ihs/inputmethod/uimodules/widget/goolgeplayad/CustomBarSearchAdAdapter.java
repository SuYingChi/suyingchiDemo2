package com.ihs.inputmethod.uimodules.widget.goolgeplayad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import com.ihs.inputmethod.uimodules.BuildConfig;
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

    @Override
    public SearchAdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchAdViewHolder(LayoutInflater.from(context).inflate(R.layout.customize_bar_search_ad, parent, false));
    }

    @Override
    public void onBindViewHolder(SearchAdViewHolder holder, int position) {
        AdCaffeNativeAd adCaffeNativeAd = adList.get(position);

        RequestOptions requestOptions = new RequestOptions().placeholder(new ColorDrawable(ContextCompat.getColor(context, R.color.search_ad_placeholder_color)))
                .error(new ColorDrawable(Color.RED)).diskCacheStrategy(DiskCacheStrategy.DATA);
        Glide.with(holder.itemView.getContext()).asBitmap().apply(requestOptions)
                .load(adCaffeNativeAd.getIconUrl()).transition(withCrossFade(500))
                .into(holder.adIcon);
        holder.adIcon.setOnClickListener((v -> directToMarket(adCaffeNativeAd.getPackageName())));

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

    /**
     * 用于跳转到 google play 下载 locker 的界面
     *
     * @param lockerPackageName 要跳转的 locker 的包名
     */
    public static void directToMarket(String lockerPackageName) {
        StringBuilder parametersStr = new StringBuilder();
        parametersStr.append("packageName=" + BuildConfig.APPLICATION_ID);
        parametersStr.append("&versionName=" + BuildConfig.VERSION_NAME);
        parametersStr.append("&internal=" + BuildConfig.APPLICATION_ID);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            intent.setData(Uri.parse("market://details?id=" + lockerPackageName + "&referrer=" + Uri.encode(parametersStr.toString())));
            HSApplication.getContext().startActivity(intent);

        } catch (Exception e) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + lockerPackageName + "&referrer=" + Uri.encode(parametersStr.toString())));
            HSApplication.getContext().startActivity(intent);

        }
    }
}
