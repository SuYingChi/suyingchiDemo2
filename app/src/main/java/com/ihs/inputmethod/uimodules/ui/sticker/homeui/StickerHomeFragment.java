package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.uimodules.BuildConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiDownloadManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiCategory;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.sticker.StoreStickerDetailActivity;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate.StickerFacemojiAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate.StickerHomeCardAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;
import com.ihs.inputmethod.uimodules.ui.theme.utils.LockedCardActionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guonan.lv on 17/8/10.
 */

public class StickerHomeFragment extends Fragment implements LockerAppGuideManager.ILockerInstallStatusChangeListener {
    private boolean isVisibleToUser;
    private boolean isResume;

    private RecyclerView recyclerView;
    private HomeStickerAdapter stickerCardAdapter;
    private List<StickerHomeModel> stickerModelList = new ArrayList<>();
    private StickerHomeModel lastCheckedSticker;

    private INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (LockedCardActionUtils.UNLOCK_RATE_ALERT_SHOW.equals(s)
                    || LockedCardActionUtils.UNLOCK_SHARE_ALERT_SHOW.equals(s)
                    || FacemojiManager.FACEMOJI_SAVED.equals(s)
                    || FacemojiDownloadManager.FACEMOJI_CATEGORY_DOWNLOADED.equals(s)
                    || StickerDataManager.STICKER_DATA_LOAD_FINISH_NOTIFICATION.equals(s)
                    || (FacemojiManager.FACE_DELETED.equals(s) && FacemojiManager.getDefaultFacePicUri() == null) /** face被删除光了，才重新加载页面数据 */) {
                loadDatas();
            } else if (StickerHomeCardAdapterDelegate.NOTIFICATION_STICKER_DOWNLOADED.equals(s)) {
                int position = hsBundle.getInt("position");
                if (position > 0 && position < stickerModelList.size()) {
                    stickerModelList.remove(position);
                    stickerCardAdapter.notifyItemRemoved(position);
                }
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //随便设置一个，修复按Home键会crash的问题，方法来自https://stackoverflow.com/questions/14516804/nullpointerexception-android-support-v4-app-fragmentmanagerimpl-savefragmentbasi
        outState.putString("xxx", "xxx");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LockerAppGuideManager.getInstance().addLockerInstallStatusChangeListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sticker, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        initView();
        HSGlobalNotificationCenter.addObserver(FacemojiManager.FACEMOJI_SAVED, observer);
        HSGlobalNotificationCenter.addObserver(StickerDataManager.STICKER_DATA_LOAD_FINISH_NOTIFICATION, observer);
        HSGlobalNotificationCenter.addObserver(FacemojiManager.FACE_DELETED, observer);
        HSGlobalNotificationCenter.addObserver(FacemojiDownloadManager.FACEMOJI_CATEGORY_DOWNLOADED, observer);
        HSGlobalNotificationCenter.addObserver(LockedCardActionUtils.UNLOCK_RATE_ALERT_SHOW, observer);
        HSGlobalNotificationCenter.addObserver(LockedCardActionUtils.UNLOCK_SHARE_ALERT_SHOW, observer);
        HSGlobalNotificationCenter.addObserver(StickerHomeCardAdapterDelegate.NOTIFICATION_STICKER_DOWNLOADED, observer);
        return view;
    }

