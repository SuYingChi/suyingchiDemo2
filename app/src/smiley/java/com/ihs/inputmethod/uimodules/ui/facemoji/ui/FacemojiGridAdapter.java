package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kc.utils.KCAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSInstallationUtils;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.ProgressListener;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareChannel;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareUtils;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiAnimationView;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.kc.commons.utils.KCCommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dsapphire on 15/11/27.
 */
public class FacemojiGridAdapter extends BaseAdapter implements View.OnClickListener {
    private Activity activity;
    private List<FacemojiSticker> facemojiStickerList;
    private LayoutInflater mInflater;
    private Dialog dialog;
    private FacemojiAnimationView stickerPlayer;
    private ShareAdapter shareAdapter;
    private final int stickerWidth;
    private final int stickerHeight;

    public static final int[] colorArray = new int[]{
        0xff00c3ff, 0xffd947ff,0xff00f3d4,0xff0063ff,0xffffc823,0xff4df6f9,
    };

    private ProgressBar mProgressBar;
    private ProgressListener mShareProgressListener = new ProgressListener() {
        @Override
        public void startProgress() {
            mProgressBar.setVisibility(View.VISIBLE);
            dialog.setCancelable(false);
        }

        @Override
        public void stopProgress() {
            mProgressBar.setVisibility(View.GONE);
            dialog.setCancelable(true);
            KCCommonUtils.dismissDialog(dialog);
        }
    };


    public FacemojiGridAdapter(Activity activity, List<FacemojiSticker> facemojiStickerList) {
        this.activity = activity;
        this.facemojiStickerList = facemojiStickerList;
        this.mInflater = LayoutInflater.from(HSApplication.getContext());

        Resources resources = HSApplication.getContext().getResources();
        stickerWidth = (int) ((resources.getDisplayMetrics().widthPixels - resources.getDimension(R.dimen.facemoji_grid_item_horizontal_space) - resources.getDimension(R.dimen.facemoji_grid_left_margin) * 2) / 2);
        stickerHeight = (int) ((float) 3 / 4 * stickerWidth);
    }

    public void setFacemojiStickerList(List<FacemojiSticker> facemojiStickerList) {
        this.facemojiStickerList = facemojiStickerList;
    }

    @Override
    public int getCount() {
        if (facemojiStickerList == null) return 0;
        return facemojiStickerList.size();
    }

