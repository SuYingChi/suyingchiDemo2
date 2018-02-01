package com.ihs.inputmethod.uimodules.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.List;

public class LockerGuideAlert extends AlertDialog implements View.OnClickListener {
    private static final int TYPE_0 = 0; //蓝色
    private static final int TYPE_1 = 1; //紫色

    public LockerGuideAlert(@NonNull Context context) {
        super(context, R.style.LockerGuideDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locker_guide_alert);

        TextView title = findViewById(R.id.title);
        title.setText(HSConfig.optString(getContext().getResources().getString(R.string.locker_alert_title), "Application", "DownloadScreenLocker", "AppOpen", "title"));

        Button enableBtn = findViewById(R.id.enable_btn);
        enableBtn.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.locker_guide_button_bg));
        enableBtn.setText(HSConfig.optString(getContext().getResources().getString(R.string.enable_now), "Application", "DownloadScreenLocker","AppOpen", "button"));
        enableBtn.setOnClickListener(this);

        ImageView closeBtn = findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(this);

        ImageView headBg = findViewById(R.id.head_bg);
        View middleContainer = findViewById(R.id.middle_container);
        ImageView bottomBg = findViewById(R.id.bottom_bg);

        if (!(getContext() instanceof Activity)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(HSApplication.getContext())) {
                getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            } else {
                getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        int type = Double.valueOf(AutopilotConfig.getDoubleToTestNow("topic-1512033355055", "ui_type", TYPE_0)).intValue();
        List<String> text = (List<String>) HSConfig.getList("Application", "DownloadScreenLocker", "AppOpen", "body");

        recyclerView.addItemDecoration(new GridSpacingItemDecoration(type,HSDisplayUtils.dip2px(8),HSDisplayUtils.dip2px(16)));
        ItemAdapter adapter = new ItemAdapter(text, type);

        int width = HSDisplayUtils.getScreenWidthForContent();

        if (type == TYPE_0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            middleContainer.setPadding((int) (100.0 * width / 1080), HSDisplayUtils.dip2px(10), (int) (80.0 * width / 1080), HSDisplayUtils.dip2px(10));

            FrameLayout.LayoutParams enableBtnLayoutParams = (FrameLayout.LayoutParams) enableBtn.getLayoutParams();
            enableBtnLayoutParams.leftMargin = (100 - 82) / 2;
        } else {
            headBg.setImageResource(R.drawable.locker_guide_alert_style1_head);
            bottomBg.setImageResource(R.drawable.locker_guide_alert_style1_bottom);

            middleContainer.setBackgroundResource(R.drawable.locker_guide_alert_style1_middle_bg);
            middleContainer.setPadding((int) (107.0 * width / 1080), 0, (int) (86.0 * width / 1080), 0);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 1;
                }
            });
            recyclerView.setLayoutManager(gridLayoutManager);


            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) enableBtn.getLayoutParams();
            layoutParams.gravity = Gravity.CENTER;

            FrameLayout.LayoutParams titleLayoutParms = (FrameLayout.LayoutParams) title.getLayoutParams();
            titleLayoutParms.topMargin = HSDisplayUtils.dip2px(100);
        }
        recyclerView.setAdapter(adapter);
    }

    private final static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int rowSpacing;
        private int columnSpacing;
        private int type;

        public GridSpacingItemDecoration(int type, int rowSpacing, int columnSpacing) {
            this.type = type;
            this.rowSpacing = rowSpacing / 2;
            this.columnSpacing = columnSpacing / 2;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (type == TYPE_1){
                int position = parent.getChildAdapterPosition(view); // item position
                if ((position & 1) != 0) {
                    outRect.left = columnSpacing;
                } else {
                    outRect.right = columnSpacing;
                }
            }

            outRect.bottom = rowSpacing;
            outRect.top = rowSpacing;
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<VHolder> {
        List<String> data;
        // --Commented out by Inspection (18/1/11 下午2:41):int type;

        public ItemAdapter(List<String> data, int type) {
            this.data = data;
        }

        @Override
        public VHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            VHolder vHolder = new VHolder(View.inflate(parent.getContext(), R.layout.item_locker_introduce, null));
            return vHolder;
        }

        @Override
        public void onBindViewHolder(VHolder holder, int position) {
            String text = data.get(position);
            holder.text.setText(text);
        }

        @Override
        public int getItemCount() {
            if (data != null) {
                return data.size();
            }
            return 0;
        }

    }


    public static class VHolder extends RecyclerView.ViewHolder {
        TextView text;

        public VHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.enable_btn) {
            AutopilotEvent.logTopicEvent("topic-1512033355055", "locker_alert_button_clicked");
            LockerAppGuideManager.getInstance().downloadOrRedirectToLockerApp(LockerAppGuideManager.FLURRY_ALERT_OPEN_APP);
            dismiss();
        } else if (id == R.id.close_btn) {
            dismiss();
        }
    }


    @Override
    public void show() {
        try {
            super.show();
            /**
             * 设置dialog宽度全屏
             */
            WindowManager.LayoutParams params = getWindow().getAttributes();  //获取对话框当前的参数值、
            params.width = HSDisplayUtils.getScreenWidthForContent();    //宽度设置全屏宽度
            getWindow().setAttributes(params);     //设置生效
        } catch (Exception e) {
        }
    }
}
