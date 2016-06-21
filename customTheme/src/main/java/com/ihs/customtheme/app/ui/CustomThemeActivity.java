package com.ihs.customtheme.app.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodSubtype;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSFragmentActivity;
import com.ihs.commons.utils.HSLog;
import com.ihs.customtheme.R;
import com.ihs.customtheme.app.iap.CircleProgressView;
import com.ihs.customtheme.app.iap.HSThemePromptPurchaseView;
import com.ihs.customtheme.app.iap.IAPManager;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.ihs.inputmethod.base.utils.ResourceUtils;
import com.ihs.inputmethod.keyboard.Keyboard;
import com.ihs.inputmethod.keyboard.KeyboardId;
import com.ihs.inputmethod.keyboard.KeyboardLayoutSet;
import com.ihs.inputmethod.keyboard.KeyboardPreviewView;
import com.ihs.inputmethod.language.HSImeSubtypeManager;
import com.ihs.inputmethod.language.SubtypeSwitcher;
import com.ihs.inputmethod.theme.HSCustomThemeDataManager;
import com.ihs.inputmethod.theme.HSCustomThemeItemBackground;
import com.ihs.inputmethod.theme.HSCustomThemeItemBase;
import com.ihs.inputmethod.theme.HSCustomThemeItemFont;
import com.ihs.inputmethod.theme.HSCustomThemeManager;
import com.ihs.inputmethod.theme.HSKeyboardThemeManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class CustomThemeActivity extends HSFragmentActivity implements HSThemePromptPurchaseView.IItemClickListener {


    private static final String TAG = CustomThemeActivity.class.getSimpleName();
    static final int CAPTURE_IMAGE_REQUEST_CODE = 100;
    static final int PICK_PHOTO_REQUEST_CODE = 101;
    static final int CROPPER_IMAGE_REQUEST_CODE = 102;

    private Uri customThemeBackgroundImageUri;

    private KeyboardPreviewView mKeyboardView;
    private Context mThemeContext;
    private KeyboardLayoutSet mKeyboardLayoutSet;
    private CircleProgressView circleProgressView;

    private CustomThemeItem1Fragment backgroundFragment;
    private CustomThemeItem2Fragment buttonFragment;
    private CustomThemeItem2Fragment textFragment;

    private HSThemePromptPurchaseView themePromptPurchaseView;
    private HSCustomThemeItemBackground currentSelectedBackground;
    private HSCustomThemeItemFont currentSelectedFont;

    private ProgressDialog saveProgressDialog = null;
    private Bundle savedBundle = null;

    public static final String NEW_CUSTOM_THEME_NAME = "new_custom_theme_name";
    private static final String TAG_BACKGROUNDS_FRAGMENT = "tag_background_fragment";
    private static final String TAG_BUTTON_FRAGMENT = "tag_button_fragment";
    private static final String TAG_FONT_FRAGMENT = "tag_font_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        HSKeyboardThemeManager.init();
        HSKeyboardThemeManager.setPreviewCustomTheme(true);


        mThemeContext = new ContextThemeWrapper(HSApplication.getContext(), HSKeyboardThemeManager.getCommonThemeResourceId());
        setContentView(R.layout.custom_theme);
        mKeyboardView = (KeyboardPreviewView) findViewById(R.id.keyboard_view);
        circleProgressView = (CircleProgressView) findViewById(R.id.progress_view);
        loadKeyboard(HSImeSubtypeManager.getInstance().getCurrentInputMethodSubtype(SubtypeSwitcher.DUMMY_NO_LANGUAGE_SUBTYPE));

        savedBundle = savedInstanceState;

        themePromptPurchaseView = (HSThemePromptPurchaseView) findViewById(R.id.prompt_theme_purchase_view);
        themePromptPurchaseView.setOnClickListener(promptPurchaseViewClickListener);
        themePromptPurchaseView.addProductPurchaseListener(this);

        backgroundFragment = new CustomThemeItem1Fragment();
        backgroundFragment.setViewParams("CANCEL", "BACKGROUND", "NEXT", false, true, getResources().getString(R.string.choose_background), headButtonClickListener);


        buttonFragment = new CustomThemeItem2Fragment();
        buttonFragment.setViewParams("BACKGROUND", "BUTTON", "NEXT", true, true,
                getResources().getString(R.string.choose_button_shape), getResources().getString(R.string.choose_button_style), headButtonClickListener);

        // textFragment
        textFragment = new CustomThemeItem2Fragment();
        textFragment.setViewParams("BUTTON", "FONT", "SAVE", true, false,
                getResources().getString(R.string.choose_text_font), getResources().getString(R.string.choose_text_color), headButtonClickListener);

        new LoadThemePicturesTask().execute();

        mKeyboardView.refreshPreview();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSKeyboardThemeManager.setPreviewCustomTheme(false);
        System.gc();
    }

