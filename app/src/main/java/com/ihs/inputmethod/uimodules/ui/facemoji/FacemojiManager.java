package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.managers.HSPictureManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.Notification;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FaceItem;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacePictureParam;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiCategory;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiFrame;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.facemoji.faceswitcher.FacePalettesView;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.FaceListActivity;
import com.ihs.inputmethod.uimodules.utils.BitmapUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FacemojiManager {

    public enum FacemojiType{
        CLASSIC,NEW
    }

    private static final String FACE_PICTURE_URI = "face_picture_uri";
    private static final String UPLOAD_FILE_PATH = "upload_file_path";
    private static final String MOJIME_DIRECTORY = "Mojime";
    private static final String PREF_CATEGORY_LAST_PAGE_ID = "sticker_category_last_page_id ";
    private static int currentPageSize = FacemojiPalettesParam.SIZE;

    private static FacemojiManager instance;
    private static Uri currentFacePicUri;
    private static String currentUploadFile;
    private List<FacemojiCategory> newCategories = new ArrayList<>();
    private List<FacemojiCategory> classicCategories = new ArrayList<>();
    private static Bitmap originFace;
    private int currentClassicCategoryId = 0;
    private static int mCurrentClassicCategoryPageId = 0;
    private static boolean mFaceSwitcherShowing;
    private static FacePalettesView mFacePalettesView;
    private static List<FaceItem> faces = new ArrayList<>();
    private static final String[] classicCategoryNames = {
            "person",
            "star",
            "fruit",
            "dance"
    };
    private static final String[] newCategoryNames = {
            "star",
            "fruit"};

    // Take photo but not saved
    private static Uri mTempFacePicUri;
    private static boolean mUsingTempFace;
    private static Bitmap mTempFaceBmp;


    private FacemojiManager() {

    }

    public static FacemojiManager getInstance(){
        if (instance == null){
            synchronized (FacemojiManager.class){
                if (instance == null){
                    instance = new FacemojiManager();
                }
            }
        }
        return instance;
    }

    public static void destroyTempFace() {
        setTempFacePicUri(null);
    }

    public void init() {
        copyAssetStickersToStorage();
        loadStickerList();
        getDefaultFacePicUri();
        loadFaceList();
        initFaceSwitchView();
        loadLastUploadPreviewPic();
    }

    private void copyAssetStickersToStorage() {
        Set<String> allCategoryNames = new HashSet();
        for(String category : classicCategoryNames){
            allCategoryNames.add(category);
        }

        for(String category : newCategoryNames){
            allCategoryNames.add(category);
        }

        for(String category :allCategoryNames){
            copyAssetFileToStorage(category);
        }
    }

    public static boolean isUsingTempFace() {
        return mUsingTempFace;
    }

    private static void loadLastUploadPreviewPic() {
        SharedPreferences sharedPreferences = HSApplication.getContext().getSharedPreferences(UPLOAD_FILE_PATH, Context.MODE_PRIVATE);
        String fileName = sharedPreferences.getString(UPLOAD_FILE_PATH, "");
        File file = new File(fileName);
        if (file.exists()) {
            try {
                FacemojiManager.currentUploadFile = fileName;
            } catch (Exception e) {
                HSLog.d("face pic does not exist");
            }
        }
    }

    public int getCurrentCategoryId() {
        return currentClassicCategoryId;
    }

    public void setCurrentCategoryId(int currentId) {
        currentClassicCategoryId = currentId;
    }

    public List<FacemojiCategory> getCategories(FacemojiType facemojiType){
        switch (facemojiType){
            case NEW:
                return newCategories;
            default:
                return classicCategories;
        }
    }

    public List<FacemojiCategory> getClassicCategories() {
        return classicCategories;
    }

    public static void setCurrentFacePicUri(Uri uri) {
        currentFacePicUri = uri;
        SharedPreferences sharedPreferences = HSApplication.getContext().getSharedPreferences(FACE_PICTURE_URI, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (null == uri) {
            editor.remove(FACE_PICTURE_URI);
            editor.commit();
            originFace = null;
            return;
        } else {
            editor.putString(FACE_PICTURE_URI, currentFacePicUri.toString());
            editor.commit();

            try {
                if (originFace != null) {
                    originFace.recycle();
                }

                originFace = MediaStore.Images.Media.getBitmap(HSApplication.getContext().getContentResolver(), currentFacePicUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        HSGlobalNotificationCenter.sendNotificationOnMainThread(CameraActivity.FACE_CHANGED);
        hideFaceSwitchView();
    }

    public static void setTempFacePicUri(Uri uri) {
        // TODO: Delete temp face file ???

        // New value
        mTempFacePicUri = uri;

        // Recycle face bmp first
        if (mTempFaceBmp != null) {
            mTempFaceBmp.recycle();
            mTempFaceBmp = null;
        }

        // New temp face be set
        if (mTempFacePicUri != null) {
            try {
                mTempFaceBmp = MediaStore.Images.Media.getBitmap(HSApplication.getContext().getContentResolver(), mTempFacePicUri);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Now using temp face
            setUsingTempFace(true);
        } else {
            setUsingTempFace(false);
        }
    }

    public static void setUsingTempFace(final boolean usingTempFace) {
        mUsingTempFace = usingTempFace;
    }

    public static Uri getCurrentFacePicUri() {
        return FacemojiManager.currentFacePicUri;
    }

    public static Uri getDefaultFacePicUri() {
        if (null != FacemojiManager.currentFacePicUri) {
            return FacemojiManager.currentFacePicUri;
        }

        SharedPreferences sharedPreferences = HSApplication.getContext().getSharedPreferences(FACE_PICTURE_URI, Context.MODE_PRIVATE);
        String uri = sharedPreferences.getString(FacemojiManager.FACE_PICTURE_URI, "");
        if (!uri.isEmpty()) {

            File file = new File(uri);
            if (file.exists()) {
                try {
                    FacemojiManager.currentFacePicUri = Uri.parse(uri);

                    if (originFace != null) {
                        originFace.recycle();
                    }

                    originFace = MediaStore.Images.Media.getBitmap(HSApplication.getContext().getContentResolver(), currentFacePicUri);

                    return FacemojiManager.currentFacePicUri;
                } catch (Exception e) {
                    HSLog.d("face pic does not exist");
                }
            }
        }

        if (null != faces && faces.size() > 1) {
            currentFacePicUri = faces.get(1).getUri();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(FACE_PICTURE_URI, currentFacePicUri.toString());
            editor.commit();
            return FacemojiManager.currentFacePicUri;
        }

        return null;
    }

    public static Bitmap getCurrentFaceIcon(int color) {

        Uri uri = getDefaultFacePicUri();
        if (uri == null) {
            return BitmapFactory.decodeResource(HSApplication.getContext().getResources(),  R.drawable.tabbar_facemoji);
        }

        return BitmapUtils.corpAndAddBorder(getDefaultFacePicUri(), color, 20);
    }

    public static String getCurrentUploadFile(){
        return currentUploadFile;
    }

    public static void setCurrentUploadFile(String filePath){
        currentUploadFile = filePath;
        SharedPreferences sharedPreferences = HSApplication.getContext().getSharedPreferences(UPLOAD_FILE_PATH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (null == currentUploadFile) {
            editor.remove(UPLOAD_FILE_PATH);
        } else {
            editor.putString(UPLOAD_FILE_PATH, currentUploadFile);
        }
        editor.commit();
        HSGlobalNotificationCenter.sendNotificationOnMainThread(Notification.LOCAL_UPLOAD_DATA_CHANGE);
    }

    private boolean copyAssetFileToStorage(String fileName) {
        String filePath = HSApplication.getContext().getFilesDir().getAbsolutePath();
        AssetManager assetMgr = HSApplication.getContext().getAssets();
        String path = filePath + "/" + MOJIME_DIRECTORY;
        String categoryDirectory = path + "/" + fileName;
        File mojimeFile = new File(categoryDirectory);
        if (mojimeFile.exists()) {
            return false;
        }
        mojimeFile.mkdirs();
        AssetManager am = assetMgr;
        try {
            InputStream isd = am.open(fileName + ".zip");
            OutputStream os = new FileOutputStream(path + "/" + fileName + "/" + fileName + ".zip");
            byte[] b = new byte[1024];
            int length;
            while ((length = isd.read(b)) > 0) {
                os.write(b, 0, length);
            }
            isd.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String zipPath = path + "/" + fileName + "/" + fileName + ".zip";
        File mojimeZipFile = new File(zipPath);
        if (mojimeZipFile.exists()) {
            unzip(zipPath, fileName);
        }
        mojimeZipFile.delete();
        HSLog.d("mojime files succesfully decompressed to file directory " + path + "/" + fileName);
        return true;
    }


    private static void unzip(String zipFileName, String fileName) {
        try {
            File file = new File(zipFileName);
            ZipFile zipFile = new ZipFile(file);

            File zipDir = new File(file.getParentFile(), "");
            zipDir.mkdir();

            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String nme = entry.getName();
                File entryDestination = new File(zipDir, nme);
                entryDestination.getParentFile().mkdirs();
                if (!entry.isDirectory()) {
                    generateFile(entryDestination, entry, zipFile);
                } else {
                    entryDestination.mkdirs();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void generateFile(File destination, ZipEntry entry, ZipFile owner) {
        InputStream in = null;
        OutputStream out = null;

        InputStream rawIn;
        try {
            rawIn = owner.getInputStream(entry);
            in = new BufferedInputStream(rawIn, 1024);
            FileOutputStream rawOut = new FileOutputStream(destination);
            out = new BufferedOutputStream(rawOut, 1024);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap getCurrentFaceBmp() {
        if (mUsingTempFace) {
            if (mTempFaceBmp == null) {
                try {
                    mTempFaceBmp = MediaStore.Images.Media.getBitmap(HSApplication.getContext().getContentResolver(), mTempFacePicUri);
                } catch (Exception e) {
                    return null;
                }
            }

            return mTempFaceBmp;
        } else {
            if (originFace == null) {
                try {
                    originFace = MediaStore.Images.Media.getBitmap(HSApplication.getContext().getContentResolver(), currentFacePicUri);
                } catch (Exception e) {
                    return null;
                }
            }

            return originFace;
        }
    }

    public static Bitmap getFrame(FacemojiSticker sticker, int frameNumber) {
        final Bitmap currentFaceBmp = getCurrentFaceBmp();
        if (currentFaceBmp == null) {
            return null;
        }

        FacePictureParam param = sticker.getFacemojiFrames().get(frameNumber).getFacePictureParam();

        Matrix basicMatrix = new Matrix();
        basicMatrix.setValues(new float[]{param.scaleX, param.skewX, param.translateX, param.skewY, param.scaleY, param.translateY, 0, 0, 1});

        Matrix faceCanvasMatrix = new Matrix();
        faceCanvasMatrix.preTranslate(param.width / 2, param.height / 2);
        faceCanvasMatrix.preConcat(basicMatrix);
        faceCanvasMatrix.preTranslate(-param.width / 2, -param.height / 2);

        Bitmap resultBitmap = Bitmap.createBitmap(sticker.getWidth(), sticker.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(resultBitmap);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);

        for (FacemojiFrame.FacemojiLayer layer : sticker.getFacemojiFrames().get(frameNumber).getLayerList()){
            if (layer.isFace()){
                c.setMatrix(faceCanvasMatrix);
                c.drawBitmap(currentFaceBmp, null, new Rect(0, 0, param.width, param.height), paint);
            }else {
                c.setMatrix(new Matrix());
                String frameBgFilePath = HSApplication.getContext().getFilesDir().getAbsolutePath() + "/" + MOJIME_DIRECTORY + "/" + sticker.getCategoryName() + "/" + sticker.getName() + "/" + layer.srcName;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap originBg = BitmapFactory.decodeFile(frameBgFilePath, options);
                int bgwidth = originBg.getWidth();
                int bgheight = originBg.getHeight();
                Matrix bgMatrix = new Matrix();
                bgMatrix.postScale((float) sticker.getWidth() / bgwidth, (float) sticker.getHeight() / bgheight);
                c.drawBitmap(originBg, bgMatrix, paint);
                originBg.recycle();
            }
        }
        return resultBitmap;
    }

    public static Bitmap getFrameBitmap(FacemojiSticker sticker, int frameNumber) {
        String frameBgFilePath = HSApplication.getContext().getFilesDir().getAbsolutePath() + "/" + MOJIME_DIRECTORY + "/" + sticker.getCategoryName() + "/" + sticker.getName() + "/" + sticker.getName() + "-" + (frameNumber + 1) + ".png";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(frameBgFilePath, options);
        return bitmap;
    }


    private void loadStickerList() {
        classicCategories.clear();
        for (int i = 0; i < classicCategoryNames.length; i++) {
            classicCategories.add(i, new FacemojiCategory(classicCategoryNames[i], i));
        }

        newCategories.clear();
        for (int i = 0; i < newCategoryNames.length; i++) {
            newCategories.add(i, new FacemojiCategory(newCategoryNames[i], i));
        }
    }

    public List<FacemojiSticker> getStickerList(FacemojiType facemojiType,int postion) {
        return getCategories(facemojiType).get(postion).getStickerList();
    }


    public static int getCategoryPosition(String name) {
        for (int i = 0; i < classicCategoryNames.length; i++) {
            if (name.equals(classicCategoryNames[i])) {
                return i;
            }
        }
        return -1;
    }

    public String getCategoryName(FacemojiType facemojiType,int id) {
        return getCategories(facemojiType).get(id).getName();
    }


    public void loadFaceList(){

        faces.clear();
        faces.add(new FaceItem(null));

        File fileFolder = new File(HSPictureManager.getFaceDirectory());

        if (null != fileFolder) {

            File[] files = fileFolder.listFiles();
            if (null != files && files.length != 0) {

                Arrays.sort(files, new Comparator() {
                    public int compare(Object o1, Object o2) {

                        if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                            return -1;
                        } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                            return +1;
                        } else {
                            return 0;
                        }
                    }
                });

                Log.d("Files", "Size: " + files.length);
                Uri uriToAdd;
                for (int i = 0; i < files.length; i++) {
                    uriToAdd = Uri.fromFile(files[i]);
                    faces.add(new FaceItem(uriToAdd));
                }
            }
        }
    }


    public static List<FaceItem> getFaceList() {
        return faces;
    }

    public static List<FaceItem> getFaceByPagePosition(int position) {
        List<FaceItem> result = new ArrayList<>();
        List<FaceItem> faces = getFaceList();
        for (int i = position * 8; i < Math.min((position + 1) * 8, faces.size()); i++) {
            result.add(faces.get(i));
        }
        return result;
    }

    public static void deleteFace(FaceItem faceItem) {

        File fdelete = new File(faceItem.getUri().getPath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.e("-->", "file Deleted :" + fdelete);
            } else {
                Log.e("-->", "file not Deleted :" + fdelete);
            }
        }
    }

    static class FacemojiPalettesParam {
        static final int SIZE = 6;
        static final int COL = 3;
        static final int ROW = 2;
        static final int ROW_LANDSCAPE = 1;
    }

    public static int getCurrentPageSize() {
        return currentPageSize;
    }


    public List<FacemojiSticker> getDataFromPagePosition(FacemojiType facemojiType,int position) {
        Pair<Integer, Integer> pair = getCategoryIdAndPageIdFromPagePosition(facemojiType,position);
        if (pair == null) {
            return new ArrayList<>();
        }
        return getCategories(facemojiType).get(pair.first).getStickerList(pair.second);
    }

    public  Pair<Integer, Integer> getCategoryIdAndPageIdFromPagePosition(FacemojiType facemojiType,final int position) {
        int sum = 0;
        for (final FacemojiCategory category : getCategories(facemojiType)) {
            final int temp = sum;
            sum += category.getPageCount();
            if (sum > position) {
                return new Pair<>(category.getCategoryId(), position - temp);
            }
        }
        return null;
    }

    public  int getCategoryIdByName(FacemojiType facemojiType,String name) {
        for (FacemojiCategory category : getCategories(facemojiType)) {
            if (name.equals(category.getName())) {
                return category.getCategoryId();
            }
        }
        return -1;
    }

    public int getCurrentCategoryPageSize(FacemojiType facemojiType) {
        return getCategoryPageSize(facemojiType,currentClassicCategoryId);
    }

    public int getCategoryPageSize(FacemojiType facemojiType,final int categoryId) {
        for (final FacemojiCategory category : getCategories(facemojiType)) {
            if (category.getCategoryId() == categoryId) {
                return category.getPageCount();
            }
        }
        return 0;
    }

    public static void setCurrentCategoryPageId(final int id) {
        mCurrentClassicCategoryPageId = id;
    }

    public static int getCurrentCategoryPageId() {
        return mCurrentClassicCategoryPageId;
    }

    public int getPageIdFromCategoryId(FacemojiType facemojiType,final int categoryId) {
        String key = PREF_CATEGORY_LAST_PAGE_ID + categoryId;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        final int lastSavedCategoryPageId = mPrefs.getInt(key, 0);
        int sum = 0;
        List<FacemojiCategory> categories = getCategories(facemojiType);
        for (int i = 0; i < categories.size(); ++i) {
            final FacemojiCategory category = categories.get(i);
            if (category.getCategoryId() == categoryId) {
                return sum + lastSavedCategoryPageId;
            }
            sum += category.getPageCount();
        }
        return 0;
    }

    public int getTotalPageCount(FacemojiType facemojiType) {
        int sum = 0;
        for (FacemojiCategory category : getCategories(facemojiType)) {
            sum += category.getPageCount();
        }
        return sum;
    }

    public static void showFaceSwitchView() {
        HSLog.d("show face switch view");

        if (mFaceSwitcherShowing) {
            return;
        }

        mFacePalettesView.prepare();

        final FrameLayout inputArea = HSInputMethod.getInputArea();

        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;

        inputArea.addView(mFacePalettesView, params);

        mFaceSwitcherShowing = true;
        HSGlobalNotificationCenter.sendNotificationOnMainThread(Notification.SHOW_FACE_LIST);
    }

    public static void initFaceSwitchView() {

        if (mFacePalettesView != null) {
            mFacePalettesView.destroy();
        }

        mFacePalettesView = (FacePalettesView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.face_switcher_layout, null);
        mFacePalettesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                return; // set dummy listener here to filter any touch event located on the view
            }
        });
        mFacePalettesView.setHapticFeedbackEnabled(false);
        mFacePalettesView.setSoundEffectsEnabled(false);
        final View closeButton = mFacePalettesView.findViewById(R.id.face_switch_close_btn);
        mFaceSwitcherShowing = false;
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFaceSwitchView();
            }
        });

        final View editButton = mFacePalettesView.findViewById(R.id.face_switch_edit_btn);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HSApplication.getContext(), FaceListActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("toggleEditMode", true);
                HSApplication.getContext().startActivity(i);
            }
        });
    }

    public static boolean hasTempFace() {
        return mTempFacePicUri != null;
    }

    public static void hideFaceSwitchView() {
        HSGlobalNotificationCenter.sendNotificationOnMainThread(Notification.HIDE_FACE_LIST);

        if (mFaceSwitcherShowing) {
            final FrameLayout inputArea = HSInputMethod.getInputArea();
            inputArea.removeView(mFacePalettesView);
            mFaceSwitcherShowing = false;
        }
    }

    public static int getFacePageCount() {
        return (int) Math.ceil(1.0 * getFaceList().size() / 8);
    }

}