    @Override
    public Object getItem(int position) {
        if (facemojiStickerList != null) {
            if (facemojiStickerList.size() > position)
                return facemojiStickerList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StickerViewHolder holder;
        FacemojiSticker sticker = (FacemojiSticker) getItem(position);
        if (convertView != null) {
            holder = (StickerViewHolder) convertView.getTag();
            if (!sticker.equals(holder.facemojiView.getSticker())) {
                holder.facemojiView.setSticker(sticker);
                holder.facemojiView.setTag(sticker);
                holder.facemojiView.setOnClickListener(this);
                convertView.setTag(holder);
            }
        }else {
            convertView = mInflater.inflate(R.layout.facemoji_view, null);
            final AnimationLayout containerLayout = (AnimationLayout) convertView.findViewById(R.id.facemoji_cell_layout);
            containerLayout.setLayoutParams(new GridView.LayoutParams(stickerWidth, stickerHeight));

            final FacemojiAnimationView facemojiView = (FacemojiAnimationView) containerLayout.findViewById(R.id.sticker_player_view);
            facemojiView.setSticker(sticker);
            facemojiView.setTag(sticker);
            if (sticker.getWidth() == sticker.getHeight()) { //方形的sticker，内容保持高度
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(stickerHeight, stickerHeight);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                facemojiView.setLayoutParams(layoutParams);
            }

            holder = new StickerViewHolder();
            holder.facemojiView = facemojiView;
            holder.facemojiContainer = containerLayout;
            holder.facemojiView.setOnClickListener(this);
            convertView.setTag(holder);
        }

        if (sticker.getName() == null){
            holder.facemojiContainer.setBackgroundColor(colorArray[position%colorArray.length]);
            holder.facemojiContainer.setClickable(false);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(HSDisplayUtils.dip2px(50), HSDisplayUtils.dip2px(50));
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            holder.facemojiView.setImageResource(R.drawable.ic_facemoji_placeholder);
            holder.facemojiView.setLayoutParams(layoutParams);
        }else {
            RelativeLayout.LayoutParams layoutParams;
            if (sticker.getWidth() == sticker.getHeight()) {//方形的需要修改宽高
                layoutParams = new RelativeLayout.LayoutParams(stickerHeight, stickerHeight);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            }else {
                layoutParams = new RelativeLayout.LayoutParams(stickerWidth, stickerHeight);
            }
            holder.facemojiView.setLayoutParams(layoutParams);
            holder.facemojiView.setVisibility(View.VISIBLE);
            holder.facemojiContainer.setBackgroundDrawable(null);
            holder.facemojiContainer.setClickable(true);
        }
        if (allowPlayAnim){
            holder.facemojiView.startAnim();
        }else {
            holder.facemojiView.stopAnim();
        }

        return convertView;
    }

    private boolean allowPlayAnim;

    public void setAllowPlayAnim(boolean allowPlayAnim) {
        if (this.allowPlayAnim != allowPlayAnim){
            this.allowPlayAnim = allowPlayAnim;
        }
    }

    @Override
    public void onClick(View arg0) {
        if (arg0.getTag() == null) {
            return;
        }

        final FacemojiSticker sticker = (FacemojiSticker) arg0.getTag();
        if (sticker.getName() != null) {
            showShareAlert(sticker);
        }
    }

    private void share(final FacemojiSticker sticker, ShareChannel channel) {
        Map<String,String> params = new HashMap<>();
        params.put("facemoji",sticker.getCategoryName()+"-"+sticker.getName());
        params.put("share_app",channel.getAppName());
        KCAnalytics.logEvent("app_facemoji_shared",params);
        MediaController.getShareManager().shareFacemojiByIntent(
                sticker,
                ShareUtils.refreshCurrentShareMode(channel.getPackageName()).second,
                channel,
                mShareProgressListener);
    }

    class StickerViewHolder {
        public AnimationLayout facemojiContainer;
        public FacemojiAnimationView facemojiView;
    }

    private void showShareAlert(FacemojiSticker sticker) {
        if (dialog == null) {
            initShareAlert();
        } else {
            KCCommonUtils.dismissDialog(dialog);
            stickerPlayer.stop();
        }
        stickerPlayer.setSticker(sticker);
        stickerPlayer.start();
        shareAdapter.setSticker(sticker);
        mProgressBar.setVisibility(View.GONE);
        dialog.setCancelable(true);
        KCCommonUtils.showDialog(dialog);
    }

    private void initShareAlert() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_alert);
        WindowManager.LayoutParams param = new WindowManager.LayoutParams();
        param.copyFrom(dialog.getWindow().getAttributes());
        WindowManager wm = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        param.width = (int) (size.x * 0.95);
        dialog.getWindow().setAttributes(param);

        stickerPlayer = (FacemojiAnimationView) dialog.findViewById(R.id.share_sticker_preview);

