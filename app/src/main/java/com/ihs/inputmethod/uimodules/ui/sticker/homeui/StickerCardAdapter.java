package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.stickerplus.PlusButton;
import com.ihs.inputmethod.uimodules.ui.sticker.Sticker;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.keyboardutils.view.HSGifImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;


/**
 * Created by guonan.lv on 17/8/10.
 */

public class StickerCardAdapter extends RecyclerView.Adapter<StickerCardAdapter.StickerCardViewHolder> {

    private static List<String> savedPreStickerGroupNamelist;
    private List<StickerModel> stickerModelList;
    private int imageWidth;
    private int imageHeight;
    private OnStickerCardClickListener onStickerCardClickListener;
    private String FROM_FRAGMENT_TYPE;
    private static SharedPreferences sharedPreferences;
    private List<String> newStickersList;
    private List<StickerGroup> stickerGroupList;

    public void refreshNewStickersList(List<String> newStickersList) {
        this.newStickersList = newStickersList;
    }

    public enum ITEM_TYPE {
        ITEM_TYPE_HOME,
        ITEM_TYPE_MY,
        ITEM_TYPE_MORE
    }

    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).build();

    public StickerCardAdapter(List<StickerModel> data, OnStickerCardClickListener onStickerCardClickListener) {
        stickerModelList = data;
        Resources resources = HSApplication.getContext().getResources();
        imageWidth = (int) (resources.getDisplayMetrics().widthPixels / 2 - resources.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        imageHeight = (int) (imageWidth / 1.6f);
        this.onStickerCardClickListener = onStickerCardClickListener;

        stickerGroupList = StickerDataManager.getInstance().getStickerGroupList();
        sharedPreferences = HSApplication.getContext().getSharedPreferences("sticker_new_list", Context.MODE_PRIVATE);
        newStickersList = new ArrayList<>();
        newStickersList = getNewStickersList();

        /**
         if (最新的list和以前的不一样){
         更新list；
         将增加的sticker放入newStickerList集合中；
         }
         */
        List<String> preStickerGroupNameList = getPreStickerGroupNameList();
        savePreStickerGroupNamelist(preStickerGroupNameList);

        List<String> currentStickerGroupNameList = new ArrayList<>();

        if (preStickerGroupNameList == null) {
            newStickersList.add(stickerGroupList.get(0).getStickerGroupName());
            newStickersList.add(stickerGroupList.get(1).getStickerGroupName());

            SharedPreferences.Editor edit = sharedPreferences.edit();
            try {
                // 将list集合转成字符串
                String listStr = ListSaveUtil.SceneList2String(newStickersList);
                // 存储
                edit.putString("new_sticker_list", listStr);
                edit.apply();
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentStickerGroupNameList = getCurrentStickerGroupNameList(stickerGroupList);
        } else {
            currentStickerGroupNameList = getCurrentStickerGroupNameList(stickerGroupList);
            for (String stickerGroupName : currentStickerGroupNameList) {
                if (!preStickerGroupNameList.contains(stickerGroupName)) {
                    newStickersList.add(stickerGroupName);
                }
            }
        }
        saveCurrentStickerGroupNameList(currentStickerGroupNameList);
        saveNewStickerList(newStickersList);
    }

    public static void savePreStickerGroupNamelist(List<String> preStickerGroupNameList) {
        savedPreStickerGroupNamelist = preStickerGroupNameList;
    }

    public static List<String> getSavedPreStickerGroupNamelist() {
        return savedPreStickerGroupNamelist;
    }

    private List<String> getNewStickersList() {
        List<String> newStickersList = new ArrayList<>();
        if (sharedPreferences == null) {
            newStickersList = null;
        } else {
            String liststr = sharedPreferences.getString("new_sticker_list", null);
            try {
                if (liststr != null) {
                    newStickersList = (ArrayList<String>) ListSaveUtil.String2SceneList(liststr);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return newStickersList;
    }

    private void saveNewStickerList(List<String> newStickersList) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        try {
            edit.putString("new_sticker_list", ListSaveUtil.SceneList2String(newStickersList));
            edit.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static List<String> getPreStickerGroupNameList() {
        List<String> preStickerGroupNameList = new ArrayList<>();
        try {
            String liststr = sharedPreferences.getString("current_sticker_group_name_list", null);
            if (liststr != null) {
                preStickerGroupNameList = (ArrayList<String>) ListSaveUtil.String2SceneList(liststr);
            } else {
                preStickerGroupNameList = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return preStickerGroupNameList;
    }

    public static List<String> getCurrentStickerGroupNameList(List<StickerGroup> stickerGroupList) {
        List<String> currentStickerGroupNameList = new ArrayList<> ();
        for (StickerGroup stickerGroup : stickerGroupList) {
            currentStickerGroupNameList.add(stickerGroup.getStickerGroupName());
        }
        return currentStickerGroupNameList;
    }

    private void saveCurrentStickerGroupNameList(List<String> currentStickerGroupNameList) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        try {
            edit.putString("current_sticker_group_name_list", ListSaveUtil.SceneList2String(currentStickerGroupNameList));
            edit.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFragmentType(String type) {
        FROM_FRAGMENT_TYPE = type;
    }

    @Override
    public StickerCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_MY.ordinal()) {
            return new MyStickerCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_card, parent, false));
        } else {
            return new StickerCardHomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_card, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final StickerCardViewHolder holder, final int position) {
        if (stickerModelList == null) {
            return;
        }

        if (getItemViewType(position) == ITEM_TYPE.ITEM_TYPE_MORE.ordinal()) {
            ((StickerCardHomeViewHolder) holder).moreStickersComing.setVisibility(View.VISIBLE);
            holder.stickerCardView.setVisibility(View.GONE);
            return;
        }

        final StickerModel stickerModel = stickerModelList.get(position);
        final StickerGroup stickerGroup = stickerModel.getStickerGroup();
        holder.stickerGroupName.setText(stickerGroup.getStickerGroupName());
        final String realImageUrl = stickerGroup.getStickerGroupDownloadPreviewImageUri();
        if (realImageUrl != null) {
            ImageSize imageSize = new ImageSize(imageWidth, imageHeight);
            ImageLoader.getInstance().displayImage(realImageUrl, new ImageViewAware(holder.stickerRealImage), options, imageSize, null, null);
        }
        if (getItemViewType(position) == ITEM_TYPE.ITEM_TYPE_HOME.ordinal()) {
            ((StickerCardHomeViewHolder) holder).moreMenuImage.setVisibility(View.VISIBLE);
            ((StickerCardHomeViewHolder) holder).moreMenuImage.setImageResource(R.drawable.ic_download_icon);
            ((StickerCardHomeViewHolder) holder).moreMenuImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStickerCardClickListener.onDownloadButtonClick(stickerModel, holder.stickerRealImage.getDrawable());
                }
            });
        } else {

        }
        holder.stickerRealImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStickerCardClickListener.onCardViewClick(stickerModel, holder.stickerRealImage.getDrawable());
            }
        });

        /**
         * 判断是否有当前sticker group 是否是new
         */
        if (isNewStickerGroup(stickerGroup)) {
            holder.stickerNewImage.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse("android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.app_theme_new_gif);
            holder.stickerNewImage.setImageURI(uri);
        } else {
            holder.stickerNewImage.setVisibility(View.GONE);
        }
    }

    private boolean isNewStickerGroup(StickerGroup stickerGroup) {
        if (newStickersList.contains(stickerGroup.getStickerGroupName())) {
            return true;
        }
        return false;
    }

    private boolean isFromHomeType() {
        return TextUtils.equals(FROM_FRAGMENT_TYPE, StickerHomeFragment.class.getSimpleName());
    }

    @Override
    public int getItemViewType(int position) {
        if (isFromHomeType() && position == getItemCount() - 1) {
            return ITEM_TYPE.ITEM_TYPE_MORE.ordinal();
        }
        return isFromHomeType() ? ITEM_TYPE.ITEM_TYPE_HOME.ordinal() : ITEM_TYPE.ITEM_TYPE_MY.ordinal();
    }

    @Override
    public int getItemCount() {
        if (isFromHomeType()) {
            return stickerModelList.size() + 1;
        }
        return stickerModelList.size();
    }

    public interface OnStickerCardClickListener {
        void onCardViewClick(StickerModel stickerModel, Drawable drawable);

        void onDownloadButtonClick(StickerModel stickerModel, Drawable drawable);

        void removeNewStickerFromNewStickerList(StickerGroup stickerGroup);
    }

    public class StickerCardViewHolder extends RecyclerView.ViewHolder {
        View stickerCardView;

        TextView stickerGroupName;
        GifImageView stickerNewImage;
        ImageView stickerRealImage;


        public StickerCardViewHolder(View itemView) {
            super(itemView);

            stickerCardView = itemView.findViewById(R.id.sticker_card_view);
            stickerGroupName = (TextView) itemView.findViewById(R.id.sticker_name);
            stickerRealImage = (ImageView) itemView.findViewById(R.id.sticker_image_real_view);
            stickerNewImage = (GifImageView) itemView.findViewById(R.id.sticker_new_view);
        }
    }

    private class StickerCardHomeViewHolder extends StickerCardViewHolder {
        TextView moreStickersComing;
        ImageView moreMenuImage;

        public StickerCardHomeViewHolder(View view) {
            super(view);
            moreMenuImage = (ImageView) itemView.findViewById(R.id.more_menu_image);
            moreStickersComing = (TextView) itemView.findViewById(R.id.more_sticker_coming);
        }
    }

    private class MyStickerCardViewHolder extends StickerCardViewHolder {
        public MyStickerCardViewHolder(View view) {
            super(view);
        }
    }

    /**
     * SharedPreferences 存储帮助类：list集合与字符串相互转化工具
     */
    public static class ListSaveUtil {
        //将list集合转换成字符串

        public static String SceneList2String(List SceneList) throws IOException {
            // 实例化一个ByteArrayOutputStream对象，用来装载压缩后的字节文件。
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // 然后将得到的字符数据装载到ObjectOutputStream
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    byteArrayOutputStream);
            // writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
            objectOutputStream.writeObject(SceneList);
            // 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
            String SceneListString = new String(Base64.encode(
                    byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
            // 关闭objectOutputStream
            objectOutputStream.close();
            return SceneListString;
        }

        //将字符串转换成list集合

        @SuppressWarnings("unchecked")
        public static List String2SceneList(String SceneListString)
                throws StreamCorruptedException, IOException,
                ClassNotFoundException {
            byte[] mobileBytes = Base64.decode(SceneListString.getBytes(),
                    Base64.DEFAULT);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    mobileBytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(
                    byteArrayInputStream);
            List SceneList = (List) objectInputStream.readObject();
            objectInputStream.close();
            return SceneList;
        }

    }

}

