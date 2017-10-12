package jp.co.cyberagent.android.gpuimage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.FaceDetector;
import android.opengl.GLES20;


/**
 * Created by guanche on 21/09/2017.
 */

public class GPUImageRendererSourceBitmap implements GPUImageRendererSource {
    private Bitmap bitmap;
    private int textureId = OpenGlUtils.NO_TEXTURE;
    private boolean isDetected;
    private OnFaceDetectEndListener onFaceDetectEndListener;

    public GPUImageRendererSourceBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public int getTextureId() {
        return textureId;
    }

    @Override
    public boolean isTextureExternalOES() {
        return false;
    }

    @Override
    public int getTextureWidth() {
        return bitmap.getWidth();
    }

    @Override
    public int getTextureHeight() {
        return bitmap.getHeight();
    }

    @Override
    public int getFaceOrientation() {
        return 0;
    }

    @Override
    public void initTexture() {
        Bitmap resizedBitmap = null;
        if (bitmap.getWidth() % 2 == 1) {
            resizedBitmap = Bitmap.createBitmap(bitmap.getWidth() + 1, bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas can = new Canvas(resizedBitmap);
            can.drawARGB(0x00, 0x00, 0x00, 0x00);
            can.drawBitmap(bitmap, 0, 0, null);
        }

        textureId = OpenGlUtils.loadTexture(
                resizedBitmap != null ? resizedBitmap : bitmap, textureId, false);
        if (resizedBitmap != null) {
            resizedBitmap.recycle();
        }
    }

    @Override
    public void updateTexture() {
    }

    @Override
    public void destroyTexture() {
        GLES20.glDeleteTextures(1, new int[]{
                textureId
        }, 0);
        textureId = OpenGlUtils.NO_TEXTURE;
    }

    public void setOnFaceDetectEndListener(OnFaceDetectEndListener onFaceDetectEndListener) {
        this.onFaceDetectEndListener = onFaceDetectEndListener;
    }

    public interface OnFaceDetectEndListener {
        void onFaceDetectEnd(FaceDetector.Face[] faces);
    }
}
