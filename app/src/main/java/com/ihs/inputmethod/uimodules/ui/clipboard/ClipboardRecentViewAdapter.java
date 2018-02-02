package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.TextUtils;

import junit.framework.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class ClipboardRecentViewAdapter extends RecyclerView.Adapter<ClipboardRecentViewAdapter.ViewHolder> {


    private List<ClipboardRecentMessage> clipRecentData = new ArrayList<ClipboardRecentMessage>();
    private SaveRecentItemToPinsListener saveRecentItemToPinsListener;

    //注意处理完list再传进来
    public ClipboardRecentViewAdapter(List<ClipboardRecentMessage> list, SaveRecentItemToPinsListener saveRecentItemToPinsListener) {
        clipRecentData.addAll(list);
        this.saveRecentItemToPinsListener = saveRecentItemToPinsListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.recentclip_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ClipboardRecentMessage recentClipItemContent = clipRecentData.get(position);
        holder.clipContent.setText(recentClipItemContent.recentClipItemContent);
        holder.saveToPinsImageView.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.launch_page_icon));
        if (!recentClipItemContent.isPined) {
            holder.saveToPinsImageView.setBackgroundDrawable(ClipboardPresenter.getInstance().pinedBackground());
        } else {
            holder.saveToPinsImageView.setBackgroundDrawable(ClipboardPresenter.getInstance().noPinedBackground());
        }
        holder.saveToPinsImageView.setClickable(true);
        holder.saveToPinsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加到pins页面
                saveRecentItemToPinsListener.saveToPins(recentClipItemContent.recentClipItemContent, holder.saveToPinsImageView, position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.inputText(recentClipItemContent.recentClipItemContent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return clipRecentData.size();
    }

    public void dataChangeAndRefresh(List<ClipboardRecentMessage> clipRecentData) {
        this.clipRecentData.clear();
        this.clipRecentData.addAll(clipRecentData);
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView saveToPinsImageView;
        TextView clipContent;

        public ViewHolder(View view) {
            super(view);
            clipContent = (TextView) view.findViewById(R.id.clip_content);
            saveToPinsImageView = (ImageView) view.findViewById(R.id.save_to_pins);
        }
    }

    public interface SaveRecentItemToPinsListener {
        void saveToPins(String itemRecentContent, ImageView RecentItemImageView, int position);
    }


    //重写equals方法
    public static class ClipboardRecentMessage {
        String recentClipItemContent;
        boolean isPined;

        public ClipboardRecentMessage(String recentClipItemContent, boolean isPined) {
            this.recentClipItemContent = recentClipItemContent;
            this.isPined = isPined;
        }

        public ClipboardRecentMessage() {

        }

        public void setRecentClipItemContent(String recentClipItemContent) {
            this.recentClipItemContent = recentClipItemContent;
        }

        public void setPined(boolean pined) {
            isPined = pined;
        }

        public String getRecentClipItemContent() {
            return recentClipItemContent;
        }

        public boolean getPined() {
            return isPined;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if ((obj == null) || (obj.getClass() != this.getClass()))
                return false;
            // object must be Test at this point
            ClipboardRecentMessage clipboardRecentMessage = (ClipboardRecentMessage) obj;
            return recentClipItemContent.equals(clipboardRecentMessage.recentClipItemContent) && isPined == clipboardRecentMessage.isPined;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = result * 31 + recentClipItemContent.hashCode();
            if (isPined) {
                result = result * 31 +1;
            }else {
                result = result * 31 +0;
            }
            return result;
        }
    }
}
