package com.megvii.beautify.sdk;

import android.content.Context;
import android.util.Log;

import com.megvii.beautify.sdk.jni.BeautifyApi;

public class Beautify {

    public int mPointSize = 81;
    private long beautyHandle, stickerHandle, segHandle;
    public float[] segData;

    public final static int MG_BEAUTIFY_ENLARGE_EYE = 1;
    public final static int MG_BEAUTIFY_SHRINK_FACE = 2;
    public final static int MG_BEAUTIFY_BRIGHTNESS = 3;
    public final static int MG_BEAUTIFY_DENOISE = 4;
    public final static int MG_BEAUTIFY_ADD_PINK = 5;


    public final static int MG_IMAGEMODE_GRAY = 0;
    public final static int MG_IMAGEMODE_BGR = 1;
    public final static int MG_IMAGEMODE_NV21 = 2;


    /**
     * @return resultCode
     * @brief 初始化美颜检测器
     * @param[in] context 环境变量
     * @param[in] cameraWidth 数据宽
     * @param[in] cameraHeight 数据高
     * @param[in] orientation 旋转角度
     * @param[in] beautyModel beauty模型数据
     */
    public String createBeautyHandle(Context context, int width, int height, int orientation, byte[] beautyModel) {
        if (context == null || beautyModel == null)
            return getErrorType(MG_RETCODE_INVALID_ARGUMENT);

        long handle = BeautifyApi.nativeCreateBeautyHandle(context, width, height, orientation, beautyModel);
        String errorType = getErrorType((int) handle);
        if (errorType == null) {
            beautyHandle = handle;
            return null;
        }
        return errorType;
    }

    /**
     * @return resultCode
     * @brief 设置滤镜
     * @param[in] filterData RGBA个数数据
     * @param[in] filterWidth filter宽
     * @param[in] filterHeight filter高
     */
    public int setFilter(String filterPath) {
        if (filterPath == null)
            return MG_RETCODE_INVALID_ARGUMENT;
        int code = BeautifyApi.nativeSetFilter(filterPath, beautyHandle);
        return code;
    }

    /**
     * @return resultCode
     * @brief 删除滤镜
     */
    public int removeFilter() {
        return BeautifyApi.nativeRemoveFilter(beautyHandle);
    }

    /**
     * @return resultCode
     * @brief 释放美颜检测器
     */
    public int releaseBeautyHandle() {
        if (beautyHandle == 0)
            return MG_RETCODE_FAILED;
        int retCode = BeautifyApi.nativeReleaseBeautyHandle(beautyHandle);
        beautyHandle = 0;
        return retCode;
    }

    /**
     * @return resultCode
     * @brief 设置美颜参数
     * @param[in] beautyType 美颜类型
     * @param[in] beautyValue 美颜程度0-10之间
     */
    public int setBeautyParam(int beautyType, float beautyValue) {
        return BeautifyApi.nativeSetBeautyParam(beautyType, beautyValue, beautyHandle);
    }

    /**
     * 在低配手机上可以使用快速磨皮算法
     * @param useFastFilter
     * @return
     */
    public int useFastFilter(boolean useFastFilter){
        return BeautifyApi.nativeUseFastFilter(useFastFilter, beautyHandle);
    }
    /**
     * @return resultCode
     * @brief 获取美颜后的Texture
     * @param[in] oldTextureIndex 美颜前的原Texture
     * @param[out] newTextureIndex 美颜后输出的Texture
     * @param[in] facesCount 人脸数量
     */
    public int beautyProcessTexture(int oldTextureIndex, int newTextureIndex, float[] faceData, int width, int height, int orientation) {
        int i = BeautifyApi.nativeBeautifyProcessTexture(oldTextureIndex, newTextureIndex
                , faceData, mPointSize, width, height, orientation, beautyHandle);
        return i;
    }

    /**
     * @brief 初始化贴纸加载器
     */
    public void createStickerHandle() {
        stickerHandle = BeautifyApi.nativeCreateStickerHandle(beautyHandle);
    }

    /**
     * @brief 释放贴纸加载器
     */
    public void releaseStickerHandle() {
        if (stickerHandle == 0)
            return;
        BeautifyApi.nativeReleaseStickerHandle(stickerHandle);
        stickerHandle = 0;
    }

    public boolean isValid() {
        return beautyHandle != 0 && stickerHandle != 0;
    }

