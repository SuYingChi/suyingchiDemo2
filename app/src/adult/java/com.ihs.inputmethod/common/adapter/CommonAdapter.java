package com.ihs.inputmethod.common.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by jixiang on 18/1/18.
 */

public abstract class CommonAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected List<T> dataList;
    protected Activity activity;

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }

    public CommonAdapter(Activity activity) {
        this.activity = activity;
    }


    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }
}
