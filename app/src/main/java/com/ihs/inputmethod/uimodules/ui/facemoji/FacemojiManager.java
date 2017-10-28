package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
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

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.managers.HSPictureManager;
import com.ihs.inputmethod.uimodules.BuildConfig;
import com.ihs.inputmethod.uimodules.constants.Notification;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FaceItem;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacePictureParam;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiCategory;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiFrame;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;

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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FacemojiManager {
    public static final String FACEMOJI_SAVED = "FACEMOJI_SAVED";
    public static final String FACE_CHANGED = "FACE_CHANGED";
    public static final String FACE_DELETED = "FACE_DELETED";
    public final static String INIT_SHOW_TAB_CATEGORY = "initShowTabCategory";

    public enum ShowLocation {
        Keyboard,App
    }

    private static final String FACE_PICTURE_URI = "face_picture_uri";
    private static final String UPLOAD_FILE_PATH = "upload_file_path";
    private static final String MOJIME_DIRECTORY = "Mojime";
    private static final String PREF_CATEGORY_LAST_PAGE_ID = "sticker_category_last_page_id ";
    private static final String[] classicCategoryNames;
    private static int currentPageSize = FacemojiPalettesParam.SIZE;
    private static FacemojiManager instance;
    private static Uri currentFacePicUri;
    private static String currentUploadFile;
    private static Bitmap originFace;
    private static int mCurrentClassicCategoryPageId = 0;
    private static List<FaceItem> faces = new ArrayList<>();
    // Take photo but not saved
    private static Uri mTempFacePicUri;
    private static boolean mUsingTempFace;
    private static Bitmap mTempFaceBmp;
    private List<FacemojiCategory> classicCategories = new ArrayList<>();
    private int currentClassicCategoryId = 0;
    private static BitmapFactory.Options lowQualityOption;
    private static BitmapFactory.Options highQualityOption;

    static {
        if (BuildConfig.ENABLE_FACEMOJI){
            classicCategoryNames = new String[]{
                    "dance",
                    "star",
                    "person",
                    "fruit"
            };
        }else {
            classicCategoryNames = new String[]{};
        }
    }

    private FacemojiManager() {
        //新的facemoji尺寸是400x300，因此做压缩到200x150，同时图片质量用RGB_565
        lowQualityOption = new BitmapFactory.Options();
        lowQualityOption.inPreferredConfig = Bitmap.Config.RGB_565;
        lowQualityOption.inSampleSize = 2;

        //老的facemoji尺寸是200x200，直接加载
        highQualityOption = new BitmapFactory.Options();
        highQualityOption.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    public static FacemojiManager getInstance() {
        if (instance == null) {
            synchronized (FacemojiManager.class) {
                if (instance == null) {
                    instance = new FacemojiManager();
                }
            }
        }
        return instance;
    }

    public static void destroyTempFace() {
        setTempFacePicUri(null);
    }

    public static boolean isUsingTempFace() {
        return mUsingTempFace;
    }

    public static void setUsingTempFace(final boolean usingTempFace) {
        mUsingTempFace = usingTempFace;
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

    public static Uri getCurrentFacePicUri() {
        return FacemojiManager.currentFacePicUri;
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
                originFace = MediaStore.Images.Media.getBitmap(HSApplication.getContext().getContentResolver(), currentFacePicUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        HSGlobalNotificationCenter.sendNotificationOnMainThread(FacemojiManager.FACE_CHANGED);
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

    public static String getCurrentUploadFile() {
        return currentUploadFile;
    }

    public static void setCurrentUploadFile(String filePath) {
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

    public static Bitmap getFrame(FacemojiSticker sticker, int frameNumber,boolean gifEncode) {
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

        for (FacemojiFrame.FacemojiLayer layer : sticker.getFacemojiFrames().get(frameNumber).getLayerList()) {
            if (layer.isFace()) {
                c.setMatrix(faceCanvasMatrix);
                c.drawBitmap(currentFaceBmp, null, new Rect(0, 0, param.width, param.height), paint);
            } else {
                c.setMatrix(new Matrix());
                String frameBgFilePath = HSApplication.getContext().getFilesDir().getAbsolutePath() + "/" + MOJIME_DIRECTORY + "/" + sticker.getCategoryName() + "/" + sticker.getName() + "/" + layer.srcName;

                Bitmap originBg = BitmapFactory.decodeFile(frameBgFilePath, getFacemojiStickerOption(sticker,gifEncode));
                if (originBg == null) {
                    break;
                }
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

    private static BitmapFactory.Options getFacemojiStickerOption(FacemojiSticker sticker, boolean gifEncode){
        if (gifEncode) {
            return highQualityOption;
        }
        return sticker.getWidth() == sticker.getHeight() ? highQualityOption : lowQualityOption;
    }

    public static Bitmap getFrameBitmap(FacemojiSticker sticker, int frameNumber) {
        String frameBgFilePath = HSApplication.getContext().getFilesDir().getAbsolutePath() + "/" + MOJIME_DIRECTORY + "/" + sticker.getCategoryName() + "/" + sticker.getName() + "/" + sticker.getName() + "-" + (frameNumber + 1) + ".png";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(frameBgFilePath, options);
        return bitmap;
    }

    public static int getCategoryPosition(String name) {
        for (int i = 0; i < classicCategoryNames.length; i++) {
            if (name.equals(classicCategoryNames[i])) {
                return i;
            }
        }
        return -1;
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

    public static int getCurrentPageSize(ShowLocation showLocation, int orientation, FacemojiSticker facemojiSticker) {
        if (showLocation == FacemojiManager.ShowLocation.Keyboard && orientation == Configuration.ORIENTATION_LANDSCAPE && facemojiSticker.getWidth() != facemojiSticker.getHeight()) {
            return 3;
        }
        return currentPageSize;
    }

    public static int getCurrentCategoryPageId() {
        return mCurrentClassicCategoryPageId;
    }

    public static void setCurrentCategoryPageId(final int id) {
        mCurrentClassicCategoryPageId = id;
    }


    public static boolean hasTempFace() {
        return mTempFacePicUri != null;
    }

    public static int getFacePageCount() {
        return (int) Math.ceil(1.0 * getFaceList().size() / 8);
    }

    public void init() {
        copyAssetStickersToStorage();
        loadStickerList();
        getDefaultFacePicUri();
        loadFaceList();
        loadLastUploadPreviewPic();
    }

    private void copyAssetStickersToStorage() {
        for (String category : classicCategoryNames) {
            copyAssetFileToStorage(category);
        }
    }

    public int getCurrentCategoryId() {
        return currentClassicCategoryId;
    }

    public void setCurrentCategoryId(int currentId) {
        currentClassicCategoryId = currentId;
    }

    public List<FacemojiCategory> getCategories() {
        return classicCategories;
    }

    public List<FacemojiCategory> getClassicCategories() {
        return classicCategories;
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

    private void loadStickerList() {
        classicCategories.clear();
        for (int i = 0; i < classicCategoryNames.length; i++) {
            classicCategories.add(i, new FacemojiCategory(classicCategoryNames[i], i));
        }
    }

    public List<FacemojiSticker> getStickerList(int postion) {
        return getCategories().get(postion).getStickerList();
    }

    public String getCategoryName(int id) {
        return getCategories().get(id).getName();
    }

    public void loadFaceList() {

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

    public List<FacemojiSticker> getDataFromPagePosition(int position,ShowLocation showLocation, int orientation) {
        Pair<Integer, Integer> pair = getCategoryIdAndPageIdFromPagePosition(position,showLocation,orientation);
        if (pair == null) {
            return new ArrayList<>();
        }
        return getCategories().get(pair.first).getStickerList(pair.second,showLocation,orientation);
    }

    public Pair<Integer, Integer> getCategoryIdAndPageIdFromPagePosition(final int position,ShowLocation showLocation, int orientation) {
        int sum = 0;
        for (final FacemojiCategory category : getCategories()) {
            final int temp = sum;
            sum += category.getPageCount(showLocation, orientation);
            if (sum > position) {
                return new Pair<>(category.getCategoryId(), position - temp);
            }
        }
        return null;
    }

    public int getCategoryIdByName(String name) {
        for (FacemojiCategory category : getCategories()) {
            if (name.equals(category.getName())) {
                return category.getCategoryId();
            }
        }
        return -1;
    }

    public int getCategoryPageSize(final int categoryId,ShowLocation showLocation, int orientation) {
        for (final FacemojiCategory category : getCategories()) {
            if (category.getCategoryId() == categoryId) {
                return category.getPageCount(showLocation, orientation);
            }
        }
        return 0;
    }

    public int getPageIdFromCategoryId(final int categoryId,ShowLocation showLocation, int orientation) {
        String key = PREF_CATEGORY_LAST_PAGE_ID + categoryId;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        final int lastSavedCategoryPageId = mPrefs.getInt(key, 0);
        int sum = 0;
        List<FacemojiCategory> categories = getCategories();
        for (int i = 0; i < categories.size(); ++i) {
            final FacemojiCategory category = categories.get(i);
            if (category.getCategoryId() == categoryId) {
                return sum + lastSavedCategoryPageId;
            }
            sum += category.getPageCount(showLocation, orientation);
        }
        return 0;
    }

    public int getTotalPageCount(ShowLocation showLocation, int orientation) {
        int sum = 0;
        for (FacemojiCategory category : getCategories()) {
            sum += category.getPageCount(showLocation,orientation);
        }
        return sum;
    }

    static class FacemojiPalettesParam {
        static final int SIZE = 6;
        static final int COL = 3;
        static final int ROW = 2;
        static final int ROW_LANDSCAPE = 1;
    }

}