    /**
     * @return resultCode
     * @brief 获取贴纸后的Texture
     * @param[in] oldTextureIndex 贴纸前的原Texture
     * @param[out] newTextureIndex 贴纸后输出的Texture
     * @param[in] facesCount 人脸数量
     */
    public int stickerProcessTexture(int oldTextureIndex, int newTextureIndex, float[] faceData, int width, int height, int orientation) {
        if (stickerHandle == 0)
            return MG_RETCODE_FAILED;
        int i = BeautifyApi.nativeStickerProcessTexture(oldTextureIndex, newTextureIndex, faceData, mPointSize, width, height, orientation, stickerHandle);
        return i;
    }

    /**
     * @brief 预加载贴纸
     * @param[in] path 贴纸压缩包所在路径
     */
    public void preparePackage(String path) {
        if (path != null)
            BeautifyApi.nativePreparePackage(path, stickerHandle);
    }

    /**
     * @brief 更改贴纸
     * @param[in] path 贴纸压缩包所在路径
     */
    public void changePackage(String path) {
        if (path != null)
            BeautifyApi.nativeChangePackage(path, stickerHandle);
    }

    /**
     * @brief 停止贴纸
     */
    public void disablePackage() {
        BeautifyApi.nativeDisablePackage(stickerHandle);
    }


    public String createSegmentationHandle(Context context, byte[] segModel) {
        if (context == null || segModel == null) {
            return getErrorType(MG_RETCODE_INVALID_ARGUMENT);
        }
        long handle = BeautifyApi.nativeCreateSegmentationHandle(context, segModel);
        String errorType = getErrorType((int) handle);
        if (errorType == null) {
            segHandle = handle;
            return null;
        }
        return errorType;
    }

    public int segmentation_1(byte[] img, int width, int height, int rotation) {
        if (img == null)
            return MG_RETCODE_INVALID_ARGUMENT;


        int dstWidth = width / 2;
        int dstHeight = height / 2;

        byte[] dstImg = new byte[dstWidth * dstHeight * 4];

        int retCode = cvtAndResize(img, width, height, dstWidth, dstHeight, dstImg, MG_IMAGEMODE_NV21);

        if (retCode != MG_RETCODE_OK)
            return retCode;

        if (segData == null)
            segData = new float[dstWidth * dstHeight];

        float[] segData_temp = new float[dstWidth * dstHeight];

        int retCode_1 = setment(dstImg, dstWidth, dstHeight
                , segData_temp, MG_IMAGEMODE_BGR);

        for (int i = 0; i < segData_temp.length; i++) {
            segData[i] = segData_temp[segData_temp.length - 1 - i];
        }
        return 0;
    }

    byte[] _dstImg;
    float[] segData_temp;
    private final static String NEXUS = "Nexus 5X";

//    public int segmentation(byte[] img, int width, int height, int angle, boolean isBackCamera) {
//        if (img == null)
//            return MG_RETCODE_INVALID_ARGUMENT;
//        long t0 = System.currentTimeMillis();
//        if (isBackCamera && NEXUS.equals(android.os.Build.MODEL)) {
//            angle = 360 - angle;
//        }
//        int rotation = angle;
//        if (!isBackCamera)
//            rotation = 360 - rotation;
//        int ratio = Math.max(width, height) / 200;
//        int _dstWidth = width / ratio / 4 * 4;
//        int _dstHeight = height / ratio / 4 * 4;
//
//        if (_dstImg == null)
//            _dstImg = new byte[_dstWidth * _dstHeight * 4];
//        int retCode = cvtAndResize(img, width, height, _dstWidth, _dstHeight, _dstImg, MG_IMAGEMODE_NV21);
//        if (retCode != MG_RETCODE_OK) {
//            return retCode;
//        }
//
//        long t1 = System.currentTimeMillis();
//
//        byte[] dstImg = rotate(_dstImg, _dstWidth, _dstHeight, rotation);
//
//
//        int dstWidth = _dstWidth;
//        int dstHeight = _dstHeight;
//        if (rotation == 90 || rotation == 270) {
//            dstWidth = _dstHeight;
//            dstHeight = _dstWidth;
//        }
//
//        if (segData == null)
//            segData = new float[dstWidth * dstHeight];
//
//        long t2 = System.currentTimeMillis();
//        if (segData_temp == null)
//            segData_temp = new float[dstWidth * dstHeight];
//
//        int retCode_1 = setment(dstImg, dstWidth, dstHeight
//                , segData_temp, MG_IMAGEMODE_BGR);
//        if (retCode_1 != MG_RETCODE_OK) {
//            return retCode;
//        }
//        long t3 = System.currentTimeMillis();
//        segData = ConUtil.rotateFloat_270(segData_temp, dstWidth, dstHeight, isBackCamera);
//        return 0;
//    }

