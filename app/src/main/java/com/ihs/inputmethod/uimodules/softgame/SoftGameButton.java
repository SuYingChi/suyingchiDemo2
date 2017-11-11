package com.ihs.inputmethod.uimodules.softgame;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSJsonUtil;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.feature.common.VectorCompat;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static com.ihs.inputmethod.uimodules.softgame.FirstFragment.JSON_GAMES;

/**
 * Created by liuzhongtao on 17/7/18.
 */

public class SoftGameButton extends FrameLayout {
    private static final String NEW_GAME_URL = "http://api.famobi.com/feed?a=A-KCVWU&n=1";
    private static final String LAST_GAME_ID = "last_game_id";


    private ImageView buttonIcon;
    private View newTipDotView; //小红点


    public SoftGameButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public SoftGameButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SoftGameButton(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        buttonIcon = new ImageView(getContext());
        int padding = HSDisplayUtils.dip2px(6);
        buttonIcon.setPadding(padding, padding, padding, padding);
        buttonIcon.setImageDrawable(VectorCompat.createVectorDrawable(getContext(), R.drawable.soft_game_button_icon));

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;

        addView(buttonIcon, lp);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到详情页
                HSAnalytics.logEvent("keyboard_game_clicked");
                SoftGameDisplayHelper.DisplaySoftGames(getContext().getString(R.string.ad_placement_themetryad));
                hideNewMark();
            }
        });
    }

    public void checkNewGame() {
        HSHttpConnection hsHttpConnection = new HSHttpConnection(NEW_GAME_URL);
        hsHttpConnection.startAsync();
        hsHttpConnection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                JSONObject bodyJSON = hsHttpConnection.getBodyJSON();
                try {
                    List<Object> jsonMap = HSJsonUtil.toList(bodyJSON.getJSONArray(JSON_GAMES));
                    if (!jsonMap.isEmpty()) {
                        Map<String, String> object = (Map<String, String>) jsonMap.get(0);
                        String newId = object.get("package_id");
                        String lastId = HSPreferenceHelper.getDefault().getString(LAST_GAME_ID, "");
                        if (lastId.equals(newId)) {
//                            return;
                        } else {
                            HSPreferenceHelper.getDefault().putString(LAST_GAME_ID, newId);
                        }

                        String name = object.get("name");
                        String description = object.get("description");
                        String thumb = object.get("thumb");
                        String link = object.get("link");

                        showNewGameTip(new SoftGameItemBean(name, description, thumb, link));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                hsError.getMessage();
            }
        });
        showNewMark();
    }

    private void showNewGameTip(SoftGameItemBean softGameItemBean) {
        if (softGameItemBean == null) {
            return;
        }

        // 显示新游戏提示
        final View newGameTip = LayoutInflater.from(getContext()).inflate(R.layout.new_game_tip, this, false);
        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DisplayUtils.dip2px(239), DisplayUtils.dip2px(112));
        int[] location = new int[2];
        buttonIcon.getLocationInWindow(location);
        lp.setMargins(0, location[1] - DisplayUtils.dip2px(112), 0, 0);
        lp.addRule(ALIGN_PARENT_RIGHT);

        TextView gameTitle = (TextView) newGameTip.findViewById(R.id.new_game_title);
        gameTitle.setText(softGameItemBean.getName());

        ImageLoader.getInstance().displayImage(softGameItemBean.getThumb(), (ImageView) newGameTip.findViewById(R.id.new_game_icon));

        newGameTip.findViewById(R.id.iv_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                HSUIInputMethodService.getKeyboardPanelMananger().getKeyboardPanelSwitchContainer().removeView(newGameTip);
            }
        });

        newGameTip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        try {
            PopupWindow popupWindow = new PopupWindow(newGameTip);
            popupWindow.showAtLocation(HSUIInputMethodService.getKeyboardPanelMananger().getKeyboardPanelSwitchContainer(),ALIGN_PARENT_RIGHT,-100,-100);
        } catch (Exception e) {
            e.printStackTrace();
        }


//        HSUIInputMethodService.getKeyboardPanelMananger().getKeyboardPanelSwitchContainer().addView(newGameTip, lp);
    }

    public void showNewMark() {
        if (newTipDotView == null) {
            newTipDotView = new View(HSApplication.getContext());
            GradientDrawable redPointDrawable = new GradientDrawable();
            redPointDrawable.setColor(Color.RED);
            redPointDrawable.setShape(GradientDrawable.OVAL);
            newTipDotView.setBackgroundDrawable(redPointDrawable);

            int width = HSDisplayUtils.dip2px(7);
            int height = HSDisplayUtils.dip2px(7);
            LayoutParams layoutParams = new LayoutParams(width, height);
            layoutParams.rightMargin = HSDisplayUtils.dip2px(4);
            layoutParams.topMargin = HSDisplayUtils.dip2px(4);
            layoutParams.gravity = Gravity.TOP | Gravity.END;
            newTipDotView.setLayoutParams(layoutParams);
            addView(newTipDotView);
        }
    }

    public void hideNewMark() {
        if (newTipDotView != null) {
            removeView(newTipDotView);
            newTipDotView.setVisibility(GONE);
            newTipDotView = null;
        }
    }
}