//    private void onCancelClick() {
//        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
//            HSCustomThemeDataManager.getInstance().resetCustomThemeData();
//            Intent resultIntent = new Intent();
//            setResult(Activity.RESULT_CANCELED, resultIntent);
//            CustomThemeActivity.this.finish();
//        } else if (getSupportFragmentManager().getBackStackEntryCount() >= 1) {
//            onBackPressed();
//        }
//    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            HSCustomThemeDataManager.getInstance().resetCustomThemeData();
            finish();
        }
        super.onBackPressed();
    }

    private void onOKClick() {
        if (getSupportFragmentManager().getBackStackEntryCount() < 2) {
            Fragment nextFragment = null;
            String fragmentTag = "";

            switch (getSupportFragmentManager().getBackStackEntryCount()) {
                case 0:
                    // need pay,can't pass through here
                    if (currentSelectedBackground != null && !IAPManager.getManager().isOwnProduct(currentSelectedBackground)) {
                        showPromptPurchaseView(currentSelectedBackground);
                        return;
                    }
                    nextFragment = buttonFragment;
                    fragmentTag = TAG_BUTTON_FRAGMENT;
                    break;

                case 1:
                    nextFragment = textFragment;
                    fragmentTag = TAG_FONT_FRAGMENT;
                    break;
            }

            if (nextFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.custom_theme_items_container, nextFragment, fragmentTag)
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            if (currentSelectedFont != null && !IAPManager.getManager().isOwnProduct(currentSelectedFont)) {
                showPromptPurchaseView(currentSelectedFont);
                return;
            }
            saveProgressDialog = ProgressDialog.show(this, null, HSApplication.getContext().getResources().getString(R.string.theme_save_action));
            new SaveThemeChangesTask().execute();
        }
    }

    private static Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(HSApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CustomTheme");

        // 如果不存在的话，则创建存储目录
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "Background.jpg");
        return mediaFile;
    }

    private boolean checkCameraHardware() {
        return HSApplication.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_REQUEST_CODE || requestCode == PICK_PHOTO_REQUEST_CODE) {
            String imagePath = null;
            if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    imagePath = getOutputMediaFile().getAbsolutePath();
                    HSLog.d(TAG, "Image saved to: \n" + imagePath);
                } else if (resultCode == RESULT_CANCELED) {
                    HSLog.d(TAG, "Image capture canceled.");
                }
            } else {
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imagePath = cursor.getString(columnIndex);
                        cursor.close();
                        HSLog.d(TAG, "image selected: " + imagePath);
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    HSLog.d(TAG, "Photo pick canceled.");
                }
            }

            if (imagePath != null) {
                Intent openBackgroundCropperIntent = new Intent(CustomThemeActivity.this, CustomThemeBackgroundCropperActivity.class);
                openBackgroundCropperIntent.putExtra(CustomThemeBackgroundCropperActivity.CopperImagePath, imagePath);
                final Resources res = mThemeContext.getResources();
                final int keyboardWidth = ResourceUtils.getDefaultKeyboardWidth(res);
                final int keyboardHeight = ResourceUtils.getDefaultKeyboardHeight(res);
                openBackgroundCropperIntent.putExtra(CustomThemeBackgroundCropperActivity.KeyboardWidth, keyboardWidth);
                openBackgroundCropperIntent.putExtra(CustomThemeBackgroundCropperActivity.KeyboardHeight, keyboardHeight);
                startActivityForResult(openBackgroundCropperIntent, CROPPER_IMAGE_REQUEST_CODE);
            }
        } else if (requestCode == CROPPER_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                HSCustomThemeItemBackground background = HSCustomThemeDataManager.getInstance().getCustomThemeData().getBackground();
                if (background != null) {
                    mKeyboardView.refreshPreview();
                }
            }
        }
    }

    public void loadKeyboard(InputMethodSubtype subtype) {
        final KeyboardLayoutSet.Builder builder = new KeyboardLayoutSet.Builder(mThemeContext, new EditorInfo());
        final Resources res = mThemeContext.getResources();
        final int keyboardWidth = ResourceUtils.getDefaultKeyboardWidth(res);
        final int keyboardHeight = ResourceUtils.getDefaultKeyboardHeight(res);
        builder.setKeyboardGeometry(keyboardWidth, keyboardHeight);
        builder.setSubtype(subtype);
        builder.setVoiceInputKeyEnabled(false);
        builder.setLanguageSwitchKeyEnabled(false);
        builder.setForKeyboardPreview(true);
        mKeyboardLayoutSet = builder.build();
        Keyboard keyboard = mKeyboardLayoutSet.getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        mKeyboardView.setHardwareAcceleratedDrawingEnabled(mKeyboardView.isHardwareAccelerated());
        mKeyboardView.setKeyboard(keyboard);
        mKeyboardView.setKeyPreviewPopupEnabled(true, 70);
        mKeyboardView.setKeyPreviewAnimationParams(false,
                ResourceUtils.getFloatFromFraction(res, R.fraction.config_key_preview_show_up_start_scale),
                ResourceUtils.getFloatFromFraction(res, R.fraction.config_key_preview_show_up_start_scale),
                50,
                ResourceUtils.getFloatFromFraction(res, R.fraction.config_key_preview_dismiss_end_scale),
                ResourceUtils.getFloatFromFraction(res, R.fraction.config_key_preview_dismiss_end_scale),
                100);
    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(final CustomThemeItemView view) {
            if (view.getCustomThemeItem().getItemType() == HSCustomThemeItemBase.ItemType.BACKGROUND) {
                onBackgroundItemClick(view);
            } else {
                onCustomItemClick(view);
            }
        }
    };

    private final OnHeadButtonClickListener headButtonClickListener = new OnHeadButtonClickListener() {
        @Override
        public void onCancelClick() {
            CustomThemeActivity.this.onBackPressed();
        }

        @Override
        public void onOKClick() {
            CustomThemeActivity.this.onOKClick();
        }
    };

    private View.OnClickListener promptPurchaseViewClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            showPromptPurchaseView(null);
        }
    };

    /**
     * purchaseView close button click
     */
    @Override
    public void onCloseButtonClick() {
        showPromptPurchaseView(null);
    }

    private void onBackgroundItemClick(final CustomThemeItemView view) {


        HSCustomThemeItemBackground background = (HSCustomThemeItemBackground)
                view.getCustomThemeItem();
        HSCustomThemeDataManager.getInstance().getCustomThemeData().setCustomThemeData(background);
        if (background.getItemSource() == HSCustomThemeItemBase.ItemSource.BUILT_IN) {
            /** add by cjx */
            // 检测
            currentSelectedBackground = background;
            if (!IAPManager.getManager().isOwnProduct(background)) {
                showPromptPurchaseView(background);
            } else {
                showPromptPurchaseView(null);
            }
            /** add by cjx */
            mKeyboardView.refreshPreview();
        } else if (background.getItemSource() == HSCustomThemeItemBase.ItemSource.CUSTOMIZED) {
            if (background.getCustomizedSource() == HSCustomThemeItemBackground.CustomizedSource.CAMERA) {
                if (checkCameraHardware()) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    customThemeBackgroundImageUri = getOutputMediaFileUri();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, customThemeBackgroundImageUri);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST_CODE);
                    }
                }
            } else if (background.getCustomizedSource() == HSCustomThemeItemBackground.CustomizedSource.PHOTO_ALBUM) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_PHOTO_REQUEST_CODE);
            }
        }
    }


    private void showPromptPurchaseView(HSCustomThemeItemBase item) {
        if (item != null) {
            themePromptPurchaseView.setVisibility(View.VISIBLE);
            themePromptPurchaseView.swapData(item);
            //TODO ok,cancle button disable
        } else if (themePromptPurchaseView.getVisibility() != View.GONE) {
            themePromptPurchaseView.setVisibility(View.GONE);
            //TODO ok,cancle button enable
        }
    }


    private void onCustomItemClick(final CustomThemeItemView view) {
        HSCustomThemeItemBase base = view.getCustomThemeItem();
        if (base != null) {
            HSCustomThemeDataManager.getInstance().getCustomThemeData().setCustomThemeData(base);
            view.selectedThemeItem();
            if (base.getItemType() == HSCustomThemeItemBase.ItemType.FONT) {
                /** change by cjx*/
                currentSelectedFont = (HSCustomThemeItemFont) base;
            }
            // 检测
            if (!IAPManager.getManager().isOwnProduct(base)) {
                showPromptPurchaseView(base);
            } else {
                showPromptPurchaseView(null);
            }
            /** change by cjx*/
            mKeyboardView.refreshPreview();
        }
    }

    private class SaveThemeChangesTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String themeName = HSCustomThemeManager.getInstance().saveCustomTheme(HSCustomThemeDataManager.getInstance().getCustomThemeData());
            HSCustomThemeDataManager.getInstance().resetCustomThemeData();
            return themeName;
        }

        @Override
        protected void onPostExecute(String name) {
            HSKeyboardThemeManager.reloadKeyboardThemes();
            HSInputMethodTheme.saveKeyboardThemeName(name);
            saveProgressDialog.dismiss();
            finish();
        }
    }

    private class LoadThemePicturesTask extends AsyncTask<Void, Void, List> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            circleProgressView.start();
        }

        @Override
        protected List doInBackground(Void... params) {
            List resource = new ArrayList();


            final List<HSCustomThemeItemBase> backgrounds = HSCustomThemeDataManager.getInstance().getItems(HSCustomThemeItemBase.ItemType.BACKGROUND);
            resource.add(backgrounds);

            final List<HSCustomThemeItemBase> buttonShapes = HSCustomThemeDataManager.getInstance().getItems(HSCustomThemeItemBase.ItemType.BUTTON_SHAPE);
            final List<HSCustomThemeItemBase> buttonStyles = HSCustomThemeDataManager.getInstance().getItems(HSCustomThemeItemBase.ItemType.BUTTON_STYLE);

            resource.add(buttonShapes);
            resource.add(buttonStyles);

            final List<HSCustomThemeItemBase> fonts = HSCustomThemeDataManager.getInstance().getItems(HSCustomThemeItemBase.ItemType.FONT);
            final List<HSCustomThemeItemBase> colors = HSCustomThemeDataManager.getInstance().getItems(HSCustomThemeItemBase.ItemType.FONT_COLOR);

            resource.add(fonts);
            resource.add(colors);

            return resource;
        }

        @Override
        protected void onPostExecute(final List resource) {
            //查询拥有的商品id集合,查询完了才能设置数据
            IAPManager.getManager().queryOwnProductIds(new IAPManager.IIAPQueryOwnProductListener() {
                @Override
                public void onQueryFinish() {
                    circleProgressView.stop();

                    backgroundFragment.setItemViewsParams(R.layout.custom_theme_item_cell, (List<HSCustomThemeItemBase>) resource.get(0), itemClickListener);

                    if (savedBundle == null) {
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.custom_theme_items_container, backgroundFragment, TAG_BACKGROUNDS_FRAGMENT)
                                .commit();
                        HSCustomThemeManager.getInstance().clearCustomThemePath();
                    }


                    buttonFragment.setItemViewsParams(R.layout.custom_theme_item_cell, R.layout.custom_theme_item_cell,
                            (List<HSCustomThemeItemBase>) resource.get(1), (List<HSCustomThemeItemBase>) resource.get(2), itemClickListener, itemClickListener
                    );


                    textFragment.setItemViewsParams(R.layout.custom_theme_item_cell_2, R.layout.custom_theme_item_cell,
                            (List<HSCustomThemeItemBase>) resource.get(3), (List<HSCustomThemeItemBase>) resource.get(4), itemClickListener, itemClickListener
                    );
                }
            });

        }
    }


}