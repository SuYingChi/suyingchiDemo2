package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.Manifest;
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
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSInstallationUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.ProgressListener;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareChannel;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareUtils;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsapphire on 15/11/27.
 */
public class FacemojiGridAdapter extends BaseAdapter implements View.OnClickListener {

    private List<FacemojiSticker> mData;
    private LayoutInflater mInflater;
    private Dialog dialog;
    private int stickerDimension;
    private FacemojiAnimationView stickerPlayer;
    private ShareAdapter shareAdapter;

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
            dialog.dismiss();
        }
    };

    public FacemojiGridAdapter(List<FacemojiSticker> dataList, int stickerDimension) {
        this.mData = dataList;
        this.stickerDimension = stickerDimension;
        this.mInflater = LayoutInflater.from(HSApplication.getContext());
    }

    @Override
    public int getCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        if (mData != null) {
            if (mData.size() > position)
                return mData.get(position);
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
            return convertView;
        }
        convertView = mInflater.inflate(R.layout.facemoji_view, null);
        final View containerLayout = convertView.findViewById(R.id.facemoji_cell_layout);

        Resources resources = convertView.getResources();
        int width = (int) ((resources.getDisplayMetrics().widthPixels - resources.getDimension(R.dimen.facemoji_grid_item_horizontal_space) - resources.getDimension(R.dimen.facemoji_grid_left_margin) * 2) / 2);
        int height = (int) ((float) sticker.getHeight() / sticker.getWidth() * width);

        GridView.LayoutParams layoutParams = new GridView.LayoutParams(width, height);
        containerLayout.setLayoutParams(layoutParams);

        final FacemojiAnimationView facemojiView = (FacemojiAnimationView) containerLayout.findViewById(R.id.sticker_player_view);
        facemojiView.setSticker(sticker);
        facemojiView.setTag(sticker);
        holder = new StickerViewHolder();
        holder.facemojiView = facemojiView;
        holder.facemojiView.setOnClickListener(this);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void onClick(View arg0) {
        if (arg0.getTag() == null) {
            return;
        }

        if (FacemojiManager.isUsingTempFace()) {
            return;
        }

        final FacemojiSticker sticker = (FacemojiSticker) arg0.getTag();
        showShareAlert(sticker);
    }

    private void share(final FacemojiSticker sticker, ShareChannel channel) {

        MediaController.getShareManager().shareFacemojiByIntent(
                sticker,
                ShareUtils.refreshCurrentShareMode(channel.getPackageName()).second,
                channel,
                mShareProgressListener);
    }

    class StickerViewHolder {
        public FacemojiAnimationView facemojiView;
    }

    private void showShareAlert(FacemojiSticker sticker) {
        if (dialog == null) {
            initShareAlert();
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        stickerPlayer.setSticker(sticker);
        shareAdapter.setSticker(sticker);
        mProgressBar.setVisibility(View.GONE);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void initShareAlert() {
        dialog = new Dialog(HSApplication.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_alert);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        WindowManager.LayoutParams param = new WindowManager.LayoutParams();
        param.copyFrom(dialog.getWindow().getAttributes());
        WindowManager wm = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        param.width = (int) (size.x * 0.85);
        dialog.getWindow().setAttributes(param);

        stickerPlayer = (FacemojiAnimationView) dialog.findViewById(R.id.share_sticker_preview);
        LinearLayout.LayoutParams playerParam = (LinearLayout.LayoutParams) stickerPlayer.getLayoutParams();
        playerParam.height = (int) (size.y * 0.57 * 0.5);
        stickerPlayer.setLayoutParams(playerParam);
        stickerPlayer.setScaleType(ImageView.ScaleType.FIT_CENTER);

        RecyclerView shareRecyclerView = (RecyclerView) dialog.findViewById(R.id.share_apps_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(HSApplication.getContext(), 4);
        shareRecyclerView.addItemDecoration(new SpacesItemDecoration(HSApplication.getContext().getResources().getDimension(R.dimen.facemoji_share_alert_share_app_recycler_horizontal_space), HSApplication.getContext().getResources().getDimension(R.dimen.facemoji_share_alert_share_app_recycler_vertical_space)));
        shareRecyclerView.setLayoutManager(gridLayoutManager);

        shareAdapter = new ShareAdapter(getSharedAppsList());
        shareRecyclerView.setAdapter(shareAdapter);

        LinearLayout closeBtn = (LinearLayout) dialog.findViewById(R.id.back_button_holder);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
            dialog.dismiss();
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
