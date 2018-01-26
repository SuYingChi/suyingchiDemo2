package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.background;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeBackgroundCropperActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.ThemePageItem;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.ThemePageItem.CategoryItem;
import com.ihs.inputmethod.uimodules.ui.theme.utils.easyphotopicker.DefaultCallback;
import com.ihs.inputmethod.uimodules.ui.theme.utils.easyphotopicker.EasyImage;
import com.keyboard.core.themes.custom.KCCustomThemeData;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.keyboard.core.themes.custom.elements.KCBackgroundElement;

import java.io.File;
import java.util.Arrays;

import static android.app.Activity.RESULT_OK;

/**
 * Created by chenyuanming on 31/10/2016.
 */

public class BackgroundFragment extends BaseThemeFragment implements LockerAppGuideManager.ILockerInstallStatusChangeListener {
    public static final int CROPPER_IMAGE_REQUEST_CODE = 102;
    private final static int TYPE_OPEN_CAMERA = 1000;
    private final static int TYPE_OPEN_GALLERY = 1001;
    int lastTakePicType = -1;
    private EntryMode entryMode;
    private OnSelectCallback callback;

    public EntryMode getEntryMode() {
        return entryMode;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LockerAppGuideManager.getInstance().addLockerInstallStatusChangeListener(this);
    }

    public void setEntryMode(EntryMode entryMode) {
        this.entryMode = entryMode;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EasyImage.configuration(getActivity())
                .setImagesFolderName("keyboard")
                .setCopyExistingPicturesToPublicLocation(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        HSThemeNewTipController.getInstance().removeNewTip(HSThemeNewTipController.ThemeTipType.NEW_TIP_BACKGROUND); // 清除对应元素new mark
    }

    @Override
    protected ThemePageItem initiateThemePageItem() {
        return new ThemePageItem(Arrays.<CategoryItem<? extends Object>>asList(
                new CategoryItem<>(HSApplication.getContext().getString(R.string.custom_theme_title_background), Integer.class, new CameraAlbumProvider(this), Arrays.asList(new Integer[]{R.drawable.custom_theme_background_camera_fg, R.drawable.custom_theme_background_album_fg})),
                new CategoryItem<>(HSApplication.getContext().getString(R.string.custom_theme_title_background), KCBackgroundElement.class, new BackgroundProvider(this), KCCustomThemeManager.getInstance().getBackgroundElements())
        ));
    }

    public void pickFromCamera(OnSelectCallback callback) {
        EasyImage.openCamera(this, TYPE_OPEN_CAMERA);
        this.callback = callback;
    }

    public void pickFromGallery(OnSelectCallback callback) {
        try {
            EasyImage.openGallery(this, TYPE_OPEN_GALLERY);
        } catch (Exception e) {
            EasyImage.openDocuments(this, TYPE_OPEN_GALLERY);
        }
        this.callback = callback;
    }

    public void setKeyboardTheme(Intent data) {
        if (callback != null) {
            callback.onSelectItem(TYPE_OPEN_GALLERY);
        }
        if (data != null) {
            getCustomThemeData().setCustomizedBackgroundImagePath(data.getStringExtra("CropperImagePath"));
            KCCustomThemeData.ImageSource preImageSource = getCustomThemeData().getBackgroundImageSource();
            getCustomThemeData().setBackgroundImageSource(KCCustomThemeData.ImageSource.Album);
            getCustomThemeData().setBackgroundImageSource(KCCustomThemeData.ImageSource.Album);
            if (preImageSource == KCCustomThemeData.ImageSource.Official) {
                notifyAdapterOnMainThread();
            }
            refreshKeyboardView();
        }
    }

    @Override
    public void onDestroy() {
        LockerAppGuideManager.getInstance().removeLockerInstallStatusChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        HSLog.d("BackgroundFragment", "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (requestCode == CROPPER_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (lastTakePicType == TYPE_OPEN_CAMERA || lastTakePicType == TYPE_OPEN_GALLERY) {
                    if (callback != null) {
                        callback.onSelectItem(lastTakePicType);
                    }
                }

                if (data != null) {
                    getCustomThemeData().setCustomizedBackgroundImagePath(data.getStringExtra("CropperImagePath"));
                    KCCustomThemeData.ImageSource preImageSource = getCustomThemeData().getBackgroundImageSource();
                    if (lastTakePicType == TYPE_OPEN_CAMERA) {
                        getCustomThemeData().setBackgroundImageSource(KCCustomThemeData.ImageSource.Camera);
                    } else if (lastTakePicType == TYPE_OPEN_GALLERY) {
                        getCustomThemeData().setBackgroundImageSource(KCCustomThemeData.ImageSource.Album);
                    }
                    if (preImageSource == KCCustomThemeData.ImageSource.Official) {
                        notifyAdapterOnMainThread();
                    }
                    refreshKeyboardView();
                }
            }
        } else {
            lastTakePicType = -1;//reset last select state
            EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
                @Override
                public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                    //Some error handling
                }

                @Override
                public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                    //Handle the image
                    lastTakePicType = type;
                    onPhotoReturned(imageFile);
                }

                @Override
                public void onCanceled(EasyImage.ImageSource source, int type) {
                    //Cancel handling, you might wanna remove taken photo if it was canceled
                    if (source == EasyImage.ImageSource.CAMERA) {
                        File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getActivity());
                        if (photoFile != null) photoFile.delete();
                    }

                }
            });
        }
    }

    private void onPhotoReturned(File photoFile) {
        Intent openBackgroundCropperIntent = new Intent(getContext(), CustomThemeBackgroundCropperActivity.class);
        openBackgroundCropperIntent.putExtra(CustomThemeBackgroundCropperActivity.CopperImagePath, photoFile.getAbsolutePath());
        final Resources res = HSApplication.getContext().getResources();
        final int keyboardWidth = HSResourceUtils.getDefaultKeyboardWidth(res);
        final int keyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(res);
        openBackgroundCropperIntent.putExtra(CustomThemeBackgroundCropperActivity.KeyboardWidth, keyboardWidth);
        openBackgroundCropperIntent.putExtra(CustomThemeBackgroundCropperActivity.KeyboardHeight, keyboardHeight);
        String oldCropperImagePath = getCustomThemeData().getCustomizedBackgroundImagePath();
        openBackgroundCropperIntent.putExtra(CustomThemeBackgroundCropperActivity.OldCropperImagePath, oldCropperImagePath == null ? "" : oldCropperImagePath);
        startActivityForResult(openBackgroundCropperIntent, CROPPER_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onLockerInstallStatusChange() {
        notifyDataSetChange();
    }

    public enum EntryMode {
        Default,
        Camera,
        Gallery
    }


    public interface OnSelectCallback {
        void onSelectItem(int type);
    }
}