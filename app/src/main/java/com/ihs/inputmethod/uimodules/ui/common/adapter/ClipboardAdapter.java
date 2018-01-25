package com.ihs.inputmethod.uimodules.ui.common.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class ClipboardAdapter extends RecyclerView.Adapter<ClipboardAdapter.ViewHolder> {

    public static final String clipSpName = "Clip_Content";
    private List<String> clipContent = new ArrayList<String>();

    public ClipboardAdapter(List<String> list){
        clipContent = list;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.clipboard_item_layout,parent,false));

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView saveToPins;
        TextView clipContent;

        public ViewHolder(View view) {
            super(view);
            clipContent = (TextView) view.findViewById(R.id.clip_content);
            saveToPins = (ImageView) view.findViewById(R.id.save_to_pins);
        }
    }
}
