package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FaceItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;


public class FaceGridAdapter extends BaseAdapter {

    private List<FaceItem> mData;
    private LayoutInflater mInflater;
    private int stickerDimension;
    private boolean editMode;
    private List<FaceItem> faceTobeDeleted;
    private Activity faceListActivity;
    private OnSelectedFaceChangedListener onSelectedFaceChangedListener;

    public FaceGridAdapter(int stickerDimension, Activity activity) {
        this.mData = FacemojiManager.getFaceList();
        faceTobeDeleted = new ArrayList<FaceItem>();
        this.stickerDimension = stickerDimension;
        this.mInflater = LayoutInflater.from(HSApplication.getContext());
        this.faceListActivity = activity;
    }

    public void setOnSelectedFaceChangedListener(OnSelectedFaceChangedListener onSelectedFaceChangedListener) {
        this.onSelectedFaceChangedListener = onSelectedFaceChangedListener;
    }

    @Override
    public int getCount() {
        if (mData == null) return 1;
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

        StickerViewHolder holder = null;

        final FaceItem face = (FaceItem) getItem(position);

        if (convertView != null) {
            HSLog.d("use old convertview");
            holder = (StickerViewHolder) convertView.getTag();
            loadFaceImg(holder.faceImg, face,holder.nowUsed);
            refreshPickerView(holder.picker, face);
            setItemClickListener(face, convertView, holder.picker);
            return convertView;
        }
        HSLog.d("recreating convertview ");
        convertView = mInflater.inflate(R.layout.face_item_view, null);
        final View containerLayout = convertView.findViewById(R.id.face_item_layout);
        containerLayout.setLayoutParams(new GridView.LayoutParams(stickerDimension, stickerDimension));
        final ImageView face_img = (ImageView) containerLayout.findViewById(R.id.face_item_img);
        final ImageView picker = (ImageView) containerLayout.findViewById(R.id.face_item_picker_icon);
        final ImageView faceNowUsed = (ImageView) containerLayout.findViewById(R.id.face_now_used);
        refreshPickerView(picker, face);
        setItemClickListener(face, convertView, picker);
        holder = new StickerViewHolder();
        holder.faceImg = face_img;
        holder.nowUsed = faceNowUsed;
        holder.faceImg.setTag(face);
        holder.picker = picker;
        convertView.setTag(holder);
        loadFaceImg(face_img, face,holder.nowUsed);
        return convertView;
    }


    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isInEditMode() {
        return editMode;
    }

    public void setItemClickListener(final FaceItem face, final View itemView, final View picker) {

        if (face.isAddButton()) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isInEditMode()) {
                        return;
                    }
                    //启动照相页面
                    Intent i = new Intent(faceListActivity, CameraActivity.class);
                    i.putExtra("FaceGridAdapter", true);
                    faceListActivity.startActivity(i);
                }
            });
        } else {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isInEditMode()) {
                        boolean wasSelected = face.isSelected();
                        if (wasSelected) {
                            faceTobeDeleted.remove(face);
                            face.setIsSelected(false);
                        } else {
                            faceTobeDeleted.add(face);
                            face.setIsSelected(true);
                        }
                        if (onSelectedFaceChangedListener != null){
                            onSelectedFaceChangedListener.onSelectedFaceChange(faceTobeDeleted.size());
                        }
                    } else {
                        if (face.getUri().equals(FacemojiManager.getCurrentFacePicUri())) {
                            return;
                        }
                        FacemojiManager.setCurrentFacePicUri(face.getUri());
                        notifyDataSetChanged();
                        faceListActivity.finish();
                    }
                    refreshPickerView(picker, face);
                }
            });
        }
    }

    public void deleteSelectedFace() {
        mData.removeAll(faceTobeDeleted);

        for (FaceItem faceItem : faceTobeDeleted) {
            FacemojiManager.deleteFace(faceItem);
        }

        if (null != mData && 1 == mData.size()) {
            FacemojiManager.setCurrentFacePicUri(null);
        } else if (faceTobeDeleted.contains(new FaceItem(FacemojiManager.getCurrentFacePicUri()))) {
            FacemojiManager.setCurrentFacePicUri(null);
            FacemojiManager.setCurrentFacePicUri(FacemojiManager.getDefaultFacePicUri());
        }
        faceTobeDeleted.clear();
        FacemojiManager.getInstance().loadFaceList();
        notifyDataSetChanged();
    }

    class StickerViewHolder {
        public ImageView faceImg;
        public ImageView picker;
        public ImageView nowUsed;
    }

    private void refreshPickerView(View picker, FaceItem face) {

        if (!editMode || face.isAddButton()) {
            picker.setVisibility(View.GONE);
        } else {
            picker.setVisibility(View.VISIBLE);
            picker.setBackgroundDrawable(HSApplication.getContext().getResources().getDrawable(
                    face.isSelected() ? R.drawable.facemoji_choose : R.drawable.facemoji_unchoose));
        }
    }


    private void initAddFaceBtn(ImageView faceView) {
        FrameLayout.LayoutParams param = (FrameLayout.LayoutParams)faceView.getLayoutParams();
        param.height = (int) (stickerDimension * 0.5);
        param.width = param.height;
        faceView.setLayoutParams(param);
        Drawable drawable = isInEditMode()? HSApplication.getContext().getResources().getDrawable(R.drawable.facemoji_add_disable) : HSApplication.getContext().getResources().getDrawable(R.drawable.facemoji_add_btn);
        faceView.setBackgroundDrawable(drawable);
        faceView.setImageDrawable(null);
    }

    private void loadFaceImg(ImageView faceView, FaceItem face, ImageView nowUsed) {

        if (face.isAddButton()) {
            initAddFaceBtn(faceView);
            return;
        }

        FrameLayout.LayoutParams param = (FrameLayout.LayoutParams)faceView.getLayoutParams();
        param.height = (int)(stickerDimension*0.8);
        param.width = param.height;
        faceView.setLayoutParams(param);

        nowUsed.setVisibility((!isInEditMode()&&(face.getUri()!=null)&&face.getUri().equals(FacemojiManager.getCurrentFacePicUri()))?View.VISIBLE:View.GONE);

        ImageLoader.getInstance().loadImage(
                face.getUri().toString(),
                new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY).cacheInMemory(true).build()
                , new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (loadedImage != null) {
                            if (!isInEditMode()) {
                                faceView.setImageDrawable(HSDrawableUtils.getDimmedForegroundDrawable(loadedImage));
                            } else {
                                faceView.setImageBitmap(loadedImage);
                            }
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
    }

    public void resetAllItems() {
        for (FaceItem faceItem : mData) {
            faceItem.setIsSelected(false);
            faceTobeDeleted.clear();
        }
    }


    public interface OnSelectedFaceChangedListener{
        void onSelectedFaceChange(int selectedCount);
    }
}