        RecyclerView shareRecyclerView = (RecyclerView) dialog.findViewById(R.id.share_apps_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(HSApplication.getContext(), 4);
        shareRecyclerView.addItemDecoration(new SpacesItemDecoration(HSApplication.getContext().getResources().getDimension(R.dimen.facemoji_share_alert_share_app_recycler_vertical_space), HSApplication.getContext().getResources().getDimension(R.dimen.facemoji_share_alert_share_app_recycler_horizontal_space)));
        shareRecyclerView.setLayoutManager(gridLayoutManager);

        shareAdapter = new ShareAdapter(getSharedAppsList());
        shareRecyclerView.setAdapter(shareAdapter);

        LinearLayout closeBtn = (LinearLayout) dialog.findViewById(R.id.back_button_holder);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KCCommonUtils.dismissDialog(dialog);
            }
        });

        mProgressBar = (ProgressBar) dialog.findViewById(R.id.progressBar);

        dialog.setCancelable(true);
    }

    private List<ShareChannel> getSharedAppsList() {
        List<ShareChannel> shareChannelList = new ArrayList<>();

        if (HSInstallationUtils.isAppInstalled(ShareChannel.MESSAGE.getPackageName()) || (getMmsPackages().length > 0)) {
            shareChannelList.add(ShareChannel.MESSAGE);
        }

        if (HSInstallationUtils.isAppInstalled(ShareChannel.EMAIL.getPackageName()) || (getEmailPackages().length > 0)) {
            shareChannelList.add(ShareChannel.EMAIL);
        }

        if (HSInstallationUtils.isAppInstalled(ShareChannel.MESSENGER.getPackageName())) {
            shareChannelList.add(ShareChannel.MESSENGER);
        }

        if (HSInstallationUtils.isAppInstalled(ShareChannel.TWITTER.getPackageName())) {
            shareChannelList.add(ShareChannel.TWITTER);
        }

        if (HSInstallationUtils.isAppInstalled(ShareChannel.INSTAGRAM.getPackageName())) {
            shareChannelList.add(ShareChannel.INSTAGRAM);
        }

        if (HSInstallationUtils.isAppInstalled(ShareChannel.FACEBOOK.getPackageName())) {
            shareChannelList.add(ShareChannel.FACEBOOK);
        }

        if (HSInstallationUtils.isAppInstalled(ShareChannel.WHATSAPP.getPackageName())) {
            shareChannelList.add(ShareChannel.WHATSAPP);
        }

        shareChannelList.add(ShareChannel.MORE);
        return shareChannelList;
    }

    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private float verticalSpace;
        private float horizontalSpace;

        public SpacesItemDecoration(float verticalSpace, float horizontalSpace) {
            this.verticalSpace = verticalSpace;
            this.horizontalSpace = horizontalSpace;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            int position = parent.getChildLayoutPosition(view);

            int left = (position == 0 ? 0 : (int) (horizontalSpace / 2));
            int right = ((position == parent.getChildCount() - 1) ? 0 : (int) (horizontalSpace / 2));
            int top = (int) (verticalSpace / 2);
            int bottom = top;

            outRect.left = left;
            outRect.right = right;
            outRect.top = top;
            outRect.bottom = bottom;
        }
    }


    private class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ShareViewHolder> {
        FacemojiSticker sticker;
        List<ShareChannel> sharedAppsList;

        public ShareAdapter(List<ShareChannel> sharedAppsList) {
            this.sharedAppsList = sharedAppsList;
        }


        public void setSticker(FacemojiSticker sticker) {
            this.sticker = sticker;
        }


        @Override
        public ShareAdapter.ShareViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ShareViewHolder(View.inflate(parent.getContext(), R.layout.facemoji_share_app_item, null));
        }

        @Override
        public void onBindViewHolder(ShareAdapter.ShareViewHolder holder, int position) {
            ShareChannel shareChannel = sharedAppsList.get(position);
            holder.shareAppIcon.setImageResource(shareChannel.getIconId());
            holder.shareAppName.setText(shareChannel.getAppName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sticker.getName() == null){
                        return;
                    }
                    share(sticker, shareChannel);
                }
            });
        }

        @Override
        public int getItemCount() {
            return sharedAppsList.size();
        }

        public class ShareViewHolder extends RecyclerView.ViewHolder {
            ImageView shareAppIcon;
            TextView shareAppName;

            public ShareViewHolder(View itemView) {
                super(itemView);
                shareAppIcon = (ImageView) itemView.findViewById(R.id.share_app_icon);
                shareAppName = (TextView) itemView.findViewById(R.id.share_app_name);
            }
        }
    }

    public void finish() {
        if (dialog != null) {
            KCCommonUtils.dismissDialog(dialog);
        }
    }

    /**
     * get availabe mms packages
     *
     * @return
     */
    private String[] getMmsPackages() {
        List<String> packages = new ArrayList<>();
        PackageManager packageManager = HSApplication.getContext().getPackageManager();

        // Get the list of apps registered for SMS
        Intent intent = new Intent(Telephony.Sms.Intents.SMS_DELIVER_ACTION);
        List<ResolveInfo> smsReceivers = packageManager.queryBroadcastReceivers(intent, 0);

        // Add one entry to the map for every sms receiver (ignoring duplicate sms receivers)
        for (ResolveInfo resolveInfo : smsReceivers) {
            final ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo == null) {
                continue;
            }
            if (!Manifest.permission.BROADCAST_SMS.equals(activityInfo.permission)) {
                continue;
            }
            if (!packages.contains(activityInfo.packageName)) {
                packages.add(activityInfo.packageName);
            }
        }
        String[] result = new String[packages.size()];
        packages.toArray(result);
        return result;
    }

    /**
     * get availabe email packages
     *
     * @return
     */
    private String[] getEmailPackages() {
        List<String> packages = new ArrayList<>();

        Uri uri = Uri.parse("mailto:" + "test@qq.com");
        Intent shareIntent = new Intent(Intent.ACTION_SENDTO, uri);

        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfos) {
            final ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (!packages.contains(activityInfo.packageName)) {
                packages.add(activityInfo.packageName);
            }
        }
        String[] result = new String[packages.size()];
        packages.toArray(result);
        return result;
    }
}
