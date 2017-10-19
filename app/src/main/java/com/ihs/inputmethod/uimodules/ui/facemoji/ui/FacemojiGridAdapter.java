package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Telephony;
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

import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSInstallationUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
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
    private ImageView messageIcon;
    private ImageView emailIcon;
    private ImageView messengerIcon;
    private ImageView twitterIcon;
    private ImageView facebookIcon;
    private ImageView whatsappIcon;

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

        int id = arg0.getId();
        if (id == R.id.message_share_icon_btn) {
            share(sticker, ShareChannel.MESSAGE);
        } else if (id == R.id.facebook_share_icon_btn) {
            share(sticker, ShareChannel.FACEBOOK);
        } else if (id == R.id.email_share_icon_btn) {
            share(sticker, ShareChannel.EMAIL);
        } else if (id == R.id.messenger_share_icon_btn) {
            share(sticker, ShareChannel.MESSENGER);
        } else if (id == R.id.whatsapp_share_icon_btn) {
            share(sticker, ShareChannel.WHATSAPP);
        } else if (id == R.id.twitter_share_icon_btn) {
            share(sticker, ShareChannel.TWITTER);
        } else {
            showShareAlert(sticker);
        }
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
        }else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        stickerPlayer.setSticker(sticker);
        messageIcon.setTag(sticker);
        emailIcon.setTag(sticker);
        messengerIcon.setTag(sticker);
        twitterIcon.setTag(sticker);
        facebookIcon.setTag(sticker);
        whatsappIcon.setTag(sticker);
        mProgressBar.setVisibility(View.GONE);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void initShareAlert() {
        dialog = new Dialog(HSApplication.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_alert);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        WindowManager.LayoutParams param = new WindowManager.LayoutParams();
        param.copyFrom(dialog.getWindow().getAttributes());
        WindowManager wm = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        param.width = (int) (size.x * 0.80);
        param.height = (int) (size.y * 0.57);
        dialog.getWindow().setAttributes(param);


        stickerPlayer = (FacemojiAnimationView) dialog.findViewById(R.id.share_sticker_preview);
        LinearLayout.LayoutParams playerParam = (LinearLayout.LayoutParams) stickerPlayer.getLayoutParams();
        playerParam.height = (int) (param.height * 0.5);
        playerParam.width = playerParam.height;
        stickerPlayer.setLayoutParams(playerParam);
        stickerPlayer.setScaleType(ImageView.ScaleType.FIT_XY);

        messageIcon = (ImageView) dialog.findViewById(R.id.message_share_icon_btn);
        messageIcon.setImageDrawable(HSDrawableUtils.getDimmedDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.share_icon_message)));
        if (!HSInstallationUtils.isAppInstalled(ShareChannel.MESSAGE.getPackageName()) && !(getMmsPackages().length > 0)) {
            messageIcon.setVisibility(View.GONE);
        } else {
            messageIcon.setOnClickListener(this);
        }


        emailIcon = (ImageView) dialog.findViewById(R.id.email_share_icon_btn);
        emailIcon.setImageDrawable(HSDrawableUtils.getDimmedDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.share_icon_email)));

        if (!HSInstallationUtils.isAppInstalled(ShareChannel.EMAIL.getPackageName()) && !(getEmailPackages().length > 0)) {
            emailIcon.setVisibility(View.GONE);
        } else {
            emailIcon.setOnClickListener(this);
        }

        messengerIcon = (ImageView) dialog.findViewById(R.id.messenger_share_icon_btn);
        messengerIcon.setImageDrawable(HSDrawableUtils.getDimmedDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.share_icon_messenger)));

        if (!HSInstallationUtils.isAppInstalled(ShareChannel.MESSENGER.getPackageName())) {
            messengerIcon.setVisibility(View.GONE);
        } else {
            messengerIcon.setOnClickListener(this);
        }

        twitterIcon = (ImageView) dialog.findViewById(R.id.twitter_share_icon_btn);
        twitterIcon.setImageDrawable(HSDrawableUtils.getDimmedDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.share_icon_twitter)));
        if (!HSInstallationUtils.isAppInstalled(ShareChannel.TWITTER.getPackageName())) {
            twitterIcon.setVisibility(View.GONE);
        } else {
            twitterIcon.setOnClickListener(this);
        }

        facebookIcon = (ImageView) dialog.findViewById(R.id.facebook_share_icon_btn);
        facebookIcon.setImageDrawable(HSDrawableUtils.getDimmedDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.share_icon_facebook)));
        if (!HSInstallationUtils.isAppInstalled(ShareChannel.FACEBOOK.getPackageName())) {
            facebookIcon.setVisibility(View.GONE);
        } else {
            facebookIcon.setOnClickListener(this);
        }

        whatsappIcon = (ImageView) dialog.findViewById(R.id.whatsapp_share_icon_btn);
        whatsappIcon.setImageDrawable(HSDrawableUtils.getDimmedDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.share_icon_whatsapp)));

        if (!HSInstallationUtils.isAppInstalled(ShareChannel.WHATSAPP.getPackageName())) {
            whatsappIcon.setVisibility(View.GONE);
        } else {
            whatsappIcon.setOnClickListener(this);
        }

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
        HSLog.d("AAAAA getMmsPackages");
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
                HSLog.d("AAAAA available mms packageName :" + activityInfo.packageName);
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
        HSLog.d("AAAAA getEmailPackages");
        List<String> packages = new ArrayList<>();

        Uri uri = Uri.parse("mailto:" + "test@qq.com");
        Intent shareIntent = new Intent(Intent.ACTION_SENDTO, uri);

        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfos) {
            final ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (!packages.contains(activityInfo.packageName)) {
                HSLog.d("AAAAA available email packageName :" + activityInfo.packageName);
                packages.add(activityInfo.packageName);
            }
        }
        String[] result = new String[packages.size()];
        packages.toArray(result);
        return result;
    }
}
