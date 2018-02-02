package com.ihs.inputmethod.base;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.inputmethod.mydownload.MyDownloadsActivity;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.utils.ActionbarUtils;

/**
 * Created by jixiang on 18/1/20.
 */

public abstract class BaseListActivity extends HSAppCompatActivity {
    protected RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_list);

        ActionbarUtils.setCustomTitleWithBackIcon(this, findViewById(R.id.toolbar), getTitleTextResId());

        recyclerView = findViewById(R.id.recycler_view);

        findViewById(R.id.download_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDownloadsActivity.startThisActivity(BaseListActivity.this);
            }
        });

        initView();
    }

    protected abstract void initView();

    protected abstract int getTitleTextResId();

    protected void showDownloadIcon(boolean show) {
        findViewById(R.id.download_btn).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