    private void initView() {
        stickerCardAdapter = new HomeStickerAdapter(getActivity(), new CommonStickerAdapter.OnStickerItemClickListener() {
            @Override
            public void onFacemojiClick(FacemojiSticker facemojiSticker) {
                goMyFacemojiActivity(facemojiSticker.getCategoryName());
            }

            @Override
            public void onCardClick(StickerHomeModel stickerHomeModel, Drawable drawable) {
                onDownloadClick(stickerHomeModel, drawable);
            }

            @Override
            public void onDownloadClick(final StickerHomeModel stickerHomeModel, Drawable drawable) {
                final StickerGroup stickerGroup = stickerHomeModel.stickerGroup;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 移除点击过的new角标
                        StickerDataManager.getInstance().removeNewTipOfStickerGroup(stickerGroup);
                        stickerCardAdapter.notifyItemChanged(stickerModelList.indexOf(stickerHomeModel));

                        Intent intent = new Intent(getActivity(), StoreStickerDetailActivity.class);
                        intent.putExtra(StoreStickerDetailActivity.STICKER_GROUP_BUNDLE, stickerGroup);
                        startActivityForResult(intent, 0);
                        lastCheckedSticker = stickerHomeModel;
                    }
                };

                if (LockedCardActionUtils.shouldLock(stickerHomeModel)) {
                    LockedCardActionUtils.handleLockAction(getActivity(), LockedCardActionUtils.LOCKED_CARD_FROM_STICKER, stickerHomeModel, runnable);
                } else {
                    runnable.run();
                }
            }

        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 6);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return stickerCardAdapter.getSpanSize(position);
            }
        });
        recyclerView.setAdapter(stickerCardAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);

        loadDatas();
    }

    private void loadDatas() {
        stickerModelList.clear();
        StickerHomeModel stickerHomeModel;

        if (BuildConfig.ENABLE_FACEMOJI) {
            stickerHomeModel = new StickerHomeModel();
            stickerHomeModel.isTitle = true;
            if (FacemojiManager.getDefaultFacePicUri() != null) {
                stickerHomeModel.title = HSApplication.getContext().getResources().getString(R.string.sticker_title_my_facemojis);
                stickerHomeModel.rightButton = HSApplication.getContext().getResources().getString(R.string.theme_store_more);
                stickerHomeModel.titleClickable = true;
                stickerHomeModel.titleClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goMyFacemojiActivity(null);
                    }
                };
                stickerModelList.add(stickerHomeModel);

                stickerHomeModel = new StickerHomeModel();
                stickerHomeModel.isSmallCreateFacemoji = true;
                stickerModelList.add(stickerHomeModel);

                List<FacemojiCategory> facemojiCategories = FacemojiManager.getInstance().getFacemojiCategories();
                if (facemojiCategories.size() > 0) {
                    FacemojiCategory facemojiCategory = facemojiCategories.get(0);
                    if (FacemojiDownloadManager.isFacemojiCategoryDownloadedSuccess(facemojiCategory.getName())) {
                        stickerHomeModel = new StickerHomeModel();
                        stickerHomeModel.isFacemoji = true;
                        stickerHomeModel.facemojiSticker = facemojiCategory.getStickerList().get(0);
                        stickerModelList.add(stickerHomeModel);
                    }

                    if (facemojiCategories.size() > 1) {
                        facemojiCategory = facemojiCategories.get(1);
                        if (FacemojiDownloadManager.isFacemojiCategoryDownloadedSuccess(facemojiCategory.getName())) {
                            if (FacemojiManager.getFaceList().size() >= 2) {
                                stickerHomeModel = new StickerHomeModel();
                                stickerHomeModel.isFacemoji = true;
                                stickerHomeModel.facemojiSticker = facemojiCategory.getStickerList().get(0);
                                stickerModelList.add(stickerHomeModel);
                            }
                        }
                    }
                }
            } else {
                stickerHomeModel.title = HSApplication.getContext().getResources().getString(R.string.sticker_title_create_my_facemojis);
                stickerHomeModel.titleClickable = false;
                stickerModelList.add(stickerHomeModel);

                stickerHomeModel = new StickerHomeModel();
                stickerHomeModel.isBigCreateFacemoji = true;
                stickerModelList.add(stickerHomeModel);
            }
        }

        stickerHomeModel = new StickerHomeModel();
        stickerHomeModel.isTitle = true;
        stickerHomeModel.title = HSApplication.getContext().getResources().getString(R.string.sticker_title_funny_stickers);
        stickerModelList.add(stickerHomeModel);

        List<StickerGroup> stickerGroupList = StickerDataManager.getInstance().getStickerGroupList();
        for (StickerGroup stickerGroup : stickerGroupList) {
            if (!stickerGroup.isStickerGroupDownloaded()) {
                stickerHomeModel = new StickerHomeModel();
                stickerHomeModel.stickerGroup = stickerGroup;
                stickerModelList.add(stickerHomeModel);
            }
        }

        stickerHomeModel = new StickerHomeModel();
        stickerHomeModel.isMoreComing = true;
        stickerModelList.add(stickerHomeModel);

        stickerCardAdapter.setItems(stickerModelList);
        stickerCardAdapter.notifyDataSetChanged();
    }

    private void goMyFacemojiActivity(String categoryName) {
        try {
            Intent intent = new Intent(getActivity(), Class.forName("com.ihs.inputmethod.uimodules.ui.facemoji.ui.MyFacemojiActivity"));
            if (!TextUtils.isEmpty(categoryName)) {
                intent.putExtra(FacemojiManager.INIT_SHOW_TAB_CATEGORY, categoryName);
            }
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("com.ihs.inputmethod.uimodules.ui.facemoji.ui.MyFacemojiActivity not find");
        }
    }

    @Override
    public void onResume() {
        isResume = true;
        startFacemojiAnim();
        super.onResume();
    }

    @Override
    public void onStop() {
        isResume = false;
        stopFacemojiAnim();
        super.onStop();
    }

    private void startFacemojiAnim() {
        if (BuildConfig.ENABLE_FACEMOJI) {
            if (isVisibleToUser && isResume) {
                setAnimStatus(true);
            }
        }
    }

    private void stopFacemojiAnim() {
        if (BuildConfig.ENABLE_FACEMOJI) {
            setAnimStatus(false);
        }
    }

    private void setAnimStatus(boolean start) {
        if (recyclerView != null && recyclerView.getChildCount() > 0 && stickerCardAdapter != null) {
            stickerCardAdapter.setPlayStickerFacemoij(start);
            for (int i = 0; i < stickerModelList.size(); i++) {
                if (stickerModelList.get(i).isFacemoji) {
                    RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                    if (viewHolder != null && viewHolder instanceof StickerFacemojiAdapterDelegate.StickerFacemojiViewHolder) {
                        if (start) {
                            ((StickerFacemojiAdapterDelegate.StickerFacemojiViewHolder) viewHolder).facemojiAnimationView.start();
                        } else {
                            ((StickerFacemojiAdapterDelegate.StickerFacemojiViewHolder) viewHolder).facemojiAnimationView.stop();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        if (isVisibleToUser) {
            startFacemojiAnim();
        } else {
            stopFacemojiAnim();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }


    @Override
    public void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(observer);
        LockerAppGuideManager.getInstance().removeLockerInstallStatusChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onLockerInstallStatusChange() {
        if (stickerCardAdapter != null) {
            loadDatas();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == StoreStickerDetailActivity.RESULT_CODE_SUCCESS) {
            if (lastCheckedSticker != null) {
                int position = stickerModelList.indexOf(lastCheckedSticker);
                if (position > 0 && position < stickerModelList.size()) {
                    stickerModelList.remove(position);
                    stickerCardAdapter.notifyItemRemoved(position);
                }
            }
        }
    }
}
