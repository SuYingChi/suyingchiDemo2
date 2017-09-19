package com.ihs.inputmethod.uimodules.ui.facemoji.faceswitcher;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FaceItem;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.BitmapAddBorderProcessor;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class FacePageGridViewAdapter extends BaseAdapter {

    private FaceLayoutParams faceLayoutParams;
    private List<FaceItem> mData;
    private LayoutInflater mInflater;
    private FacePalettesViewAdapter adapter;
    private int size;

    public FacePageGridViewAdapter(final FacePalettesViewAdapter parentAdapter, final List<FaceItem> data,
                                   final FaceLayoutParams esLayoutParams) {
        mData = data;
        mInflater = LayoutInflater.from(HSApplication.getContext());
        faceLayoutParams = esLayoutParams;
        adapter = parentAdapter;
        size =  Math.min(faceLayoutParams.getGridHeight(), faceLayoutParams.getGridWidth());
    }

    @Override
    public int getCount() {
        return (mData != null) ? mData.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
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
            holder = (StickerViewHolder) convertView.getTag();

            loadFaceImg(holder.faceImg, face);
            setItemClickListener(face, convertView);
            return convertView;
        }
        convertView = mInflater.inflate(R.layout.face_item_view, null);
        final View containerLayout = convertView.findViewById(R.id.face_item_layout);
        containerLayout.setLayoutParams(new GridView.LayoutParams(size, size));
        final ImageView face_img = (ImageView) containerLayout.findViewById(R.id.face_item_img);
        setItemClickListener(face, convertView);

        holder = new StickerViewHolder();
        holder.faceImg = face_img;
        holder.faceImg.setTag(face);
        convertView.setTag(holder);
        loadFaceImg(face_img, face);
        return convertView;

    }

    class StickerViewHolder {
        public ImageView faceImg;
    }


    private void loadFaceImg(ImageView faceView, FaceItem face) {

        if (face.isAddButton()) {
            initAddFaceBtn(faceView);
            return;
        }

        FrameLayout.LayoutParams param = (FrameLayout.LayoutParams)faceView.getLayoutParams();
        param.height = (int)(size*0.8);
        param.width = param.height;
        faceView.setLayoutParams(param);
        int color = Color.WHITE;
        if (face.getUri().equals(FacemojiManager.getCurrentFacePicUri())) {
            color = 0x1cde97;
        }

        faceView.setImageBitmap(ImageLoader.getInstance().loadImageSync(face.getUri().toString(), new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
                .postProcessor(new BitmapAddBorderProcessor(color)).build()));
    }


    public void setItemClickListener(final FaceItem face, final View itemView) {
        if (face.isAddButton()) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //FacemojiManager.hideFaceSwitchView();
                    Intent i = new Intent(HSApplication.getContext(), CameraActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("FaceGridAdapter", true);
                    HSApplication.getContext().startActivity(i);
                }
            });
        } else {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (face.getUri().equals(FacemojiManager.getCurrentFacePicUri())) {
                        return;
                    }
                    FacemojiManager.setCurrentFacePicUri(face.getUri());
                    adapter.onFaceSelected();
                    notifyDataSetChanged();
                }
            });
        }
    }


    private void initAddFaceBtn(ImageView faceView) {

        FrameLayout.LayoutParams param = (FrameLayout.LayoutParams)faceView.getLayoutParams();
        param.height = (int) (size * 0.5);
        param.width = param.height;
        faceView.setLayoutParams(param);
        Drawable drawable = HSApplication.getContext().getResources().getDrawable(R.drawable.facemoji_add_btn);
        faceView.setBackgroundDrawable(drawable);
        faceView.setImageDrawable(null);

    }




}