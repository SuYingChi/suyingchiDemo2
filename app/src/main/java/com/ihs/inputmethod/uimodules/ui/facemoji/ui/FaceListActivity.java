package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.feature.common.VectorCompat;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.utils.DisplayUtils;


public class FaceListActivity extends HSAppCompatActivity implements View.OnClickListener, FaceGridAdapter.OnSelectedFaceChangedListener {
    public final static String TOGGLE_MANAGE_FACE_MODE = "toggleManageFaceMode";
    private int screenHeight;
    private GridView faceGrid;
    private static final int PAGER_TOP_PADDING_PX = 10;
    private static final int PAGER_BOTTOM_PADDING_PX = 10;
    private static final int GRID_VERTICAL_GAP_PX = 30;
    private static final int GRID_COLUMN_NUMBER = 3;
    private static final int GRID_ROW_NUMBER = 5;
    private int faceItemDimension;
    private FaceGridAdapter adapter;
    private TextView editBtn;
    private ImageView deleteBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.facelist_activity);
        screenHeight = DisplayUtils.getScreenHeightForContent() - DisplayUtils.getStatusBarHeight(getWindow());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.switch_facemoji_toolbar_title));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        faceGrid = (GridView) findViewById(R.id.face_grid);
        setGridViewLayoutProperties();
        adapter = new FaceGridAdapter(faceItemDimension, this);
        adapter.setOnSelectedFaceChangedListener(this);
        faceGrid.setAdapter(adapter);

        editBtn = (TextView) findViewById(R.id.facelist_edit_btn);

        View editButtonHolder = findViewById(R.id.facelist_edit_btn_holder);
        RelativeLayout.LayoutParams editHolderPara = (RelativeLayout.LayoutParams) editButtonHolder.getLayoutParams();
        editHolderPara.height = getNavigateBarHeight();
        editButtonHolder.setLayoutParams(editHolderPara);
        editButtonHolder.setOnClickListener(this);

        deleteBtn = (ImageView) findViewById(R.id.delete_face);
        deleteBtn.setOnClickListener(this);
        LinearLayout.LayoutParams deleteBtn_param = (LinearLayout.LayoutParams) deleteBtn.getLayoutParams();
        deleteBtn_param.height = getNavigateBarHeight();

        Intent intent = getIntent();
        if (intent.getBooleanExtra(TOGGLE_MANAGE_FACE_MODE, false)) {
            switchEditMode();
        }
    }

    private int getNavigateBarHeight() {
        return (int) (screenHeight * 0.10);
    }

    private void setGridViewLayoutProperties() {
        int gridHeight = (int) (screenHeight * 0.90);
        faceGrid.setNumColumns(GRID_COLUMN_NUMBER);
        faceGrid.setVerticalSpacing(GRID_VERTICAL_GAP_PX);
        faceItemDimension = (int) ((float) (gridHeight - PAGER_BOTTOM_PADDING_PX - PAGER_TOP_PADDING_PX - GRID_VERTICAL_GAP_PX * (GRID_ROW_NUMBER - 1)) / GRID_ROW_NUMBER);
    }

    private void switchEditMode() {

        adapter.setEditMode(!adapter.isInEditMode());
        if (adapter.isInEditMode()) {
            deleteBtn.setVisibility(View.VISIBLE);
            editBtn.setVisibility(View.GONE);
            adapter.resetAllItems();
            if (adapter.getCount() <= 1) {
                deleteBtn.setClickable(false);
            } else {
                deleteBtn.setClickable(true);
            }
            deleteBtn.setVisibility(View.VISIBLE);
            VectorDrawableCompat closeDrawable = VectorCompat.createVectorDrawable(this, R.drawable.ic_close_black_24dp);
            DrawableCompat.setTint(closeDrawable,getResources().getColor(R.color.white));
            getSupportActionBar().setHomeAsUpIndicator(closeDrawable);
            showEditStatusTitle(0);
        } else {
            deleteBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.VISIBLE);
            getSupportActionBar().setHomeAsUpIndicator(null);
            getSupportActionBar().setTitle(getResources().getString(R.string.switch_facemoji_toolbar_title));
        }
        adapter.notifyDataSetChanged();

    }

    private void showEditStatusTitle(int selectedCount){
        getSupportActionBar().setTitle(getResources().getString(R.string.switch_facemoji_selected_status_title,selectedCount));
    }

    private void deleteSelectedFace() {
        adapter.deleteSelectedFace();
        switchEditMode();
        HSGlobalNotificationCenter.sendNotificationOnMainThread(CameraActivity.FACE_DELETED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (adapter.isInEditMode()) {
                    switchEditMode();
                }else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.facelist_edit_btn_holder) {
            switchEditMode();
        } else if (id == R.id.delete_face) {
            deleteSelectedFace();
        }
    }

    @Override
    public void onSelectedFaceChange(int selectedCount) {
        showEditStatusTitle(selectedCount);
    }
}
