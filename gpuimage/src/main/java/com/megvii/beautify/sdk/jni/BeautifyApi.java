package com.megvii.beautify.sdk.jni;

import android.content.Context;

public class BeautifyApi {

    public static native long nativeCreateBeautyHandle(Context context, int cameraWidth, int cameraHeight, int orientation, byte[] beautyModel);
    public static native int nativeSetFilter(String filterPath, long beautyHandle);
    public static native int nativeRemoveFilter(long beautyHandle);
    public static native int nativeReleaseBeautyHandle(long beautyHandle);
    public static native int nativeSetBeautyParam(int beautyType, float beautyValue, long beautyHandle);
    public static native int nativeUseFastFilter(boolean useFastFilter, long beautyHandle);
    public static native int nativeBeautifyProcessTexture(int oldTextureIndex, int newTextureIndex, float[] faceData
            , int pointSize, int width, int height, int orientation, long beautyHandle);

    public static native long nativeCreateStickerHandle(long beautyHandle);
    public static native void nativeReleaseStickerHandle(long stickerHandle);
    public static native int nativeStickerProcessTexture(int oldTextureIndex, int newTextureIndex, float[] faceData
            , int pointSize, int width, int height, int orientation, long stickerHandle);

    public static native void nativeChangePackage(String path, long stickerHandle);
    public static native void nativePreparePackage(String path, long stickerHandle);
    public static native void nativeDisablePackage(long stickerHandle);

    public static native long nativeCreateSegmentationHandle(Context context, byte[] segModel);
    public static native int nativeCvtAndResize(byte[] img, int width, int height, int imageMode
            , int dstWidth, int dstHeight, byte[] dstImag, long segHandle);

    public static native int nativeRotate(byte[] img, int width, int height, int rotation, byte[] dstImg, long segHandle);
    public static native int nativeSegment(byte[] img, int width, int height, int imageMode
            , float[] segData, long segHandle);
    public static native int nativeCreateBudySegHandle(long beautyHandle, long segHandle);
    public static native int nativeChangeBack(String path, long segHandle);
    public static native int nativeRemoveBack(long segHandle);
    public static native int nativeBudySegProcessTexture(int oldTextureIndex, int newTextureIndex, float[] segData, int width, long segHandle);
    public static native void nativeReleaseSegHandle(long segHandle);

    static {
        System.loadLibrary("MegviiBeautify-jni");
        System.loadLibrary("MGBeauty");
    }
}