    byte[] dstData;

    public byte[] rotate(byte[] img, int width, int height, int rotation) {
        if (img == null)
            return null;
        if (dstData == null)
            dstData = new byte[width * height * 4];

        int retCode = BeautifyApi.nativeRotate(img, width, height, rotation, dstData, segHandle);
        if (retCode == 0) {
            return dstData;
        }
        return null;
    }

    public int cvtAndResize(byte[] img, int width, int height, int dstWidth, int dstHeight, byte[] dstImg, int imageMode) {
        if (img == null || dstImg == null || segHandle == 0)
            return MG_RETCODE_INVALID_ARGUMENT;

        int retCode = BeautifyApi.nativeCvtAndResize(img, width, height, imageMode, dstWidth, dstHeight, dstImg, segHandle);
        return retCode;
    }

    public int setment(byte[] img, int width, int height, float[] segData_temp, int imageMode) {
        if (img == null)
            return MG_RETCODE_INVALID_ARGUMENT;

        int retCode = BeautifyApi.nativeSegment(img, width, height, imageMode, segData_temp, segHandle);
        return retCode;
    }


    public String createBudySegHandle() {
        if (beautyHandle == 0 || segHandle == 0) {
            String errorType = getErrorType(MG_RETCODE_INVALID_ARGUMENT);
            return errorType;
        }
        int i = BeautifyApi.nativeCreateBudySegHandle(beautyHandle, segHandle);

        if (i != 0) {
            String error = getErrorType(i);
            return error;
        }
        return null;
    }

    public int changeBack(String path) {
        Log.i("changeBack", "changeBack: " + path);
        if (path == null)
            return MG_RETCODE_INVALID_ARGUMENT;
        int retCode = BeautifyApi.nativeChangeBack(path, segHandle);
        return retCode;
    }

    public int removeBack() {
        int retCode = BeautifyApi.nativeRemoveBack(segHandle);
        return retCode;
    }


    public int budySegProcessTexture(int oldTextureIndex, int newTextureIndex, int rotation) {
        if (segData == null || segHandle == 0)
            return MG_RETCODE_INVALID_ARGUMENT;

        int i = BeautifyApi.nativeBudySegProcessTexture(oldTextureIndex, newTextureIndex, segData, rotation, segHandle);
        return i;
    }

    public void releaseSegHandle() {
        if (segHandle == 0)
            return;
        BeautifyApi.nativeReleaseSegHandle(segHandle);
        segHandle = 0;
        segData = null;
    }


    /**
     * @brief 释放检测器
     */
    public void release() {
        releaseBeautyHandle();
        releaseStickerHandle();
        releaseSegHandle();
    }


    private static final int MG_RETCODE_FAILED = -1;
    private static final int MG_RETCODE_OK = 0;
    private static final int MG_RETCODE_INVALID_ARGUMENT = 1;
    private static final int MG_RETCODE_INVALID_HANDLE = 2;
    private static final int MG_RETCODE_INDEX_OUT_OF_RANGE = 3;
    private static final int MG_RETCODE_EXPIRE = 101;
    private static final int MG_RETCODE_INVALID_BUNDLEID = 102;
    private static final int MG_RETCODE_INVALID_LICENSE = 103;
    private static final int MG_RETCODE_INVALID_MODEL = 104;

    private String getErrorType(int retCode) {
        switch (retCode) {
            case MG_RETCODE_FAILED:
                return "MG_RETCODE_FAILED";
            case MG_RETCODE_OK:
                return "MG_RETCODE_OK";
            case MG_RETCODE_INVALID_ARGUMENT:
                return "MG_RETCODE_INVALID_ARGUMENT";
            case MG_RETCODE_INVALID_HANDLE:
                return "MG_RETCODE_INVALID_HANDLE";
            case MG_RETCODE_INDEX_OUT_OF_RANGE:
                return "MG_RETCODE_INDEX_OUT_OF_RANGE";
            case MG_RETCODE_EXPIRE:
                return "MG_RETCODE_EXPIRE";
            case MG_RETCODE_INVALID_BUNDLEID:
                return "MG_RETCODE_INVALID_BUNDLEID";
            case MG_RETCODE_INVALID_LICENSE:
                return "MG_RETCODE_INVALID_LICENSE";
            case MG_RETCODE_INVALID_MODEL:
                return "MG_RETCODE_INVALID_MODEL";
        }

        return null;
    }
}
