package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.utils.DisplayUtils;


public class FaceListActivity extends HSAppCompatActivity implements View.OnClickListener{
    private int screenHeight;
    private RelativeLayout navigation_bar;
    private GridView faceGrid;
    private static final int PAGER_TOP_PADDING_PX = 10;
    private static final int PAGER_BOTTOM_PADDING_PX = 10;
    private static final int GRID_VERTICAL_GAP_PX = 30;
    private static final int GRID_COLUMN_NUMBER = 3;
    private static final int GRID_ROW_NUMBER = 5;
    private int faceItemDimension;
    private FaceGridAdapter adapter;
    private ImageView back_button;
    private TextView editBtn;
    private TextView deleteBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.facelist_activity);
        screenHeight = DisplayUtils.getScreenHeightForContent() - DisplayUtils.getStatusBarHeight(getWindow());
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        navigation_bar = (RelativeLayout)findViewById(R.id.navigation_bar_facelist);
        LinearLayout.LayoutParams navigation_bar_param=(LinearLayout.LayoutParams)navigation_bar.getLayoutParams();
        navigation_bar_param.height = getNavigateBarHeight();
        navigation_bar.setLayoutParams(navigation_bar_param);


        faceGrid = (GridView)findViewById(R.id.face_grid);
        setGridViewLayoutProperties();
        adapter = new FaceGridAdapter(faceItemDimension, this);
        faceGrid.setAdapter(adapter);


        back_button = (ImageView)findViewById(R.id.facelist_close_button);
        LinearLayout.LayoutParams back_param= (LinearLayout.LayoutParams)back_button.getLayoutParams();
        back_param.height = (int)(getResources().getDrawable(R.drawable.back_button).getIntrinsicHeight() * 0.8);
        back_param.width = (int)(getResources().getDrawable(R.drawable.back_button).getIntrinsicWidth() * 0.8);
        back_button.setLayoutParams(back_param);

        View backButtonHolder = findViewById(R.id.facelist_close_button_holder);
        RelativeLayout.LayoutParams holderPara = (RelativeLayout.LayoutParams) backButtonHolder.getLayoutParams();
        holderPara.width = (int) (back_param.width * 2.5f);
        holderPara.height = getNavigateBarHeight();
        backButtonHolder.setLayoutParams(holderPara);
        backButtonHolder.setOnClickListener(this);

        editBtn = (TextView) findViewById(R.id.facelist_edit_btn);

        View editButtonHolder = findViewById(R.id.facelist_edit_btn_holder);
        RelativeLayout.LayoutParams editHolderPara = (RelativeLayout.LayoutParams) editButtonHolder.getLayoutParams();
        //editHolderPara.width = getNavigateBarHeight();
        editHolderPara.height = getNavigateBarHeight();
        editButtonHolder.setLayoutParams(editHolderPara);
        editButtonHolder.setOnClickListener(this);

        deleteBtn = (TextView)findViewById(R.id.face_delete_btn);
        StateListDrawable deletedBackground = new StateListDrawable();
        deletedBackground.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(getResources().getColor(R.color.facemoji_face_pressed_color)));
        deletedBackground.addState(new int[]{}, new ColorDrawable(getResources().getColor(R.color.facemoji_face_deleted_bg)));
        deleteBtn.setBackgroundDrawable(deletedBackground);
        deleteBtn.setClickable(true);
        deleteBtn.setOnClickListener(this);
        LinearLayout.LayoutParams deleteBtn_param = (LinearLayout.LayoutParams)deleteBtn.getLayoutParams();
        deleteBtn_param.height = getNavigateBarHeight();
        navigation_bar.setLayoutParams(navigation_bar_param);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("toggleEditMode", false)) {
            toggleEditMode();
        }
    }

    private int getNavigateBarHeight(){
        return (int)(screenHeight * 0.10);
    }

    private void setGridViewLayoutProperties(){
        int gridHeight = (int)(screenHeight * 0.90);
        faceGrid.setNumColumns(GRID_COLUMN_NUMBER);
        faceGrid.setVerticalSpacing(GRID_VERTICAL_GAP_PX);
        faceItemDimension =(int)((float)(gridHeight - PAGER_BOTTOM_PADDING_PX - PAGER_TOP_PADDING_PX - GRID_VERTICAL_GAP_PX * (GRID_ROW_NUMBER - 1)) / GRID_ROW_NUMBER);
    }

    private void toggleEditMode(){

        adapter.setEditMode(!adapter.isInEditMode());
        if(adapter.isInEditMode()){
            adapter.resetAllItems();
            editBtn.setText("Cancel");
            if(adapter.getCount()<=1){
                deleteBtn.setTextColor(getResources().getColor(R.color.facemoji_delete_btn_text_disabled));
                deleteBtn.setClickable(false);
            }else{
                deleteBtn.setTextColor(Color.RED);
                deleteBtn.setClickable(true);
            }
            deleteBtn.setVisibility(View.VISIBLE);
        }
        else{
            editBtn.setText("Edit");
            deleteBtn.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();

    }



    private void deleteSelectedFace(){
        adapter.deleteSelectedFace();
        toggleEditMode();
        HSGlobalNotificationCenter.sendNotificationOnMainThread(CameraActivity.FACE_CHANGED);
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
       if (id == R.id.facelist_close_button_holder) {
                finish();
       } else if (id == R.id.facelist_edit_btn_holder){
                toggleEditMode();
       } else if (id == R.id.face_delete_btn) {
                deleteSelectedFace();
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        adapter.setmData(FacemojiManager.getFaceList());
//    }
}
