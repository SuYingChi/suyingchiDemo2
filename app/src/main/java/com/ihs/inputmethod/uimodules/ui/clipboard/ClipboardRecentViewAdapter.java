package com.ihs.inputmethod.uimodules.ui.clipboard;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;



public class ClipboardRecentViewAdapter extends RecyclerView.Adapter<ClipboardRecentViewAdapter.ViewHolder> {


    private List<ClipboardRecentMessage> clipRecentData = new ArrayList<ClipboardRecentMessage>();
    private SaveRecentItemToPinsListener saveRecentItemToPinsListener;
    private final String TAG = ClipboardRecentViewAdapter.class.getSimpleName();
    private Drawable saveToPinsDrawable;
    private Drawable saveToPinsNoPinedDrawable;
    ClipboardRecentViewAdapter(List<ClipboardRecentMessage> list, SaveRecentItemToPinsListener saveRecentItemToPinsListener) {
        clipRecentData.addAll(list);
        this.saveRecentItemToPinsListener = saveRecentItemToPinsListener;
        saveToPinsDrawable = VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.clipboard_save_to_pins_pined,null);
        saveToPinsNoPinedDrawable = VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.clipboard_save_to_pins_no_pined,null);
        HSLog.d(TAG, "create ClipboardRecentViewAdapter , clipRecentData  is" + clipRecentData.toString());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.clipboard_recent_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ClipboardRecentMessage recentClipItemContent = clipRecentData.get(position);
        holder.clipContent.setText(recentClipItemContent.recentClipItemContent);
        holder.clipContent.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getKeyTextColor(Color.WHITE));
        if (recentClipItemContent.isPined == 1) {
            holder.saveToPinsImageView.setImageDrawable(saveToPinsDrawable);
        } else if (recentClipItemContent.isPined == 0) {
            holder.saveToPinsImageView.setImageDrawable(saveToPinsNoPinedDrawable);
        }
        holder.saveToPinsImageView.setClickable(true);
        holder.saveToPinsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加到pins页面
                saveRecentItemToPinsListener.saveToPins(recentClipItemContent.recentClipItemContent);
                HSLog.d(TAG, "save recentItem content to pins  " + recentClipItemContent.recentClipItemContent);
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
        HSLog.d("clipboard", " notifyDataSetChanged  recentAdapter ,  clipRecentData is" + clipRecentData.toString());
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
        void saveToPins(String itemRecentContent);
    }


    //布尔值改为int值 不然不方便数据库操作
    public static class ClipboardRecentMessage {
        String recentClipItemContent;
        int isPined;

        public ClipboardRecentMessage(String recentClipItemContent, int isPined) {
            this.recentClipItemContent = recentClipItemContent;
            this.isPined = isPined;
        }

        @Override
        public String toString() {
            return "  recentClipItemContent  =" + recentClipItemContent + "  isPined =" + isPined;
        }
    }
}